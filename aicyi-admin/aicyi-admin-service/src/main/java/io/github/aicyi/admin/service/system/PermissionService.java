package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.dao.mapper.SysMenuMapper;
import io.github.aicyi.admin.dao.mapper.SysRoleMapper;
import io.github.aicyi.admin.dao.mapper.SysRoleMenuMapper;
import io.github.aicyi.admin.dao.mapper.SysUserPermissionMapper;
import io.github.aicyi.admin.dao.mapper.SysUserRoleMapper;
import io.github.aicyi.admin.domain.entity.SysMenu;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysRoleMenu;
import io.github.aicyi.admin.domain.entity.SysUserPermission;
import io.github.aicyi.admin.domain.entity.SysUserRole;
import io.github.aicyi.admin.domain.type.PermissionType;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.domain.bo.AssignUserPermsBO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.middleware.kit.util.IdUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务：角色授权 / 用户单独授权 / 权限集计算与缓存（需求 4.5 权限管理模块）。
 *
 * <p>权限模型（需求 5.1）：用户 → 多角色 → 多权限，权限自动叠加；
 * 优先级：用户单独权限 > 角色默认权限（追加 / 扣除）。
 */
@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleMapper roleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final SysMenuMapper menuMapper;

    private final SysUserPermissionMapper userPermissionMapper;

    private final PermissionCache permissionCache;

    public PermissionService(SysUserRoleMapper userRoleMapper,
                             SysRoleMapper roleMapper,
                             SysRoleMenuMapper roleMenuMapper,
                             SysMenuMapper menuMapper,
                             SysUserPermissionMapper userPermissionMapper,
                             PermissionCache permissionCache) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
        this.userPermissionMapper = userPermissionMapper;
        this.permissionCache = permissionCache;
    }

    /**
     * 获取用户有效权限集（角色权限 ∪ 单独追加 − 单独扣除），带 Redis 缓存
     */
    public Set<String> getEffectivePermissions(Long userId) {
        Set<String> cached = permissionCache.get(userId);
        if (cached != null) {
            return cached;
        }
        Set<String> perms = computePermissions(userId);
        permissionCache.put(userId, perms);
        return perms;
    }

    /**
     * 实时计算用户权限集（不带缓存），用于权限预览与缓存失效后的重算
     */
    public Set<String> computePermissions(Long userId) {
        Set<String> perms = new LinkedHashSet<>();

        // 1. 角色权限：启用角色 → 角色菜单 → 权限标识
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        for (SysUserRole userRole : userRoles) {
            SysRole role = roleMapper.selectById(userRole.getRoleId());
            if (role == null || role.getStatus() == null
                    || role.getStatus() != StatusType.ENABLED) {
                continue;
            }
            List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                    Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, role.getId()));
            for (SysRoleMenu roleMenu : roleMenus) {
                SysMenu menu = menuMapper.selectById(roleMenu.getMenuId());
                if (menu != null && StringUtils.hasText(menu.getPermCode())) {
                    perms.add(menu.getPermCode());
                }
            }
        }

        // 2. 用户单独授权：追加 / 扣除（优先级高于角色权限）
        List<SysUserPermission> userPerms = userPermissionMapper.selectList(
                Wrappers.<SysUserPermission>lambdaQuery().eq(SysUserPermission::getUserId, userId));
        for (SysUserPermission userPerm : userPerms) {
            if (userPerm.getPermType() == PermissionType.ADD) {
                perms.add(userPerm.getPermCode());
            } else if (userPerm.getPermType() == PermissionType.REMOVE) {
                perms.remove(userPerm.getPermCode());
            }
        }
        return perms;
    }

    /**
     * 查询角色已分配的菜单 ID 集合（角色授权回显）
     */
    public Set<Long> getRoleMenuIds(Long roleId) {
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        Set<Long> menuIds = new LinkedHashSet<>();
        for (SysRoleMenu roleMenu : roleMenus) {
            menuIds.add(roleMenu.getMenuId());
        }
        return menuIds;
    }

    /**
     * 查询用户已配置的单独授权（追加 / 扣除）
     */
    public List<SysUserPermission> getUserPermissions(Long userId) {
        return userPermissionMapper.selectList(
                Wrappers.<SysUserPermission>lambdaQuery().eq(SysUserPermission::getUserId, userId));
    }

    /**
     * 配置用户单独授权：整体覆盖（先清空再按追加 / 扣除写入），权限实时生效
     *
     * @param bo 授权参数（用户 ID / 追加权限集 / 扣除权限集）
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignUserPermissions(AssignUserPermsBO bo) {
        userPermissionMapper.delete(
                Wrappers.<SysUserPermission>lambdaQuery().eq(SysUserPermission::getUserId, bo.getUserId()));

        if (bo.getAddCodes() != null) {
            for (String code : bo.getAddCodes()) {
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                SysUserPermission perm = new SysUserPermission();
                perm.setId(IdUtils.generateId());
                perm.setUserId(bo.getUserId());
                perm.setPermCode(code.trim());
                perm.setPermType(PermissionType.ADD);
                userPermissionMapper.insert(perm);
            }
        }
        if (bo.getRemoveCodes() != null) {
            for (String code : bo.getRemoveCodes()) {
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                SysUserPermission perm = new SysUserPermission();
                perm.setId(IdUtils.generateId());
                perm.setUserId(bo.getUserId());
                perm.setPermCode(code.trim());
                perm.setPermType(PermissionType.REMOVE);
                userPermissionMapper.insert(perm);
            }
        }
        permissionCache.evict(bo.getUserId());
        log.info("user_permissions_assigned userId={} adds={} removes={}", bo.getUserId(), bo.getAddCodes(), bo.getRemoveCodes());
    }

    /**
     * 重置用户权限：清空用户单独授权，恢复角色默认权限（需求 4.5.2 权限重置）
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetUserPermissions(Long userId) {
        userPermissionMapper.delete(
                Wrappers.<SysUserPermission>lambdaQuery().eq(SysUserPermission::getUserId, userId));
        permissionCache.evict(userId);
        log.info("user_permissions_reset userId={}", userId);
    }

    /**
     * 校验用户是否拥有指定权限标识（接口权限拦截调用）
     *
     * @return true 拥有 / false 未拥有
     */
    public boolean hasPermission(Long userId, String permCode) {
        if (permCode == null || permCode.isEmpty()) {
            return true;
        }
        return getEffectivePermissions(userId).contains(permCode);
    }

    /**
     * 按接口路径匹配所需权限标识；未配置权限的接口放行
     *
     * @return 权限标识；接口未配置权限时返回空集（放行）
     */
    public Set<String> findPermCodesByApiPath(String apiPath) {
        List<SysMenu> menus = menuMapper.selectList(
                Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getApiPath, apiPath));
        if (menus.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> codes = new HashSet<>();
        for (SysMenu menu : menus) {
            if (StringUtils.hasText(menu.getPermCode())) {
                codes.add(menu.getPermCode());
            }
        }
        return codes;
    }
}
