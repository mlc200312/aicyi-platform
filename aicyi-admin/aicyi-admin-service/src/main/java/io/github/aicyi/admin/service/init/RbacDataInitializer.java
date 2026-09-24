package io.github.aicyi.admin.service.init;

import io.github.aicyi.admin.dao.mapper.SysMenuMapper;
import io.github.aicyi.admin.dao.mapper.SysRoleMapper;
import io.github.aicyi.admin.dao.mapper.SysRoleMenuMapper;
import io.github.aicyi.admin.dao.mapper.SysUserMapper;
import io.github.aicyi.admin.dao.mapper.SysUserRoleMapper;
import io.github.aicyi.admin.domain.constant.SysConstants;
import io.github.aicyi.admin.domain.entity.SysMenu;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysRoleMenu;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.domain.entity.SysUserRole;
import io.github.aicyi.admin.domain.type.MenuType;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.domain.type.VisibleType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.middleware.kit.util.IdUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统初始化器。
 *
 * <p>首次启动自动执行：创建超级管理员 admin（初始密码 admin，BCrypt 加密）、
 * 超级管理员角色（super_admin，拥有系统所有权限，不可删除、不可禁用）、
 * 基础菜单（用户管理 / 角色管理 / 菜单管理 / 权限管理 / 日志管理 / 登录退出）、
 * 绑定 admin 与超级管理员角色、超级管理员角色绑定全部菜单权限。
 *
 * <p><b>幂等</b>：仅当 admin 用户不存在时执行一次，重复启动不重复覆盖（避免自定义配置丢失）。
 */
