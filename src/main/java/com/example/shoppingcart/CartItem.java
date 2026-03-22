package com.example.shoppingcart;

public class CartItem {
    private final Product product;
    private final int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(product, newQuantity);
    }
}
