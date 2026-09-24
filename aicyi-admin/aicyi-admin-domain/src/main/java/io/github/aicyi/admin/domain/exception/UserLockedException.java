package io.github.aicyi.admin.domain.exception;

import io.github.aicyi.common.model.CommonResultCode;
import io.github.aicyi.common.model.exception.BusinessException;

/**
 * 账号锁定异常（登录失败次数超限后临时锁定）。
 */
public class UserLockedException extends BusinessException {

    public UserLockedException(long remainingMinutes) {
        super(CommonResultCode.UNAUTHORIZED, "登录失败次数过多，账号已锁定，请 " + remainingMinutes + " 分钟后再试");
    }
}
