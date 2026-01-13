package org.example.worker;

import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Component
public class OrderWorkListener {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String fnBaseUrl;
    private final String fnApp;
    private final String fnFunctionName;

    public OrderWorkListener(
        ObjectMapper objectMapper,
        RestTemplate restTemplate,
        @Value("${faas.fn.base-url}") String fnBaseUrl,
        @Value("${faas.fn.app}") String fnApp,
        @Value("${faas.fn.function-name}") String fnFunctionName
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.fnBaseUrl = fnBaseUrl;
        this.fnApp = fnApp;
        this.fnFunctionName = fnFunctionName;
    }

    @RabbitListener(queues = RabbitConfig.ORDERS_QUEUE)
    public void onOrderMessage(String payload) {
        try {
            OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
            invokeFaas(event);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to process order message", ex);
        }
    }

    private void invokeFaas(OrderEvent event) {
        String url = fnBaseUrl + "/t/" + fnApp + "/" + fnFunctionName;
        restTemplate.postForEntity(url, Map.of(
            "orderId", event.orderId(),
            "userId", event.userId(),
            "tableNumber", event.tableNumber(),
            "item", event.item()
        ), String.class);
    }
}
