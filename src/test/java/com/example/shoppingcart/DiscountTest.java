package com.example.shoppingcart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ─────────────────────────────────────────────────────────────────────────────
// Discount tests
//
// Note: @Nested classes are avoided intentionally. JUnit 5 + Maven Surefire
// generates an empty outer XML file for a class that only contains @Nested
// classes (tests=0), which causes the Testream JUnit reporter to fail.
// Keeping all methods flat produces a single, well-formed XML per class.
//
// See the "INTENTIONALLY FAILING" comment below for a test that is
// deliberately broken to demonstrate Testream failure inspection in Jira.
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("Discount")
class DiscountTest {

    private static final Instant FUTURE = Instant.parse("2099-12-31T00:00:00Z");
    private static final Instant NOW    = Instant.parse("2025-06-01T12:00:00Z");

    private static final Discount.Coupon PERCENTAGE_COUPON =
            new Discount.Coupon("SUMMER20", Discount.CouponType.PERCENTAGE, 20, FUTURE);

    private static final Discount.Coupon FIXED_COUPON =
            new Discount.Coupon("SAVE500", Discount.CouponType.FIXED, 500, FUTURE);

    // ── applyPercentage ───────────────────────────────────────────────────────

    @Test
    @DisplayName("applyPercentage - applies a 20% discount to a price")
    void applyPercentageTwentyPercent() {
        assertEquals(800, Discount.applyPercentage(1000, 20));
    }

    @Test
    @DisplayName("applyPercentage - applies a 0% discount (no change)")
    void applyPercentageZeroPercent() {
        assertEquals(1000, Discount.applyPercentage(1000, 0));
    }

    @Test
    @DisplayName("applyPercentage - applies a 100% discount (free)")
    void applyPercentageHundredPercent() {
        assertEquals(0, Discount.applyPercentage(1000, 100));
    }

    @Test
    @DisplayName("applyPercentage - clamps to 0 and never returns negative")
    void applyPercentageClampedToZero() {
        assertTrue(Discount.applyPercentage(100, 100) >= 0);
    }

    @Test
    @DisplayName("applyPercentage - throws when percent is below 0")
    void applyPercentageThrowsForNegative() {
        assertThrows(IllegalArgumentException.class, () -> Discount.applyPercentage(1000, -1));
    }

    @Test
    @DisplayName("applyPercentage - throws when percent exceeds 100")
    void applyPercentageThrowsForOver100() {
        assertThrows(IllegalArgumentException.class, () -> Discount.applyPercentage(1000, 101));
    }

    // ── applyFixed ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("applyFixed - subtracts a fixed amount from the price")
    void applyFixedSubtractsAmount() {
        assertEquals(700, Discount.applyFixed(1000, 300));
    }

    @Test
    @DisplayName("applyFixed - clamps to 0 when discount exceeds price")
    void applyFixedClampedWhenExceedsPrice() {
        assertEquals(0, Discount.applyFixed(200, 500));
    }

    @Test
    @DisplayName("applyFixed - returns unchanged price for a zero discount")
    void applyFixedZeroDiscount() {
        assertEquals(1000, Discount.applyFixed(1000, 0));
    }

    @Test
    @DisplayName("applyFixed - throws when discount amount is negative")
    void applyFixedThrowsForNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> Discount.applyFixed(1000, -1));
    }

    // ── validateCoupon ────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateCoupon - returns no errors for a valid percentage coupon")
    void validateCouponValidPercentageCoupon() {
        assertTrue(Discount.validateCoupon(PERCENTAGE_COUPON, 5000, NOW).isEmpty());
    }

    @Test
    @DisplayName("validateCoupon - returns no errors for a valid fixed coupon")
    void validateCouponValidFixedCoupon() {
        assertTrue(Discount.validateCoupon(FIXED_COUPON, 5000, NOW).isEmpty());
    }

    @Test
    @DisplayName("validateCoupon - reports an error when the coupon has expired")
    void validateCouponExpiredCoupon() {
        Discount.Coupon expired = new Discount.Coupon(
                "OLD10", Discount.CouponType.PERCENTAGE, 10, Instant.parse("2020-01-01T00:00:00Z"));
        List<String> errors = Discount.validateCoupon(expired, 5000, NOW);
        assertTrue(errors.contains("Coupon has expired"));
    }

    @Test
    @DisplayName("validateCoupon - reports an error when the minimum order value is not met")
    void validateCouponMinimumOrderNotMet() {
        Discount.Coupon coupon = new Discount.Coupon(
                "BIG20", Discount.CouponType.PERCENTAGE, 20, FUTURE, 10000);
        List<String> errors = Discount.validateCoupon(coupon, 5000, NOW);
        assertTrue(errors.stream().anyMatch(e -> e.contains("Minimum order value")));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTENTIONALLY FAILING TEST
    //
    // This test checks that an expired coupon with an unmet minimum order
    // value produces 3 errors. The real implementation returns 2.
    // This simulates a developer adding a validation rule and forgetting to
    // update the test. Testream will flag this in Jira with the exact diff.
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("validateCoupon - reports multiple errors for an expired coupon that also fails min order check")
    void validateCouponMultipleValidationErrors() {
        Discount.Coupon badCoupon = new Discount.Coupon(
                "BAD", Discount.CouponType.PERCENTAGE, 10,
                Instant.parse("2020-01-01T00:00:00Z"), 20000);
        List<String> errors = Discount.validateCoupon(badCoupon, 5000, NOW);
        // BUG: expects 3 errors but only 2 are returned (expired + min order)
        assertEquals(3, errors.size(),
                "Expected 3 validation errors but got: " + errors);
    }

    // ── applyCoupon ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("applyCoupon - applies a valid percentage coupon")
    void applyCouponPercentageCoupon() {
        assertEquals(800, Discount.applyCoupon(1000, PERCENTAGE_COUPON, NOW)); // 20% off
    }

    @Test
    @DisplayName("applyCoupon - applies a valid fixed coupon")
    void applyCouponFixedCoupon() {
        assertEquals(500, Discount.applyCoupon(1000, FIXED_COUPON, NOW)); // 500 off
    }

    @Test
    @DisplayName("applyCoupon - throws when an expired coupon is applied")
    void applyCouponThrowsForExpiredCoupon() {
        Discount.Coupon expired = new Discount.Coupon(
                "OLD10", Discount.CouponType.PERCENTAGE, 10, Instant.parse("2020-01-01T00:00:00Z"));
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> Discount.applyCoupon(1000, expired, NOW));
        assertEquals("Coupon has expired", ex.getMessage());
    }
}
