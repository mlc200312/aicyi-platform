package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.client.model.UserAuthInfo;
import io.github.aicyi.admin.dao.mapper.SysRoleMapper;
import io.github.aicyi.admin.dao.mapper.SysUserMapper;
import io.github.aicyi.admin.dao.mapper.SysUserRoleMapper;
import io.github.aicyi.admin.domain.constant.SysConstants;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.domain.entity.SysUserRole;
import io.github.aicyi.admin.domain.exception.LoginFailException;
import io.github.aicyi.admin.domain.exception.UserDisabledException;
import io.github.aicyi.admin.domain.exception.UserNotFoundException;
import io.github.aicyi.admin.domain.exception.UsernameAlreadyExistsException;
import io.github.aicyi.admin.domain.bo.UserCreateBO;
import io.github.aicyi.admin.domain.bo.UserEditBO;
import io.github.aicyi.admin.domain.bo.UserBatchDeleteBO;
import io.github.aicyi.admin.domain.bo.UserImportResultBO;
import io.github.aicyi.admin.domain.bo.UserImportRow;
import io.github.aicyi.admin.domain.bo.UserQueryBO;
import io.github.aicyi.admin.domain.convert.UserImportConverter;
import io.github.aicyi.admin.service.convert.ServiceConverter;
import io.github.aicyi.common.util.media.ExcelUtils;
import io.github.aicyi.admin.domain.type.StatusType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.middleware.kit.util.IdUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户管理服务：列表 / 新增 / 编辑 / 禁用 / 删除 / 重置密码 / 角色分配。
 *
 * <p>账号保护规则：超级管理员 admin 禁止删除、禁止禁用、禁止修改用户名。
 */
@Service
public class UserManageService {

    private static final Logger log = LoggerFactory.getLogger(UserManageService.class);

    private final SysUserMapper userMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleMapper roleMapper;

    private final SessionRevoker sessionRevoker;

    private final PasswordEncoder passwordEncoder;

    private final PermissionCache permissionCache;

