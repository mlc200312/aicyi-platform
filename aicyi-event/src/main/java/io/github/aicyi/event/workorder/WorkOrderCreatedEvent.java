package io.github.aicyi.event.workorder;

import io.github.aicyi.common.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单已创建领域事件（work-order 发布 → message 订阅，消费端按 orderId+事件类型幂等）。
 */
@Getter
@Setter
public class WorkOrderCreatedEvent extends BaseBean {

    private Long orderId;

    private String orderNo;

    private String title;

    private Long submitterId;

    private String submitterName;

    public WorkOrderCreatedEvent() {
    }

    public WorkOrderCreatedEvent(Long orderId, String orderNo, String title,
                                 Long submitterId, String submitterName) {
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.title = title;
        this.submitterId = submitterId;
        this.submitterName = submitterName;
    }
}
