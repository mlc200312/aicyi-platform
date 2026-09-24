package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 编辑用户请求（用户名不可修改）。
 */
@Schema(description = "编辑用户请求（用户名不可修改）")
@Getter
@Setter
public class UserEditReq extends BaseBean implements DtoBean {

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

    /**
     * 状态：1 启用 / 0 禁用
     */
    @Schema(description = "状态：1 启用 / 0 禁用", example = "1")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "测试账号")
    private String remark;

    /**
     * 绑定的角色 ID 列表
     */
    @Schema(description = "绑定的角色 ID 列表", example = "[1, 2]")
    private List<@NotNull Long> roleIds;
    @Override
    public String toString() {
        return "UserEditReq{nickname='" + nickname + "', mobile='***', email='***', status=" + status
                + ", remark='" + remark + "', roleIds=" + roleIds + "}";
    }
}
