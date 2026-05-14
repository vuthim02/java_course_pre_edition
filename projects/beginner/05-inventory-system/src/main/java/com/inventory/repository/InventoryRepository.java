package com.inventory.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.inventory.model.*;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InventoryRepository {

    private final Map<String, Product> storage = new ConcurrentHashMap<>();

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                out.value(value.toString());
            }
            @Override
            public LocalDate read(JsonReader in) throws IOException {
                return LocalDate.parse(in.nextString());
            }
        })
        .setPrettyPrinting()
        .create();

    public void add(Product product) {
        storage.put(product.id(), product);
    }

    public Optional<Product> update(String id, Product updated) {
        if (!storage.containsKey(id)) return Optional.empty();
        storage.put(id, updated);
        return Optional.of(updated);
    }

    public Optional<Product> delete(String id) {
        return Optional.ofNullable(storage.remove(id));
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Product> findAll() {
        return List.copyOf(storage.values());
    }

    public List<Product> searchByName(String query) {
        String lower = query.toLowerCase();
        return storage.values().stream()
            .filter(p -> p.name().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    public List<Product> searchByCategory(Category category) {
        return storage.values().stream()
            .filter(p -> p.category() == category)
            .collect(Collectors.toList());
    }

    public List<Product> getLowStock(int threshold) {
        return storage.values().stream()
            .filter(p -> p.quantity() < threshold)
            .collect(Collectors.toList());
    }

    public List<Product> sortByName() {
        return storage.values().stream()
            .sorted(Comparator.comparing(Product::name))
            .collect(Collectors.toList());
    }

    public List<Product> sortByPrice() {
        return storage.values().stream()
            .sorted(Comparator.comparingDouble(Product::price))
            .collect(Collectors.toList());
    }

    public List<Product> sortByQuantity() {
        return storage.values().stream()
            .sorted(Comparator.comparingInt(Product::quantity))
            .collect(Collectors.toList());
    }

    public void saveToFile(String path) throws IOException {
        try (Writer writer = new FileWriter(path)) {
            GSON.toJson(storage, writer);
        }
    }

    public void loadFromFile(String path) throws IOException {
        try (Reader reader = new FileReader(path)) {
            Type type = new TypeToken<Map<String, Product>>() {}.getType();
            Map<String, Product> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                storage.clear();
                storage.putAll(loaded);
            }
        }
    }
}
