package com.atlasdeals.common.exception;

public class AtlasDealsException extends RuntimeException {
    private final String errorCode;

    public AtlasDealsException(String message) {
        super(message);
        this.errorCode = "ATLAS_DEALS_ERROR";
    }

    public AtlasDealsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AtlasDealsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ATLAS_DEALS_ERROR";
    }

    public AtlasDealsException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
