package io.github.aicyi.admin.web.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.aicyi.admin.service.system.PermissionService;
import io.github.aicyi.admin.web.convert.AdminConverter;
import io.github.aicyi.admin.web.dto.AssignUserPermsReq;
import io.github.aicyi.admin.web.vo.PermissionPreviewResp;
import io.github.aicyi.admin.web.vo.UserPermissionResp;
import io.github.aicyi.common.model.Result;
import io.github.aicyi.middleware.operatelog.annotation.OperLog;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 权限管理接口。
 */
@Tag(name = "权限管理", description = "用户有效权限预览 / 单独授权 / 权限重置（需求 4.5）")
@RestController
@RequestMapping("/api/system/perm")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * 权限预览：用户有效权限（角色权限 + 用户单独授权后的最终结果）与单独授权明细
     */
    @Operation(summary = "权限预览",
            description = "返回指定用户的有效权限集合（角色权限与单独授权合并后的最终结果）" +
                    "及用户单独授权明细")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/list")
    public Result<PermissionPreviewResp> list(
            @Parameter(description = "用户 ID", example = "1", required = true) @RequestParam Long userId) {
        Set<String> effective = permissionService.getEffectivePermissions(userId);
        List<UserPermissionResp> userPermissions = permissionService.getUserPermissions(userId).stream()
                .map(AdminConverter.INSTANCE::toUserPermissionResp).toList();
        return Result.success(PermissionPreviewResp.of(userId, effective, userPermissions));
    }

    /**
     * 用户单独授权：追加 / 扣除（整体覆盖），权限实时生效
     */
    @Operation(summary = "用户单独授权",
            description = "整体覆盖用户的单独授权：addCodes 追加、removeCodes 扣除；权限实时生效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "授权成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "权限管理", operType = "PERM", desc = "分配用户权限")
    @PutMapping("/assign")
    public Result<Void> assign(
            @Parameter(description = "用户 ID", example = "1", required = true) @RequestParam Long userId,
            @Valid @RequestBody AssignUserPermsReq req) {
        permissionService.assignUserPermissions(
                AdminConverter.INSTANCE.toAssignUserPermsBO(userId, req));
        return Result.success();
    }

    /**
     * 权限重置：清空用户单独授权，恢复角色默认权限
     */
    @Operation(summary = "权限重置",
            description = "清空指定用户的单独授权，恢复为角色默认权限")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "权限管理", operType = "PERM", desc = "重置用户权限")
    @PostMapping("/reset")
    public Result<Void> reset(
            @Parameter(description = "用户 ID", example = "1", required = true) @RequestParam Long userId) {
        permissionService.resetUserPermissions(userId);
        return Result.success();
    }
}
