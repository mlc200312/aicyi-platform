-- =====================================================================
-- 消息模块菜单 / 权限初始化脚本（aicyi-message 域）
-- =====================================================================
-- 说明：
--   1. 幂等：菜单主键固定（450~479，消息保留 ID 段），配合 INSERT IGNORE
--      重复执行自动跳过已存在记录；角色-菜单绑定同理（550 段）。
--   2. 主键语义：本脚本使用的 3 位小整数为保留段，与自增主键（AUTO_INCREMENT）
--      兼容：AUTO 列允许显式插入 id，自增序列从 max(id)+1 继续，互不冲突。
--   3. 结构：目录(menu_type=1) → 菜单(menu_type=2, path=前端路由) → 按钮
--      (menu_type=3, perm_code+api_path)。侧边栏仅渲染 visible=1 且 type≠3，
--      且按「角色-菜单绑定（sys_role_menu）」过滤，超级管理员亦需显式绑定。
--   4. 路由对齐：菜单 path 与前端已注册路由逐字一致——
--      /message（我的消息，用户端）、/message/template（消息模板，管理端）。
--   5. 权限码对齐 aicyi-message 实际端点：
--      用户端（MessageController /api/message）：
--        message:view → GET  /api/message/list、/api/message/unread-count
--        message:read → PUT  /api/message/read/{id}、/api/message/read-all
--      管理端（MessageTemplateController /api/message/template）：
--        message:template:list/detail/add/edit/delete/status/test-send
--   6. 执行方式：手动执行（需先指定库，如 USE aicyi_platform;）。
-- =====================================================================

USE aicyi_platform;

-- 运行时变量：超级管理员角色 ID
SET @super_role_id = (SELECT id FROM sys_role WHERE role_key = 'super_admin' AND deleted = 0 LIMIT 1);

-- ---------------------------------------------------------------------
-- 一、消息中心（顶级目录）及其菜单 / 按钮
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_menu
    (id, parent_id, menu_name, menu_type, path, icon, sort, visible, perm_code, api_path, builtin)
VALUES
    -- 顶级目录：消息中心
    (450, 0,   '消息中心', 1, NULL,              'Bell',    3, 1, NULL, NULL, 1),
    -- 菜单：我的消息（用户端）/ 消息模板（管理端）
    (451, 450, '我的消息', 2, '/message',        'Message', 1, 1, NULL, NULL, 1),
    (452, 450, '消息模板', 2, '/message/template','Memo',   2, 1, NULL, NULL, 1),
    -- 按钮（用户端：站内信）
    (461, 451, '消息列表',  3, NULL, NULL, 1, 1, 'message:view', '/api/message/list',           1),
    (462, 451, '消息未读数',3, NULL, NULL, 2, 1, 'message:view', '/api/message/unread-count',   1),
    (463, 451, '标记已读',  3, NULL, NULL, 3, 1, 'message:read', '/api/message/read/{id}',      1),
    (464, 451, '全部已读',  3, NULL, NULL, 4, 1, 'message:read', '/api/message/read-all',       1),
    -- 按钮（管理端：消息模板）
    (471, 452, '模板查询',    3, NULL, NULL, 1, 1, 'message:template:list',      '/api/message/template/list',        1),
    (472, 452, '模板详情',    3, NULL, NULL, 2, 1, 'message:template:detail',    '/api/message/template/detail/{id}',  1),
    (473, 452, '模板新增',    3, NULL, NULL, 3, 1, 'message:template:add',       '/api/message/template/add',          1),
    (474, 452, '模板编辑',    3, NULL, NULL, 4, 1, 'message:template:edit',      '/api/message/template/edit/{id}',    1),
    (475, 452, '模板删除',    3, NULL, NULL, 5, 1, 'message:template:delete',    '/api/message/template/delete',       1),
    (476, 452, '模板启停',    3, NULL, NULL, 6, 1, 'message:template:status',    '/api/message/template/status/{id}',  1),
    (477, 452, '模板测试发送',3, NULL, NULL, 7, 1, 'message:template:test-send', '/api/message/template/test-send/{id}',1);

-- ---------------------------------------------------------------------
-- 二、超级管理员角色绑定全部消息菜单 / 按钮（否则侧边栏不展示）
--     role_menu.id = 550 + (menu_id - 450)，主键固定保证幂等
-- ---------------------------------------------------------------------
INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
SELECT 550 + t.menu_id - 450, @super_role_id, t.menu_id
FROM (
             SELECT 450 AS menu_id
    UNION ALL SELECT 451
    UNION ALL SELECT 452
    UNION ALL SELECT 461
    UNION ALL SELECT 462
    UNION ALL SELECT 463
    UNION ALL SELECT 464
    UNION ALL SELECT 471
    UNION ALL SELECT 472
    UNION ALL SELECT 473
    UNION ALL SELECT 474
    UNION ALL SELECT 475
    UNION ALL SELECT 476
    UNION ALL SELECT 477
) t
WHERE @super_role_id IS NOT NULL;
