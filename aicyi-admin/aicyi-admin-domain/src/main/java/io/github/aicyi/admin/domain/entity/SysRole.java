package io.github.aicyi.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BaseEntity;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.infra.mybatisplus.handlers.IEnumTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统角色实体（sys_role）。
 */
@Getter
@Setter
@TableName(value = "sys_role", autoResultMap = true)
public class SysRole extends BaseEntity {

    /** 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值） */
    @TableId(type = IdType.INPUT)
    private Long id;

    private String roleName;

    /** 角色标识（唯一） */
    private String roleKey;

    private String description;

    @TableField(typeHandler = IEnumTypeHandler.class)
    private StatusType status;

    /** 内置角色（超级管理员） */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType builtin;

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
