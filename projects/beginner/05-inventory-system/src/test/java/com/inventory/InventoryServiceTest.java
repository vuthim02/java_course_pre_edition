package com.inventory;

import com.inventory.model.Category;
import com.inventory.model.Product;
import com.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService();
    }

    @Nested
    class AddProduct {

        @Test
        void testAddProduct() {
            Product product = service.addProduct("Laptop", Category.ELECTRONICS, 5, 999.99);

            assertNotNull(product.id());
            assertEquals("Laptop", product.name());
            assertEquals(Category.ELECTRONICS, product.category());
            assertEquals(5, product.quantity());
            assertEquals(999.99, product.price());
            assertNotNull(product.addedDate());
        }

        @Test
        void testAddProductGeneratesId() {
            Product p1 = service.addProduct("A", Category.OTHER, 1, 1.0);
            Product p2 = service.addProduct("B", Category.OTHER, 1, 1.0);

            assertNotNull(p1.id());
            assertNotNull(p2.id());
            assertNotEquals(p1.id(), p2.id());
        }

        @Test
        void testAddProductIncrementsCount() {
            service.addProduct("A", Category.OTHER, 1, 1.0);
            service.addProduct("B", Category.OTHER, 2, 2.0);

            assertEquals(2, service.productCount());
        }
    }

    @Nested
    class UpdateProduct {

        @Test
        void testUpdateProduct() {
            Product p = service.addProduct("Old", Category.OTHER, 1, 1.0);

            Optional<Product> updated = service.updateProduct(p.id(), "New", Category.ELECTRONICS, 10, 99.99);

            assertTrue(updated.isPresent());
            assertEquals("New", updated.get().name());
            assertEquals(Category.ELECTRONICS, updated.get().category());
            assertEquals(10, updated.get().quantity());
            assertEquals(99.99, updated.get().price());
        }

        @Test
        void testUpdateNonExistent() {
            assertFalse(service.updateProduct("ghost", "X", Category.OTHER, 1, 1.0).isPresent());
        }
    }

    @Nested
    class DeleteProduct {

        @Test
        void testDeleteProduct() {
            Product p = service.addProduct("Temp", Category.OTHER, 1, 1.0);

            Optional<Product> deleted = service.deleteProduct(p.id());

            assertTrue(deleted.isPresent());
            assertEquals("Temp", deleted.get().name());
            assertEquals(0, service.productCount());
        }

        @Test
        void testDeleteNonExistent() {
            assertFalse(service.deleteProduct("ghost").isPresent());
        }
    }

    @Nested
    class FindProduct {

        @Test
        void testFindProduct() {
            Product p = service.addProduct("Test", Category.FOOD, 3, 5.99);

            Optional<Product> found = service.findProduct(p.id());

            assertTrue(found.isPresent());
            assertEquals("Test", found.get().name());
        }

        @Test
        void testFindNonExistent() {
            assertFalse(service.findProduct("unknown").isPresent());
        }
    }

    @Nested
    class ListAll {

        @Test
        void testListAll_Empty() {
            assertTrue(service.listAll().isEmpty());
        }

        @Test
        void testListAll_WithProducts() {
            service.addProduct("A", Category.OTHER, 1, 1.0);
            service.addProduct("B", Category.OTHER, 2, 2.0);

            assertEquals(2, service.listAll().size());
        }
    }

    @Nested
    class Search {

        @Test
        void testSearchByName() {
            service.addProduct("Widget", Category.OTHER, 5, 9.99);
            service.addProduct("Gadget", Category.OTHER, 3, 19.99);

            List<Product> results = service.searchByName("Widget");

            assertEquals(1, results.size());
        }

        @Test
        void testSearchByCategory() {
            service.addProduct("Apple", Category.FOOD, 10, 1.99);
            service.addProduct("Shirt", Category.CLOTHING, 5, 19.99);

            List<Product> results = service.searchByCategory(Category.FOOD);

            assertEquals(1, results.size());
        }
    }

    @Nested
    class StockUpdates {

        @Test
        void testUpdateStock() {
            Product p = service.addProduct("StockTest", Category.OTHER, 5, 10.0);

            service.updateStock(p.id(), 20);

            Product updated = service.findProduct(p.id()).get();
            assertEquals(20, updated.quantity());
        }

        @Test
        void testUpdateStock_Zero() {
            Product p = service.addProduct("StockTest", Category.OTHER, 5, 10.0);

            service.updateStock(p.id(), 0);

            assertEquals(0, service.findProduct(p.id()).get().quantity());
        }

        @Test
        void testUpdateStock_NonExistentThrows() {
            assertThrows(NoSuchElementException.class,
                    () -> service.updateStock("ghost", 10));
        }
    }

    @Nested
    class Discount {

        @Test
        void testApplyDiscount_TenPercent() {
            Product p = service.addProduct("Discounted", Category.OTHER, 5, 100.0);

            service.applyDiscount(p.id(), 10);

            assertEquals(90.0, service.findProduct(p.id()).get().price(), 0.001);
        }

        @Test
        void testApplyDiscount_FiftyPercent() {
            Product p = service.addProduct("HalfOff", Category.OTHER, 5, 200.0);

            service.applyDiscount(p.id(), 50);

            assertEquals(100.0, service.findProduct(p.id()).get().price(), 0.001);
        }

        @Test
        void testApplyDiscount_ZeroPercent() {
            Product p = service.addProduct("NoDiscount", Category.OTHER, 5, 50.0);

            service.applyDiscount(p.id(), 0);

            assertEquals(50.0, service.findProduct(p.id()).get().price(), 0.001);
        }

        @Test
        void testApplyDiscount_HundredPercent() {
            Product p = service.addProduct("Free", Category.OTHER, 5, 50.0);

            service.applyDiscount(p.id(), 100);

            assertEquals(0.0, service.findProduct(p.id()).get().price(), 0.001);
        }

        @Test
        void testApplyDiscount_NonExistentThrows() {
            assertThrows(NoSuchElementException.class,
                    () -> service.applyDiscount("ghost", 10));
        }
    }

    @Nested
    class LowStockReport {

        @Test
        void testLowStockReport() {
            service.addProduct("High", Category.OTHER, 100, 1.0);
            service.addProduct("Low", Category.OTHER, 3, 1.0);

            List<Product> low = service.lowStockReport(10);

            assertEquals(1, low.size());
            assertEquals("Low", low.get(0).name());
        }

        @Test
        void testLowStockReport_ThresholdZero() {
            service.addProduct("A", Category.OTHER, 0, 1.0);

            List<Product> low = service.lowStockReport(1);

            assertEquals(1, low.size());
        }
    }

    @Nested
    class Sort {

        @Test
        void testSortedByName() {
            service.addProduct("Zebra", Category.OTHER, 1, 1.0);
            service.addProduct("Apple", Category.OTHER, 1, 1.0);

            List<Product> sorted = service.sortedBy("name");

            assertEquals("Apple", sorted.get(0).name());
            assertEquals("Zebra", sorted.get(1).name());
        }

        @Test
        void testSortedByPrice() {
            service.addProduct("Expensive", Category.OTHER, 1, 100.0);
            service.addProduct("Cheap", Category.OTHER, 1, 1.0);

            List<Product> sorted = service.sortedBy("price");

            assertEquals(1.0, sorted.get(0).price());
            assertEquals(100.0, sorted.get(1).price());
        }

        @Test
        void testSortedByQuantity() {
            service.addProduct("Few", Category.OTHER, 1, 1.0);
            service.addProduct("Many", Category.OTHER, 99, 1.0);

            List<Product> sorted = service.sortedBy("quantity");

            assertEquals(1, sorted.get(0).quantity());
            assertEquals(99, sorted.get(1).quantity());
        }

        @Test
        void testSortedBy_InvalidField() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.sortedBy("invalid"));
        }
    }

    @Nested
    class CsvImport {

        @Test
        void testImportFromCsv() {
            String csv = "Apple,FOOD,10,1.99\nBanana,FOOD,20,0.99";

            service.importFromCsv(csv);

            assertEquals(2, service.productCount());
        }

        @Test
        void testImportFromCsv_SkipsMalformedLines() {
            String csv = "Valid,FOOD,10,1.99\nbadline\nAlsoValid,CLOTHING,5,9.99";

            service.importFromCsv(csv);

            assertEquals(2, service.productCount());
        }

        @Test
        void testImportFromCsv_EmptyLinesSkipped() {
            String csv = "A,FOOD,1,1.0\n\nB,FOOD,2,2.0";

            service.importFromCsv(csv);

            assertEquals(2, service.productCount());
        }

        @Test
        void testImportFromCsv_InvalidCategory() {
            String csv = "Item,BADCAT,1,1.0";

            service.importFromCsv(csv);

            assertEquals(0, service.productCount());
        }

        @Test
        void testImportFromCsv_EmptyString() {
            service.importFromCsv("");

            assertEquals(0, service.productCount());
        }

        @Test
        void testImportFromCsv_InvalidNumber() {
            String csv = "Item,FOOD,notanumber,1.0";

            service.importFromCsv(csv);

            assertEquals(0, service.productCount());
        }
    }
}
