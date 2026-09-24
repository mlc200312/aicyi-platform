package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 角色分配权限（菜单 / 按钮）请求。
 */
@Schema(description = "角色分配权限（菜单 / 按钮）请求")
@Getter
@Setter
public class AssignMenusReq extends BaseBean implements DtoBean {

    /**
     * 菜单 ID 列表（含按钮；可为空数组表示清空）
     */
    @Schema(description = "菜单 ID 列表（含按钮；空数组表示清空）", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "菜单列表不能为空")
    private List<Long> menuIds;
}
