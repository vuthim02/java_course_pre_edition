package com.inventory.model;

import java.time.LocalDate;

public record Product(String id, String name, Category category, int quantity, double price, LocalDate addedDate) {
}
