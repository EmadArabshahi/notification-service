package com.iptiq.notificationservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderCreated {
    private Integer customerId;
    private String customerName;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd-MM-yyyy hh:mm:ss")
    private Date creationDateTime;
    private Integer orderId;
    private List<OrderRow> orderRows;
}
