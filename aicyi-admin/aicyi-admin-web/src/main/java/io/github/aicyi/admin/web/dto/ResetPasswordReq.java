package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 重置密码请求（管理员重置用户密码）。
 */
@Schema(description = "重置密码请求（管理员重置用户密码）")
@Getter
@Setter
public class ResetPasswordReq extends BaseBean implements DtoBean {

    /**
     * 新密码（重置后用户下次登录将收到初始密码修改提醒）
     */
    @Schema(description = "新密码（重置后用户下次登录将收到初始密码修改提醒）", example = "reset123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String newPassword;
    @Override
    public String toString() {
        return "ResetPasswordReq{newPassword='***'}";
    }
}
