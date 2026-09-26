package com.atlasdeals.affiliateengine.application.dto;

import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class RegisterConversionRequest {

    @PositiveOrZero(message = "revenue must be positive or zero")
    private BigDecimal revenue;

    public RegisterConversionRequest() {
    }

    public RegisterConversionRequest(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
