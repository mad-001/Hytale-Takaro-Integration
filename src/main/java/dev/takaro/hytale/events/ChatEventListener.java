package dev.takaro.hytale.events;

import dev.takaro.hytale.TakaroPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for chat events from Hytale and forwards them to Takaro
 *
 * TODO: This needs to be updated with actual Hytale event API once available
 * Look for: ChatEvent, PlayerChatEvent, or similar in com.hypixel.hytale.server.core.events
 */
public class ChatEventListener {
    private final TakaroPlugin plugin;

    public ChatEventListener(TakaroPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Example chat event handler - replace with actual Hytale event annotation/method
     *
     * In Hytale, this might look like:
     * @EventHandler
     * public void onPlayerChat(PlayerChatEvent event) { ... }
     */
    public void handleChatMessage(String playerName, String gameId, String steamId, String message, String channel) {
        plugin.getLogger().info("[CHAT] " + playerName + ": " + message);

        if (!plugin.getWebSocket().isIdentified()) {
            return;
        }

        // Build chat event for Takaro
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("type", "chat-message");
        chatData.put("msg", message);
        chatData.put("channel", channel); // "global" or "team"

        Map<String, String> player = new HashMap<>();
        player.put("name", playerName);
        player.put("gameId", gameId);
        player.put("steamId", steamId);
        chatData.put("player", player);

        // Send to Takaro
        plugin.getWebSocket().sendGameEvent("chat-message", chatData);
    }

    public void register() {
        // TODO: Register this listener with Hytale's event system
        // Example: plugin.getEventManager().registerListener(this);
        plugin.getLogger().info("Chat event listener registered");
    }
}
