package io.github.aicyi.admin.web.controller;

import io.github.aicyi.admin.web.vo.UserExportResp;
import io.github.aicyi.admin.web.vo.UserImportResp;
import io.github.aicyi.admin.web.vo.UserResp;
import io.github.aicyi.common.util.media.ExcelUtils;
import io.github.aicyi.admin.domain.bo.UserImportRow;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.service.system.UserManageService;
import io.github.aicyi.admin.web.convert.AdminConverter;
import io.github.aicyi.admin.web.dto.AssignRolesReq;
import io.github.aicyi.admin.web.dto.ResetPasswordReq;
import io.github.aicyi.admin.web.dto.UserBatchDeleteReq;
import io.github.aicyi.admin.web.dto.UserCreateReq;
import io.github.aicyi.admin.web.dto.UserEditReq;
import io.github.aicyi.admin.web.dto.UserExportReq;
import io.github.aicyi.admin.web.dto.UserQueryReq;
import io.github.aicyi.admin.web.vo.RoleResp;
import io.github.aicyi.common.model.Result;
import io.github.aicyi.middleware.operatelog.annotation.OperLog;
import io.github.aicyi.middleware.web.model.PageResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户管理接口（需求 4.2 用户管理模块）。
 */
@Tag(name = "用户管理", description = "系统用户增删改查 / 启停 / 密码重置 / 角色分配（需求 4.2）")
@RestController
@RequestMapping("/api/system/user")
public class UserController {

    /**
     * 导出 sheet 名称
     */
    private static final String EXPORT_SHEET_NAME = "用户列表";

    /**
     * 导出文件名前缀
     */
    private static final String EXPORT_FILE_PREFIX = "用户列表_";

    /**
     * 导出文件名时间格式（yyyyMMddHHmmss）
     */
    private static final DateTimeFormatter EXPORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 导入模板 sheet 名称
     */
    private static final String IMPORT_SHEET_NAME = "用户导入模板";

    /**
     * 导入文件大小上限（5MB）
     */
    private static final long IMPORT_MAX_BYTES = 5L * 1024 * 1024;

    /**
     * 导入文件扩展名（仅支持 xlsx）
     */
    private static final String IMPORT_FILE_SUFFIX = ".xlsx";

    private final UserManageService userManageService;

    public UserController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    /**
     * 用户分页查询
     */
    @Operation(summary = "分页查询用户",
            description = "按用户名模糊、状态、创建时间区间过滤分页查询用户列表")
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
    public Result<PageResponse<UserResp>> list(@Valid UserQueryReq dto) {
        IPage<SysUser> page = userManageService.page(AdminConverter.INSTANCE.toUserQueryBO(dto));
        List<UserResp> list = page.getRecords().stream().map(AdminConverter.INSTANCE::toUserResp).toList();
        return Result.success(PageResponse.build(list, dto.getPage(), dto.getSize(), page.getTotal()));
    }

    /**
     * 新增用户
     */
    @Operation(summary = "新增用户",
            description = "创建用户并绑定角色；初始密码登录后触发修改密码提醒")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功，返回用户信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "409", description = "用户名已存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "CREATE", desc = "新增用户")
    @PostMapping("/add")
    public Result<UserResp> add(
            @Valid @RequestBody UserCreateReq req) {
        SysUser user = userManageService.add(AdminConverter.INSTANCE.toUserCreateBO(req));
        return Result.success(AdminConverter.INSTANCE.toUserResp(user));
    }

