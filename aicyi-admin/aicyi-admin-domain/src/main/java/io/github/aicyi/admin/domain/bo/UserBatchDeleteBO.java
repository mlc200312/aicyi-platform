package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.common.model.BaseBean;
import io.github.aicyi.common.model.BoBean;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户批量删除参数。
 */
@Getter
@Setter
public class UserBatchDeleteBO extends BaseBean implements BoBean {

    /** 待删除用户 ID 集合 */
    private List<Long> ids;
}
