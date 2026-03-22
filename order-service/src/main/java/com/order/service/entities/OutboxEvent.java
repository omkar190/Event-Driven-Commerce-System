package com.order.service.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("order_outbox_event")
@Getter
@Setter
public class OutboxEvent {

    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private String status;
    private LocalDateTime createdAt;

}
