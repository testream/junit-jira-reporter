package com.example.shoppingcart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// Cart tests
//
// Note: @Nested classes are avoided intentionally. JUnit 5 + Maven Surefire
// generates an empty outer XML file for a class that only contains @Nested
// classes (tests=0), which causes the Testream JUnit reporter to fail.
// Keeping all methods flat produces a single, well-formed XML per class.
//
// One test is deliberately broken to demonstrate how Testream surfaces failures
// in the Jira dashboard — with the error diff and stack trace visible, and a
// one-click button to create a Jira issue from the failed test.
// Look for the "INTENTIONALLY FAILING" comment below.
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("Cart")
class CartTest {

    private static final Product APPLE  = new Product("prod-001", "Apple",  120, 100, "Fruit");
    private static final Product BANANA = new Product("prod-002", "Banana",  80, 100, "Fruit");

    // ── adding items ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("adding items - adds a new item with default quantity of 1")
    void addsItemWithDefaultQuantity() {
        Cart cart = new Cart();
        cart.addItem(APPLE);
        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("adding items - adds a new item with a specified quantity")
    void addsItemWithSpecifiedQuantity() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 3);
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("adding items - increments quantity when the same item is added twice")
    void incrementsQuantityForDuplicateItem() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 2);
        cart.addItem(APPLE, 1);
        assertEquals(3, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("adding items - can hold multiple different items")
    void holdsMultipleItems() {
        Cart cart = new Cart();
        cart.addItem(APPLE);
        cart.addItem(BANANA);
        assertEquals(2, cart.getItems().size());
    }

    @Test
    @DisplayName("adding items - throws when quantity is zero or negative")
    void throwsForZeroOrNegativeQuantity() {
        Cart cart = new Cart();
        assertThrows(IllegalArgumentException.class, () -> cart.addItem(APPLE, 0));
        assertThrows(IllegalArgumentException.class, () -> cart.addItem(APPLE, -1));
    }

    // ── removing items ────────────────────────────────────────────────────────

    @Test
    @DisplayName("removing items - decrements quantity when removing fewer than stocked")
    void removingItemDecrementsQuantity() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 3);
        cart.removeItem(APPLE.getId(), 2);
        assertEquals(1, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("removing items - removes item entirely when quantity reaches zero")
    void removingItemAtZero() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 3);
        cart.removeItem(APPLE.getId(), 3);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("removing items - removes item entirely when quantity removed exceeds stock")
    void removingItemWhenExceedingStock() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 3);
        cart.removeItem(APPLE.getId(), 99);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("removing items - throws when item is not in the cart")
    void removingItemThrowsForMissingItem() {
        Cart cart = new Cart();
        Exception ex = assertThrows(java.util.NoSuchElementException.class,
                () -> cart.removeItem("does-not-exist"));
        assertTrue(ex.getMessage().contains("does-not-exist"));
    }

    // ── totals ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("totals - returns 0 for an empty cart")
    void zeroTotalForEmptyCart() {
        assertEquals(0, new Cart().getTotal());
    }

    @Test
    @DisplayName("totals - calculates total for a single item")
    void totalForSingleItem() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 2); // 2 × 120 = 240
        assertEquals(240, cart.getTotal());
    }

    @Test
    @DisplayName("totals - calculates total for multiple items")
    void totalForMultipleItems() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 2);  // 240
        cart.addItem(BANANA, 3); // 240
        assertEquals(480, cart.getTotal());
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clear - empties the cart")
    void clearsAllItems() {
        Cart cart = new Cart();
        cart.addItem(APPLE);
        cart.addItem(BANANA);
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
    }

    // ── checkout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkout - returns items and total for a populated cart")
    void checkoutReturnsResult() {
        Cart cart = new Cart();
        cart.addItem(APPLE, 2);
        Cart.CheckoutResult result = cart.checkout();
        assertEquals(240, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTENTIONALLY FAILING TEST
    //
    // This test asserts the wrong exception message to simulate a real-world
    // regression. In Testream you will see the exact error diff, the full
    // stack trace, and you can open a Jira issue for it in one click.
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("checkout - throws a descriptive error when checking out an empty cart")
    void throwsOnEmptyCheckout() {
        Cart cart = new Cart();
        // BUG: wrong expected message — the real message is
        // "Cannot check out with an empty cart"
        Exception ex = assertThrows(IllegalStateException.class, cart::checkout);
        assertEquals("Cart is empty", ex.getMessage());
    }
}
