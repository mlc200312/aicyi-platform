package io.github.aicyi.admin.domain.type;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.aicyi.common.model.EnumType;
import lombok.Getter;

/**
 * 通用状态枚举（启用 / 禁用）。
 */
public enum StatusType implements EnumType {

    ENABLED(1, "启用"),

    DISABLED(0, "禁用");

    private final int code;

    @Getter
    private final String description;

    StatusType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    @Override
    public Integer getCode() {
        return code;
    }

    public static StatusType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
