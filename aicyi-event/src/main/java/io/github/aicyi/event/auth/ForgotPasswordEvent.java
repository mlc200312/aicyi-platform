package io.github.aicyi.event.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 忘记密码提醒领域事件（认证中心 → 消息中心）。
 *
 * <p>免鉴权入口「忘记密码」提交后，认证中心不直接写消息库（避免跨库耦合），
 * 而是投递本事件至 topic {@code auth-events}（routing {@code auth.forgot-password}），
 * 由 aicyi-message 消费并落站内信，实现「非强一致业务异步解耦」。
 */
@Getter
@Setter
@NoArgsConstructor
public class ForgotPasswordEvent implements Serializable {
    /**
     * 用户 ID（作为站内信接收人）
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    public ForgotPasswordEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
