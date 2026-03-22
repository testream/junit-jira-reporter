package com.example.shoppingcart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// Product tests
//
// See the "INTENTIONALLY FAILING" comment below for a test that is
// deliberately broken to demonstrate Testream failure inspection in Jira.
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("Product")
class ProductTest {

    private static final Product VALID_PRODUCT =
            new Product("prod-001", "Wireless Headphones", 7999, 42, "Electronics");

    @Nested
    @DisplayName("formatPrice")
    class FormatPriceTests {

        @Test
        @DisplayName("formats a price in USD by default")
        void formatsUsd() {
            assertEquals("$19.99", Product.formatPrice(1999));
        }

        @Test
        @DisplayName("formats a price in GBP")
        void formatsGbp() {
            assertEquals("£79.99", Product.formatPrice(7999, "GBP", Locale.UK));
        }

        @Test
        @DisplayName("formats zero correctly")
        void formatsZero() {
            assertEquals("$0.00", Product.formatPrice(0));
        }

        @Test
        @DisplayName("formats whole dollars without extra decimals")
        void formatsWholeDollars() {
            assertEquals("$10.00", Product.formatPrice(1000));
        }
    }

    @Nested
    @DisplayName("validate")
    class ValidateTests {

        @Test
        @DisplayName("returns no errors for a fully valid product")
        void noErrorsForValidProduct() {
            assertTrue(Product.validate(VALID_PRODUCT).isEmpty());
        }

        @Test
        @DisplayName("reports an error when id is missing")
        void errorForMissingId() {
            Product p = new Product("", "Headphones", 7999, 42, "Electronics");
            assertTrue(Product.validate(p).contains("id is required"));
        }

        @Test
        @DisplayName("reports an error when name is missing")
        void errorForMissingName() {
            Product p = new Product("prod-001", "", 7999, 42, "Electronics");
            assertTrue(Product.validate(p).contains("name is required"));
        }

        @Test
        @DisplayName("reports an error when price is negative")
        void errorForNegativePrice() {
            Product p = new Product("prod-001", "Headphones", -1, 42, "Electronics");
            assertTrue(Product.validate(p).contains("price must be a non-negative number"));
        }

        @Test
        @DisplayName("reports an error when stock is negative")
        void errorForNegativeStock() {
            Product p = new Product("prod-001", "Headphones", 7999, -5, "Electronics");
            assertTrue(Product.validate(p).contains("stock must be a non-negative number"));
        }

        @Test
        @DisplayName("reports an error when category is missing")
        void errorForMissingCategory() {
            Product p = new Product("prod-001", "Headphones", 7999, 42, "");
            assertTrue(Product.validate(p).contains("category is required"));
        }

        // ─────────────────────────────────────────────────────────────────────
        // INTENTIONALLY FAILING TEST
        //
        // This test checks that all required fields missing produces 2 errors.
        // The real implementation returns 4 errors (id, name, stock, category).
        // Testream will show the exact diff in the Jira dashboard.
        // ─────────────────────────────────────────────────────────────────────
        @Test
        @DisplayName("reports multiple errors when several required fields are missing")
        void multipleErrorsForMissingFields() {
            // price is 999, everything else missing (id, name, stock=-1, category)
            Product p = new Product(null, null, 999, -1, null);
            List<String> errors = Product.validate(p);
            // BUG: expects 2 errors but there are actually 4 (id, name, stock, category)
            assertEquals(2, errors.size(),
                    "Expected 2 validation errors but got: " + errors);
        }
    }

    @Nested
    @DisplayName("getDiscountedPrice")
    class DiscountedPriceTests {

        @Test
        @DisplayName("applies a 10% discount correctly")
        void tenPercentDiscount() {
            assertEquals(900, Product.getDiscountedPrice(1000, 10));
        }

        @Test
        @DisplayName("applies a 50% discount correctly")
        void fiftyPercentDiscount() {
            assertEquals(500, Product.getDiscountedPrice(1000, 50));
        }

        @Test
        @DisplayName("applies a 100% discount (free item)")
        void hundredPercentDiscount() {
            assertEquals(0, Product.getDiscountedPrice(1000, 100));
        }

        @Test
        @DisplayName("returns the original price for a 0% discount")
        void zeroPercentDiscount() {
            assertEquals(1000, Product.getDiscountedPrice(1000, 0));
        }

        @Test
        @DisplayName("throws when discount percent is out of range")
        void throwsForOutOfRangeDiscount() {
            assertThrows(IllegalArgumentException.class, () -> Product.getDiscountedPrice(1000, -1));
            assertThrows(IllegalArgumentException.class, () -> Product.getDiscountedPrice(1000, 101));
        }
    }

    @Nested
    @DisplayName("isInStock")
    class InStockTests {

        @Test
        @DisplayName("returns true when stock is greater than 0")
        void inStockWhenPositive() {
            assertTrue(VALID_PRODUCT.isInStock());
        }

        @Test
        @DisplayName("returns false when stock is 0")
        void outOfStockWhenZero() {
            Product p = new Product("prod-001", "Headphones", 7999, 0, "Electronics");
            assertFalse(p.isInStock());
        }
    }
}
