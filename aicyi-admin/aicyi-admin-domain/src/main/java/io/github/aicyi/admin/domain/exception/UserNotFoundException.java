package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 用户不存在异常。
 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(CommonResultCode.PARAM_ERROR, "用户不存在: " + userId);
    }
}
