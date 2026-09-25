-- =====================================================================
-- aicyi-platform 统一授权认证后台管理系统 建表脚本（MySQL 8，utf8mb4）
-- =====================================================================
-- 说明：
--   1. 由 Spring SQL Init 在启动时自动执行（mode=always），
--      DDL 全部使用 IF NOT EXISTS，重复启动幂等，不覆盖已有表。
--   2. 主键统一数据库自增：所有表 id BIGINT AUTO_INCREMENT，
--      对应 @TableId(type = IdType.AUTO)，插入后由 MyBatis-Plus 自动回填。
--   3. 初始化数据由 RbacDataInitializer 在首次启动时写入（重复启动不覆盖）。
--   4. 枚举语义列（status / type / flag / builtin 等）统一 TINYINT 存 code，
--      对应 Java 枚举经 IEnumTypeHandler 按 code 映射。
--   5. 通用审计列约定：deleted 逻辑删除（0 未删 / 1 已删）、version 乐观锁、
--      create_time / update_time 统一 DATETIME 自动维护。
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. 系统用户表：账号主体，含登录凭据与基础资料
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    username           VARCHAR(64)  NOT NULL COMMENT '用户名（唯一标识，禁止修改）',
    password           VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 加密存储）',
    nickname           VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    mobile             VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email              VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    status             TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    remark             VARCHAR(255) DEFAULT NULL COMMENT '备注',
    password_modified  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已修改初始密码：1 是 / 0 否（初始密码登录提醒）',
    deleted            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version            INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB COMMENT = '系统用户表';

-- ---------------------------------------------------------------------
-- 2. 系统角色表：权限载体，角色标识全局唯一
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    role_name   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    role_key    VARCHAR(64)  NOT NULL COMMENT '角色标识（唯一）',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 启用 / 0 禁用',
    builtin     TINYINT      NOT NULL DEFAULT 0 COMMENT '内置角色（超级管理员）：1 是 / 0 否',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE = InnoDB COMMENT = '系统角色表';

-- ---------------------------------------------------------------------
-- 3. 系统菜单表：目录 / 菜单 / 按钮三级结构，含权限标识与接口路径
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父级 ID（0 = 顶级）',
    menu_name   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    menu_type   TINYINT      NOT NULL COMMENT '类型：1 目录 / 2 菜单 / 3 按钮',
    path        VARCHAR(128) DEFAULT NULL COMMENT '路由地址',
    icon        VARCHAR(64)  DEFAULT NULL COMMENT '图标',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    visible     TINYINT      NOT NULL DEFAULT 1 COMMENT '是否显示：1 显示 / 0 隐藏',
    perm_code   VARCHAR(128) DEFAULT NULL COMMENT '权限标识（如 system:user:list）',
    api_path    VARCHAR(255) DEFAULT NULL COMMENT '接口路径（用于后端权限拦截匹配）',
    builtin     TINYINT      NOT NULL DEFAULT 0 COMMENT '内置菜单（禁止删除）：1 是 / 0 否',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent (parent_id),
    KEY idx_api_path (api_path)
) ENGINE = InnoDB COMMENT = '系统菜单表';

-- ---------------------------------------------------------------------
-- 4. 用户-角色关联表：用户与角色多对多绑定，权限自动叠加
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    user_id     BIGINT   NOT NULL COMMENT '用户 ID',
    role_id     BIGINT   NOT NULL COMMENT '角色 ID',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version     INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_role (role_id)
) ENGINE = InnoDB COMMENT = '用户-角色关联表';

-- ---------------------------------------------------------------------
-- 5. 角色-菜单权限关联表：角色与菜单/按钮多对多绑定
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    role_id     BIGINT   NOT NULL COMMENT '角色 ID',
    menu_id     BIGINT   NOT NULL COMMENT '菜单 ID',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version     INT      NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_role (role_id),
    KEY idx_menu (menu_id)
) ENGINE = InnoDB COMMENT = '角色-菜单权限关联表';

-- ---------------------------------------------------------------------
-- 6. 用户单独授权表：在角色权限基础上按用户追加 / 扣除
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键（数据库自增）',
    user_id     BIGINT       NOT NULL COMMENT '用户 ID',
    perm_code   VARCHAR(128) NOT NULL COMMENT '权限标识',
    perm_type   TINYINT      NOT NULL DEFAULT 1 COMMENT '类型：1 追加 / 2 扣除',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 / 1 已删',
    version     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_perm (user_id)
) ENGINE = InnoDB COMMENT = '用户单独授权表';
