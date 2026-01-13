package org.example.orders;

import java.time.Instant;

public record Order(long id, long userId, long tableNumber, String item, String status, Instant createdAt) {
}
