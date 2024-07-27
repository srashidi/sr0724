package com.housejunction.sr0724;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSuite {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeAll
    static void initAll() {
        Sr0724Application.loadDatabase();
    }

    @BeforeEach
    void init() {
        System.setOut(new PrintStream(outContent));
    }

    // Test 1
    @Test
    void shouldThrowExceptionForDiscountOutsideOfRange() {
        String toolCode = "JAKR";
        String rentalDays = "5";
        String checkoutDate = "9/3/15";
        String discountPercent = "101";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        // Ensure the correct exception and message comes up
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Sr0724Application.main(args));
        assertEquals("Discount percent must be in the range 0 to 100, inclusive.", exception.getMessage());

        // Ensure the rental_agreement table is empty
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get all rows from rental_agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(0, agreements.size());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Test 2
    @Test
    void shouldChargeForLadderWeekendsNotHolidays() {
        String toolCode = "LADW";
        String rentalDays = "3";
        String checkoutDate = "7/2/20";
        String discountPercent = "10";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        Sr0724Application.main(args);

        String expectedOutput = """
                Tool code: LADW
                Tool type: Ladder
                Tool brand: Werner
                Rental days: 3
                Check out date: 07/02/20
                Due date: 07/05/20
                Daily rental charge: $1.99
                Charge days: 2
                Pre-discount charge: $3.98
                Discount percent: 10%
                Discount amount: $0.40
                Final charge: $3.58
                """;
        assertTrue(outContent.toString().contains(expectedOutput));

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Assert there is only one rental agreement in rental_agreement and that it is the correct agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(1, agreements.size());

            RentalAgreement agreement = agreements.getFirst();
            assertEquals("LADW", agreement.getTool().getCode());
            assertEquals(3, agreement.getRentalDays());
            assertEquals(LocalDate.of(2020, 7, 2), agreement.getCheckoutDate());
            assertEquals(10, agreement.getDiscountPercent());
            assertEquals(1.99f, agreement.getDailyRentalCharge());
            assertTrue(agreement.hasWeekdayCharge());
            assertTrue(agreement.hasWeekendCharge());
            assertFalse(agreement.hasHolidayCharge());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Test 3
    @Test
    void shouldChargeForChainsawHolidaysNotWeekends() {
        String toolCode = "CHNS";
        String rentalDays = "5";
        String checkoutDate = "7/2/15";
        String discountPercent = "25";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        Sr0724Application.main(args);

        String expectedOutput = """
                Tool code: CHNS
                Tool type: Chainsaw
                Tool brand: Stihl
                Rental days: 5
                Check out date: 07/02/15
                Due date: 07/07/15
                Daily rental charge: $1.49
                Charge days: 3
                Pre-discount charge: $4.47
                Discount percent: 25%
                Discount amount: $1.12
                Final charge: $3.35
                """;
        assertTrue(outContent.toString().contains(expectedOutput));

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Assert there is only one rental agreement in rental_agreement and that it is the correct agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(1, agreements.size());

            RentalAgreement agreement = agreements.getFirst();
            assertEquals("CHNS", agreement.getTool().getCode());
            assertEquals(5, agreement.getRentalDays());
            assertEquals(LocalDate.of(2015, 7, 2), agreement.getCheckoutDate());
            assertEquals(25, agreement.getDiscountPercent());
            assertEquals(1.49f, agreement.getDailyRentalCharge());
            assertTrue(agreement.hasWeekdayCharge());
            assertFalse(agreement.hasWeekendCharge());
            assertTrue(agreement.hasHolidayCharge());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Test 4
    @Test
    void shouldNotChargeForJackhammerHolidaysOrWeekends1() {
        String toolCode = "JAKD";
        String rentalDays = "6";
        String checkoutDate = "9/3/15";
        String discountPercent = "0";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        Sr0724Application.main(args);

        String expectedOutput = """
                Tool code: JAKD
                Tool type: Jackhammer
                Tool brand: DeWalt
                Rental days: 6
                Check out date: 09/03/15
                Due date: 09/09/15
                Daily rental charge: $2.99
                Charge days: 3
                Pre-discount charge: $8.97
                Discount percent: 0%
                Discount amount: $0.00
                Final charge: $8.97
                """;
        assertTrue(outContent.toString().contains(expectedOutput));

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Assert there is only one rental agreement in rental_agreement and that it is the correct agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(1, agreements.size());

            RentalAgreement agreement = agreements.getFirst();
            assertEquals("JAKD", agreement.getTool().getCode());
            assertEquals(6, agreement.getRentalDays());
            assertEquals(LocalDate.of(2015, 9, 3), agreement.getCheckoutDate());
            assertEquals(0, agreement.getDiscountPercent());
            assertEquals(2.99f, agreement.getDailyRentalCharge());
            assertTrue(agreement.hasWeekdayCharge());
            assertFalse(agreement.hasWeekendCharge());
            assertFalse(agreement.hasHolidayCharge());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Test 5
    @Test
    void shouldNotChargeForJackhammerHolidaysOrWeekends2() {
        String toolCode = "JAKR";
        String rentalDays = "9";
        String checkoutDate = "7/2/15";
        String discountPercent = "0";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        Sr0724Application.main(args);

        String expectedOutput = """
                Tool code: JAKR
                Tool type: Jackhammer
                Tool brand: Ridgid
                Rental days: 9
                Check out date: 07/02/15
                Due date: 07/11/15
                Daily rental charge: $2.99
                Charge days: 5
                Pre-discount charge: $14.95
                Discount percent: 0%
                Discount amount: $0.00
                Final charge: $14.95
                """;
        assertTrue(outContent.toString().contains(expectedOutput));

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Assert there is only one rental agreement in rental_agreement and that it is the correct agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(1, agreements.size());

            RentalAgreement agreement = agreements.getFirst();
            assertEquals("JAKR", agreement.getTool().getCode());
            assertEquals(9, agreement.getRentalDays());
            assertEquals(LocalDate.of(2015, 7, 2), agreement.getCheckoutDate());
            assertEquals(0, agreement.getDiscountPercent());
            assertEquals(2.99f, agreement.getDailyRentalCharge());
            assertTrue(agreement.hasWeekdayCharge());
            assertFalse(agreement.hasWeekendCharge());
            assertFalse(agreement.hasHolidayCharge());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Test 6
    @Test
    void shouldNotChargeForJackhammerHolidaysOrWeekends3() {
        String toolCode = "JAKR";
        String rentalDays = "4";
        String checkoutDate = "7/2/20";
        String discountPercent = "50";
        String[] args = new String[] {toolCode, rentalDays, checkoutDate, discountPercent};

        Sr0724Application.main(args);

        String expectedOutput = """
                Tool code: JAKR
                Tool type: Jackhammer
                Tool brand: Ridgid
                Rental days: 4
                Check out date: 07/02/20
                Due date: 07/06/20
                Daily rental charge: $2.99
                Charge days: 1
                Pre-discount charge: $2.99
                Discount percent: 50%
                Discount amount: $1.50
                Final charge: $1.49
                """;
        assertTrue(outContent.toString().contains(expectedOutput));

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Assert there is only one rental agreement in rental_agreement and that it is the correct agreement
            List<RentalAgreement> agreements = session
                    .createQuery("from RentalAgreement", RentalAgreement.class)
                    .getResultList();
            assertEquals(1, agreements.size());

            RentalAgreement agreement = agreements.getFirst();
            assertEquals("JAKR", agreement.getTool().getCode());
            assertEquals(4, agreement.getRentalDays());
            assertEquals(LocalDate.of(2020, 7, 2), agreement.getCheckoutDate());
            assertEquals(50, agreement.getDiscountPercent());
            assertEquals(2.99f, agreement.getDailyRentalCharge());
            assertTrue(agreement.hasWeekdayCharge());
            assertFalse(agreement.hasWeekendCharge());
            assertFalse(agreement.hasHolidayCharge());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Clear all records from the rental_agreements table
            session.createMutationQuery("delete from RentalAgreement").executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.println(e.getMessage());
        }
    }
}
