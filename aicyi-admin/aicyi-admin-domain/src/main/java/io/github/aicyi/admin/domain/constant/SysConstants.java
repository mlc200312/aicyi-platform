package io.github.aicyi.admin.domain.constant;

/**
 * 系统级常量（统一授权认证后台）。
 */
public final class SysConstants {

    private SysConstants() {
    }

    /** 超级管理员用户名 */
    public static final String ADMIN_USERNAME = "admin";

    /** 超级管理员初始密码（首次启动初始化） */
    public static final String ADMIN_INIT_PASSWORD = "admin123";

    /** 超级管理员昵称 */
    public static final String ADMIN_NICKNAME = "超级管理员";

    /** 超级管理员角色名称 */
    public static final String SUPER_ROLE_NAME = "超级管理员";

    /** 超级管理员角色标识 */
    public static final String SUPER_ROLE_KEY = "super_admin";

    /** 权限缓存 key 前缀：rbac:perms:{userId} */
    public static final String PERM_CACHE_KEY_PREFIX = "rbac:perms:";

    /** 登录失败计数 key 前缀：login:fail:{username} */
    public static final String LOGIN_FAIL_KEY_PREFIX = "login:fail:";

    /** 登录失败次数上限（超过后锁定账号） */
    public static final int LOGIN_FAIL_MAX_COUNT = 5;

    /** 账号锁定分钟数 */
    public static final long LOGIN_LOCK_MINUTES = 15L;

    /** 免权限校验接口前缀（登录/登出/刷新等认证接口） */
    public static final String AUTH_API_PREFIX = "/api/auth";
}
