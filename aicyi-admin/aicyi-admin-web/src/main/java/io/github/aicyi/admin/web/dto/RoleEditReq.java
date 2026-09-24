package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 编辑角色请求。
 */
@Schema(description = "编辑角色请求")
@Getter
@Setter
public class RoleEditReq extends BaseBean implements DtoBean {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", example = "运营")
    private String roleName;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "负责日常运营管理")
    private String description;

    /**
     * 状态：1 启用 / 0 禁用
     */
    @Schema(description = "状态：1 启用 / 0 禁用", example = "1")
    private Integer status;
}
