package io.github.aicyi.event.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 忘记密码提醒领域事件（认证中心 → 消息中心，异步解耦落站内信）。
 */
@Getter
@Setter
@NoArgsConstructor
public class ForgotPasswordEvent implements Serializable {

    /** 用户 ID（站内信接收人） */
    private Long userId;

    private String username;

    public ForgotPasswordEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
