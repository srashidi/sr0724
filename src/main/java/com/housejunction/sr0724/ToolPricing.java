package com.housejunction.sr0724;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="tool_pricing")
public class ToolPricing {
    private @Id
    @Column(name = "tool_type", unique = true)
    @NotNull String toolType;

    @Column(name = "daily_charge")
    @NotNull
    private float dailyCharge;

    @Column(name = "weekday_charge")
    @NotNull private boolean weekdayCharge;

    @Column(name = "weekend_charge")
    @NotNull private boolean weekendCharge;

    @Column(name = "holiday_charge")
    @NotNull
    private boolean holidayCharge;

    public ToolPricing() {}

    public ToolPricing(String type, float dailyCharge,
                       boolean weekdayCharge, boolean weekendCharge, boolean holidayCharge) {
        this.toolType = type;
        this.dailyCharge = dailyCharge;
        this.weekdayCharge = weekdayCharge;
        this.weekendCharge = weekendCharge;
        this.holidayCharge = holidayCharge;
    }

    public String getToolType() {
        return toolType;
    }

    public float getDailyCharge() {
        return dailyCharge;
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

    @Override
    public String toString() {
        return String.format("ToolPricing: {type=\"%s\", dailyCharge=\"%.2f\"," +
                        " weekdayCharge=\"%s\", weekendCharge=\"%s\", holidayCharge=\"%s\"}",
                this.toolType, this.dailyCharge,
                this.weekdayCharge, this.weekendCharge, this.holidayCharge);
    }
}
