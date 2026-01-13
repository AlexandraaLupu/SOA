package org.example.worker;

import java.time.Instant;

public record OrderEvent(long orderId, long userId, long tableNumber, String item, Instant createdAt) {
}
