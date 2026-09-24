package io.github.aicyi.admin.domain.type;

import io.github.aicyi.common.model.EnumType;
import lombok.Getter;

/**
 * 用户单独授权类型：追加 / 扣除。
 */
public enum PermissionType implements EnumType {

    ADD(1, "追加"),

    REMOVE(2, "扣除");

    private final int code;

    @Getter
    private final String description;

    PermissionType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
