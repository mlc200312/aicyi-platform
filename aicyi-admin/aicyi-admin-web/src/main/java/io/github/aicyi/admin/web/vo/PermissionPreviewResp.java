package io.github.aicyi.admin.web.vo;

import io.github.aicyi.common.model.VoBean;
import io.github.aicyi.common.model.BaseBean;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * 权限预览响应（用户有效权限 + 单独授权明细），替代 Map 承载出参。
 */
@Schema(description = "权限预览响应")
@Getter
@Setter
public class PermissionPreviewResp extends BaseBean implements VoBean {

    @Schema(description = "用户 ID", example = "1")
    private String userId;

    @Schema(description = "有效权限标识集合（角色权限与单独授权合并后）")
    private Set<String> permissions;

    @Schema(description = "用户单独授权明细（追加 / 扣除）")
    private List<UserPermissionResp> userPermissions;

    public static PermissionPreviewResp of(Long userId, Set<String> permissions, List<UserPermissionResp> userPermissions) {
        PermissionPreviewResp vo = new PermissionPreviewResp();
        vo.setUserId(String.valueOf(userId));
        vo.setPermissions(permissions);
        vo.setUserPermissions(userPermissions);
        return vo;
    }
}
