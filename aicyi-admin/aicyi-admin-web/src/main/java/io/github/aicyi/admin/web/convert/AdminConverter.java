package io.github.aicyi.admin.web.convert;

import io.github.aicyi.admin.domain.bo.AssignUserPermsBO;
import io.github.aicyi.admin.domain.bo.RoleCreateBO;
import io.github.aicyi.admin.domain.bo.RoleEditBO;
import io.github.aicyi.admin.domain.bo.RoleQueryBO;
import io.github.aicyi.admin.domain.bo.UserCreateBO;
import io.github.aicyi.admin.domain.bo.UserEditBO;
import io.github.aicyi.admin.domain.bo.UserBatchDeleteBO;
import io.github.aicyi.admin.domain.bo.UserExportBO;
import io.github.aicyi.admin.domain.bo.UserImportFailItemBO;
import io.github.aicyi.admin.domain.bo.UserImportResultBO;
import io.github.aicyi.admin.domain.bo.UserQueryBO;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.admin.domain.entity.SysMenu;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.domain.entity.SysUserPermission;
import io.github.aicyi.admin.service.system.MenuManageService.MenuNode;
import io.github.aicyi.admin.web.dto.AssignUserPermsReq;
import io.github.aicyi.admin.web.dto.MenuSaveReq;
import io.github.aicyi.admin.web.dto.ProfileUpdateReq;
import io.github.aicyi.admin.web.dto.RoleCreateReq;
import io.github.aicyi.admin.web.dto.RoleEditReq;
import io.github.aicyi.admin.web.dto.RoleQueryReq;
import io.github.aicyi.admin.web.dto.UserCreateReq;
import io.github.aicyi.admin.web.dto.UserEditReq;
import io.github.aicyi.admin.web.dto.UserBatchDeleteReq;
import io.github.aicyi.admin.web.dto.UserExportReq;
import io.github.aicyi.admin.web.dto.UserQueryReq;
import io.github.aicyi.admin.web.vo.MenuResp;
import io.github.aicyi.admin.web.vo.RoleResp;
import io.github.aicyi.admin.web.vo.UserPermissionResp;
import io.github.aicyi.admin.web.vo.UserExportResp;
import io.github.aicyi.admin.web.vo.UserImportFailItemResp;
import io.github.aicyi.admin.web.vo.UserImportResp;
import io.github.aicyi.admin.web.vo.UserResp;
import io.github.aicyi.common.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.common.util.bean.mapstruct.EnumTypeConverters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 管理后台统一对象转换器（MapStruct）。
 *
 * <p>DTO/VO ↔ BO/DO 全部经 MapStruct 编译期生成实现，替代手动 set；
 * 枚举 / 时间转换复用脚手架 {@link EnumTypeConverters} / {@link DateTimeTypeConverters}。
 *
 * <p>域范围：仅系统 RBAC（用户 / 角色 / 菜单 / 权限 / 个人中心）。auth、站内信、消息模板、
 * 审计日志已随绞杀者模式拆分下沉至 aicyi-auth / aicyi-message / aicyi-log 各自微服务。
 */
@Mapper(uses = {EnumTypeConverters.class, DateTimeTypeConverters.class})
public interface AdminConverter {

    AdminConverter INSTANCE = Mappers.getMapper(AdminConverter.class);

    // ==================== DTO → BO ====================

    UserCreateBO toUserCreateBO(UserCreateReq req);

    UserEditBO toUserEditBO(Long userId, UserEditReq req);

    /**
     * 个人中心更新：仅映射资料字段，status/remark/roleIds 保持 null（不修改）
     */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "remark", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    UserEditBO toProfileEditBO(Long userId, ProfileUpdateReq req);

    UserQueryBO toUserQueryBO(UserQueryReq dto);

    UserBatchDeleteBO toUserBatchDeleteBO(UserBatchDeleteReq req);

    UserImportResp toUserImportResp(UserImportResultBO bo);

    UserImportFailItemResp toUserImportFailItemResp(UserImportFailItemBO bo);

    /**
     * 导出查询条件 → 查询 BO（不含分页语义，page/size 由 PageParam 兜底，导出忽略）
     */
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "size", ignore = true)
    UserQueryBO toUserQueryBO(UserExportReq dto);

    // ==================== DO → BO（导出） ====================

    /**
     * 用户导出：DO → BO（列表映射由 MapStruct 按单元素方法自动生成）
     */
    List<UserExportBO> toUserExportBOList(List<SysUser> users);

    // ==================== BO → VO（导出） ====================

    /**
     * 用户导出：BO → Excel 行模型（状态枚举转中文标签，时间转字符串）
     */
    List<UserExportResp> toUserExportRespList(List<UserExportBO> bos);

    /**
     * 状态枚举 → 导出中文标签
     */
    default String toStatusLabel(StatusType status) {
        return status == null ? "" : status.getDescription();
    }

    RoleCreateBO toRoleCreateBO(RoleCreateReq req);

    RoleEditBO toRoleEditBO(Long roleId, RoleEditReq req);

    RoleQueryBO toRoleQueryBO(RoleQueryReq dto);

    AssignUserPermsBO toAssignUserPermsBO(Long userId, AssignUserPermsReq req);

    // ==================== DTO → DO ====================

    @Mapping(target = "builtin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SysMenu toEntity(MenuSaveReq req);

    // ==================== DO → VO ====================

    @Mapping(target = "roles", ignore = true)
    UserResp toUserResp(SysUser user);

    UserResp toUserRespWithRoles(SysUser user, List<SysRole> roles);

    RoleResp toRoleResp(SysRole role);

    @Mapping(target = "children", ignore = true)
    MenuResp toMenuResp(SysMenu menu);

    MenuResp toMenuResp(MenuNode node);

    UserPermissionResp toUserPermissionResp(SysUserPermission perm);
}
