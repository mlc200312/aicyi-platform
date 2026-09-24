package io.github.aicyi.admin.client.model;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.DtoBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 修改密码契约（auth → admin：校验原密码后更新，置 passwordModified=TRUE）。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordReq extends BaseBean implements DtoBean {

    /** 用户 ID */
    private Long userId;

    /** 原密码（明文，admin 侧校验） */
    private String oldPassword;

    /** 新密码（明文，admin 侧 BCrypt 加密落库） */
    private String newPassword;
}
