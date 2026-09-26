package com.atlasdeals.aiengine.application.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final Map<UUID, Map<String, Object>> sessions = new ConcurrentHashMap<>();
    private final List<AiAgent> agents;

    public SessionManager(List<AiAgent> agents) {
        this.agents = agents;
    }

    public Map<String, Object> createSession(UUID productId, Map<String, Object> initialContext) {
        Map<String, Object> context = new HashMap<>(initialContext);
        context.put("sessionId", productId.toString());
        context.put("productId", productId);
        sessions.put(productId, context);
        log.info("Session created for product: {}", productId);
        return context;
    }

    public Map<String, Object> orchestrate(UUID productId, Map<String, Object> context) {
        log.info("Orchestrating agents for product: {}", productId);

        Map<String, Object> mergedContext = new HashMap<>(context);

        for (AiAgent agent : agents) {
            try {
                log.info("Running agent: {}", agent.getAgentType());
                @SuppressWarnings("unchecked")
                Map<String, Object> agentResult = agent.execute(mergedContext, Map.class);
                if (agentResult != null) {
                    mergedContext.putAll(agentResult);
                    mergedContext.put(agent.getAgentType().name() + "_RESULT", agentResult);
                }
            } catch (Exception e) {
                log.error("Agent {} failed: {}", agent.getAgentType(), e.getMessage(), e);
                mergedContext.put(agent.getAgentType().name() + "_ERROR", e.getMessage());
            }
        }

        sessions.put(productId, mergedContext);
        return mergedContext;
    }

    public Map<String, Object> getSession(UUID productId) {
        return sessions.getOrDefault(productId, Collections.emptyMap());
    }

    public Map<UUID, Map<String, Object>> getAllSessions() {
        return Collections.unmodifiableMap(sessions);
    }

    public void clearSession(UUID productId) {
        sessions.remove(productId);
        log.info("Session cleared for product: {}", productId);
    }
}
