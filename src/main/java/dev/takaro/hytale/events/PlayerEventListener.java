package dev.takaro.hytale.events;

import dev.takaro.hytale.TakaroPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for player events from Hytale and forwards them to Takaro
 *
 * TODO: This needs to be updated with actual Hytale event API once available
 * Look for: PlayerJoinEvent, PlayerQuitEvent, PlayerDeathEvent, etc.
 */
public class PlayerEventListener {
    private final TakaroPlugin plugin;

    public PlayerEventListener(TakaroPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player connect event
     *
     * In Hytale, this might look like:
     * @EventHandler
     * public void onPlayerJoin(PlayerJoinEvent event) { ... }
     */
    public void handlePlayerConnect(String playerName, String gameId, String steamId) {
        plugin.getLogger().info("[EVENT] Player connected: " + playerName);

        if (!plugin.getWebSocket().isIdentified()) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "player-connected");

        Map<String, String> player = new HashMap<>();
        player.put("name", playerName);
        player.put("gameId", gameId);
        player.put("steamId", steamId);
        eventData.put("player", player);

        plugin.getWebSocket().sendGameEvent("player-connected", eventData);
    }

    /**
     * Handle player disconnect event
     */
    public void handlePlayerDisconnect(String playerName, String gameId, String steamId) {
        plugin.getLogger().info("[EVENT] Player disconnected: " + playerName);

        if (!plugin.getWebSocket().isIdentified()) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "player-disconnected");

        Map<String, String> player = new HashMap<>();
        player.put("name", playerName);
        player.put("gameId", gameId);
        player.put("steamId", steamId);
        eventData.put("player", player);

        plugin.getWebSocket().sendGameEvent("player-disconnected", eventData);
    }

    /**
     * Handle player death event
     */
    public void handlePlayerDeath(String playerName, String gameId, String steamId) {
        plugin.getLogger().info("[EVENT] Player died: " + playerName);

        if (!plugin.getWebSocket().isIdentified()) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", "player-death");

        Map<String, String> player = new HashMap<>();
        player.put("name", playerName);
        player.put("gameId", gameId);
        player.put("steamId", steamId);
        eventData.put("player", player);

        plugin.getWebSocket().sendGameEvent("player-death", eventData);
    }

    public void register() {
        // TODO: Register this listener with Hytale's event system
        plugin.getLogger().info("Player event listener registered");
    }
}
