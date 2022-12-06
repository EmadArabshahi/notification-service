package com.iptiq.notificationservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderRow {
    private Integer orderRowId;
    private Integer itemId;
    private Integer quantity;
    private Double unitaryPrice;
}
