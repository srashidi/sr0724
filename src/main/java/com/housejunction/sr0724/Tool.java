package com.housejunction.sr0724;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tool")
public class Tool {
    private @Id
    @Column(name = "code", unique = true)
    @NotNull String code;

    @ManyToOne
    private ToolPricing toolPricing;

    @Column(name = "brand")
    @NotNull private String brand;

    @OneToMany(targetEntity = RentalAgreement.class)
    private List<RentalAgreement> rentalAgreements = new ArrayList<>();

    public Tool() {}

    public Tool(String code, ToolPricing toolPricing, String brand) {
        this.code = code;
        this.toolPricing = toolPricing;
        this.brand = brand;
    }

    public String getCode() {
        return code;
    }

    public ToolPricing getToolPricing() {
        return toolPricing;
    }

    public String getType() {
        return toolPricing.getToolType();
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public String toString() {
        return String.format("com.housejunction.sr0724.Tool: {code=\"%s\", type=\"%s\", brand=\"%s\"}",
                this.code, this.getType(), this.brand);
    }
}