    public UserManageService(SysUserMapper userMapper,
                             SysUserRoleMapper userRoleMapper,
                             SysRoleMapper roleMapper,
                             SessionRevoker sessionRevoker,
                             PasswordEncoder passwordEncoder,
                             PermissionCache permissionCache) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.sessionRevoker = sessionRevoker;
        this.passwordEncoder = passwordEncoder;
        this.permissionCache = permissionCache;
    }

    /**
     * 用户分页查询（用户名模糊 / 状态 / 时间范围）
     */
    public IPage<SysUser> page(UserQueryBO query) {
        return userMapper.selectPage(new Page<>(query.getPage(), query.getSize()), buildQueryWrapper(query));
    }

    /**
     * 导出用户列表（与分页查询同条件，不分页，按创建时间倒序）
     */
    public List<SysUser> listForExport(UserQueryBO query) {
        return userMapper.selectList(buildQueryWrapper(query));
    }

    /**
     * 批量删除用户：admin 账号与不存在/已删除用户自动跳过，返回实际删除数量。
     *
     * <p>与单个删除同规则：踢下线 → 删除角色绑定 → 逻辑删除 → 权限缓存逐出
     */
    @Transactional(rollbackFor = Exception.class)
    public long batchDelete(UserBatchDeleteBO bo) {
        List<Long> ids = bo.getIds().stream().distinct().toList();
        List<SysUser> users = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .in(SysUser::getId, ids)
                .eq(SysUser::getDeleted, BooleanType.FALSE));

        // admin 账号禁止删除（与单个删除规则一致），此处跳过而非整批失败
        List<SysUser> deletable = users.stream()
                .filter(user -> !SysConstants.ADMIN_USERNAME.equals(user.getUsername()))
                .toList();

        if (deletable.isEmpty()) {
            log.info("user_batch_deleted deletedCount=0 requested={}", ids.size());
            return 0L;
        }

        List<Long> deletableIds = deletable.stream().map(SysUser::getId).toList();
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, deletableIds));
        userMapper.update(null, Wrappers.<SysUser>lambdaUpdate()
                .in(SysUser::getId, deletableIds)
                .eq(SysUser::getDeleted, BooleanType.FALSE)
                .set(SysUser::getDeleted, BooleanType.TRUE));

        // 逐个踢下线并逐出权限缓存（数量上限 100，可接受）
        for (SysUser user : deletable) {
            sessionRevoker.kickOff(user.getId());
            permissionCache.evict(user.getId());
        }

        log.info("user_batch_deleted deletedCount={} requested={}", deletableIds.size(), ids.size());
        return deletableIds.size();
    }

    /**
     * 批量导入用户：解析 Excel 行，逐行校验（密码 6-32 位、文件内用户名不重复），
     * 复用单个新增的全部规则与保护（用户名查重、昵称兜底、默认启用），逐行成败互不影响。
     */
    @Transactional(rollbackFor = Exception.class)
    public UserImportResultBO importUsers(byte[] fileBytes) {
        // 空行剔除（EasyExcel 对空白行可能返回全空字段对象）
        List<UserImportRow> rows = ExcelUtils.readFromBytes(fileBytes, UserImportRow.class).stream()
                .filter(row -> StringUtils.hasText(row.getUsername()) || StringUtils.hasText(row.getNickname())
                        || StringUtils.hasText(row.getMobile()) || StringUtils.hasText(row.getEmail())
                        || StringUtils.hasText(row.getPassword()))
                .toList();

        UserImportResultBO result = new UserImportResultBO();
        result.setTotalRows(rows.size());

        Set<String> seenUsernames = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            // 表头占第 1 行，数据从第 2 行开始
            int rowNo = i + 2;
            UserImportRow row = rows.get(i);
            try {
                validateImportRow(row, seenUsernames);
                add(UserImportConverter.INSTANCE.toUserCreateBO(row));
                result.setSuccessCount(result.getSuccessCount() + 1);
                log.info("user_imported rowNo={} username={}", rowNo, row.getUsername());
            } catch (Exception e) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailures().add(UserImportConverter.INSTANCE.toFailItemBO(rowNo, row, e.getMessage()));
                log.warn("user_import_failed rowNo={} username={} reason={}", rowNo, row.getUsername(), e.getMessage());
            }
        }
        log.info("user_import_finished total={} success={} fail={}", result.getTotalRows(), result.getSuccessCount(), result.getFailCount());
        return result;
    }

    /**
     * 导入行前置校验：用户名必填、初始密码 6-32 位、文件内用户名不重复（与新增请求 JSR-303 口径一致）
     */
    private void validateImportRow(UserImportRow row, Set<String> seenUsernames) {
        if (!StringUtils.hasText(row.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!seenUsernames.add(row.getUsername().trim())) {
            throw new IllegalArgumentException("文件内用户名重复");
        }
        String password = row.getPassword();
        if (!StringUtils.hasText(password) || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("初始密码需为 6-32 位");
        }
    }

    /**
     * 用户查询条件组装（分页 / 导出共用，避免条件漂移）
     */
    private LambdaQueryWrapper<SysUser> buildQueryWrapper(UserQueryBO query) {
        return Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getDeleted, BooleanType.FALSE)
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .ge(query.getBeginTime() != null, SysUser::getCreateTime, query.getBeginTime())
                .le(query.getEndTime() != null, SysUser::getCreateTime, query.getEndTime())
                .orderByDesc(SysUser::getCreateTime);
    }

    /**
     * 新增用户：默认启用，可绑定角色
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser add(UserCreateBO bo) {
        if (userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, bo.getUsername())) > 0) {
            throw new UsernameAlreadyExistsException(bo.getUsername());
        }

        SysUser user = ServiceConverter.INSTANCE.toDO(bo);
        user.setId(IdUtils.generateId());
        user.setPassword(passwordEncoder.encode(bo.getPassword()));
        if (!StringUtils.hasText(user.getNickname())) {
            user.setNickname(user.getUsername());
        }
        user.setStatus(StatusType.ENABLED);
        user.setPasswordModified(BooleanType.FALSE);
        user.setDeleted(BooleanType.FALSE);
        userMapper.insert(user);

        bindRoles(user.getId(), bo.getRoleIds());
        log.info("user_added userId={} username={}", user.getId(), bo.getUsername());
        return user;
    }

    /**
     * 编辑用户：修改昵称 / 手机号 / 邮箱 / 状态 / 备注 / 角色；禁止修改用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser edit(UserEditBO bo) {
        SysUser user = requireUser(bo.getUserId());
        if (bo.getStatus() != null && bo.getStatus() != StatusType.ENABLED) {
            ensureNotAdmin(user);
        }
        ServiceConverter.INSTANCE.updateDO(user, bo);
        userMapper.updateById(user);

        bindRoles(bo.getUserId(), bo.getRoleIds());
        permissionCache.evict(bo.getUserId());
        log.info("user_edited userId={}", bo.getUserId());
        return userMapper.selectById(bo.getUserId());
    }

    /**
     * 删除用户：普通用户可删，admin 禁止删除；逻辑删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        SysUser user = requireUser(userId);
        ensureNotAdmin(user);

        sessionRevoker.kickOff(userId);
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        userMapper.update(null, Wrappers.<SysUser>lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getDeleted, BooleanType.TRUE));
        permissionCache.evict(userId);
        log.info("user_deleted userId={} username={}", userId, user.getUsername());
    }

    /**
     * 启用 / 禁用用户；禁用后无法登录，原有会话自动失效
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, StatusType status) {
        SysUser user = requireUser(userId);
        if (status != StatusType.ENABLED) {
            ensureNotAdmin(user);
        }
        user.setStatus(status);
        userMapper.updateById(user);

        if (status != StatusType.ENABLED) {
            sessionRevoker.kickOff(userId);
        }
        permissionCache.evict(userId);
        log.info("user_status_changed userId={} status={}", userId, status);
    }

    /**
     * 管理员重置密码：重置后恢复初始密码提醒
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = requireUser(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordModified(BooleanType.FALSE);
        userMapper.updateById(user);
        sessionRevoker.kickOff(userId);
        log.info("password_reset_by_admin userId={}", userId);
    }

    /**
     * 分配用户角色（多角色，权限自动叠加）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        requireUser(userId);
        bindRoles(userId, roleIds);
        permissionCache.evict(userId);
        log.info("user_roles_assigned userId={} roleIds={}", userId, roleIds);
    }

    /**
     * 查询用户绑定的角色
     */
    public List<SysRole> listRolesByUser(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getDeleted, BooleanType.FALSE)
                        .eq(SysUserRole::getUserId, userId));
        List<SysRole> roles = new ArrayList<>();
        for (SysUserRole userRole : userRoles) {
            SysRole role = roleMapper.selectById(userRole.getRoleId());
            if (role != null && role.getDeleted() == BooleanType.FALSE) {
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * 按 ID 查询用户（个人中心等使用）
     */
    public SysUser getById(Long userId) {
        return requireUser(userId);
    }

    /**
     * 凭证校验（登录入口）：按用户名查未删除用户，校验状态与 BCrypt 密码。
     *
     * <p>供认证服务跨服务调用；用户不存在 / 密码错误统一抛 {@link LoginFailException}（防账号枚举），
     * 禁用账号抛 {@link UserDisabledException}；返回不含密码哈希的认证信息。
     */
    public UserAuthInfo verifyCredentials(String username, String rawPassword) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, BooleanType.FALSE));
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new LoginFailException();
        }
        if (user.getStatus() == null || user.getStatus() != StatusType.ENABLED) {
            throw new UserDisabledException(username);
        }
        return toAuthInfo(user);
    }

    /**
     * 修改密码（个人中心）：校验原密码后更新（BCrypt），置 passwordModified=TRUE。
     *
     * <p>供认证服务跨服务调用；改密后会话失效由认证服务负责踢下线。
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = requireUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new LoginFailException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordModified(BooleanType.TRUE);
        userMapper.updateById(user);
        log.info("password_changed_by_auth userId={}", userId);
    }

    /**
     * 按用户名查询未删除用户（忘记密码定位用户）。
     */
    public UserAuthInfo getByUsername(String username) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, BooleanType.FALSE));
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return toAuthInfo(user);
    }

    /**
     * 组装认证信息（不含密码哈希）。
     */
    private UserAuthInfo toAuthInfo(SysUser user) {
        UserAuthInfo info = new UserAuthInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname());
        info.setPasswordModified(user.getPasswordModified() == BooleanType.TRUE);
        info.setEnabled(user.getStatus() != null && user.getStatus() == StatusType.ENABLED);
        return info;
    }

    /**
     * 绑定角色：整体覆盖该用户的角色集合
     */
    private void bindRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setId(IdUtils.generateId());
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setDeleted(BooleanType.FALSE);
            userRoleMapper.insert(userRole);
        }
    }

    private SysUser requireUser(Long userId) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getId, userId)
                .eq(SysUser::getDeleted, BooleanType.FALSE));
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    /**
     * admin 账号保护：禁止删除 / 禁用
     */
    private void ensureNotAdmin(SysUser user) {
        if (SysConstants.ADMIN_USERNAME.equals(user.getUsername())) {
            throw new IllegalArgumentException("超级管理员账号禁止删除 / 禁用");
        }
    }
}
