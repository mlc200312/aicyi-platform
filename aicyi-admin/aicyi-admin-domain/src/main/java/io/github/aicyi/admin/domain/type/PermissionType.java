package io.github.aicyi.admin.domain.type;

import io.github.aicyi.common.model.EnumType;
import lombok.Getter;

/**
 * 用户单独授权类型（实现脚手架 {@link EnumType}，由实体字段 IEnumTypeHandler 按 code 映射 TINYINT）。
 */
public enum PermissionType implements EnumType {

    /**
     * 追加权限
     */
    ADD(1, "追加"),

    /**
     * 扣除权限
     */
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
