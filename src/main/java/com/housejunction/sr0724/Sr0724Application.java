package com.housejunction.sr0724;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Sr0724Application {
    public static void main(String[] args) {
        if (args.length != 4) {
            throw new RuntimeException("There should be 4 args: tool code, rental days, checkout date, and discount percent.");
        }

        String toolCode = args[0];
        int rentalDays;
        LocalDate checkoutDate;
        int discountPercent;

        try {
            rentalDays = Integer.parseInt(args[1]);
        } catch (Exception e) {
            throw new RuntimeException("Something is wrong with the rental days arg." +
                    " Make sure it is an integer value.");
        }

        try {
            checkoutDate = LocalDate.parse(args[2], DateTimeFormatter.ofPattern("M/d/yy"));
        } catch (Exception e) {
            throw new RuntimeException("Something is wrong with the checkout date arg." +
                    " Make sure it is in the following format: \"mm/dd/yy\" (ex, \"3/15/20\")");
        }

        try {
            discountPercent = Integer.parseInt(args[3]);
        } catch (Exception e) {
            throw new RuntimeException("Something is wrong with the discount percent arg." +
                    " Make sure it is an integer value.");
        }

        checkout(toolCode, rentalDays, checkoutDate, discountPercent);
    }

    public static void loadDatabase() {
        ToolPricing pricing_ladder = new ToolPricing("Ladder", 1.99f,
                true, true, false);
        ToolPricing pricing_chainsaw = new ToolPricing("Chainsaw", 1.49f,
                true, false, true);
        ToolPricing pricing_jackhammer = new ToolPricing("Jackhammer", 2.99f,
                true, false, false);

        Tool tool1 = new Tool("CHNS", pricing_chainsaw, "Stihl");
        Tool tool2 = new Tool("LADW", pricing_ladder, "Werner");
        Tool tool3 = new Tool("JAKD", pricing_jackhammer, "DeWalt");
        Tool tool4 = new Tool("JAKR", pricing_jackhammer, "Ridgid");

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Start a transaction
            transaction = session.beginTransaction();

            // Add the tool pricings to the database
            session.persist(pricing_ladder);
            session.persist(pricing_chainsaw);
            session.persist(pricing_jackhammer);

            // Add the tools to the database
            session.persist(tool1);
            session.persist(tool2);
            session.persist(tool3);
            session.persist(tool4);

            // Commit transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.println(e.getMessage());
        }
    }

    private static void checkout(String toolCode, int rentalDays, LocalDate checkoutDate, int discountPercent) {
        if (rentalDays < 1) {
            throw new RuntimeException("Rental days must be 1 or greater.");
        }

        if (discountPercent < 0 || discountPercent > 100) {
            throw new RuntimeException("Discount percent must be in the range 0 to 100, inclusive.");
        }

        Transaction transaction = null;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Tool tool = session.get(Tool.class, toolCode);
            RentalAgreement agreement = new RentalAgreement(tool, rentalDays, checkoutDate, discountPercent);

            // Add rental agreement to the database
            transaction = session.beginTransaction();
            session.persist(agreement);
            transaction.commit();

            // Print the agreement details to the console
            agreement.print();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
