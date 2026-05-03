package com.order.service.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Product {

    private UUID id;
    private String name;
    private int availableQty;
    private BigDecimal sellingPrice;
    private LocalDateTime createdAt;

    public Product() {}

}