package io.github.aicyi.event.workorder;

/**
 * 工单领域事件契约常量：MQ destination（生产端投递与消费端绑定须严格一致）。
 */
public final class WorkOrderEvents {

    private WorkOrderEvents() {
    }

    /** 工单已创建事件 destination（Spring Cloud Stream 绑定名） */
    public static final String MQ_DEST_WORK_ORDER_CREATED = "work-order-created-events";

    /** 工单已处理（终态）事件 destination（Spring Cloud Stream 绑定名） */
    public static final String MQ_DEST_WORK_ORDER_PROCESSED = "work-order-processed-events";
}
