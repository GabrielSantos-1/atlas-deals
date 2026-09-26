package com.atlasdeals.common.domain;

import java.net.URL;
import java.util.Objects;

public class Store extends AggregateRoot<StoreId> {
    private final String name;
    private final String slug;
    private final URL baseUrl;
    private final URL logoUrl;
    private final AffiliateNetwork affiliateNetwork;
    private final String affiliateProgramId;
    private final boolean active;
    private final String country;

    public Store(StoreId id, String name, String slug, URL baseUrl, URL logoUrl,
                 AffiliateNetwork affiliateNetwork, String affiliateProgramId,
                 boolean active, String country) {
        super(id);
        this.name = Objects.requireNonNull(name, "Store name must not be null");
        this.slug = Objects.requireNonNull(slug, "Store slug must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "Store baseUrl must not be null");
        this.logoUrl = logoUrl;
        this.affiliateNetwork = Objects.requireNonNull(affiliateNetwork, "AffiliateNetwork must not be null");
        this.affiliateProgramId = affiliateProgramId;
        this.active = active;
        this.country = Objects.requireNonNull(country, "Store country must not be null");
    }

    public static Store create(StoreId id, String name, String slug, URL baseUrl, URL logoUrl,
                                AffiliateNetwork affiliateNetwork, String affiliateProgramId,
                                String country) {
        return new Store(id, name, slug, baseUrl, logoUrl, affiliateNetwork, affiliateProgramId, true, country);
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public URL getBaseUrl() { return baseUrl; }
    public URL getLogoUrl() { return logoUrl; }
    public AffiliateNetwork getAffiliateNetwork() { return affiliateNetwork; }
    public String getAffiliateProgramId() { return affiliateProgramId; }
    public boolean isActive() { return active; }
    public String getCountry() { return country; }
}
