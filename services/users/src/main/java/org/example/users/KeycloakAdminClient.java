package org.example.users;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Component
public class KeycloakAdminClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String realm;
    private final String adminRealm;
    private final String adminUsername;
    private final String adminPassword;

    public KeycloakAdminClient(
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        @Value("${keycloak.admin.base-url}") String baseUrl,
        @Value("${keycloak.admin.realm}") String realm,
        @Value("${keycloak.admin.admin-realm}") String adminRealm,
        @Value("${keycloak.admin.username}") String adminUsername,
        @Value("${keycloak.admin.password}") String adminPassword
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.adminRealm = adminRealm;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public List<KeycloakUser> fetchUsers() {
        return Optional.ofNullable(fetchAccessToken())
            .map(this::fetchUsersWithToken)
            .orElse(Collections.emptyList());
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", adminUsername);
        form.add("password", adminPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token",
            new HttpEntity<>(form, headers),
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return null;
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(response.getBody(), Map.class);
            Object token = payload.get("access_token");
            return token == null ? null : token.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private List<KeycloakUser> fetchUsersWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + "/admin/realms/" + realm + "/users",
            org.springframework.http.HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> users = objectMapper.readValue(response.getBody(), List.class);
            return users.stream()
                .map(this::toUser)
                .toList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private KeycloakUser toUser(Map<String, Object> entry) {
        return new KeycloakUser(
            value(entry, "username"),
            value(entry, "firstName"),
            value(entry, "lastName"),
            value(entry, "email")
        );
    }

    private String value(Map<String, Object> entry, String key) {
        Object value = entry.get(key);
        return value == null ? null : value.toString();
    }

    public record KeycloakUser(String username, String firstName, String lastName, String email) {
    }
}
