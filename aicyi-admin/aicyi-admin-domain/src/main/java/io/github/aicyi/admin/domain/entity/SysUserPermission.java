package io.github.aicyi.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.aicyi.admin.domain.type.PermissionType;
import io.github.aicyi.common.model.BaseEntity;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.infra.mybatisplus.handlers.IEnumTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户单独授权实体（sys_user_permission）：在角色权限基础上追加 / 扣除。
 */
@Getter
@Setter
@TableName(value = "sys_user_permission", autoResultMap = true)
public class SysUserPermission extends BaseEntity {

    /** 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值） */
    @TableId(type = IdType.INPUT)
    private Long id;

    private Long userId;

    private String permCode;

    /** 类型：追加 / 扣除 */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private PermissionType permType;

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
