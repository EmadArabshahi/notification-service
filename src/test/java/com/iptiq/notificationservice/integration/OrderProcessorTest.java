package com.iptiq.notificationservice.integration;

import com.iptiq.notificationservice.domain.OrderCreated;
import com.iptiq.notificationservice.domain.OrderRow;
import com.iptiq.notificationservice.domain.OrderShipped;
import com.iptiq.notificationservice.dto.OrderCreatedResponse;
import com.iptiq.notificationservice.dto.OrderShippedResponse;
import com.iptiq.notificationservice.service.OrderProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EmbeddedKafka(topics = {"orders", "order-message", "shipped-orders", "shipped-message"},partitions = 1,ports = 9092,
        bootstrapServersProperty = "localhost")
public class OrderProcessorTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private OrderProcessor orderProcessor;

    private KafkaMessageListenerContainer container;
    private Map<String, Object> producerConfig;
    private Map<String, Object> consumerConfig;
    private DefaultKafkaConsumerFactory consumerFactory;
    private ContainerProperties containerProperties;

    @BeforeEach
    public void beforeEach(){
        producerConfig = KafkaTestUtils.producerProps(embeddedKafka);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        consumerConfig = new HashMap<>(KafkaTestUtils.consumerProps("order", "false", embeddedKafka));
        consumerConfig.put(JsonDeserializer.TRUSTED_PACKAGES, "com.iptiq.notificationservice.*");
    }

    @Test
    public void createdOrderShouldWorkCorrectly() throws InterruptedException {


        DefaultKafkaProducerFactory<String, OrderCreated> pf = new DefaultKafkaProducerFactory<>(producerConfig);
        KafkaTemplate<String, OrderCreated> template = new KafkaTemplate<>(pf, true);

        OrderCreated orderCreated = OrderCreated.builder()
                .orderId(1)
                .creationDateTime(new Date())
                .customerId(1)
                .orderRows(Arrays.asList(OrderRow.builder()
                        .orderRowId(1)
                        .itemId(1)
                        .quantity(10)
                        .unitaryPrice(100D)
                        .build())).build();
        template.send("orders",orderCreated);
        template.flush();

        consumerFactory = new DefaultKafkaConsumerFactory<String, OrderCreatedResponse>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>());
        containerProperties = new ContainerProperties("order-message");
        container = new KafkaMessageListenerContainer<String, OrderCreatedResponse>(consumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<String, OrderCreatedResponse>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, OrderCreatedResponse>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        ConsumerRecord<String, OrderCreatedResponse> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);

        OrderCreatedResponse orderCreatedResponse = singleRecord.value();

        assertThat(orderCreatedResponse.getTotalPrice()).isEqualTo(1000);
        assertThat(orderCreatedResponse.getOrderId()).isEqualTo(orderCreated.getOrderId());
        assertThat(orderCreatedResponse.getNumberOfItems()).isEqualTo(10);
        container.stop();
    }

    @Test
    public void shippedOrderShouldWorkCorrectly() throws InterruptedException {


        DefaultKafkaProducerFactory<String, OrderShipped> pf = new DefaultKafkaProducerFactory<String, OrderShipped>(producerConfig);
        KafkaTemplate<String, OrderShipped> template = new KafkaTemplate<String, OrderShipped>(pf, true);

        OrderShipped orderShipped = OrderShipped.builder()
                .orderId(1)
                .deliveryCompanyId(1)
                .deliveryCompanyName("x")
                .shippingCost(100D)
                .shippingDate(new Date())
                .warehousePosition(1)
                .build();

        template.send("shipped-orders",orderShipped);
        template.flush();

        consumerFactory = new DefaultKafkaConsumerFactory<String, OrderShippedResponse>(consumerConfig, new StringDeserializer(), new JsonDeserializer<>());
        containerProperties = new ContainerProperties("shipped-message");
        container = new KafkaMessageListenerContainer<String, OrderShippedResponse>(consumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<String, OrderShippedResponse>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, OrderShippedResponse>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        ConsumerRecord<String, OrderShippedResponse> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);

        OrderShippedResponse orderShippedResponse = singleRecord.value();

        assertThat(orderShippedResponse.getShippingCost()).isEqualTo(orderShipped.getShippingCost());
        assertThat(orderShippedResponse.getOrderId()).isEqualTo(orderShipped.getOrderId());
        assertThat(orderShippedResponse.getDeliveryCompanyName()).isEqualTo(orderShipped.getDeliveryCompanyName());
        container.stop();
    }
}
