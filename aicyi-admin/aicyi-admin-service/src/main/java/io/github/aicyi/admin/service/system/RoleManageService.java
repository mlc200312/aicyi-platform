package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.dao.mapper.SysRoleMapper;
import io.github.aicyi.admin.dao.mapper.SysRoleMenuMapper;
import io.github.aicyi.admin.dao.mapper.SysUserRoleMapper;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysRoleMenu;
import io.github.aicyi.admin.domain.entity.SysUserRole;
import io.github.aicyi.admin.domain.exception.RoleInUseException;
import io.github.aicyi.admin.domain.bo.RoleCreateBO;
import io.github.aicyi.admin.domain.bo.RoleEditBO;
import io.github.aicyi.admin.domain.bo.RoleQueryBO;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.service.convert.ServiceConverter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.middleware.kit.util.IdUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色管理服务：列表 / 新增 / 编辑 / 删除 / 状态管控 / 权限分配。
 *
 * <p>保护规则：超级管理员角色禁止编辑、禁止删除、禁止禁用；
 * 已绑定用户的角色禁止删除，避免用户权限失效。
 */
@Service
public class RoleManageService {

    private static final Logger log = LoggerFactory.getLogger(RoleManageService.class);

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final PermissionCache permissionCache;

    private final PermissionService permissionService;

    public RoleManageService(SysRoleMapper roleMapper,
                             SysRoleMenuMapper roleMenuMapper,
                             SysUserRoleMapper userRoleMapper,
                             PermissionCache permissionCache,
                             PermissionService permissionService) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionCache = permissionCache;
        this.permissionService = permissionService;
    }

    /**
     * 角色分页查询（名称 / 标识模糊搜索）
     */
    public IPage<SysRole> page(RoleQueryBO query) {
        return roleMapper.selectPage(new Page<>(query.getPage(), query.getSize()),
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getDeleted, BooleanType.FALSE)
                        .like(StringUtils.hasText(query.getRoleName()), SysRole::getRoleName, query.getRoleName())
                        .like(StringUtils.hasText(query.getRoleKey()), SysRole::getRoleKey, query.getRoleKey())
                        .orderByAsc(SysRole::getId));
    }

    /**
     * 全部角色（下拉选择，含状态过滤）
     */
    public List<SysRole> listAll() {
        return roleMapper.selectList(
                Wrappers.<SysRole>lambdaQuery()
                        .eq(SysRole::getDeleted, BooleanType.FALSE)
                        .eq(SysRole::getStatus, StatusType.ENABLED)
                        .orderByAsc(SysRole::getId));
    }

    /**
     * 新增角色：角色标识唯一，默认启用
     */
    public SysRole add(RoleCreateBO bo) {
        if (roleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getDeleted, BooleanType.FALSE)
                .eq(SysRole::getRoleKey, bo.getRoleKey())) > 0) {
            throw new IllegalArgumentException("角色标识已存在: " + bo.getRoleKey());
        }
        SysRole role = ServiceConverter.INSTANCE.toDO(bo);
        role.setId(IdUtils.generateId());
        role.setStatus(StatusType.ENABLED);
        role.setBuiltin(BooleanType.FALSE);
        role.setDeleted(BooleanType.FALSE);
        roleMapper.insert(role);
        log.info("role_added roleId={} roleKey={}", role.getId(), bo.getRoleKey());
        return role;
    }

    /**
     * 编辑角色基础信息与状态；超级管理员角色禁止编辑
     */
    @Transactional(rollbackFor = Exception.class)
    public SysRole edit(RoleEditBO bo) {
        SysRole role = requireRole(bo.getRoleId());
        ensureNotSuperRole(role, "编辑");

        ServiceConverter.INSTANCE.updateDO(role, bo);
        roleMapper.updateById(role);

        evictRoleUserCaches(bo.getRoleId());
        log.info("role_edited roleId={}", bo.getRoleId());
        return roleMapper.selectById(bo.getRoleId());
    }

    /**
     * 删除角色：超级管理员禁止删除；已绑定用户的角色禁止删除；逻辑删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) {
        SysRole role = requireRole(roleId);
        ensureNotSuperRole(role, "删除");

        Long boundUsers = userRoleMapper.selectCount(
                Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getDeleted, BooleanType.FALSE)
                        .eq(SysUserRole::getRoleId, roleId));
        if (boundUsers != null && boundUsers > 0) {
            throw new RoleInUseException(role.getRoleName());
        }

        roleMapper.update(null, Wrappers.<SysRole>lambdaUpdate()
                .eq(SysRole::getId, roleId)
                .set(SysRole::getDeleted, BooleanType.TRUE));
        log.info("role_deleted roleId={} roleKey={}", roleId, role.getRoleKey());
    }

    /**
     * 启用 / 禁用角色；禁用后绑定该角色的用户失去对应权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long roleId, StatusType status) {
        if (status == null) {
            // StatusType.fromCode 对非法编码返回 null：不校验会误入禁用分支触发缓存逐出
            throw new IllegalArgumentException("非法的状态参数");
        }
        SysRole role = requireRole(roleId);
        if (status != StatusType.ENABLED) {
            ensureNotSuperRole(role, "禁用");
        }
        role.setStatus(status);
        roleMapper.updateById(role);

        evictRoleUserCaches(roleId);
        log.info("role_status_changed roleId={} status={}", roleId, status);
    }

    /**
     * 角色权限分配（菜单 / 按钮 / 接口权限），保存后该角色下所有用户实时生效
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        requireRole(roleId);

        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                if (menuId == null) {
                    continue;
                }
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setId(IdUtils.generateId());
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setDeleted(BooleanType.FALSE);
                roleMenuMapper.insert(roleMenu);
            }
        }

        evictRoleUserCaches(roleId);
        log.info("role_menus_assigned roleId={} menuCount={}", roleId, menuIds == null ? 0 : menuIds.size());
    }

    /**
     * 查询角色已分配的菜单 ID（权限分配回显）
     */
    public java.util.Set<Long> menuIds(Long roleId) {
        return permissionService.getRoleMenuIds(roleId);
    }

    /**
     * 失效该角色全部绑定用户的权限缓存，使权限变更实时生效
     */
    private void evictRoleUserCaches(Long roleId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getDeleted, BooleanType.FALSE)
                        .eq(SysUserRole::getRoleId, roleId));
        for (SysUserRole userRole : userRoles) {
            permissionCache.evict(userRole.getUserId());
        }
    }

    private SysRole requireRole(Long roleId) {
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getDeleted, BooleanType.FALSE));
        if (role == null) {
            throw new IllegalArgumentException("角色不存在: roleId=" + roleId);
        }
        return role;
    }

    /**
     * 超级管理员角色保护
     */
    private void ensureNotSuperRole(SysRole role, String action) {
        if (role.getBuiltin() != null && role.getBuiltin() == BooleanType.TRUE) {
            throw new IllegalArgumentException("超级管理员角色禁止" + action);
        }
    }
}
