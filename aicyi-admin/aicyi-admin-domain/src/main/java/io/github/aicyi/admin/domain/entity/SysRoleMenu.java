package io.github.aicyi.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.aicyi.common.model.BaseEntity;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.infra.mybatisplus.handlers.IEnumTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 角色-菜单权限关联实体（sys_role_menu）。
 */
@Getter
@Setter
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    /** 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值） */
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long roleId;

    private Long menuId;

    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType deleted;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @Version
    private Integer version;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