    /**
     * 编辑用户
     */
    @Operation(summary = "编辑用户",
            description = "修改用户资料与角色绑定；用户名创建后不可修改")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "编辑成功，返回更新后的用户信息"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "UPDATE", desc = "编辑用户")
    @PutMapping("/edit/{id}")
    public Result<UserResp> edit(
            @Parameter(description = "用户 ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody UserEditReq req) {
        SysUser user = userManageService.edit(AdminConverter.INSTANCE.toUserEditBO(id, req));
        return Result.success(AdminConverter.INSTANCE.toUserResp(user));
    }

    /**
     * 删除用户（admin 禁止删除）
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户；内置 admin 账号禁止删除")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / admin 账号禁止删除"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "DELETE", desc = "删除用户")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "用户 ID", example = "1", required = true) @RequestParam Long id) {
        userManageService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除用户（admin 账号与不存在/已删除用户自动跳过）
     */
    @Operation(summary = "批量删除用户",
            description = "按 ID 集合批量逻辑删除用户；admin 账号与不存在/已删除用户自动跳过，返回实际删除数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功，返回实际删除数量"),
            @ApiResponse(responseCode = "400", description = "参数校验失败（ID 集合为空或超上限）"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "DELETE", desc = "批量删除用户", saveParam = false)
    @PostMapping("/batch-delete")
    public Result<Long> batchDelete(@Valid @RequestBody UserBatchDeleteReq req) {
        return Result.success(userManageService.batchDelete(AdminConverter.INSTANCE.toUserBatchDeleteBO(req)));
    }

    /**
     * 启用 / 禁用用户（admin 禁止禁用）
     */
    @Operation(summary = "启用 / 禁用用户", description = "切换用户状态；内置 admin 账号禁止禁用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "状态切换成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限 / admin 账号禁止禁用"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "STATUS", desc = "启用/禁用用户")
    @PutMapping("/status/{id}")
    public Result<Void> changeStatus(
            @Parameter(description = "用户 ID", example = "1", required = true) @PathVariable Long id,
            @Parameter(description = "目标状态：1 启用 / 0 禁用", example = "1", required = true) @RequestParam Integer status) {
        userManageService.changeStatus(id, StatusType.fromCode(status));
        return Result.success();
    }

    /**
     * 管理员重置用户密码（重置后触发初始密码修改提醒）
     */
    @Operation(summary = "重置用户密码",
            description = "管理员重置指定用户密码；重置后该用户下次登录触发初始密码修改提醒")
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
    @OperLog(module = "用户管理", operType = "PASSWORD", desc = "重置用户密码", saveParam = false)
    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(
            @Parameter(description = "用户 ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody ResetPasswordReq req) {
        userManageService.resetPassword(id, req.getNewPassword());
        return Result.success();
    }

    /**
     * 分配用户角色（多角色权限叠加）
     */
    @Operation(summary = "分配用户角色",
            description = "整体覆盖用户绑定的角色列表；多角色权限叠加生效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "分配成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "ROLE", desc = "分配用户角色")
    @PutMapping("/assign-role/{id}")
    public Result<Void> assignRoles(
            @Parameter(description = "用户 ID", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody AssignRolesReq req) {
        userManageService.assignRoles(id, req.getRoleIds());
        return Result.success();
    }

    /**
     * 查询用户绑定的角色
     */
    @Operation(summary = "查询用户绑定的角色", description = "返回指定用户当前绑定的全部角色")
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
    @GetMapping("/roles/{id}")
    public Result<List<RoleResp>> listRolesByUser(
            @Parameter(description = "用户 ID", example = "1", required = true) @PathVariable Long id) {
        List<RoleResp> list = userManageService.listRolesByUser(id).stream()
                .map(AdminConverter.INSTANCE::toRoleResp).toList();
        return Result.success(list);
    }

    /**
     * 按当前查询条件导出用户列表 Excel（.xlsx），不分页
     */
    @Operation(summary = "导出用户",
            description = "按查询条件（用户名模糊 / 状态 / 时间范围）导出用户列表 Excel 文件，返回 .xlsx 文件流")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "导出成功，返回 Excel 文件流"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "EXPORT", desc = "导出用户列表", saveParam = false, saveResult = false)
    @PostMapping("/export")
    public void export(@Valid @RequestBody UserExportReq req, HttpServletResponse response) throws IOException {
        List<UserExportResp> rows = AdminConverter.INSTANCE.toUserExportRespList(
                AdminConverter.INSTANCE.toUserExportBOList(userManageService.listForExport(
                        AdminConverter.INSTANCE.toUserQueryBO(req))));
        byte[] bytes = ExcelUtils.exportToBytes(EXPORT_SHEET_NAME, rows, UserExportResp.class);

        String filename = URLEncoder.encode(EXPORT_FILE_PREFIX
                + LocalDateTime.now().format(EXPORT_TIME_FORMATTER) + ".xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // RFC 5987：filename* 携带 URL 编码的中文文件名，兼容现代浏览器
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    /**
     * 下载用户导入模板（仅表头：用户名 / 昵称 / 手机号 / 邮箱 / 初始密码）
     */
    @Operation(summary = "下载用户导入模板",
            description = "返回用户批量导入 Excel 模板（.xlsx），仅含表头行")
    @GetMapping("/import-template")
    public void importTemplate(HttpServletResponse response) throws IOException {
        byte[] bytes = ExcelUtils.exportToBytes(IMPORT_SHEET_NAME, Collections.emptyList(), UserImportRow.class);

        String filename = URLEncoder.encode(IMPORT_SHEET_NAME + IMPORT_FILE_SUFFIX, StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // RFC 5987：filename* 携带 URL 编码的中文文件名，兼容现代浏览器
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    /**
     * 批量导入用户（Excel 上传，逐行校验，返回逐行成败明细）
     */
    @Operation(summary = "批量导入用户",
            description = "上传用户导入 Excel（.xlsx，≤5MB），逐行校验并创建用户；"
                    + "失败行不影响成功行，返回总行数、成功数、失败数与逐行失败原因")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "导入完成（部分失败也在 200 内，通过响应体区分）"),
            @ApiResponse(responseCode = "400", description = "文件为空 / 非 .xlsx / 超过大小上限"),
            @ApiResponse(responseCode = "401", description = "未登录或令牌失效"),
            @ApiResponse(responseCode = "403", description = "无操作权限"),
            @ApiResponse(responseCode = "500", description = "系统异常")
    })
    @Parameters({@Parameter(name = "Authorization", description = "令牌", required = true,
            in = ParameterIn.HEADER, schema = @Schema(type = "string"))})
    @OperLog(module = "用户管理", operType = "IMPORT", desc = "批量导入用户", saveParam = false, saveResult = false)
    @PostMapping("/import")
    public Result<UserImportResp> importUsers(
            @Parameter(description = "用户导入 Excel 文件（.xlsx，≤5MB）", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择导入文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(IMPORT_FILE_SUFFIX)) {
            throw new IllegalArgumentException("仅支持 " + IMPORT_FILE_SUFFIX + " 格式文件");
        }
        if (file.getSize() > IMPORT_MAX_BYTES) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }
        return Result.success(AdminConverter.INSTANCE.toUserImportResp(userManageService.importUsers(file.getBytes())));
    }
}
