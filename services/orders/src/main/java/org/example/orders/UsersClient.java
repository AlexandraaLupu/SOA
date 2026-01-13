package org.example.orders;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UsersClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsersClient(RestTemplate restTemplate, @Value("${users.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<UserSummary> fetchUser(long userId) {
        try {
            return Optional.ofNullable(
                restTemplate.getForObject(baseUrl + "/users/{id}", UserSummary.class, userId)
            );
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