@Component
public class RbacDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RbacDataInitializer.class);

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysMenuMapper menuMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysRoleMenuMapper roleMenuMapper;

    private final PasswordEncoder passwordEncoder;

    public RbacDataInitializer(SysUserMapper userMapper,
                               SysRoleMapper roleMapper,
                               SysMenuMapper menuMapper,
                               SysUserRoleMapper userRoleMapper,
                               SysRoleMenuMapper roleMenuMapper,
                               PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) {
        Long adminCount = userMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, SysConstants.ADMIN_USERNAME));
        boolean adminExists = adminCount != null && adminCount > 0;

        Long menuCount = menuMapper.selectCount(Wrappers.emptyWrapper());
        boolean menusEmpty = menuCount == null || menuCount == 0;

        // admin 不存在时：完整初始化（admin + 角色 + 菜单 + 绑定）
        if (!adminExists) {
            // 1. 超级管理员用户
            SysUser admin = new SysUser();
            admin.setId(IdUtils.generateId());
            admin.setUsername(SysConstants.ADMIN_USERNAME);
            admin.setPassword(passwordEncoder.encode(SysConstants.ADMIN_INIT_PASSWORD));
            admin.setNickname(SysConstants.ADMIN_NICKNAME);
            admin.setStatus(StatusType.ENABLED);
            admin.setPasswordModified(BooleanType.FALSE);
            userMapper.insert(admin);

            // 2. 超级管理员角色
            SysRole superRole = new SysRole();
            superRole.setId(IdUtils.generateId());
            superRole.setRoleName(SysConstants.SUPER_ROLE_NAME);
            superRole.setRoleKey(SysConstants.SUPER_ROLE_KEY);
            superRole.setDescription("拥有系统全部权限，不可删除、不可禁用");
            superRole.setStatus(StatusType.ENABLED);
            superRole.setBuiltin(BooleanType.TRUE);
            roleMapper.insert(superRole);

            // 3. 绑定 admin → 超级管理员角色
            SysUserRole userRole = new SysUserRole();
            userRole.setId(IdUtils.generateId());
            userRole.setUserId(admin.getId());
            userRole.setRoleId(superRole.getId());
            userRoleMapper.insert(userRole);

            // 4. 初始化菜单并绑定角色权限
            List<Long> menuIds = insertBaseMenus();
            bindRoleMenus(superRole.getId(), menuIds);

            log.info("RBAC init done: adminId={}, superRoleId={}, menus={}", admin.getId(), superRole.getId(), menuIds.size());
            return;
        }

        // admin 已存在但菜单为空时：仅重建菜单并绑定到超级管理员角色
        if (menusEmpty) {
            SysRole superRole = roleMapper.selectOne(
                    Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleKey, SysConstants.SUPER_ROLE_KEY));
            if (superRole != null) {
                List<Long> menuIds = insertBaseMenus();
                bindRoleMenus(superRole.getId(), menuIds);
                log.info("RBAC menu rebuild done: superRoleId={}, menus={}", superRole.getId(), menuIds.size());
            }
        }
    }

    private void bindRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setId(IdUtils.generateId());
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }

    /**
     * 插入基础菜单并返回全部菜单 ID（含按钮）
     * 结构：系统管理（目录）→ 用户/角色/菜单/权限管理（菜单）→ 各操作按钮
     */
    private List<Long> insertBaseMenus() {
        List<Long> menuIds = new ArrayList<>();

        // 系统管理（顶级目录）
        long systemDir = insertMenu(0L, "系统管理", MenuType.DIRECTORY, null, null, null, "Setting", 1, true);
        menuIds.add(systemDir);

        // 用户管理（菜单）
        long userMenu = insertMenu(systemDir, "用户管理", MenuType.MENU, "/system/user", null, null, "User", 1, true);
        menuIds.add(userMenu);
        menuIds.add(insertMenu(userMenu, "用户查询", MenuType.BUTTON, null, "system:user:list", "/api/system/user/list", null, 1, true));
        menuIds.add(insertMenu(userMenu, "用户新增", MenuType.BUTTON, null, "system:user:add", "/api/system/user/add", null, 2, true));
        menuIds.add(insertMenu(userMenu, "用户编辑", MenuType.BUTTON, null, "system:user:edit", "/api/system/user/edit", null, 3, true));
        menuIds.add(insertMenu(userMenu, "用户删除", MenuType.BUTTON, null, "system:user:delete", "/api/system/user/delete", null, 4, true));
        menuIds.add(insertMenu(userMenu, "重置密码", MenuType.BUTTON, null, "system:user:reset-password", "/api/system/user/reset-password", null, 5, true));
        menuIds.add(insertMenu(userMenu, "分配角色", MenuType.BUTTON, null, "system:user:assign-role", "/api/system/user/assign-role", null, 6, true));
        menuIds.add(insertMenu(userMenu, "用户导出", MenuType.BUTTON, null, "system:user:export", "/api/system/user/export", null, 7, true));
        menuIds.add(insertMenu(userMenu, "用户批量删除", MenuType.BUTTON, null, "system:user:batch-delete", "/api/system/user/batch-delete", null, 8, true));
        menuIds.add(insertMenu(userMenu, "用户导入", MenuType.BUTTON, null, "system:user:import", "/api/system/user/import", null, 9, true));
        menuIds.add(insertMenu(userMenu, "用户导入模板", MenuType.BUTTON, null, "system:user:import", "/api/system/user/import-template", null, 10, true));

        // 角色管理（菜单）
        long roleMenu = insertMenu(systemDir, "角色管理", MenuType.MENU, "/system/role", null, null, "Avatar", 2, true);
        menuIds.add(roleMenu);
        menuIds.add(insertMenu(roleMenu, "角色查询", MenuType.BUTTON, null, "system:role:list", "/api/system/role/list", null, 1, true));
        menuIds.add(insertMenu(roleMenu, "角色新增", MenuType.BUTTON, null, "system:role:add", "/api/system/role/add", null, 2, true));
        menuIds.add(insertMenu(roleMenu, "角色编辑", MenuType.BUTTON, null, "system:role:edit", "/api/system/role/edit", null, 3, true));
        menuIds.add(insertMenu(roleMenu, "角色删除", MenuType.BUTTON, null, "system:role:delete", "/api/system/role/delete", null, 4, true));
        menuIds.add(insertMenu(roleMenu, "分配权限", MenuType.BUTTON, null, "system:role:assign-perm", "/api/system/role/assign-perm", null, 5, true));

        // 菜单管理（菜单）
        long menuMgr = insertMenu(systemDir, "菜单管理", MenuType.MENU, "/system/menu", null, null, "Menu", 3, true);
        menuIds.add(menuMgr);
        menuIds.add(insertMenu(menuMgr, "菜单查询", MenuType.BUTTON, null, "system:menu:list", "/api/system/menu/list", null, 1, true));
        menuIds.add(insertMenu(menuMgr, "菜单新增", MenuType.BUTTON, null, "system:menu:add", "/api/system/menu/add", null, 2, true));
        menuIds.add(insertMenu(menuMgr, "菜单编辑", MenuType.BUTTON, null, "system:menu:edit", "/api/system/menu/edit", null, 3, true));
        menuIds.add(insertMenu(menuMgr, "菜单删除", MenuType.BUTTON, null, "system:menu:delete", "/api/system/menu/delete", null, 4, true));

        // 权限管理（菜单）
        long permMenu = insertMenu(systemDir, "权限管理", MenuType.MENU, "/system/permission", null, null, "Key", 4, true);
        menuIds.add(permMenu);
        menuIds.add(insertMenu(permMenu, "权限查询", MenuType.BUTTON, null, "system:perm:list", "/api/system/perm/list", null, 1, true));
        menuIds.add(insertMenu(permMenu, "用户授权", MenuType.BUTTON, null, "system:perm:assign", "/api/system/perm/assign", null, 2, true));

        // 日志管理（顶级目录）
        long logDir = insertMenu(0L, "日志管理", MenuType.DIRECTORY, null, null, null, "Document", 2, true);
        menuIds.add(logDir);
        long logMenu = insertMenu(logDir, "操作日志", MenuType.MENU, "/log", null, null, "Tickets", 1, true);
        menuIds.add(logMenu);
        menuIds.add(insertMenu(logMenu, "日志查询", MenuType.BUTTON, null, "system:log:list", "/api/system/oper-log/list", null, 1, true));

        // 配置中心（顶级目录）
        long configDir = insertMenu(0L, "配置中心", MenuType.DIRECTORY, null, null, null, "Setting", 3, true);
        menuIds.add(configDir);
        long templateMenu = insertMenu(configDir, "模板管理", MenuType.MENU, "/template", null, null, "Document", 1, true);
        menuIds.add(templateMenu);
        menuIds.add(insertMenu(templateMenu, "模板查询", MenuType.BUTTON, null, "system:template:list", "/api/system/message-template/list", null, 1, true));
        menuIds.add(insertMenu(templateMenu, "模板新增", MenuType.BUTTON, null, "system:template:add", "/api/system/message-template/add", null, 2, true));
        menuIds.add(insertMenu(templateMenu, "模板编辑", MenuType.BUTTON, null, "system:template:edit", "/api/system/message-template/edit", null, 3, true));
        menuIds.add(insertMenu(templateMenu, "模板删除", MenuType.BUTTON, null, "system:template:delete", "/api/system/message-template/delete", null, 4, true));
        menuIds.add(insertMenu(templateMenu, "模板状态", MenuType.BUTTON, null, "system:template:status", "/api/system/message-template/status", null, 5, true));

        return menuIds;
    }

    private long insertMenu(Long parentId, String name, MenuType type, String path,
                            String permCode, String apiPath, String icon, int sort, boolean visible) {
        SysMenu menu = new SysMenu();
        menu.setId(IdUtils.generateId());
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuType(type);
        menu.setPath(path);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setVisible(visible ? VisibleType.SHOW : VisibleType.HIDE);
        menu.setPermCode(permCode);
        menu.setApiPath(apiPath);
        menu.setBuiltin(BooleanType.TRUE);
        menuMapper.insert(menu);
        return menu.getId();
    }
}
