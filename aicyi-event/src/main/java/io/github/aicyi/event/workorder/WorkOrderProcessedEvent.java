package io.github.aicyi.event.workorder;

import io.github.aicyi.common.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单已处理（终态）领域事件（work-order 发布 → message 订阅，通知提交人处理结果）。
 */
@Getter
@Setter
public class WorkOrderProcessedEvent extends BaseBean {

    private Long orderId;

    private String orderNo;

    private String title;

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
