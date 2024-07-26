import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name="tool_pricing")
public class ToolPricing {
    private @Id
    @Column(name = "type", unique = true)
    final String type;

    @Column(name = "daily_charge")
    private final float dailyCharge;

    @Column(name = "weekday_charge")
    private final boolean weekdayCharge;

    @Column(name = "weekend_charge")
    private final boolean weekendCharge;

    @Column(name = "holiday_charge")
    private final boolean holidayCharge;

    public ToolPricing() {}

    public ToolPricing(String type, float dailyCharge,
                       boolean weekdayCharge, boolean weekendCharge, boolean holidayCharge) {
        this.type = type;
        this.dailyCharge = dailyCharge;
        this.weekdayCharge = weekdayCharge;
        this.weekendCharge = weekendCharge;
        this.holidayCharge = holidayCharge;
    }

    public String getType() {
        return type;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof ToolPricing toolPricing))
            return false;

        return Objects.equals(this.type, toolPricing.type)
                && Objects.equals(this.dailyCharge, toolPricing.dailyCharge)
                && Objects.equals(this.weekdayCharge, toolPricing.weekdayCharge)
                && Objects.equals(this.weekendCharge, toolPricing.weekendCharge)
                && Objects.equals(this.holidayCharge, toolPricing.holidayCharge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type, this.dailyCharge,
                this.weekdayCharge, this.weekendCharge, this.holidayCharge);
    }

    @Override
    public String toString() {
        return String.format("ToolPricing: {type=\"%s\", dailyCharge=\"%.2f\"," +
                        " weekdayCharge=\"%s\", weekendCharge=\"%s\", holidayCharge=\"%s\"}",
                this.type, this.dailyCharge,
                this.weekdayCharge, this.weekendCharge, this.holidayCharge);
    }
}
