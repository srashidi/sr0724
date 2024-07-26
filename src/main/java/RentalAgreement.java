import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class RentalAgreement {
    private final Tool tool;
    private final int rentalDays;
    private final LocalDate checkoutDate;
    private final LocalDate dueDate;
    private final float dailyRentalCharge;
    private final int chargeDays;
    private final float preDiscountCharge;
    private final int discountPercent;
    private final float discountAmount;
    private final float finalCharge;

    public RentalAgreement(Tool tool, ToolPricing pricing, int rentalDays, LocalDate checkoutDate, int discountPercent) {
        this.tool = tool;
        this.rentalDays = rentalDays;
        this.checkoutDate = checkoutDate;
        dueDate = this.checkoutDate.plusDays(this.rentalDays);

        // Calculate charge days, pre-discount charge, discount amount, and final charge
        dailyRentalCharge = pricing.getDailyCharge();
        chargeDays = calculateChargeDays(pricing, this.rentalDays, this.checkoutDate, this.dueDate);
        preDiscountCharge = dailyRentalCharge * chargeDays;
        this.discountPercent = discountPercent;
        discountAmount = preDiscountCharge * discountPercent / 100;
        finalCharge = preDiscountCharge - discountAmount;
    }

    public Tool getTool() {
        return tool;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public float getDailyRentalCharge() {
        return dailyRentalCharge;
    }

    public int getChargeDays() {
        return chargeDays;
    }

    public float getPreDiscountCharge() {
        return preDiscountCharge;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public float getDiscountAmount() {
        return discountAmount;
    }

    public float getFinalCharge() {
        return finalCharge;
    }

    public void print() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy");
        DecimalFormat currencyFormat = new DecimalFormat("#.##");

        System.out.printf("Tool code: %s%n", this.tool.getCode());
        System.out.printf("Tool type: %s%n", this.tool.getType());
        System.out.printf("Tool brand: %s%n", this.tool.getBrand());
        System.out.printf("Rental days: %d%n", this.rentalDays);
        System.out.printf("Check out date: %s%n", this.checkoutDate.format(dtf));
        System.out.printf("Due date: %s%n", this.dueDate.format(dtf));
        System.out.printf("Daily rental charge: $%s%n", currencyFormat.format(this.dailyRentalCharge));
        System.out.printf("Charge days: %d%n", this.chargeDays);
        System.out.printf("Pre-discount charge: $%s%n", currencyFormat.format(this.preDiscountCharge));
        System.out.printf("Discount percent: %d%%n", this.discountPercent);
        System.out.printf("Discount amount: $%s%n", currencyFormat.format(this.discountAmount));
        System.out.printf("Final charge: $%s%n", currencyFormat.format(this.finalCharge));
    }

    // Calculate chargeDays by identifying non-charge days and subtracting those from rental days
    private int calculateChargeDays(ToolPricing pricing, int rentalDays, LocalDate checkoutDate, LocalDate dueDate) {
        boolean weekdayCharge = pricing.hasWeekdayCharge();
        boolean weekendCharge = pricing.hasWeekendCharge();
        boolean holidayCharge = pricing.hasHolidayCharge();

        int nonChargeDays = 0;

        // Check which weekdays or weekends fall within the period
        if (!weekdayCharge || !weekendCharge) {
            LocalDate date = checkoutDate;
            HashMap<String, Integer> dayOfWeekCounts = new HashMap<>();

            String weekdays = "weekdays";
            String weekends = "weekends";
            dayOfWeekCounts.put(weekdays, 0);
            dayOfWeekCounts.put(weekends, 0);

            for (int i = 0; i < rentalDays; i++) {
                date = date.plusDays(1);
                DayOfWeek dayOfWeek = date.getDayOfWeek();

                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    dayOfWeekCounts.put(weekends, dayOfWeekCounts.get(weekends) + 1);
                } else {
                    dayOfWeekCounts.put(weekdays, dayOfWeekCounts.get(weekdays) + 1);
                }
            }

            if (!weekdayCharge) {
                nonChargeDays += dayOfWeekCounts.get(weekdays);
            }

            if (!weekendCharge) {
                nonChargeDays += dayOfWeekCounts.get(weekends);
            }
        }

        // Check which holidays (Independence Day or Labor Day) fall within the period
        if (!holidayCharge) {
            int yearDiff = dueDate.getYear() - checkoutDate.getYear();
            for (int i = 0; i < yearDiff + 1; i++) {
                int thisYear = checkoutDate.plusYears(i).getYear();

                LocalDate independenceDay = LocalDate.of(thisYear, 7, 4);

                if (independenceDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    independenceDay = independenceDay.minusDays(1);
                } else if (independenceDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    independenceDay = independenceDay.plusDays(1);
                }

                if (independenceDay.isAfter(checkoutDate)
                        && independenceDay.plusDays(1).isBefore(dueDate)) {
                    nonChargeDays += 1;
                }

                LocalDate laborDay = LocalDate.of(thisYear, 9, 1).with(firstInMonth(DayOfWeek.MONDAY));

                if (laborDay.isAfter(checkoutDate) && laborDay.plusDays(1).isBefore(dueDate)) {
                    nonChargeDays += 1;
                }
            }
        }

        return rentalDays - nonChargeDays;
    }
}
