-- =====================================================================
-- 日志模块菜单 / 权限初始化脚本（aicyi-log 域）
-- =====================================================================
-- 说明：
--   1. 幂等：菜单主键固定（500~524，日志保留 ID 段），配合 INSERT IGNORE
--      重复执行自动跳过已存在记录；角色-菜单绑定同理（600 段）。
--   2. 主键语义：本脚本使用的 3 位小整数为保留段，与自增主键（AUTO_INCREMENT）
--      兼容：AUTO 列允许显式插入 id，自增序列从 max(id)+1 继续，互不冲突。
--   3. 结构：目录(menu_type=1) → 菜单(menu_type=2, path=前端路由) → 按钮
--      (menu_type=3, perm_code+api_path)。侧边栏仅渲染 visible=1 且 type≠3，
--      且按「角色-菜单绑定（sys_role_menu）」过滤，超级管理员亦需显式绑定。
--   4. 路由对齐：菜单 path 与前端已注册路由逐字一致——/log（vite proxy → 网关 18000）。
--   5. 权限码对齐 aicyi-log 实际端点（OperLogController）：
--      system:log:list  → GET   /api/log/oper/list、/api/log/oper/{id}
--      system:log:clean → DELETE /api/log/clean
--      （清理端点特意独立前缀，避免 {id} 通配吞掉字面量 /clean 造成越权放行）
--   6. 执行方式：手动执行（需先指定库，如 USE aicyi_platform;）。
-- =====================================================================

USE aicyi_platform;

-- 运行时变量：超级管理员角色 ID
SET @super_role_id = (SELECT id FROM sys_role WHERE role_key = 'super_admin' AND deleted = 0 LIMIT 1);

-- ---------------------------------------------------------------------
-- 一、日志管理（顶级目录）及其菜单 / 按钮
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_menu
    (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
VALUES
    (500, 0,   '日志管理', 1, NULL,             'Document', 2, 1, NULL, NULL, 1),
    (501, 500, '操作日志', 2, '/log',           'Tickets',  1, 1, NULL, NULL, 1),
    -- 按钮（日志权限码）
    (511, 501, '日志查询', 3, NULL, NULL, 1, 1, 'system:log:list',  '/api/log/oper/list', 1),
    (512, 501, '日志详情', 3, NULL, NULL, 2, 1, 'system:log:list',  '/api/log/oper/{id}', 1),
    (513, 501, '日志清理', 3, NULL, NULL, 3, 1, 'system:log:clean', '/api/log/clean',     1);

-- ---------------------------------------------------------------------
-- 二、超级管理员角色绑定全部日志菜单 / 按钮（否则侧边栏不展示）
--     role_menu.id = 600 + (menu_id - 500)，主键固定保证幂等
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT 600 + t.menu_id - 500, @super_role_id, t.menu_id
FROM (
             SELECT 500 AS menu_id
    UNION ALL SELECT 501
    UNION ALL SELECT 511
    UNION ALL SELECT 512
    UNION ALL SELECT 513
) t
WHERE @super_role_id IS NOT NULL;
