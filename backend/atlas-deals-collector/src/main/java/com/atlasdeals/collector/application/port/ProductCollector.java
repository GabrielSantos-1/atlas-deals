package com.atlasdeals.collector.application.port;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.dto.ConnectorConfig;

public interface ProductCollector {
    ConnectorConfig getConfig();
    CollectionResult collect();
    boolean supports(String storeName);
}
