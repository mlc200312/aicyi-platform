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

    /**
     * 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值）
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 用户名（唯一标识，禁止修改）
     */
    private String username;

    /**
     * 密码（BCrypt 加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private StatusType status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否已修改初始密码
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType passwordModified;

    /**
     * 删除标记
     */
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
