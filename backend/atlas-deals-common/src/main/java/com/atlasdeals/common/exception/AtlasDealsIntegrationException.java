package com.atlasdeals.common.exception;

public class AtlasDealsIntegrationException extends AtlasDealsException {
    private final String integrationName;
    private final int statusCode;

    public AtlasDealsIntegrationException(String integrationName, String message) {
        super(message, "INTEGRATION_ERROR");
        this.integrationName = integrationName;
        this.statusCode = 0;
    }

    public AtlasDealsIntegrationException(String integrationName, String message, int statusCode) {
        super(message, "INTEGRATION_ERROR");
        this.integrationName = integrationName;
        this.statusCode = statusCode;
    }

    public AtlasDealsIntegrationException(String integrationName, String message, Throwable cause) {
        super(message, "INTEGRATION_ERROR", cause);
        this.integrationName = integrationName;
        this.statusCode = 0;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
