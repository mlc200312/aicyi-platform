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
 * 系统用户实体（sys_user）。
 */
@Getter
@Setter
@TableName(value = "sys_user", autoResultMap = true)
public class SysUser extends BaseEntity {

    /** 主键（数据库自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一标识，禁止修改） */
    private String username;

    /** 密码（BCrypt 加密存储） */
    private String password;

    private String nickname;

    private String mobile;

    private String email;

    @TableField(typeHandler = IEnumTypeHandler.class)
    private StatusType status;

    private String remark;

    /** 是否已修改初始密码 */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType passwordModified;

    private BooleanType deleted;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @Version
    private Integer version;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
