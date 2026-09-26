package com.atlasdeals.common.application.port;

public interface IdempotencyService {
    boolean isProcessed(String idempotencyKey);
    void markProcessed(String idempotencyKey);
}
