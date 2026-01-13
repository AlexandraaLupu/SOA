package org.example.orders;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@RestController
public class OrdersController {
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UsersClient usersClient;

    public OrdersController(
        RabbitTemplate rabbitTemplate,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        UsersClient usersClient
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.usersClient = usersClient;
    }

    @GetMapping("/orders")
    List<OrderResponse> list() {
        return orders.values().stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/orders/{id}")
    OrderResponse get(@PathVariable long id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return toResponse(order);
    }

    @PostMapping("/orders")
    OrderResponse create(@Valid @RequestBody OrderRequest request) {
        long id = idGenerator.incrementAndGet();
        Order order = new Order(
            id,
            request.userId(),
            request.tableNumber(),
            request.item(),
            "CREATED",
            Instant.now()
        );
        orders.put(id, order);

        publishOrderCreated(order);
        return toResponse(order);
    }

    private void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent(
            order.id(),
            order.userId(),
            order.tableNumber(),
            order.item(),
            order.createdAt()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDERS_EXCHANGE,
                RabbitConfig.ORDERS_CREATED_KEY,
                payload
            );
            kafkaTemplate.send("orders.events", payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to publish order event", ex);
        }
    }

    private OrderResponse toResponse(Order order) {
        if (order == null) {
            return new OrderResponse(null, null);
        }
        UserSummary user = usersClient.fetchUser(order.userId())
            .orElseGet(() -> new UserSummary(order.userId(), "Unknown", "free"));
        return new OrderResponse(order, user);
    }
}
