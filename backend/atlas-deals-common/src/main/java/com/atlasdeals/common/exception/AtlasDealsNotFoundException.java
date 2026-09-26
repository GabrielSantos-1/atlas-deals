package com.atlasdeals.common.exception;

public class AtlasDealsNotFoundException extends AtlasDealsException {
    public AtlasDealsNotFoundException(String message) {
        super(message, "NOT_FOUND");
    }

    public AtlasDealsNotFoundException(String entityType, Object entityId) {
        super(entityType + " not found: " + entityId, "NOT_FOUND");
    }

    public AtlasDealsNotFoundException(String message, Throwable cause) {
        super(message, "NOT_FOUND", cause);
    }
}
