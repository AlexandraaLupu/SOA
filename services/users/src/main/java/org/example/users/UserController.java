package org.example.users;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final KeycloakAdminClient keycloakAdminClient;

    public UserController(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }

    @GetMapping("/users")
    List<UserResponse> list() {
        List<KeycloakAdminClient.KeycloakUser> users = keycloakAdminClient.fetchUsers();
        if (users.isEmpty()) {
            return List.of();
        }
        return users.stream()
            .map(user -> toResponse(user, users.indexOf(user) + 1L))
            .toList();
    }

    @GetMapping("/users/{id}")
    UserResponse get(@PathVariable long id) {
        List<KeycloakAdminClient.KeycloakUser> users = keycloakAdminClient.fetchUsers();
        if (users.isEmpty()) {
            return new UserResponse(id, "Guest", "free", "guest");
        }
        int index = Math.toIntExact(id - 1);
        if (index < 0 || index >= users.size()) {
            return new UserResponse(id, "Guest", "free", "guest");
        }
        return toResponse(users.get(index), id);
    }

    public record UserResponse(long id, @NotBlank String name, @NotBlank String tier, @NotBlank String username) {
    }

    private UserResponse toResponse(KeycloakAdminClient.KeycloakUser user, Long idOverride) {
        String displayName = displayName(user);
        long id = idOverride == null ? 0L : idOverride;
        return new UserResponse(id, displayName, "keycloak", username(user));
    }

    private String displayName(KeycloakAdminClient.KeycloakUser user) {
        String first = user.firstName();
        String last = user.lastName();
        if (first != null && !first.isBlank()) {
            return last == null || last.isBlank() ? first : first + " " + last;
        }
        return user.username() == null ? "User" : user.username();
    }

    private String username(KeycloakAdminClient.KeycloakUser user) {
        return user.username() == null ? "user" : user.username();
    }
}
