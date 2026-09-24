package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.middleware.web.model.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色分页查询请求。
 *
 * <p>分页字段（page/size）继承 {@link PageRequest}（JSR-303 强校验，size 上限 500）。
 */
@Schema(description = "角色分页查询请求")
@Getter
@Setter
public class RoleQueryReq extends PageRequest implements DtoBean {

    @Schema(description = "角色名称（模糊匹配）", example = "运营")
    private String roleName;

    @Schema(description = "角色标识（模糊匹配）", example = "operator")
    private String roleKey;
}
