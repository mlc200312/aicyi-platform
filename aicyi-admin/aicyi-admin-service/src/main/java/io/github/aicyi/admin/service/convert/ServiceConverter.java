package io.github.aicyi.admin.service.convert;

import io.github.aicyi.admin.domain.bo.RoleCreateBO;
import io.github.aicyi.admin.domain.bo.RoleEditBO;
import io.github.aicyi.admin.domain.bo.UserCreateBO;
import io.github.aicyi.admin.domain.bo.UserEditBO;
import io.github.aicyi.admin.domain.entity.SysMenu;
import io.github.aicyi.admin.domain.entity.SysRole;
import io.github.aicyi.admin.domain.entity.SysUser;
import io.github.aicyi.admin.service.system.MenuManageService.MenuNode;
import io.github.aicyi.common.util.bean.mapstruct.DateTimeTypeConverters;
import io.github.aicyi.common.util.bean.mapstruct.EnumTypeConverters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Service 层对象转换器（MapStruct）：BO/DO 双向与同层拷贝全部经编译期生成实现，
 * 禁止在业务方法中手动 new + set 组装或 BeanUtils 反射拷贝。
 *
 * <p>枚举 / 时间转换复用脚手架 {@link EnumTypeConverters} / {@link DateTimeTypeConverters}。
 *
 * <p>域范围：仅系统 RBAC（用户 / 角色 / 菜单）。消息模板相关转换随绞杀者模式迁至 aicyi-message。
 *
 * <p>用法：{@code ServiceConverter.INSTANCE.toDO(bo)}。
 */
@Mapper(uses = {EnumTypeConverters.class, DateTimeTypeConverters.class})
public interface ServiceConverter {

    ServiceConverter INSTANCE = Mappers.getMapper(ServiceConverter.class);

    // ==================== BO → DO（写库） ====================

    /**
     * 新增用户：BO → DO（密码明文映射，Service 加密后覆盖；状态与框架字段由 Service 赋值）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passwordModified", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SysUser toDO(UserCreateBO bo);

    /**
     * 编辑用户：BO → DO 原地更新（禁止改用户名，密码走独立变更流程）
     */
    @Mapping(target = "passwordModified", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateDO(@MappingTarget SysUser user, UserEditBO bo);

    /**
     * 新增角色：BO → DO（状态与内置标记由 Service 赋值）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "builtin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    SysRole toDO(RoleCreateBO bo);

    /**
     * 编辑角色：BO → DO 原地更新（角色标识不可变更）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleKey", ignore = true)
    @Mapping(target = "builtin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateDO(@MappingTarget SysRole role, RoleEditBO bo);

    /**
     * 菜单编辑：DO → DO 原地更新（内置标记与框架字段不覆盖）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "builtin", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    void updateMenu(@MappingTarget SysMenu target, SysMenu source);

    /**
     * 菜单树节点：DO → BO
     */
    @Mapping(target = "children", ignore = true)
    MenuNode toMenuNode(SysMenu menu);
}
