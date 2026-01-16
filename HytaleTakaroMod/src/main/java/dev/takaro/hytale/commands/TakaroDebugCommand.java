package dev.takaro.hytale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.takaro.hytale.TakaroPlugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Debug command to help discover Hytale server API
 * Usage: /takarodebug [info|server|events|ws]
 */
public class TakaroDebugCommand extends CommandBase {
    private final TakaroPlugin plugin;

    public TakaroDebugCommand(TakaroPlugin plugin) {
        super("takarodebug", "Debug Takaro integration and explore Hytale API");
        this.plugin = plugin;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        // Get input string and split into arguments
        String input = context.getInputString();
        String[] parts = input.trim().split("\\s+");

        // Skip first part which is the command name itself
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        if (args.length == 0) {
            sendHelp(context);
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "info":
                showInfo(context);
                break;
            case "server":
                showServerInfo(context);
                break;
            case "events":
                showEventInfo(context);
                break;
            case "ws":
                showWebSocketInfo(context);
                break;
            case "methods":
                showServerMethods(context);
                break;
            default:
                sendHelp(context);
        }
    }

    private void sendHelp(CommandContext context) {
        context.sendMessage(Message.raw("§aTakaro Debug Commands:"));
        context.sendMessage(Message.raw("§e/takarodebug info §7- Show plugin info"));
        context.sendMessage(Message.raw("§e/takarodebug server §7- Show server info"));
        context.sendMessage(Message.raw("§e/takarodebug events §7- Show event registration status"));
        context.sendMessage(Message.raw("§e/takarodebug ws §7- Show WebSocket connection status"));
        context.sendMessage(Message.raw("§e/takarodebug methods §7- List server methods (console only)"));
    }

    private void showInfo(CommandContext context) {
        context.sendMessage(Message.raw("§a=== Takaro Integration Info ==="));
        context.sendMessage(Message.raw("§7Plugin Version: §e" + plugin.getVersion()));
        context.sendMessage(Message.raw("§7Takaro WS URL: §e" + plugin.getConfig().getWsUrl()));

        boolean hasTakaroToken = !plugin.getConfig().getIdentityToken().isEmpty();
        boolean hasHytaleToken = !plugin.getConfig().getHytaleApiToken().isEmpty();

        context.sendMessage(Message.raw("§7Identity Token: " + (hasTakaroToken ? "§aConfigured" : "§cMissing")));

        // Show Hytale API status (hidden feature - not documented yet)
        if (hasHytaleToken) {
            context.sendMessage(Message.raw("§7Hytale API: §aEnabled (hidden feature)"));
        }
    }

    private void showServerInfo(CommandContext context) {
        context.sendMessage(Message.raw("§a=== Server Info ==="));

        try {
            // Try to get server instance
            // Note: Actual method name may vary, will discover through testing
            // Object server = plugin.getServer();

            context.sendMessage(Message.raw("§7Plugin loaded: §aYes"));

            // TODO: Add actual player count once API is discovered
            // context.sendMessage(Message.raw("§7Online Players: §e" + server.getOnlinePlayers().size()));

            context.sendMessage(Message.raw("§eNote: Use /takarodebug methods in console to see available APIs"));
        } catch (Exception e) {
            context.sendMessage(Message.raw("§cError: " + e.getMessage()));
        }
    }

    private void showEventInfo(CommandContext context) {
        context.sendMessage(Message.raw("§a=== Event Registration ==="));
        context.sendMessage(Message.raw("§7Chat Listener: §eRegistration pending"));
        context.sendMessage(Message.raw("§7Player Listener: §eRegistration pending"));
        context.sendMessage(Message.raw("§eNote: Event registration will be implemented once API is tested"));
    }

    private void showWebSocketInfo(CommandContext context) {
        context.sendMessage(Message.raw("§a=== Takaro WebSocket ==="));

        if (plugin.getWebSocket() == null) {
            context.sendMessage(Message.raw("§cWebSocket: Not initialized"));
            return;
        }

        boolean isConnected = plugin.getWebSocket().isOpen();
        boolean isIdentified = plugin.getWebSocket().isIdentified();

        context.sendMessage(Message.raw("§7Connected: " + (isConnected ? "§aYes" : "§cNo")));
        context.sendMessage(Message.raw("§7Identified: " + (isIdentified ? "§aYes" : "§cNo")));

        if (!isConnected) {
            context.sendMessage(Message.raw("§eCheck config.properties for correct IDENTITY_TOKEN"));
        } else if (!isIdentified) {
            context.sendMessage(Message.raw("§eWaiting for Takaro authentication..."));
        } else {
            context.sendMessage(Message.raw("§a✓ Ready to send/receive Takaro events!"));
        }
    }

    private void showServerMethods(CommandContext context) {
        plugin.getLogger().at(java.util.logging.Level.INFO).log("=== Available Server Methods ===");

        try {
            // Reflect on the plugin to find server-related methods
            Class<?> pluginClass = plugin.getClass().getSuperclass(); // JavaPlugin

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Plugin superclass: " + pluginClass.getName());

            for (Method method : pluginClass.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.contains("server") || methodName.contains("Server") ||
                    methodName.contains("event") || methodName.contains("Event") ||
                    methodName.contains("player") || methodName.contains("Player")) {

                    plugin.getLogger().at(java.util.logging.Level.INFO).log("  - " + method.getName() + "()");
                }
            }

            context.sendMessage(Message.raw("§aMethod list printed to console (check logs)"));

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error listing methods: " + e.getMessage());
            context.sendMessage(Message.raw("§cError: " + e.getMessage()));
        }
    }
}
