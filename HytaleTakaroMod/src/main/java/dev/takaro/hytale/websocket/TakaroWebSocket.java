package dev.takaro.hytale.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.takaro.hytale.TakaroPlugin;
import dev.takaro.hytale.config.TakaroConfig;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TakaroWebSocket extends WebSocketClient {
    private final TakaroPlugin plugin;
    private final TakaroConfig config;
    private final Gson gson;
    private boolean isIdentified = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_DELAY = 60000; // 60 seconds
    private static final int BASE_RECONNECT_DELAY = 3000; // 3 seconds
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TakaroWebSocket(TakaroPlugin plugin, TakaroConfig config) throws Exception {
        super(new URI(config.getWsUrl()));
        this.plugin = plugin;
        this.config = config;
        this.gson = new Gson();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Connected to Takaro WebSocket");
        reconnectAttempts = 0;
        sendIdentify();
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.get("type").getAsString();

            switch (type) {
                case "identifyResponse":
                    handleIdentifyResponse(json);
                    break;
                case "connected":
                    plugin.getLogger().at(java.util.logging.Level.INFO).log("Takaro confirmed connection");
                    break;
                case "request":
                    handleTakaroRequest(json);
                    break;
                case "ping":
                    sendPong();
                    break;
                case "error":
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Takaro error: " + json.toString());
                    // Reconnect when Takaro sends error - backend may be in bad state
                    plugin.getLogger().at(java.util.logging.Level.WARNING).log("Reconnecting to Takaro due to error message...");
                    isIdentified = false;
                    close();
                    scheduleReconnect();
                    break;
                default:
                    plugin.getLogger().at(java.util.logging.Level.WARNING).log("Unknown message type from Takaro: " + type);
            }
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        plugin.getLogger().at(java.util.logging.Level.WARNING).log("Disconnected from Takaro: " + reason);
        isIdentified = false;
        scheduleReconnect();
    }

    @Override
    public void onError(Exception ex) {
        plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Takaro WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }

    private void sendIdentify() {
        Map<String, Object> identify = new HashMap<>();
        identify.put("type", "identify");

        Map<String, String> payload = new HashMap<>();
        payload.put("identityToken", config.getIdentityToken());
        if (!config.getRegistrationToken().isEmpty()) {
            payload.put("registrationToken", config.getRegistrationToken());
        }

        identify.put("payload", payload);

        plugin.getLogger().at(java.util.logging.Level.INFO).log("Sending identify message to Takaro");
        send(gson.toJson(identify));
    }

    private void handleIdentifyResponse(JsonObject message) {
        JsonObject payload = message.getAsJsonObject("payload");
        if (payload.has("error")) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Identification failed: " + payload.get("error").toString());
        } else {
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Successfully identified with Takaro");
            isIdentified = true;
        }
    }

    private void handleTakaroRequest(JsonObject message) {
        String requestId = message.get("requestId").getAsString();
        JsonObject payload = message.getAsJsonObject("payload");
        String action = payload.get("action").getAsString();

        plugin.getLogger().at(java.util.logging.Level.FINE).log("Received Takaro request: " + action);

        // Delegate to plugin's request handler
        plugin.handleTakaroRequest(requestId, action, payload);
    }

    private void sendPong() {
        Map<String, String> pong = new HashMap<>();
        pong.put("type", "pong");
        send(gson.toJson(pong));
    }

    public void sendToTakaro(Map<String, Object> message) {
        if (!isOpen()) {
            plugin.getLogger().at(java.util.logging.Level.WARNING).log("Cannot send to Takaro - not connected");
            return;
        }
        send(gson.toJson(message));
    }

    public void sendResponse(String requestId, Object payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "response");
        response.put("requestId", requestId);
        response.put("payload", payload);
        sendToTakaro(response);
    }

    public void sendGameEvent(String eventType, Map<String, Object> data) {
        if (!isIdentified) {
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("type", "gameEvent");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);
        payload.put("data", data);

        event.put("payload", payload);

        sendToTakaro(event);
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        int exponentialDelay = Math.min(BASE_RECONNECT_DELAY * (int)Math.pow(2, reconnectAttempts - 1), MAX_RECONNECT_DELAY);
        int jitter = (int)(Math.random() * exponentialDelay * 0.25);
        int delayMs = exponentialDelay + jitter;

        plugin.getLogger().at(java.util.logging.Level.INFO).log("Scheduling reconnect attempt " + reconnectAttempts + " in " + (delayMs / 1000) + "s");

        scheduler.schedule(() -> {
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Attempting to reconnect to Takaro...");
            try {
                reconnect();
            } catch (Exception e) {
                plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Reconnect failed: " + e.getMessage());
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    public boolean isIdentified() {
        return isIdentified;
    }

    public void shutdown() {
        scheduler.shutdownNow();
        close();
    }
}
