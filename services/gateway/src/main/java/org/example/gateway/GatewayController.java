package org.example.gateway;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GatewayController {
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
        "connection",
        "keep-alive",
        "proxy-authenticate",
        "proxy-authorization",
        "te",
        "trailer",
        "transfer-encoding",
        "upgrade"
    );

    private final RestTemplate restTemplate;
    private final String ordersBaseUrl;
    private final String usersBaseUrl;

    public GatewayController(
        RestTemplate restTemplate,
        @Value("${services.orders.base-url}") String ordersBaseUrl,
        @Value("${services.users.base-url}") String usersBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.ordersBaseUrl = ordersBaseUrl;
        this.usersBaseUrl = usersBaseUrl;
    }

    @GetMapping("/api/orders")
    ResponseEntity<String> listOrders() {
        ResponseEntity<String> response = restTemplate.getForEntity(ordersBaseUrl + "/orders", String.class);
        return forwardResponse(response);
    }

    @PostMapping("/api/orders")
    ResponseEntity<String> createOrder(@RequestBody Map<String, Object> payload) {
        ResponseEntity<String> response =
            restTemplate.postForEntity(ordersBaseUrl + "/orders", payload, String.class);
        return forwardResponse(response);
    }

    @GetMapping("/api/orders/{id}")
    ResponseEntity<String> getOrder(@PathVariable String id) {
        ResponseEntity<String> response =
            restTemplate.getForEntity(ordersBaseUrl + "/orders/{id}", String.class, id);
        return forwardResponse(response);
    }

    @GetMapping("/api/users")
    ResponseEntity<String> listUsers() {
        ResponseEntity<String> response = restTemplate.getForEntity(usersBaseUrl + "/users", String.class);
        return forwardResponse(response);
    }

    @GetMapping("/api/users/{id}")
    ResponseEntity<String> getUser(@PathVariable String id) {
        ResponseEntity<String> response =
            restTemplate.getForEntity(usersBaseUrl + "/users/{id}", String.class, id);
        return forwardResponse(response);
    }

    private ResponseEntity<String> forwardResponse(ResponseEntity<String> response) {
        HttpHeaders headers = new HttpHeaders();
        response.getHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                headers.put(name, values);
            }
        });
        return ResponseEntity.status(response.getStatusCode()).headers(headers).body(response.getBody());
    }
}
