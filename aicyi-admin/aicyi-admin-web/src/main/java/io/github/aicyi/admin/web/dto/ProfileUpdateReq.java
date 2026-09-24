package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改个人信息请求（个人中心）。
 */
@Schema(description = "修改个人信息请求（个人中心）")
@Getter
@Setter
public class ProfileUpdateReq extends BaseBean implements DtoBean {

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;
    @Override
    public String toString() {
        return "ProfileUpdateReq{nickname='" + nickname + "', mobile='***', email='***'}";
    }
}
