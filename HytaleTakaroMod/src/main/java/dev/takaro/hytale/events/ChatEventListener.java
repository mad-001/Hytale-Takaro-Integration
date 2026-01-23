package dev.takaro.hytale.events;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import dev.takaro.hytale.TakaroPlugin;
import dev.takaro.hytale.handlers.ChatFormatter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Listens for chat events from Hytale and forwards them to Takaro
 * Applies player name colors based on Takaro permissions
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

            // Get player's name color from cache (set by Takaro via setPlayerNameColor action)
            String nameColorCode = plugin.getPlayerNameColor(uuid);
            Color nameColor = ChatFormatter.parseColor(nameColorCode);

            if (nameColor != null) {
                // Apply custom name color
                Message formattedMessage = ChatFormatter.parseColoredMessage(message);
                event.setFormatter((playerRef, msg) ->
                    Message.join(
                        Message.raw(playerName).color(nameColor),
                        Message.raw(": "),
                        formattedMessage
                    )
                );
            } else {
                // No custom color - use default formatting
                ChatFormatter.onPlayerChat(event);
            }

            // Build chat event for Takaro (don't include "type" - that's added by sendGameEvent)
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("msg", message);
            chatData.put("channel", "global");

            Map<String, String> player = new HashMap<>();
            player.put("name", playerName);
            player.put("gameId", uuid);
            player.put("platformId", "hytale:" + uuid);

            chatData.put("player", player);

            // Send to all Takaro connections (production and dev if enabled)
            plugin.sendGameEventToAll("chat-message", chatData);
            plugin.getLogger().at(java.util.logging.Level.FINE).log("Forwarded chat message to Takaro");

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling chat event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
