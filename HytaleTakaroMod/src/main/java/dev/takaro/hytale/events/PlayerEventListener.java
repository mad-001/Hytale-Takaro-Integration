package dev.takaro.hytale.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import dev.takaro.hytale.TakaroPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for player events from Hytale and forwards them to Takaro
 * Uses official Hytale event pattern
 */
public class PlayerEventListener {
    private final TakaroPlugin plugin;
    private final Map<String, Long> lastDisconnectTime = new HashMap<>();
    private static final long DISCONNECT_COOLDOWN_MS = 5000; // 5 seconds

    public PlayerEventListener(TakaroPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player connect events
     */
    public void onPlayerConnect(PlayerConnectEvent event) {
        try {
            // Extract player data
            String playerName = event.getPlayerRef().getUsername();
            String uuid = event.getPlayerRef().getUuid().toString();

            plugin.getLogger().at(java.util.logging.Level.FINE).log("[EVENT] Player connected: " + playerName);

            // Don't forward if not connected to Takaro
            if (!plugin.getWebSocket().isIdentified()) {
                return;
            }

            // Build connect event for Takaro
            Map<String, Object> eventData = new HashMap<>();

            Map<String, Object> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("platformId", "hytale:" + uuid);
            // NOTE: Do NOT include position data - Takaro rejects it with whitelistValidation error

            eventData.put("player", player);

            // Send to Takaro
            plugin.getWebSocket().sendGameEvent("player-connected", eventData);
            plugin.getLogger().at(java.util.logging.Level.FINE).log("Forwarded player connect to Takaro");

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling player connect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle player disconnect events
     */
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            // Extract player data
            String playerName = event.getPlayerRef().getUsername();
            String uuid = event.getPlayerRef().getUuid().toString();

            plugin.getLogger().at(java.util.logging.Level.FINE).log("[EVENT] Player disconnected: " + playerName);

            // Deduplicate - Hytale fires PlayerDisconnectEvent multiple times
            long currentTime = System.currentTimeMillis();
            Long lastTime = lastDisconnectTime.get(uuid);
            if (lastTime != null && (currentTime - lastTime) < DISCONNECT_COOLDOWN_MS) {
                plugin.getLogger().at(java.util.logging.Level.INFO).log("Ignoring duplicate disconnect event for: " + playerName);
                return;
            }
            lastDisconnectTime.put(uuid, currentTime);

            // Don't forward if not connected to Takaro
            if (!plugin.getWebSocket().isIdentified()) {
                return;
            }

            // Build disconnect event for Takaro
            Map<String, Object> eventData = new HashMap<>();

            Map<String, Object> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("platformId", "hytale:" + uuid);
            // NOTE: Do NOT include position data - Takaro rejects it with whitelistValidation error

            eventData.put("player", player);

            // Send to Takaro
            plugin.getWebSocket().sendGameEvent("player-disconnected", eventData);

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling player disconnect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
