package com.housejunction.sr0724;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    void shouldNotAcceptDiscountOutsideOfRange() {
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

        String expectedOutput = "Tool code: LADW\nTool type: Ladder\nTool brand: Werner\nRental days: 3\n" +
                "Check out date: 07/02/20\nDue date: 07/05/20\nDaily rental charge: $1.99\nCharge days: 2\n" +
                "Pre-discount charge: $3.98\nDiscount percent: 10%\nDiscount amount: $0.40\nFinal charge: $3.58\n";
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
