package com.example.shoppingcart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// Cart tests — using @Nested classes to group by behaviour.
//
// @Nested classes are supported by the Testream JUnit reporter.
// Maven Surefire generates a separate XML per nested class; the reporter
// processes all of them and merges results into a single run in Jira.
//
// One test is deliberately broken to demonstrate how Testream surfaces
// failures in the Jira dashboard — with the error diff and stack trace
// visible, and a one-click button to create a Jira issue from the failed test.
// Look for the "INTENTIONALLY FAILING" comment below.
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("Cart")
class CartTest {

    private static final Product APPLE  = new Product("prod-001", "Apple",  120, 100, "Fruit");
    private static final Product BANANA = new Product("prod-002", "Banana",  80, 100, "Fruit");

    @Nested
    @DisplayName("Adding Items")
    class AddingItems {

        @Test
        @DisplayName("adds a new item with default quantity of 1")
        void addsItemWithDefaultQuantity() {
            Cart cart = new Cart();
            cart.addItem(APPLE);
            assertEquals(1, cart.getItems().size());
            assertEquals(1, cart.getItems().get(0).getQuantity());
        }

        @Test
        @DisplayName("adds a new item with a specified quantity")
        void addsItemWithSpecifiedQuantity() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 3);
            assertEquals(3, cart.getItems().get(0).getQuantity());
        }

        @Test
        @DisplayName("increments quantity when the same item is added twice")
        void incrementsQuantityForDuplicateItem() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 2);
            cart.addItem(APPLE, 1);
            assertEquals(3, cart.getItems().get(0).getQuantity());
        }

        @Test
        @DisplayName("can hold multiple different items")
        void holdsMultipleItems() {
            Cart cart = new Cart();
            cart.addItem(APPLE);
            cart.addItem(BANANA);
            assertEquals(2, cart.getItems().size());
        }

        @Test
        @DisplayName("throws when quantity is zero or negative")
        void throwsForZeroOrNegativeQuantity() {
            Cart cart = new Cart();
            assertThrows(IllegalArgumentException.class, () -> cart.addItem(APPLE, 0));
            assertThrows(IllegalArgumentException.class, () -> cart.addItem(APPLE, -1));
        }
    }

    @Nested
    @DisplayName("Removing Items")
    class RemovingItems {

        @Test
        @DisplayName("decrements quantity when removing fewer than stocked")
        void removingItemDecrementsQuantity() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 3);
            cart.removeItem(APPLE.getId(), 2);
            assertEquals(1, cart.getItems().get(0).getQuantity());
        }

        @Test
        @DisplayName("removes item entirely when quantity reaches zero")
        void removingItemAtZero() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 3);
            cart.removeItem(APPLE.getId(), 3);
            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("removes item entirely when quantity removed exceeds stock")
        void removingItemWhenExceedingStock() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 3);
            cart.removeItem(APPLE.getId(), 99);
            assertTrue(cart.isEmpty());
        }

        @Test
        @DisplayName("throws when item is not in the cart")
        void removingItemThrowsForMissingItem() {
            Cart cart = new Cart();
            Exception ex = assertThrows(java.util.NoSuchElementException.class,
                    () -> cart.removeItem("does-not-exist"));
            assertTrue(ex.getMessage().contains("does-not-exist"));
        }
    }

    @Nested
    @DisplayName("Totals")
    class Totals {

        @Test
        @DisplayName("returns 0 for an empty cart")
        void zeroTotalForEmptyCart() {
            assertEquals(0, new Cart().getTotal());
        }

        @Test
        @DisplayName("calculates total for a single item")
        void totalForSingleItem() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 2); // 2 × 120 = 240
            assertEquals(240, cart.getTotal());
        }

        @Test
        @DisplayName("calculates total for multiple items")
        void totalForMultipleItems() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 2);  // 240
            cart.addItem(BANANA, 3); // 240
            assertEquals(480, cart.getTotal());
        }
    }

    @Nested
    @DisplayName("Clear")
    class Clear {

        @Test
        @DisplayName("empties the cart")
        void clearsAllItems() {
            Cart cart = new Cart();
            cart.addItem(APPLE);
            cart.addItem(BANANA);
            cart.clear();
            assertTrue(cart.isEmpty());
            assertEquals(0, cart.getItems().size());
        }
    }

    @Nested
    @DisplayName("Checkout")
    class Checkout {

        @Test
        @DisplayName("returns items and total for a populated cart")
        void checkoutReturnsResult() {
            Cart cart = new Cart();
            cart.addItem(APPLE, 2);
            Cart.CheckoutResult result = cart.checkout();
            assertEquals(240, result.getTotal());
            assertEquals(1, result.getItems().size());
        }

        // ─────────────────────────────────────────────────────────────────────
        // INTENTIONALLY FAILING TEST
        //
        // This test asserts the wrong exception message to simulate a real-world
        // regression. In Testream you will see the exact error diff, the full
        // stack trace, and you can open a Jira issue for it in one click.
        // ─────────────────────────────────────────────────────────────────────
        @Test
        @DisplayName("throws a descriptive error when checking out an empty cart")
        void throwsOnEmptyCheckout() {
            Cart cart = new Cart();
            // BUG: wrong expected message — the real message is
            // "Cannot check out with an empty cart"
            Exception ex = assertThrows(IllegalStateException.class, cart::checkout);
            assertEquals("Cart is empty", ex.getMessage());
        }
    }
}
