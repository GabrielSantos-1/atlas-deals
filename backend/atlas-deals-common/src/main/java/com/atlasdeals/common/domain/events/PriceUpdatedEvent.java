package com.atlasdeals.common.domain.events;

import com.atlasdeals.common.domain.BaseDomainEvent;
import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import java.time.Instant;

public class PriceUpdatedEvent extends BaseDomainEvent {
    private final ProductId productId;
    private final Money previousPrice;
    private final Money currentPrice;
    private final Instant updatedAt;

    public PriceUpdatedEvent(ProductId productId, Money previousPrice, Money currentPrice, Instant updatedAt) {
        this.productId = productId;
        this.previousPrice = previousPrice;
        this.currentPrice = currentPrice;
        this.updatedAt = updatedAt;
    }

    @Override
    public String getEventType() { return "price.updated"; }

    public ProductId getProductId() { return productId; }
    public Money getPreviousPrice() { return previousPrice; }
    public Money getCurrentPrice() { return currentPrice; }
    public Instant getUpdatedAt() { return updatedAt; }
}
