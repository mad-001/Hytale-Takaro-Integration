package dev.takaro.hytale.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import dev.takaro.hytale.TakaroPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for chat events from Hytale and forwards them to Takaro
 * Uses official Hytale event pattern
 */
public class ChatEventListener {
    private final TakaroPlugin plugin;

    public ChatEventListener(TakaroPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player chat events
     * Registered via: getEventRegistry().registerGlobal(PlayerChatEvent.class, this::onPlayerChat)
     */
    public void onPlayerChat(PlayerChatEvent event) {
        try {
            // Extract player data
            String playerName = event.getSender().getUsername();
            String uuid = event.getSender().getUuid().toString();
            String message = event.getContent();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("[CHAT] " + playerName + ": " + message);

            // Don't forward if not connected to Takaro
            if (!plugin.getWebSocket().isIdentified()) {
                return;
            }

            // Build chat event for Takaro
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("type", "chat-message");
            chatData.put("msg", message);
            chatData.put("channel", "global"); // TODO: Detect channel from event if available

            Map<String, String> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("steamId", uuid); // Using UUID as steamId for now

            chatData.put("player", player);

            // Send to Takaro
            plugin.getWebSocket().sendGameEvent("chat-message", chatData);
            plugin.getLogger().at(java.util.logging.Level.FINE).log("Forwarded chat message to Takaro");

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling chat event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
