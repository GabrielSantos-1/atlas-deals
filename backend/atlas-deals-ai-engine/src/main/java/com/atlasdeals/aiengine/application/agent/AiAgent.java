package com.atlasdeals.aiengine.application.agent;

import com.atlasdeals.aiengine.domain.AgentType;
import java.util.Map;

public interface AiAgent {

    AgentType getAgentType();

    <T> T execute(Map<String, Object> context, Class<T> responseType);
}
