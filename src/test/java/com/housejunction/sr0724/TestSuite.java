package com.housejunction.sr0724;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSuite {
    @Test
    void shouldNotAcceptDiscountOutsideOfRange() {
        String toolCode = "JAKR";
        String rentalDays = "5";
        String checkoutDate = "9/3/15";
        String discount = "101";

        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discount};
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Sr0724Application.main(args));
        assertEquals("Discount percent must be in the range 0 to 100, inclusive.", exception.getMessage());
    }
}
