package io.github.aicyi.admin.client.model;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户认证信息契约（admin → auth 跨服务返回，不含密码哈希）。
 *
 * <p>由 aicyi-admin 用户域返回，供认证服务签发令牌 / 判断初始密码；密码校验在 admin 侧完成，
 * 密码哈希永不跨服务传输。
 */
@Getter
@Setter
public class UserAuthInfo extends BaseBean implements DtoBean {

    /** 用户 ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 是否已修改初始密码 */
    private Boolean passwordModified;

    /** 是否启用 */
    private Boolean enabled;
}
