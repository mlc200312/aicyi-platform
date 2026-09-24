package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增角色请求。
 */
@Schema(description = "新增角色请求")
@Getter
@Setter
public class RoleCreateReq extends BaseBean implements DtoBean {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "运营", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 角色标识（唯一）
     */
    @Schema(description = "角色标识（唯一）", example = "operator", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "负责日常运营管理")
    private String description;
}
