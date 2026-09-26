package com.atlasdeals.affiliateengine.infrastructure.connector;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAffiliateConnector {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected String encodeParam(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    protected String buildUrl(String base, String... params) {
        if (params.length == 0 || params.length % 2 != 0) {
            return base;
        }
        String query = Stream.iterate(0, i -> i < params.length, i -> i + 2)
            .map(i -> encodeParam(params[i]) + "=" + encodeParam(params[i + 1]))
            .collect(Collectors.joining("&"));
        return base + "?" + query;
    }
}
