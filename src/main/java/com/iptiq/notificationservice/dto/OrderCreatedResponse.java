package com.iptiq.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreatedResponse {
    private Integer orderId;
    private Date creationDateTime;
    private Integer numberOfItems;
    private Double totalPrice;
}
