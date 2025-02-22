package com.example.userservice.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ResponseOrder {
    private String productId;
    private Integer qty; // quantity
    private Integer unitPrice;
    private Integer totalPrice;
    private Date createdAt;
    private String orderId;
}
