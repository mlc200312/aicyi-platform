-- =====================================================================
-- 日志菜单 api_path 修复 + 清理端点独立权限（存量环境执行一次；
-- 新环境由新版 RbacDataInitializer 直接建对，无需执行）
--
-- 背景：
--   1. 「日志查询」按钮注册的 api_path 为 /api/system/oper-log/list（不存在的路径），
--      实际路由是 /api/log/oper/list → 网关匹配不到权限标识，
--      「未配置放行」语义下任意登录用户可读全量审计日志。
--   2. 清理端点 DELETE /api/log/clean 此前为 /api/log/oper/clean 且未注册——
--      任意登录用户可清空审计日志且动作不留痕。
--   3. 清理端点特意迁出 /api/log/oper/** 前缀（避免与详情 {id} 占位归一化后的
--      单段通配重叠，经网关「任一命中即放行」聚合被查看权限误放行），
--      并以独立权限 system:log:clean 收口，默认仅超级管理员持有。
-- 全部语句幂等，可重复执行。
-- =====================================================================

-- 1. 修正「日志查询」api_path 为实际路由（仅当仍是旧错误值时更新）
UPDATE sys_menu SET api_path = '/api/log/oper/list'
WHERE menu_name = '日志查询' AND menu_type = 3 AND deleted = 0
  AND api_path = '/api/system/oper-log/list';

-- 2. 补配「日志详情」按钮（复用查看权限；父级挂「操作日志」菜单，存在即跳过）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
SELECT 503, m.id, '日志详情', 3, NULL, NULL, 2, 1, 'system:log:list', '/api/log/oper/{id}', 1
FROM sys_menu m
WHERE m.menu_name = '操作日志' AND m.menu_type = 2 AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu e
                  WHERE e.perm_code = 'system:log:list' AND e.api_path = '/api/log/oper/{id}' AND e.deleted = 0);

-- 3. 补配「日志清理」按钮（独立高危权限；存在即跳过）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
SELECT 504, m.id, '日志清理', 3, NULL, NULL, 3, 1, 'system:log:clean', '/api/log/clean', 1
FROM sys_menu m
WHERE m.menu_name = '操作日志' AND m.menu_type = 2 AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_menu e
                  WHERE e.perm_code = 'system:log:clean' AND e.deleted = 0);

-- 4. 超级管理员角色绑定新增按钮（存在即跳过）
INSERT INTO sys_role_menu (id, role_id, menu_id, deleted)
SELECT 603, r.id, m.id, 0
FROM sys_role r
JOIN sys_menu m ON m.perm_code = 'system:log:clean' AND m.deleted = 0
WHERE r.role_key = 'super_admin' AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0);

INSERT INTO sys_role_menu (id, role_id, menu_id, deleted)
SELECT 604, r.id, m.id, 0
FROM sys_role r
JOIN sys_menu m ON m.perm_code = 'system:log:list' AND m.api_path = '/api/log/oper/{id}' AND m.deleted = 0
WHERE r.role_key = 'super_admin' AND r.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm
                  WHERE rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0);
