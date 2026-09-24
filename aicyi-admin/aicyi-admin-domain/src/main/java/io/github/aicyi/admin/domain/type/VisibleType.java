package io.github.aicyi.admin.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.aicyi.common.model.EnumType;
import lombok.Getter;

/**
 * 菜单显示状态枚举（显示 / 隐藏）。
 */
public enum VisibleType implements EnumType {

    /**
     * 显示
     */
    SHOW(1, "显示"),

    /**
     * 隐藏
     */
    HIDE(0, "隐藏");

    private final int code;

    @Getter
    private final String description;

    VisibleType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    @Override
    public Integer getCode() {
        return code;
    }
}
