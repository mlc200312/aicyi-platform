package io.github.aicyi.admin.web.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.aicyi.admin.service.system.MenuManageService;
import io.github.aicyi.admin.web.convert.AdminConverter;
import io.github.aicyi.admin.web.dto.MenuSaveReq;
import io.github.aicyi.admin.web.vo.MenuResp;
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

/**
 * 菜单管理接口。
 */
@Tag(name = "菜单管理", description = "菜单 / 按钮树维护（需求 4.4）")
@RestController
@RequestMapping("/api/system/menu")
public class MenuController {

    private final MenuManageService menuManageService;

    public MenuController(MenuManageService menuManageService) {
        this.menuManageService = menuManageService;
    }

    /**
     * 菜单树（目录 → 菜单 → 按钮三级）
     */
    @Operation(summary = "查询菜单树",
            description = "返回 目录 → 菜单 → 按钮 三级菜单树结构，用于左侧导航")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/list")
    public Result<List<MenuResp>> list() {
        List<MenuResp> list = menuManageService.tree().stream().map(AdminConverter.INSTANCE::toMenuResp).toList();
        return Result.success(list);
    }

    /**
     * 全部菜单（拉平，权限分配用）
     */
    @Operation(summary = "查询全部菜单",
            description = "返回拉平后的全部菜单 / 按钮列表，用于权限分配界面")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @GetMapping("/all")
    public Result<List<MenuResp>> all() {
        List<MenuResp> list = menuManageService.listAll().stream().map(AdminConverter.INSTANCE::toMenuResp).toList();
        return Result.success(list);
    }

    /**
     * 新增菜单 / 按钮
     */
    @Operation(summary = "新增菜单 / 按钮", description = "创建目录 / 菜单 / 按钮节点，挂载到指定父节点下")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功，返回菜单信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "409", description = "同级菜单名称或权限标识冲突"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "菜单管理", operType = "CREATE", desc = "新增菜单/按钮")
    @PostMapping("/add")
    public Result<MenuResp> add(
            @Valid @RequestBody MenuSaveReq req) {
        return Result.success(AdminConverter.INSTANCE.toMenuResp(menuManageService.add(AdminConverter.INSTANCE.toEntity(req))));
    }

    /**
     * 编辑菜单
     */
    @Operation(summary = "编辑菜单",
            description = "修改菜单信息；请求体需携带菜单 ID，系统内置菜单可编辑名称等展示信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "编辑成功，返回更新后的菜单信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败 / 菜单 ID 不能为空"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "菜单管理", operType = "UPDATE", desc = "编辑菜单")
    @PutMapping("/edit")
    public Result<MenuResp> edit(
            @Valid @RequestBody MenuSaveReq req) {
        if (req.getId() == null) {
            throw new IllegalArgumentException("菜单 ID 不能为空");
        }
        return Result.success(AdminConverter.INSTANCE.toMenuResp(menuManageService.edit(AdminConverter.INSTANCE.toEntity(req))));
    }

    /**
     * 删除菜单（系统内置菜单禁止删除）
     */
    @Operation(summary = "删除菜单",
            description = "逻辑删除菜单节点及其子节点；系统内置菜单禁止删除")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / 系统内置菜单禁止删除"),
            @ApiResponse(responseCode = "404", description = "菜单不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "菜单管理", operType = "DELETE", desc = "删除菜单")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "菜单 ID", example = "1001", required = true) @RequestParam Long id) {
        menuManageService.delete(id);
        return Result.success();
    }
}
