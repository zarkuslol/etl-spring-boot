package com.atlassoftware.etl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    private Long id;
    private String customerName;
    private Boolean active;
    private String product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal total;

    public Order() {}
}
