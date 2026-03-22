package com.example.shoppingcart;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Discount {

    public enum CouponType { PERCENTAGE, FIXED }

    public static class Coupon {
        private final String code;
        private final CouponType type;
        private final int value;
        private final Instant expiresAt;
        private final Integer minOrderValue;

        public Coupon(String code, CouponType type, int value, Instant expiresAt) {
            this(code, type, value, expiresAt, null);
        }

        public Coupon(String code, CouponType type, int value, Instant expiresAt, Integer minOrderValue) {
            this.code = code;
            this.type = type;
            this.value = value;
            this.expiresAt = expiresAt;
            this.minOrderValue = minOrderValue;
        }

        public String getCode() { return code; }
        public CouponType getType() { return type; }
        public int getValue() { return value; }
        public Instant getExpiresAt() { return expiresAt; }
        public Integer getMinOrderValue() { return minOrderValue; }
    }

    /** Applies a percentage discount to a price; throws if percent is out of range. */
    public static int applyPercentage(int price, int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        return Math.max(0, price - (price * percent / 100));
    }

    /** Subtracts a fixed amount from a price, clamped to 0; throws if amount is negative. */
    public static int applyFixed(int price, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        return Math.max(0, price - amount);
    }

    /** Validates a coupon against the cart total; returns a list of error messages. */
    public static List<String> validateCoupon(Coupon coupon, int cartTotal) {
        return validateCoupon(coupon, cartTotal, Instant.now());
    }

    public static List<String> validateCoupon(Coupon coupon, int cartTotal, Instant now) {
        List<String> errors = new ArrayList<>();
        if (coupon.expiresAt.isBefore(now)) {
            errors.add("Coupon has expired");
        }
        if (coupon.minOrderValue != null && cartTotal < coupon.minOrderValue) {
            errors.add("Minimum order value not met (requires " + coupon.minOrderValue
                    + " cents, cart is " + cartTotal + " cents)");
        }
        return Collections.unmodifiableList(errors);
    }

    /** Applies the coupon to the price; throws if the coupon is invalid. */
    public static int applyCoupon(int price, Coupon coupon) {
        return applyCoupon(price, coupon, Instant.now());
    }

    public static int applyCoupon(int price, Coupon coupon, Instant now) {
        List<String> errors = validateCoupon(coupon, price, now);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }
        return switch (coupon.getType()) {
            case PERCENTAGE -> applyPercentage(price, coupon.getValue());
            case FIXED      -> applyFixed(price, coupon.getValue());
        };
    }
}
