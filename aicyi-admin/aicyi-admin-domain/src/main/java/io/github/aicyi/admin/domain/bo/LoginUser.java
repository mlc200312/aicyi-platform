package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.token.JwtInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 登录用户主体：作为 AccessToken 的 principal 载荷，实现脚手架 {@link JwtInfo} 接口。
 *
 * <p>经脚手架 {@code ClaimFilteredPrincipalSerializer} 序列化进 JWT {@code principal} claim，
 * 需保持无参构造 + getter/setter 以便 Jackson 反序列化。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements JwtInfo {

    /**
     * 用户 ID（字符串形式）
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 设备标识（客户端登录时携带，用于单点会话的设备识别）
     */
    private String deviceId;

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String getUniqueName() {
        return username;
    }
}
