package com.iptiq.notificationservice.service;

import com.iptiq.notificationservice.domain.OrderCreated;
import com.iptiq.notificationservice.domain.OrderShipped;
import com.iptiq.notificationservice.dto.OrderCreatedResponse;
import com.iptiq.notificationservice.dto.OrderShippedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;

import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.function.Function;

@Component
@Slf4j
public class OrderProcessor {

    private Function<OrderCreated, AbstractMap.SimpleEntry<Double, Integer>> orderCostAmountProcess = orderCreated -> new AbstractMap.SimpleEntry<>(orderCreated.getOrderRows().stream().mapToDouble(orderRow -> orderRow.getUnitaryPrice() * orderRow.getQuantity()).sum()
            , orderCreated.getOrderRows().stream().mapToInt(orderRow -> orderRow.getQuantity()).sum());

    @Bean
    public Function<KStream<Object, OrderCreated>, KStream<Object, OrderCreatedResponse>> createdOrderProcessor() {
        return orderCreatedKStream -> orderCreatedKStream.map((key, value) -> {
            AbstractMap.SimpleEntry<Double, Integer> pair = orderCostAmountProcess.apply(value);
            return KeyValue.pair(key, OrderCreatedResponse.builder()
                    .orderId(value.getOrderId())
                    .creationDateTime(value.getCreationDateTime())
                    .numberOfItems(pair.getValue())
                    .totalPrice(pair.getKey()).build());
        });
    }

    @Bean
    public Function<KStream<Object, OrderShipped>, KStream<Object, OrderShippedResponse>> shippedOrderProcessor() {
        return orderShippedKStream  -> orderShippedKStream.map((key, value) -> {
            return KeyValue.pair(key, OrderShippedResponse.builder()
                    .orderId(value.getOrderId())
                    .shippingCost(value.getShippingCost())
                    .deliveryCompanyName(value.getDeliveryCompanyName())
                    .shippingDate(value.getShippingDate()).build());
        });
    }
}
