package dev.takaro.hytale.handlers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {
    // Named color mappings
    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("red", Color.RED);
        NAMED_COLORS.put("green", Color.GREEN);
        NAMED_COLORS.put("blue", Color.BLUE);
        NAMED_COLORS.put("yellow", Color.YELLOW);
        NAMED_COLORS.put("orange", Color.ORANGE);
        NAMED_COLORS.put("pink", Color.PINK);
        NAMED_COLORS.put("white", Color.WHITE);
        NAMED_COLORS.put("black", Color.BLACK);
        NAMED_COLORS.put("gray", Color.GRAY);
        NAMED_COLORS.put("grey", Color.GRAY);
        NAMED_COLORS.put("cyan", Color.CYAN);
        NAMED_COLORS.put("magenta", Color.MAGENTA);
        NAMED_COLORS.put("purple", new Color(128, 0, 128));
        NAMED_COLORS.put("gold", new Color(255, 215, 0));
        NAMED_COLORS.put("lime", new Color(0, 255, 0));
        NAMED_COLORS.put("aqua", new Color(0, 255, 255));
    }

    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String content = event.getContent();

        // Parse color codes in player messages
        if (content.contains("[") && content.contains("]")) {
            Message formattedMessage = parseColoredMessage(content);
            if (formattedMessage != null) {
                event.setFormatter((playerRef, message) ->
                    Message.join(
                        Message.raw("<").color(Color.GRAY),
                        Message.raw(sender.getUsername()).color(Color.WHITE),
                        Message.raw("> ").color(Color.GRAY),
                        formattedMessage
                    )
                );
            }
        }
    }

    /**
     * Parses a message with color codes like [ff0000]text[-] or [red]text[-]
     * Returns a formatted Message object with colors applied
     */
    public static Message parseColoredMessage(String input) {
        if (input == null || input.isEmpty()) {
            return Message.raw(input);
        }

        // Pattern to match [colorcode]text[-]
        // Supports hex like [ff0000] or named like [red]
        Pattern pattern = Pattern.compile("\\[([a-fA-F0-9]{6}|[a-zA-Z]+)\\](.*?)\\[-\\]");
        Matcher matcher = pattern.matcher(input);

        List<Message> parts = new ArrayList<>();
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before the color code as plain text
            if (matcher.start() > lastEnd) {
                String beforeText = input.substring(lastEnd, matcher.start());
                if (!beforeText.isEmpty()) {
                    parts.add(Message.raw(beforeText));
                }
            }

            String colorCode = matcher.group(1);
            String coloredText = matcher.group(2);

            Color color = parseColor(colorCode);
            if (color != null) {
                parts.add(Message.raw(coloredText).color(color));
            } else {
                // If color parsing failed, just add as plain text
                parts.add(Message.raw(coloredText));
            }

            lastEnd = matcher.end();
        }

        // Add remaining text after last match
        if (lastEnd < input.length()) {
            String remainingText = input.substring(lastEnd);
            if (!remainingText.isEmpty()) {
                parts.add(Message.raw(remainingText));
            }
        }

        if (parts.isEmpty()) {
            return Message.raw(input);
        } else if (parts.size() == 1) {
            return parts.get(0);
        } else {
            return Message.join(parts.toArray(new Message[0]));
        }
    }

    /**
     * Parses a color code - supports hex (ff0000) or named colors (red)
     */
    private static Color parseColor(String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            return null;
        }

        colorCode = colorCode.toLowerCase();

        // Check if it's a named color
        if (NAMED_COLORS.containsKey(colorCode)) {
            return NAMED_COLORS.get(colorCode);
        }

        // Try parsing as hex
        if (colorCode.length() == 6) {
            try {
                int r = Integer.parseInt(colorCode.substring(0, 2), 16);
                int g = Integer.parseInt(colorCode.substring(2, 4), 16);
                int b = Integer.parseInt(colorCode.substring(4, 6), 16);
                return new Color(r, g, b);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
