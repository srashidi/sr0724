package com.housejunction.sr0724;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

@Entity
@Table(name = "rental_agreement")
public class RentalAgreement {
    private @Id
    @GeneratedValue
    @Column(name = "id")
    long id;

    @ManyToOne
    @JoinColumn(name = "tool_code", nullable = false)
    private Tool tool;

    @Column(name = "rental_days")
    @NotNull private int rentalDays;

    @Column(name = "checkout_date")
    @NotNull private LocalDate checkoutDate;

    @Column(name = "daily_rental_charge")
    @NotNull private float dailyRentalCharge;

    @Column(name = "weekday_charge")
    @NotNull private boolean weekdayCharge;

    @Column(name = "weekend_charge")
    @NotNull private boolean weekendCharge;

    @Column(name = "holiday_charge")
    @NotNull private boolean holidayCharge;

    @Column(name = "discount_percent")
    @NotNull private int discountPercent;

    public RentalAgreement() {}

    public RentalAgreement(Tool tool, int rentalDays, LocalDate checkoutDate, int discountPercent) {
        this.tool = tool;
        this.rentalDays = rentalDays;
        this.checkoutDate = checkoutDate;
        this.discountPercent = discountPercent;

        // Setting the daily rental charge and whether charges apply on weekdays,
        // weekends, and/or holidays will maintain the price originally agreed on,
        // even if the price changes in the tool_pricing table in the future
        ToolPricing pricing = tool.getToolPricing();
        this.dailyRentalCharge = pricing.getDailyCharge();
        this.weekdayCharge = pricing.hasWeekdayCharge();
        this.weekendCharge = pricing.hasWeekendCharge();
        this.holidayCharge = pricing.hasHolidayCharge();
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
        return checkoutDate.plusDays(rentalDays);
    }

    public float getDailyRentalCharge() {
        return dailyRentalCharge;
    }

    public boolean hasWeekdayCharge() {
        return weekdayCharge;
    }

    public boolean hasWeekendCharge() {
        return weekendCharge;
    }

    public boolean hasHolidayCharge() {
        return holidayCharge;
    }

    // Calculate chargeDays by identifying non-charge days and subtracting those from rental days
    public int getChargeDays() {
        LocalDate dueDate = getDueDate();

        int nonChargeDays = 0;

        // Check which weekdays or weekends fall within the period
        if (!this.weekdayCharge || !this.weekendCharge) {
            LocalDate date = this.checkoutDate;
            HashMap<String, Integer> dayOfWeekCounts = new HashMap<>();

            String weekdays = "weekdays";
            String weekends = "weekends";
            dayOfWeekCounts.put(weekdays, 0);
            dayOfWeekCounts.put(weekends, 0);

            for (int i = 0; i < this.rentalDays; i++) {
                date = date.plusDays(1);
                DayOfWeek dayOfWeek = date.getDayOfWeek();

                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    dayOfWeekCounts.put(weekends, dayOfWeekCounts.get(weekends) + 1);
                } else {
                    dayOfWeekCounts.put(weekdays, dayOfWeekCounts.get(weekdays) + 1);
                }
            }

            if (!this.weekdayCharge) {
                nonChargeDays += dayOfWeekCounts.get(weekdays);
            }

            if (!this.weekendCharge) {
                nonChargeDays += dayOfWeekCounts.get(weekends);
            }
        }

        // Check which holidays (Independence Day or Labor Day) fall within the period
        if (!this.holidayCharge) {
            int yearDiff = dueDate.getYear() - this.checkoutDate.getYear();
            for (int i = 0; i < yearDiff + 1; i++) {
                int thisYear = this.checkoutDate.plusYears(i).getYear();

                LocalDate independenceDay = LocalDate.of(thisYear, 7, 4);

                if (independenceDay.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    independenceDay = independenceDay.minusDays(1);
                } else if (independenceDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    independenceDay = independenceDay.plusDays(1);
                }

                if (independenceDay.isAfter(this.checkoutDate)
                        && independenceDay.plusDays(1).isBefore(dueDate)) {
                    nonChargeDays += 1;
                }

                LocalDate laborDay = LocalDate.of(thisYear, 9, 1).with(firstInMonth(DayOfWeek.MONDAY));

                if (laborDay.isAfter(this.checkoutDate) && laborDay.plusDays(1).isBefore(dueDate)) {
                    nonChargeDays += 1;
                }
            }
        }

        return this.rentalDays - nonChargeDays;
    }

    public float getPreDiscountCharge() {
        return dailyRentalCharge * getChargeDays();
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public float getDiscountAmount() {
        BigDecimal unroundedAmount = new BigDecimal(getPreDiscountCharge() * discountPercent / 100);
        return unroundedAmount.setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    public float getFinalCharge() {
        return getPreDiscountCharge() - getDiscountAmount();
    }

    public void print() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yy");
        DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");

        System.out.printf("Tool code: %s%n", this.tool.getCode());
        System.out.printf("Tool type: %s%n", this.tool.getType());
        System.out.printf("Tool brand: %s%n", this.tool.getBrand());
        System.out.printf("Rental days: %d%n", this.rentalDays);
        System.out.printf("Check out date: %s%n", this.checkoutDate.format(dtf));
        System.out.printf("Due date: %s%n", getDueDate().format(dtf));
        System.out.printf("Daily rental charge: $%s%n", currencyFormat.format(this.dailyRentalCharge));
        System.out.printf("Charge days: %d%n", getChargeDays());
        System.out.printf("Pre-discount charge: $%s%n", currencyFormat.format(getPreDiscountCharge()));
        System.out.printf("Discount percent: %d%%%n", this.discountPercent);
        System.out.printf("Discount amount: $%s%n", currencyFormat.format(getDiscountAmount()));
        System.out.printf("Final charge: $%s%n", currencyFormat.format(getFinalCharge()));
    }

}
