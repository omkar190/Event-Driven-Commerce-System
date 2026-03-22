package com.order.service.queries;

public class OutboxQueries {

    private OutboxQueries() {}

    public static final String INSERT = """
                        INSERT INTO order_outbox_event
                        (id, aggregate_type, aggregate_id, event_type, payload, status, created_at)
                        VALUES
                        (:id, :aggregateType, :aggregateId, :eventType, :payload, :status, :createdAt)
                        """;

}
