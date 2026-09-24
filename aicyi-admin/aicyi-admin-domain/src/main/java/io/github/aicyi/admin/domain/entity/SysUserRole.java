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
 * 用户-角色关联实体（sys_user_role）。
 */
@Getter
@Setter
@TableName(value = "sys_user_role", autoResultMap = true)
public class SysUserRole extends BaseEntity {

    /**
     * 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值）
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 删除标记
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType deleted;

    /**
     * 乐观锁版本
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
