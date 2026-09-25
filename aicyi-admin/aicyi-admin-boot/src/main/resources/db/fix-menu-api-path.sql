-- =====================================================================
-- 菜单 api_path 修复（存量环境执行一次；新环境由新版 RbacDataInitializer 直接建对，无需执行）
--
-- 背景：带 {id} 路径参数的端点此前按精确路径配置（如 /api/system/user/edit），
-- 网关模式匹配与 admin 本地精确匹配均无法命中真实请求（/edit/123），
-- 导致「未配置放行」语义下编辑/启停/重置密码/分配角色等写操作绕过权限拦截。
--
-- 内容：
--   1. 既有按钮 api_path 改为通配模式（对齐网关 ApiPathMatcher，* 匹配单段路径）
--   2. 补配用户/角色启停按钮（此前无对应菜单，端点完全未受控）
--   3. 超级管理员角色绑定新增按钮
-- 全部语句幂等，可重复执行。
-- =====================================================================

-- 1. 既有按钮 api_path 改为通配模式
UPDATE sys_menu SET api_path = '/api/system/user/edit/*'
WHERE api_path = '/api/system/user/edit' AND deleted = 0;

UPDATE sys_menu SET api_path = '/api/system/user/reset-password/*'
WHERE api_path = '/api/system/user/reset-password' AND deleted = 0;

UPDATE sys_menu SET api_path = '/api/system/user/assign-role/*'
WHERE api_path = '/api/system/user/assign-role' AND deleted = 0;

UPDATE sys_menu SET api_path = '/api/system/role/edit/*'
WHERE api_path = '/api/system/role/edit' AND deleted = 0;

UPDATE sys_menu SET api_path = '/api/system/role/assign-perm/*'
WHERE api_path = '/api/system/role/assign-perm' AND deleted = 0;

-- 2. 补配「用户启停」按钮（父级挂用户管理菜单，存在即跳过）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
SELECT 501, m.id, '用户启停', 3, NULL, NULL, 5, 1, 'system:user:status', '/api/system/user/status/*', 1
FROM sys_menu m
WHERE m.menu_name = '用户管理' AND m.menu_type = 2 AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu e
                  WHERE e.perm_code = 'system:user:status' AND e.deleted = 0);

-- 3. 补配「角色启停」按钮（父级挂角色管理菜单，存在即跳过）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
SELECT 502, m.id, '角色启停', 3, NULL, NULL, 6, 1, 'system:role:status', '/api/system/role/status/*', 1
FROM sys_menu m
WHERE m.menu_name = '角色管理' AND m.menu_type = 2 AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu e
                  WHERE e.perm_code = 'system:role:status' AND e.deleted = 0);

-- 4. 超级管理员角色绑定新增按钮（存在即跳过）
INSERT INTO sys_role_menu (id, role_id, menu_id, deleted)
SELECT 601, r.id, m.id, 0
FROM sys_role r
JOIN sys_menu m ON m.perm_code = 'system:user:status' AND m.deleted = 0
WHERE r.role_key = 'super_admin' AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0);

INSERT INTO sys_role_menu (id, role_id, menu_id, deleted)
SELECT 602, r.id, m.id, 0
FROM sys_role r
JOIN sys_menu m ON m.perm_code = 'system:role:status' AND m.deleted = 0
WHERE r.role_key = 'super_admin' AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0);
