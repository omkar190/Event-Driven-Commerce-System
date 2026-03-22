package com.order.service.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Table("orders")
public class Order {

    @Id
    private String id;

    @NotBlank
    private String userId;

    private String status;

    @NotNull
    @Positive
    private Double amount;

    private LocalDateTime createdAt;
}
