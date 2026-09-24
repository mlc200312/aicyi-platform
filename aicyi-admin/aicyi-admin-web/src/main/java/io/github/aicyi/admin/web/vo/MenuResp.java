package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.VoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单响应（脱敏：不返回 deleted/version 等内部字段；含子节点，树形结构）。
 */
@Schema(description = "菜单响应")
@Getter
@Setter
public class MenuResp extends BaseBean implements VoBean {

    @Schema(description = "菜单 ID", example = "1001")
    private Long id;

    @Schema(description = "父级 ID（0 = 顶级）", example = "0")
    private Long parentId;

    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @Schema(description = "类型：1 目录 / 2 菜单 / 3 按钮", example = "2")
    private Integer menuType;

    @Schema(description = "路由地址", example = "/system/user")
    private String path;

    @Schema(description = "图标", example = "user")
    private String icon;

    @Schema(description = "排序序号", example = "1")
    private Integer sort;

    @Schema(description = "是否显示：1 显示 / 0 隐藏", example = "1")
    private Integer visible;

    @Schema(description = "权限标识", example = "system:user:list")
    private String permCode;

    @Schema(description = "接口路径", example = "/api/system/user/list")
    private String apiPath;

    @Schema(description = "内置菜单：1 是 / 0 否", example = "0")
    private Integer builtin;

    @Schema(description = "子节点列表")
    private List<MenuResp> children = new ArrayList<>();
}
