package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 分配角色请求。
 */
@Schema(description = "分配角色请求")
@Getter
@Setter
public class AssignRolesReq extends BaseBean implements DtoBean {

    /**
     * 角色 ID 列表
     */
    @Schema(description = "角色 ID 列表", example = "[\"1\", \"2\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "角色列表不能为空")
    private List<String> roleIds;
}
