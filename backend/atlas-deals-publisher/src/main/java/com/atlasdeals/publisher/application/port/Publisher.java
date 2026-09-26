package com.atlasdeals.publisher.application.port;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;

public interface Publisher {
    String getPlatform();
    PublishResult publish(PublishRequest request);
    boolean isAvailable();
}
