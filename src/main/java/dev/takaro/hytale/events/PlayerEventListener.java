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

    public PlayerEventListener(TakaroPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player connect events
     * Registered via: getEventRegistry().registerGlobal(PlayerConnectEvent.class, this::onPlayerConnect)
     */
    public void onPlayerConnect(PlayerConnectEvent event) {
        try {
            // Extract player data
            String playerName = event.getPlayerRef().getUsername();
            String uuid = event.getPlayerRef().getUuid().toString();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("[EVENT] Player connected: " + playerName);

            // Don't forward if not connected to Takaro
            if (!plugin.getWebSocket().isIdentified()) {
                return;
            }

            // Build connect event for Takaro
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("type", "player-connected");

            Map<String, String> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("steamId", uuid); // Using UUID as steamId

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
     * Registered via: getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect)
     */
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            // Extract player data
            String playerName = event.getPlayerRef().getUsername();
            String uuid = event.getPlayerRef().getUuid().toString();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("[EVENT] Player disconnected: " + playerName);

            // Don't forward if not connected to Takaro
            if (!plugin.getWebSocket().isIdentified()) {
                return;
            }

            // Build disconnect event for Takaro
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("type", "player-disconnected");

            Map<String, String> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("steamId", uuid); // Using UUID as steamId

            eventData.put("player", player);

            // Send to Takaro
            plugin.getWebSocket().sendGameEvent("player-disconnected", eventData);
            plugin.getLogger().at(java.util.logging.Level.FINE).log("Forwarded player disconnect to Takaro");

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling player disconnect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
