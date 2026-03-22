package com.example.shoppingcart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class Cart {

    private final Map<String, CartItem> items = new LinkedHashMap<>();

    /** Adds one unit of the given product. */
    public void addItem(Product product) {
        addItem(product, 1);
    }

    /** Adds the specified number of units of the given product. */
    public void addItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        CartItem existing = items.get(product.getId());
        if (existing != null) {
            items.put(product.getId(), existing.withQuantity(existing.getQuantity() + quantity));
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
    }

    /** Removes all units of the given product. */
    public void removeItem(String productId) {
        removeItem(productId, Integer.MAX_VALUE);
    }

    /** Removes the specified number of units; removes the item entirely if quantity drops to 0. */
    public void removeItem(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new NoSuchElementException("Item \"" + productId + "\" not found in cart");
        }
        if (quantity >= item.getQuantity()) {
            items.remove(productId);
        } else {
            items.put(productId, item.withQuantity(item.getQuantity() - quantity));
        }
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public int getTotal() {
        return items.values().stream()
                .mapToInt(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    public CheckoutResult checkout() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot check out with an empty cart");
        }
        return new CheckoutResult(getItems(), getTotal());
    }

    public static class CheckoutResult {
        private final List<CartItem> items;
        private final int total;

        public CheckoutResult(List<CartItem> items, int total) {
            this.items = items;
            this.total = total;
        }

        public List<CartItem> getItems() { return items; }
        public int getTotal() { return total; }
    }
}
