-- =====================================================================
-- 清理旧版「配置中心 / 模板管理」死配置菜单（存量环境执行一次；新环境无需执行）
--
-- 背景：旧版 RbacDataInitializer 在 admin 首启时创建了「配置中心 → 模板管理」
-- 菜单，其按钮 api_path 指向 /api/system/message-template/*——该路径经网关
-- 路由到 aicyi-admin（无此 Controller，恒 404），属死配置；真正的模板管理
-- 接口在 aicyi-message（/api/message/template/*），其菜单由
-- aicyi-gateway/src/main/resources/db/init-message-menu.sql 统一初始化。
--
-- 内容：按 api_path 特征与「空目录/空菜单」判定软删（deleted=1），不物理删除，
-- 可追溯；全部语句幂等，可重复执行。角色-菜单绑定（sys_role_menu）随菜单
-- 软删自动失效（权限查询均过滤菜单 deleted），无需单独清理。
-- =====================================================================

USE aicyi_platform;

-- 1. 死配置按钮（api_path 指向 admin 不存在的 /api/system/message-template/*）
UPDATE sys_menu SET deleted = 1
WHERE api_path LIKE '/api/system/message-template/%' AND deleted = 0;

-- 2. 「模板管理」菜单：按钮已全部软删、自身无存活子节点时软删
--    （派生表包裹规避 MySQL 1093：UPDATE 目标表不可直接出现在子查询中）
UPDATE sys_menu SET deleted = 1
WHERE id IN (
    SELECT id FROM (
        SELECT m.id FROM sys_menu m
        WHERE m.menu_name = '模板管理' AND m.menu_type = 2 AND m.builtin = 1 AND m.deleted = 0
          AND NOT EXISTS (SELECT 1 FROM sys_menu b
                          WHERE b.parent_id = m.id AND b.deleted = 0)
    ) x
);

-- 3. 「配置中心」目录：子节点已全部软删、自身为空目录时软删
UPDATE sys_menu SET deleted = 1
WHERE id IN (
    SELECT id FROM (
        SELECT d.id FROM sys_menu d
        WHERE d.menu_name = '配置中心' AND d.menu_type = 1 AND d.parent_id = 0 AND d.builtin = 1 AND d.deleted = 0
          AND NOT EXISTS (SELECT 1 FROM sys_menu c
                          WHERE c.parent_id = d.id AND c.deleted = 0)
    ) y
);
