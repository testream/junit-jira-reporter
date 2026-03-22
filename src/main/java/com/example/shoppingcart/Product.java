package com.example.shoppingcart;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class Product {

    private final String id;
    private final String name;
    private final int price;   // stored in pence/cents
    private final int stock;
    private final String category;

    public Product(String id, String name, int price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }

    public boolean isInStock() { return stock > 0; }

    /** Validates the product and returns a list of error messages (empty = valid). */
    public static List<String> validate(Product product) {
        List<String> errors = new ArrayList<>();
        if (product.id == null || product.id.isBlank())         errors.add("id is required");
        if (product.name == null || product.name.isBlank())     errors.add("name is required");
        if (product.price < 0)                                   errors.add("price must be a non-negative number");
        if (product.stock < 0)                                   errors.add("stock must be a non-negative number");
        if (product.category == null || product.category.isBlank()) errors.add("category is required");
        return Collections.unmodifiableList(errors);
    }

    /** Returns price with the given percentage discount applied, clamped to 0. */
    public static int getDiscountedPrice(int price, int percentOff) {
        if (percentOff < 0 || percentOff > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
        return Math.max(0, price - (price * percentOff / 100));
    }

    /** Formats a price given in cents/pence as a locale-aware currency string (default USD). */
    public static String formatPrice(int cents) {
        return formatPrice(cents, "USD", Locale.US);
    }

    public static String formatPrice(int cents, String currencyCode, Locale locale) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        fmt.setCurrency(Currency.getInstance(currencyCode));
        return fmt.format(cents / 100.0);
    }
}
