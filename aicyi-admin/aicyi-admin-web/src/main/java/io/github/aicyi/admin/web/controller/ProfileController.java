package io.github.aicyi.admin.web.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.service.system.UserManageService;
import io.github.aicyi.admin.web.convert.AdminConverter;
import io.github.aicyi.admin.web.dto.ProfileUpdateReq;
import io.github.aicyi.admin.web.vo.UserResp;
import io.github.aicyi.common.model.Result;
import io.github.aicyi.common.util.context.CurrentContextHolder;
import io.github.aicyi.middleware.operatelog.annotation.OperLog;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 个人中心接口。
 */
@Tag(name = "个人中心", description = "当前登录用户查看 / 修改个人信息（需求 4.2.2）")
@RestController
@RequestMapping("/api/system/profile")
public class ProfileController {

    private final UserManageService userManageService;

    public ProfileController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    /**
     * 查看当前登录用户信息
     */
    @Operation(summary = "查看当前用户信息",
            description = "返回当前登录用户的资料（脱敏，不含密码）与绑定角色")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/info")
    public Result<UserResp> info() {
        Long userId = Long.valueOf(CurrentContextHolder.getUserId());
        SysUser user = requireUser(userId);
        List<SysRole> roles = userManageService.listRolesByUser(userId);
        return Result.success(AdminConverter.INSTANCE.toUserRespWithRoles(user, roles));
    }

    /**
     * 修改当前登录用户个人信息（昵称 / 手机号 / 邮箱）
     */
    @Operation(summary = "修改个人信息",
            description = "当前登录用户修改自己的昵称 / 手机号 / 邮箱；未传字段保持原值")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功，返回更新后的用户信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "个人中心", operType = "UPDATE", desc = "更新个人资料")
    @PutMapping("/update")
    public Result<UserResp> update(
            @Valid @RequestBody ProfileUpdateReq req) {
        Long userId = Long.valueOf(CurrentContextHolder.getUserId());
        SysUser user = userManageService.edit(AdminConverter.INSTANCE.toProfileEditBO(userId, req));
        return Result.success(AdminConverter.INSTANCE.toUserResp(user));
    }

    private SysUser requireUser(Long userId) {
        return userManageService.getById(userId);
    }
}
