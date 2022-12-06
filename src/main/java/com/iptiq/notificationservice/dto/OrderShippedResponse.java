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
public class OrderShippedResponse {
    private Integer orderId;
    private Date shippingDate;
    private String deliveryCompanyName;
    private Double shippingCost;
}
