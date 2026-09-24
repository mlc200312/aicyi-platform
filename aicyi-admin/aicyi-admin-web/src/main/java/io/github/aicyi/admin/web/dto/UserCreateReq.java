package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 新增用户请求。
 */
@Schema(description = "新增用户请求")
@Getter
@Setter
public class UserCreateReq extends BaseBean implements DtoBean {

    /**
     * 用户名（唯一，创建后不可修改）
     */
    @Schema(description = "用户名（唯一，创建后不可修改）", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 初始密码
     */
    @Schema(description = "初始密码（6-32 位）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "初始密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String password;

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
        return "UserCreateReq{username='" + username + "', password='***', nickname='" + nickname
                + "', mobile='***', email='***', remark='" + remark + "', roleIds=" + roleIds + "}";
    }
}
