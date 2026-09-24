package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * 用户单独授权请求（追加 / 扣除权限标识）。
 */
@Schema(description = "用户单独授权请求（追加 / 扣除权限标识）")
@Getter
@Setter
public class AssignUserPermsReq extends BaseBean implements DtoBean {

    /**
     * 追加权限标识集合
     */
    @Schema(description = "追加权限标识集合", example = "[\"system:user:add\"]")
    private Set<String> addCodes;

    /**
     * 扣除权限标识集合
     */
    @Schema(description = "扣除权限标识集合", example = "[\"system:user:delete\"]")
    private Set<String> removeCodes;
}
