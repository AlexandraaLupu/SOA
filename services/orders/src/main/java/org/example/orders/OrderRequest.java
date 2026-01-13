package org.example.orders;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotNull Long userId, @NotNull Long tableNumber, @NotBlank String item) {
}
