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
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.service.system.RoleManageService;
import io.github.aicyi.admin.web.convert.AdminConverter;
import io.github.aicyi.admin.web.dto.AssignMenusReq;
import io.github.aicyi.admin.web.dto.RoleCreateReq;
import io.github.aicyi.admin.web.dto.RoleEditReq;
import io.github.aicyi.admin.web.dto.RoleQueryReq;
import io.github.aicyi.admin.web.vo.RoleResp;
import io.github.aicyi.common.model.Result;
import io.github.aicyi.middleware.operatelog.annotation.OperLog;
import io.github.aicyi.middleware.web.model.PageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * 角色管理接口（需求 4.3 角色管理模块）。
 */
@Tag(name = "角色管理", description = "角色增删改查 / 启停 / 权限分配（需求 4.3）")
@RestController
@RequestMapping("/api/system/role")
public class RoleController {

    private final RoleManageService roleManageService;

    public RoleController(RoleManageService roleManageService) {
        this.roleManageService = roleManageService;
    }

    /**
     * 角色分页查询
     */
    @Operation(summary = "分页查询角色",
            description = "按角色名称、角色标识模糊过滤分页查询角色列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/list")
    public Result<PageResponse<RoleResp>> list(@Valid RoleQueryReq dto) {
        IPage<SysRole> page = roleManageService.page(AdminConverter.INSTANCE.toRoleQueryBO(dto));
        List<RoleResp> list = page.getRecords().stream().map(AdminConverter.INSTANCE::toRoleResp).toList();
        return Result.success(PageResponse.build(list, dto.getPage(), dto.getSize(), page.getTotal()));
    }

    /**
     * 全部启用角色（用户创建 / 分配下拉）
     */
    @Operation(summary = "查询全部启用角色",
            description = "返回全部启用状态的角色，用于用户创建 / 角色分配下拉选项")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/all")
    public Result<List<RoleResp>> listAll() {
        List<RoleResp> list = roleManageService.listAll().stream().map(AdminConverter.INSTANCE::toRoleResp).toList();
        return Result.success(list);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色", description = "创建角色，角色标识全局唯一")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功，返回角色信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "409", description = "角色标识已存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "角色管理", operType = "CREATE", desc = "新增角色")
    @PostMapping("/add")
    public Result<RoleResp> add(
            @Valid @RequestBody RoleCreateReq req) {
        return Result.success(AdminConverter.INSTANCE.toRoleResp(
                roleManageService.add(AdminConverter.INSTANCE.toRoleCreateBO(req))));
    }

    /**
     * 编辑角色（超级管理员角色禁止编辑）
     */
    @Operation(summary = "编辑角色", description = "修改角色名称、描述与状态；内置超级管理员角色禁止编辑")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "编辑成功，返回更新后的角色信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / 超级管理员角色禁止编辑"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "角色管理", operType = "UPDATE", desc = "编辑角色")
    @PutMapping("/edit/{id}")
    public Result<RoleResp> edit(
            @Parameter(description = "角色 ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody RoleEditReq req) {
        return Result.success(AdminConverter.INSTANCE.toRoleResp(
                roleManageService.edit(AdminConverter.INSTANCE.toRoleEditBO(id, req))));
    }

    /**
     * 删除角色（超级管理员角色 / 已绑定用户的角色禁止删除）
     */
    @Operation(summary = "删除角色",
            description = "逻辑删除角色；超级管理员角色或已绑定用户的角色禁止删除")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / 角色禁止删除"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "409", description = "角色已绑定用户，无法删除"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "角色管理", operType = "DELETE", desc = "删除角色")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "角色 ID", example = "1", required = true) @RequestParam Long id) {
        roleManageService.delete(id);
        return Result.success();
    }

    /**
     * 启用 / 禁用角色（禁用后角色下用户权限失效）
     */
    @Operation(summary = "启用 / 禁用角色",
            description = "切换角色状态；禁用后该角色下所有用户的权限立即失效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "状态切换成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "角色管理", operType = "STATUS", desc = "启用/禁用角色")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(
            @Parameter(description = "角色 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态：1 启用 / 0 禁用", example = "1", required = true) @RequestParam Integer status) {
        roleManageService.changeStatus(id, StatusType.fromCode(status));
        return Result.success();
    }

    /**
     * 角色分配权限（菜单 / 按钮 / 接口权限）
     */
    @Operation(summary = "角色分配权限",
            description = "整体覆盖角色的菜单 / 按钮 / 接口权限；权限变更实时生效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "分配成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / 超级管理员角色禁止修改"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "角色管理", operType = "PERM", desc = "分配角色权限")
    @PutMapping("/assign-perm/{id}")
    public Result<Void> assignMenus(
            @Parameter(description = "角色 ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody AssignMenusReq req) {
        roleManageService.assignMenus(id, req.getMenuIds().stream().map(Long::valueOf).toList());
        return Result.success();
    }

    /**
     * 角色已分配菜单 ID 集合（权限分配回显）
     */
    @Operation(summary = "查询角色已分配菜单 ID",
            description = "返回角色已分配的菜单 / 按钮 ID 集合，用于权限分配界面回显")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "角色不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/menu-ids/{id}")
    public Result<Set<Long>> menuIds(
            @Parameter(description = "角色 ID", example = "1", required = true) @PathVariable Long id) {
        return Result.success(roleManageService.menuIds(id));
    }
}
