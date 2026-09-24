package io.github.aicyi.event.workorder;

import io.github.aicyi.common.model.BaseBean;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单已创建领域事件（跨服务契约：work-order 发布 → message 订阅）。
 *
 * <p>RabbitMQ 载荷，触发「提交成功」与「新工单待处理」通知；消费端按 orderId + 事件类型幂等。
 */
@Getter
@Setter
public class WorkOrderCreatedEvent extends BaseBean {

    /**
     * 工单 ID
     */
    private Long orderId;

    /**
     * 工单编号
     */
    private String orderNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 提交人 ID
     */
    private Long submitterId;

    /**
     * 提交人姓名
     */
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
