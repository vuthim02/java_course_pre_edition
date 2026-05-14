package com.inventory;

import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InventoryRepositoryTest {

    private InventoryRepository repository;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setUp() {
        repository = new InventoryRepository();
        productA = new Product("id1", "Apple", Category.FOOD, 10, 1.99, LocalDate.now());
        productB = new Product("id2", "Banana", Category.FOOD, 20, 0.99, LocalDate.now());
    }

    @Nested
    class Add {

        @Test
        void testAddProduct() {
            repository.add(productA);

            assertTrue(repository.findById("id1").isPresent());
            assertEquals("Apple", repository.findById("id1").get().name());
        }

        @Test
        void testAddDuplicateOverwrites() {
            repository.add(productA);
            Product updated = new Product("id1", "Apple Updated", Category.FOOD, 15, 2.99, LocalDate.now());
            repository.add(updated);

            assertEquals(1, repository.findAll().size());
            assertEquals("Apple Updated", repository.findById("id1").get().name());
        }
    }

    @Nested
    class Update {

        @Test
        void testUpdateExisting() {
            repository.add(productA);
            Product updated = new Product("id1", "Apple Updated", Category.FOOD, 15, 2.99, LocalDate.now());

            Optional<Product> result = repository.update("id1", updated);

            assertTrue(result.isPresent());
            assertEquals("Apple Updated", result.get().name());
            assertEquals(15, result.get().quantity());
        }

        @Test
        void testUpdateNonExistent() {
            Product updated = new Product("ghost", "Ghost", Category.OTHER, 1, 1.0, LocalDate.now());

            Optional<Product> result = repository.update("ghost", updated);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    class Delete {

        @Test
        void testDeleteExisting() {
            repository.add(productA);

            Optional<Product> result = repository.delete("id1");

            assertTrue(result.isPresent());
            assertEquals("Apple", result.get().name());
            assertFalse(repository.findById("id1").isPresent());
        }

        @Test
        void testDeleteNonExistent() {
            Optional<Product> result = repository.delete("ghost");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    class Search {

        @Test
        void testFindById_Existing() {
            repository.add(productA);

            Optional<Product> result = repository.findById("id1");

            assertTrue(result.isPresent());
            assertEquals("Apple", result.get().name());
        }

        @Test
        void testFindById_NonExistent() {
            assertFalse(repository.findById("unknown").isPresent());
        }

        @Test
        void testFindAll_Empty() {
            assertTrue(repository.findAll().isEmpty());
        }

        @Test
        void testFindAll_ReturnsCopy() {
            repository.add(productA);
            List<Product> all = repository.findAll();
            assertEquals(1, all.size());
        }

        @Test
        void testSearchByName_Exact() {
            repository.add(productA);
            repository.add(productB);

            List<Product> results = repository.searchByName("Apple");

            assertEquals(1, results.size());
        }

        @Test
        void testSearchByName_Partial() {
            repository.add(productA);
            repository.add(new Product("id3", "Pineapple", Category.FOOD, 5, 3.99, LocalDate.now()));

            List<Product> results = repository.searchByName("apple");

            assertEquals(2, results.size());
        }

        @Test
        void testSearchByName_NoMatch() {
            repository.add(productA);

            List<Product> results = repository.searchByName("xyz");

            assertTrue(results.isEmpty());
        }

        @Test
        void testSearchByName_EmptyQuery() {
            repository.add(productA);

            List<Product> results = repository.searchByName("");

            assertEquals(1, results.size());
        }

        @Test
        void testSearchByCategory() {
            repository.add(productA);
            repository.add(new Product("id3", "Shirt", Category.CLOTHING, 5, 19.99, LocalDate.now()));

            List<Product> results = repository.searchByCategory(Category.FOOD);

            assertEquals(2, results.size());
        }

        @Test
        void testSearchByCategory_NoMatch() {
            repository.add(productA);

            List<Product> results = repository.searchByCategory(Category.ELECTRONICS);

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    class Sort {

        @Test
        void testSortByName() {
            repository.add(productB);
            repository.add(productA);

            List<Product> sorted = repository.sortByName();

            assertEquals("Apple", sorted.get(0).name());
            assertEquals("Banana", sorted.get(1).name());
        }

        @Test
        void testSortByPrice() {
            repository.add(productA);
            repository.add(productB);

            List<Product> sorted = repository.sortByPrice();

            assertEquals(0.99, sorted.get(0).price());
            assertEquals(1.99, sorted.get(1).price());
        }

        @Test
        void testSortByQuantity() {
            repository.add(productA);
            repository.add(productB);

            List<Product> sorted = repository.sortByQuantity();

            assertEquals(10, sorted.get(0).quantity());
            assertEquals(20, sorted.get(1).quantity());
        }

        @Test
        void testSort_EmptyList() {
            assertTrue(repository.sortByName().isEmpty());
            assertTrue(repository.sortByPrice().isEmpty());
            assertTrue(repository.sortByQuantity().isEmpty());
        }
    }

    @Nested
    class LowStock {

        @Test
        void testGetLowStock() {
            repository.add(productA);
            repository.add(new Product("id3", "Low", Category.OTHER, 2, 1.0, LocalDate.now()));

            List<Product> lowStock = repository.getLowStock(5);

            assertEquals(1, lowStock.size());
            assertEquals("Low", lowStock.get(0).name());
        }

        @Test
        void testGetLowStock_NoneBelow() {
            repository.add(productA);

            assertTrue(repository.getLowStock(5).isEmpty());
        }

        @Test
        void testGetLowStock_AllBelow() {
            repository.add(new Product("id1", "A", Category.OTHER, 1, 1.0, LocalDate.now()));
            repository.add(new Product("id2", "B", Category.OTHER, 2, 2.0, LocalDate.now()));

            assertEquals(2, repository.getLowStock(10).size());
        }
    }
}
