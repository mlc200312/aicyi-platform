package io.github.aicyi.admin.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.aicyi.common.model.EnumType;
import lombok.Getter;

/**
 * 菜单类型：目录 / 菜单 / 按钮。
 */
public enum MenuType implements EnumType {

    /** 目录（一级） */
    DIRECTORY(1, "目录"),

    /** 菜单（二级） */
    MENU(2, "菜单"),

    /** 按钮（三级，绑定接口权限） */
    BUTTON(3, "按钮");

    private final int code;

    @Getter
    private final String description;

    MenuType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    @Override
    public Integer getCode() {
        return code;
    }
}
