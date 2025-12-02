package io.warmup.framework.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.Health;
import java.util.Map;

public class HealthController {

    private final WarmupContainer container;

    public HealthController(WarmupContainer container) {
        this.container = container;
    }

    @Health(name = "api.health", description = "API health endpoint")
    public HealthResult apiHealth() {
        return HealthResult.up("API is healthy")
                .withDetail("version", "1.0.0")
                .withDetail("timestamp", System.currentTimeMillis());
    }

    public String getHealthJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(container.getHealthStatus());
        } catch (JsonProcessingException e) {
            return "{\"error\": \"Failed to generate health JSON\"}";
        }
    }

    public String getHealthHtml() {
        Map<String, Object> status = container.getHealthStatus();
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head><title>Health Status</title>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                .append(".health-up { color: green; }")
                .append(".health-down { color: red; }")
                .append(".health-degraded { color: orange; }")
                .append("</style></head><body>")
                .append("<h1>System Health Status</h1>")
                .append("<p>Overall status: <span class='health-")
                .append(container.isHealthy() ? "up'>HEALTHY" : "down'>UNHEALTHY")
                .append("</span></p>");

        // Agregar detalles de cada health check...
        html.append("</body></html>");
        return html.toString();
    }
}
