package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 无权限访问异常（接口权限拦截，40300）。
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(CommonResultCode.FORBIDDEN, message);
    }
}
