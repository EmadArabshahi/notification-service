package com.iptiq.notificationservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderShipped {
    private Integer orderId;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy hh:mm:ss")
    private Date shippingDate;
    private Integer deliveryCompanyId;
    private String deliveryCompanyName;
    private Double shippingCost;
    private Integer warehousePosition;
}
