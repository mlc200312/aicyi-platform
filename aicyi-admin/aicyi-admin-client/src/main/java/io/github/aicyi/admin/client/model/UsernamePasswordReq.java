package io.github.aicyi.admin.client.model;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户名密码凭证契约（auth → admin 登录校验入参）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsernamePasswordReq extends BaseBean implements DtoBean {

    /** 用户名 */
    private String username;

    /** 明文密码（仅请求传输，admin 侧 BCrypt 校验） */
    private String password;
}
