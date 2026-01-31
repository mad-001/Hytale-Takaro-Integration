package com.hytalecharts.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * HytaleCharts - Server Status Plugin for Hytale
 * Reports server online status and player count to HytaleCharts.com
 */
public class HytaleChartsPlugin extends JavaPlugin {

    private static final String PLUGIN_VERSION = "1.4.0";
    private static final String API_URL = "https://hytalecharts.com/api/heartbeat";
    private static final int HEARTBEAT_INTERVAL_SECONDS = 300; // 5 minutes
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Config config;
    private HttpClient httpClient;
    private ScheduledExecutorService scheduler;
    private EventRegistration connectRegistration;
    private EventRegistration disconnectRegistration;
    private volatile int trackedPlayerCount = 0;

    public HytaleChartsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        loadConfig();
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    protected void start() {
        if (config.secret == null || config.secret.isEmpty() || config.secret.equals("YOUR_SECRET_HERE")) {
            getLogger().at(Level.WARNING).log("HytaleCharts not configured! Generate a heartbeat secret at hytalecharts.com");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HytaleCharts-Heartbeat");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 5, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // Schedule promo link broadcast if enabled
        if (config.promoLinkEnabled && config.promoLinkIntervalMinutes > 0) {
            long intervalSeconds = config.promoLinkIntervalMinutes * 60L;
            scheduler.scheduleAtFixedRate(this::broadcastPromoLink, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
            if (config.debug) {
                getLogger().at(Level.INFO).log("Promo link broadcasts enabled - every %d minutes", config.promoLinkIntervalMinutes);
            }
        }

        // Register player count tracking events as fallback
        EventBus eventBus = HytaleServer.get().getEventBus();

        connectRegistration = eventBus.register(PlayerConnectEvent.class, event -> {
            trackedPlayerCount++;
            PlayerRef playerRef = event.getPlayerRef();
            if (config.debug) {
                getLogger().at(Level.INFO).log("Player connected: %s (tracked: %d)",
                    playerRef.getUsername(), trackedPlayerCount);
            }
            // Send promo link on login if enabled (with delay for player to load)
            if (config.promoLinkOnLogin) {
                scheduler.schedule(() -> sendPromoLinkToPlayer(playerRef), 22, TimeUnit.SECONDS);
            }
        });

        disconnectRegistration = eventBus.register(PlayerDisconnectEvent.class, event -> {
            trackedPlayerCount = Math.max(0, trackedPlayerCount - 1);
            if (config.debug) {
                getLogger().at(Level.INFO).log("Player disconnected: %s (tracked: %d)",
                    event.getPlayerRef().getUsername(), trackedPlayerCount);
            }
        });

        getLogger().at(Level.INFO).log("HytaleCharts v%s started - sending heartbeat every %d seconds", PLUGIN_VERSION, HEARTBEAT_INTERVAL_SECONDS);
    }

    @Override
    protected void shutdown() {
        if (connectRegistration != null) {
            connectRegistration.unregister();
        }
        if (disconnectRegistration != null) {
            disconnectRegistration.unregister();
        }
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        getLogger().at(Level.INFO).log("HytaleCharts plugin stopped");
    }

    private void loadConfig() {
        try {
            Path configPath = getDataDirectory().resolve("config.json");
            if (Files.exists(configPath)) {
                config = GSON.fromJson(Files.readString(configPath), Config.class);
            } else {
                config = new Config();
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, GSON.toJson(config));
                getLogger().at(Level.INFO).log("Created config at %s - add your heartbeat secret from hytalecharts.com", configPath);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).log("Failed to load config: %s", e.getMessage());
            config = new Config();
        }
    }

    /**
     * Gets the most accurate player count by comparing event-based tracking
     * with the authoritative Universe.getPlayerCount(). Uses the higher value
     * to avoid undercounting when events are missed on high-traffic servers.
     * Also syncs the tracked count if drift is detected.
     */
    private int getReliablePlayerCount() {
        int universeCount = Universe.get().getPlayerCount();
        int tracked = trackedPlayerCount;

        if (universeCount != tracked) {
            if (config.debug) {
                getLogger().at(Level.INFO).log("Player count drift detected: tracked=%d, universe=%d",
                    tracked, universeCount);
            }
            // Sync tracked count to universe count to correct drift
            trackedPlayerCount = universeCount;
        }

        // Use higher value as fallback in case universe query fails momentarily
        return Math.max(universeCount, tracked);
    }

    /**
     * Builds a JSON array of all online players with their username, UUID, and current world.
     */
    private JsonArray buildPlayerList() {
        JsonArray players = new JsonArray();
        try {
            for (var entry : Universe.get().getWorlds().entrySet()) {
                var world = entry.getValue();
                for (var ref : world.getPlayerRefs()) {
                    JsonObject player = new JsonObject();
                    player.addProperty("username", ref.getUsername());
                    player.addProperty("uuid", ref.getUuid().toString());
                    player.addProperty("world", world.getName());
                    players.add(player);
                }
            }
        } catch (Exception e) {
            if (config.debug) {
                getLogger().at(Level.WARNING).log("Failed to build player list: %s", e.getMessage());
            }
        }
        return players;
    }

    private void sendPromoLinkToPlayer(PlayerRef playerRef) {
        try {
            String messageText = config.promoLinkMessage != null && !config.promoLinkMessage.isEmpty()
                ? config.promoLinkMessage
                : "Vote for our server on HytaleCharts!";
            String linkUrl = config.promoLinkUrl != null && !config.promoLinkUrl.isEmpty()
                ? config.promoLinkUrl
                : "https://hytalecharts.com";

            Message promoLink;
            if (config.promoLinkPrefix != null && !config.promoLinkPrefix.isEmpty()) {
                Message prefix = Message.raw(config.promoLinkPrefix).color(Color.WHITE);
                Message linkText = Message.raw(messageText).color(Color.WHITE).link(linkUrl);
                promoLink = prefix.insert(linkText);
            } else {
                promoLink = Message.raw(messageText).color(Color.WHITE).link(linkUrl);
            }
            playerRef.sendMessage(promoLink);

            if (config.debug) {
                getLogger().at(Level.INFO).log("Sent promo link to %s", playerRef.getUsername());
            }
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("Failed to send promo link to %s: %s", playerRef.getUsername(), e.getMessage());
        }
    }

    private void broadcastPromoLink() {
        if (trackedPlayerCount == 0) return; // Don't broadcast if no one is online

        try {
            String messageText = config.promoLinkMessage != null && !config.promoLinkMessage.isEmpty()
                ? config.promoLinkMessage
                : "Vote for our server on HytaleCharts!";
            String linkUrl = config.promoLinkUrl != null && !config.promoLinkUrl.isEmpty()
                ? config.promoLinkUrl
                : "https://hytalecharts.com";

            Message promoLink;
            if (config.promoLinkPrefix != null && !config.promoLinkPrefix.isEmpty()) {
                Message prefix = Message.raw(config.promoLinkPrefix).color(Color.WHITE);
                Message linkText = Message.raw(messageText).color(Color.WHITE).link(linkUrl);
                promoLink = prefix.insert(linkText);
            } else {
                promoLink = Message.raw(messageText).color(Color.WHITE).link(linkUrl);
            }
            Universe.get().sendMessage(promoLink);

            if (config.debug) {
                getLogger().at(Level.INFO).log("Promo link broadcast to %d player(s)", trackedPlayerCount);
            }
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("Failed to broadcast promo link: %s", e.getMessage());
        }
    }

    private void sendHeartbeat() {
        try {
            int playerCount = getReliablePlayerCount();
            int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();

            JsonObject body = new JsonObject();
            body.addProperty("secret", config.secret);
            body.addProperty("player_count", playerCount);
            body.addProperty("max_players", maxPlayers);
            body.addProperty("version", PLUGIN_VERSION);

            // Include player list with usernames, UUIDs, and worlds
            body.add("players", buildPlayerList());

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("User-Agent", "HytaleCharts-Plugin/" + PLUGIN_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .timeout(Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (config.debug) {
                    getLogger().at(Level.INFO).log("Heartbeat sent: %d/%d players, response: %d",
                        playerCount, maxPlayers, response.statusCode());
                }
            } else if (response.statusCode() == 429) {
                getLogger().at(Level.WARNING).log("Heartbeat rate limited (HTTP 429) - this is normal after restarts, will retry in %d seconds",
                    HEARTBEAT_INTERVAL_SECONDS);
            } else if (response.statusCode() == 401) {
                getLogger().at(Level.WARNING).log("Heartbeat failed: Invalid secret key - generate a new one at hytalecharts.com");
            } else {
                getLogger().at(Level.WARNING).log("Heartbeat failed: HTTP %d", response.statusCode());
            }
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("Heartbeat failed: %s", e.getMessage());
        }
    }

    private static class Config {
        String secret = "YOUR_SECRET_HERE";
        boolean debug = false;
        // Promo link settings
        boolean promoLinkOnLogin = true; // Send clickable link when player joins
        boolean promoLinkEnabled = false; // Enable periodic clickable link broadcasts
        int promoLinkIntervalMinutes = 15; // How often to broadcast promo link (in minutes)
        String promoLinkPrefix = "[hytalecharts.com] "; // Prefix before message (set to empty string to disable)
        String promoLinkMessage = "Vote for our server on HytaleCharts!"; // Promo link message
        String promoLinkUrl = "https://hytalecharts.com"; // URL for the clickable link
    }
}
