-- =====================================================================
-- 工单模块菜单 / 权限初始化脚本（aicyi-work-order 域）
-- =====================================================================
-- 说明：
--   1. 幂等：菜单主键固定（400~424，工单保留 ID 段），配合 INSERT IGNORE
--      重复执行自动跳过已存在记录；角色-菜单绑定同理（500 段）。
--   2. 主键语义：本脚本使用的 3 位小整数为保留段，与自增主键（AUTO_INCREMENT）
--      兼容：AUTO 列允许显式插入 id，自增序列从 max(id)+1 继续，互不冲突。
--   3. 结构：目录(menu_type=1) → 菜单(menu_type=2, path=前端路由) → 按钮
--      (menu_type=3, perm_code+api_path)。侧边栏仅渲染 visible=1 且 type≠3，
--      且按「角色-菜单绑定（sys_role_menu）」过滤，超级管理员亦需显式绑定。
--   4. 路由对齐：菜单 path 与前端已注册路由逐字一致——
--      /work-order/my、/work-order/create、/system/work-order（vite proxy → 网关 18000）。
--      管理端「工单管理」挂在「工单中心」目录（400）下，与用户端菜单同属一级目录。
--   5. 权限码对齐 aicyi-work-order 实际端点：
--      用户端（WorkOrderController /api/work-order）：
--        work-order:submit → POST /api/work-order
--        work-order:view   → GET  /api/work-order/page、/{id}、/replies/{id}；
--                             POST /api/work-order/reply/{id}
--      管理端（WorkOrderManageController /api/system/work-order）：
--        system:work-order:view    → GET  /api/system/work-order/page、/{id}
--        system:work-order:process → POST /api/system/work-order/process/{id}
--   6. 执行方式：手动执行（需先指定库，如 USE aicyi_platform;）。
--   7. 存量库修正：历史版本按钮 api_path 与真实端点不一致（{id}/replies、
--      {id}/process 顺序颠倒；管理端无独立回复按钮），INSERT IGNORE 不更新
--      已存在记录，故第三节提供幂等 UPDATE / DELETE 修正。
-- =====================================================================

USE aicyi_platform;

-- 运行时变量：超级管理员角色 ID
SET @super_role_id = (SELECT id FROM sys_role WHERE role_key = 'super_admin' AND deleted = 0 LIMIT 1);

-- ---------------------------------------------------------------------
-- 一、工单中心（顶级目录）及其用户端菜单 / 按钮
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_menu
    (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
VALUES
    -- 顶级目录：工单中心
    (400, 0,   '工单中心', 1, NULL,                'Tickets', 4, 1, NULL, NULL, 1),
    -- 菜单：我的工单 / 提交工单 / 工单管理（管理端，挂工单中心下）
    (401, 400, '我的工单', 2, '/work-order/my',     'Tickets', 1, 1, NULL, NULL, 1),
    (402, 400, '提交工单', 2, '/work-order/create', 'EditPen', 2, 1, NULL, NULL, 1),
    (420, 400, '工单管理', 2, '/system/work-order', 'Tickets', 3, 1, NULL, NULL, 1),
    -- 按钮（用户端权限码，对齐真实端点）
    (410, 402, '提交工单',    3, NULL, NULL, 1, 1, 'work-order:submit', '/api/work-order',                   1),
    (411, 401, '工单查询',    3, NULL, NULL, 1, 1, 'work-order:view',   '/api/work-order/page',              1),
    (412, 401, '工单详情',    3, NULL, NULL, 2, 1, 'work-order:view',   '/api/work-order/{id}',              1),
    (413, 401, '回复工单',    3, NULL, NULL, 3, 1, 'work-order:view',   '/api/work-order/reply/{id}',        1),
    (414, 401, '回复记录',    3, NULL, NULL, 4, 1, 'work-order:view',   '/api/work-order/replies/{id}',      1),
    -- 按钮（管理端权限码，对齐真实端点）
    (421, 420, '工单列表查询', 3, NULL, NULL, 1, 1, 'system:work-order:view',    '/api/system/work-order/page',            1),
    (422, 420, '工单详情查看', 3, NULL, NULL, 2, 1, 'system:work-order:view',    '/api/system/work-order/{id}',            1),
    (423, 420, '工单处理',     3, NULL, NULL, 3, 1, 'system:work-order:process', '/api/system/work-order/process/{id}',    1);

-- ---------------------------------------------------------------------
-- 二、超级管理员角色绑定全部工单菜单 / 按钮（否则侧边栏不展示）
--     role_menu.id = 500 + (menu_id - 400)，主键固定保证幂等
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT 500 + t.menu_id - 400, @super_role_id, t.menu_id
FROM (
             SELECT 400 AS menu_id
    UNION ALL SELECT 401
    UNION ALL SELECT 402
    UNION ALL SELECT 410
    UNION ALL SELECT 411
    UNION ALL SELECT 412
    UNION ALL SELECT 413
    UNION ALL SELECT 414
    UNION ALL SELECT 420
    UNION ALL SELECT 421
    UNION ALL SELECT 422
    UNION ALL SELECT 423
) t
WHERE @super_role_id IS NOT NULL;

-- ---------------------------------------------------------------------
-- 三、存量库修正（历史版本按钮 api_path 与真实端点不一致，幂等可重复执行）
-- ---------------------------------------------------------------------
-- 3.1 用户端回复记录端点：历史按 /{id}/replies 配置，实际为 /replies/{id}
UPDATE sys_menu SET api_path = '/api/work-order/replies/{id}'
WHERE api_path = '/api/work-order/{id}/replies' AND deleted = 0;

-- 3.2 管理端处理端点：历史按 /{id}/process 配置，实际为 /process/{id}
UPDATE sys_menu SET api_path = '/api/system/work-order/process/{id}'
WHERE api_path = '/api/system/work-order/{id}/process' AND deleted = 0;

-- 3.3 历史「工单回复」按钮指向管理端 /api/system/work-order/{id}/reply（该端点不存在），
--     回复能力归属用户端 /reply/{id}（菜单 413），清理该死配置
DELETE FROM sys_menu
WHERE menu_name = '工单回复' AND api_path = '/api/system/work-order/{id}/reply' AND deleted = 0;

-- 3.4 历史「工单管理」挂在「系统管理」目录：统一改挂「工单中心」（400），排序 3
UPDATE sys_menu SET parent_id = 400, sort = 3
WHERE menu_name = '工单管理' AND menu_type = 2 AND path = '/system/work-order' AND deleted = 0;
