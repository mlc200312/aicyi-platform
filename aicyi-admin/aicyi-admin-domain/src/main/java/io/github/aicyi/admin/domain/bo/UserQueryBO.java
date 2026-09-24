package io.github.aicyi.admin.domain.bo;

import io.github.aicyi.admin.domain.type.StatusType;
import io.github.aicyi.common.model.BoBean;
import io.github.aicyi.common.model.PageParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户分页查询参数（用户名模糊 / 状态 / 创建时间范围）。
 *
 * <p>分页字段（page/size）继承 {@link PageParam}，兑底与上限由
 * {@code getPageOrDefault()/getSizeOrDefault()} 提供。
 */
@Getter
@Setter
@NoArgsConstructor
public class UserQueryBO extends PageParam implements BoBean {

    /**
     * 用户名（模糊匹配，可选）
     */
    private String username;

    /**
     * 状态（可选）
     */
    private StatusType status;

    /**
     * 创建时间起始（可选）
     */
    private LocalDateTime beginTime;

    /**
     * 创建时间结束（可选）
     */
    private LocalDateTime endTime;
}
