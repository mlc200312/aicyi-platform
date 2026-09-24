package io.github.aicyi.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.github.aicyi.admin.domain.type.MenuType;
import io.github.aicyi.admin.domain.type.VisibleType;
import io.github.aicyi.common.model.BaseEntity;
import io.github.aicyi.common.model.type.BooleanType;
import io.github.aicyi.infra.mybatisplus.handlers.IEnumTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统菜单实体（sys_menu）：目录 / 菜单 / 按钮三级，含权限标识与接口路径。
 */
@Getter
@Setter
@TableName(value = "sys_menu", autoResultMap = true)
public class SysMenu extends BaseEntity {

    /**
     * 主键（雪花算法生成，应用层 IdUtils.generateId() 赋值）
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 父级 ID（0 = 顶级）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 类型：目录 / 菜单 / 按钮
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private MenuType menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序序号
     */
    private Integer sort;

    /**
     * 是否显示
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private VisibleType visible;

    /**
     * 权限标识（如 system:user:list）
     */
    private String permCode;

    /**
     * 接口路径（用于后端权限拦截匹配）
     */
    private String apiPath;

    /**
     * 内置菜单（禁止删除）
     */
    @TableField(typeHandler = IEnumTypeHandler.class)
    private BooleanType builtin;

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
