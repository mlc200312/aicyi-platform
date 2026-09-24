package io.github.aicyi.event.workorder;

import io.github.aicyi.common.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单已处理领域事件（跨服务契约：work-order 发布 → message 订阅）。
 *
 * <p>工单进入终态（已解决 / 已驳回 / 已关闭）时发布，触发通知提交人处理结果。
 */
@Getter
@Setter
public class WorkOrderProcessedEvent extends BaseBean {

    /** 工单 ID */
    private Long orderId;

    /** 工单编号 */
    private String orderNo;

    /** 工单标题 */
    private String title;

    /** 提交人 ID */
    private Long submitterId;

    /** 处理结果状态 code */
    private Integer status;

    /** 处理结果状态文案 */
    private String statusText;

    /** 处理意见 */
    private String opinion;

    public WorkOrderProcessedEvent() {
    }

    public WorkOrderProcessedEvent(Long orderId, String orderNo, String title, Long submitterId,
                                   Integer status, String statusText, String opinion) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.title = title;
        this.submitterId = submitterId;
        this.status = status;
        this.statusText = statusText;
        this.opinion = opinion;
    }
}
