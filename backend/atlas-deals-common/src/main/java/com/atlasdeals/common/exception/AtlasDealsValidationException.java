package com.atlasdeals.common.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AtlasDealsValidationException extends AtlasDealsException {
    private final List<String> errors;

    public AtlasDealsValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.errors = List.of(message);
    }

    public AtlasDealsValidationException(String message, List<String> errors) {
        super(message, "VALIDATION_ERROR");
        this.errors = errors != null ? Collections.unmodifiableList(new ArrayList<>(errors)) : List.of();
    }

    public AtlasDealsValidationException(String message, String field, String error) {
        super(message, "VALIDATION_ERROR");
        this.errors = List.of(field + ": " + error);
    }

    public List<String> getErrors() {
        return errors;
    }
}
