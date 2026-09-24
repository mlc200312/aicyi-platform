package io.github.aicyi.admin.web.dto;

import io.github.aicyi.common.model.DtoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增 / 编辑菜单请求。
 */
@Schema(description = "新增 / 编辑菜单请求")
@Getter
@Setter
public class MenuSaveReq extends BaseBean implements DtoBean {

    /**
     * 菜单 ID（编辑时必填）
     */
    @Schema(description = "菜单 ID（编辑时必填）", example = "1001")
    private Long id;

    /**
     * 父级 ID（顶级为 0）
     */
    @Schema(description = "父级 ID（顶级为 0）", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父级菜单不能为空")
    private Long parentId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 类型：1 目录 / 2 菜单 / 3 按钮
     */
    @Schema(description = "类型：1 目录 / 2 菜单 / 3 按钮", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system/user")
    private String path;

    /**
     * 图标
     */
    @Schema(description = "图标", example = "user")
    private String icon;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号", example = "1")
    private Integer sort;

    /**
     * 是否显示：1 显示 / 0 隐藏
     */
    @Schema(description = "是否显示：1 显示 / 0 隐藏", example = "1")
    private Integer visible;

    /**
     * 权限标识（如 system:user:list）
     */
    @Schema(description = "权限标识（如 system:user:list）", example = "system:user:list")
    private String permCode;

    /**
     * 接口路径（后端权限拦截匹配）
     */
    @Schema(description = "接口路径（后端权限拦截匹配）", example = "/api/system/user/list")
    private String apiPath;
}
