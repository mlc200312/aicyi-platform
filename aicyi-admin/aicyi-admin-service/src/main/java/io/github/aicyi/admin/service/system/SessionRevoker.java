package io.github.aicyi.admin.service.system;

import io.github.aicyi.admin.domain.bo.LoginUser;
import io.github.aicyi.common.logging.Logger;
import io.github.aicyi.common.logging.LoggerFactory;
import io.github.aicyi.common.token.AuthenticationTokenService;
import io.github.aicyi.common.token.JwtInfo;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 会话吊销组件：使指定用户的全部登录会话失效（删除 / 禁用 / 重置密码后调用）。
 *
 * <p>登录签发已由 aicyi-auth 认证中心接管，本组件仅保留 RBAC 管理侧必需的踢会话能力：
 * 复用脚手架 {@code JwtRefreshAuthenticationTokenService} 的 RefreshToken 注册表
 * （与 aicyi-auth 共用同一 Redis token 存储，实现跨服务吊销）。
 *
 * <p>注入为 {@link JwtInfo} 泛型：脚手架自动装配的 Token Bean 以
 * {@code AuthenticationTokenService<IJWTInfo>} 注册，若按 LoginUser 泛型注入会因
 * Spring 泛型匹配失败导致无可用 Bean。
 */
@Component
public class SessionRevoker {

    private static final Logger log = LoggerFactory.getLogger(SessionRevoker.class);

    private final AuthenticationTokenService<JwtInfo> tokenService;

    public SessionRevoker(AuthenticationTokenService<JwtInfo> tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * 使指定用户全部会话失效
     *
     * @param userId 用户 ID
     */
    public void kickOff(Long userId) {
        LoginUser principal = new LoginUser(String.valueOf(userId), "", "");
        Set<String> refreshTokens = tokenService.getRefreshTokens(principal);
        for (String refreshToken : refreshTokens) {
            tokenService.revokeToken(refreshToken);
        }
        if (!refreshTokens.isEmpty()) {
            log.info("kick_off_user userId={} sessions={}", userId, refreshTokens.size());
        }
    }
}
