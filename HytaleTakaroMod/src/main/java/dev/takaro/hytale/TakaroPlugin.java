package dev.takaro.hytale;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.takaro.hytale.api.HytaleApiClient;
import dev.takaro.hytale.commands.TakaroDebugCommand;
import dev.takaro.hytale.config.TakaroConfig;
import dev.takaro.hytale.events.ChatEventListener;
import dev.takaro.hytale.events.PlayerEventListener;
import dev.takaro.hytale.handlers.TakaroRequestHandler;
import dev.takaro.hytale.websocket.TakaroWebSocket;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TakaroPlugin extends JavaPlugin {
    private static final String VERSION = "1.3.2";
    private TakaroConfig config;
    private TakaroWebSocket webSocket;
    private TakaroRequestHandler requestHandler;
    private HytaleApiClient hytaleApi; // Hidden feature - not in user config yet
    private ChatEventListener chatListener;
    private PlayerEventListener playerListener;
    private ScheduledExecutorService telemetryScheduler;

    public TakaroPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().at(java.util.logging.Level.INFO).log("Hytale-Takaro Integration v" + VERSION + " initializing...");

        // Load configuration
        File configFile = getDataDirectory().resolve("config.properties").toFile();
        config = new TakaroConfig(configFile);

        // Initialize Hytale API client (hidden feature - optional)
        hytaleApi = new HytaleApiClient(this, config.getHytaleApiUrl());
        if (!config.getHytaleApiToken().isEmpty()) {
            hytaleApi.setAuthToken(config.getHytaleApiToken());
            getLogger().at(java.util.logging.Level.INFO).log("Hytale API client initialized");
        }

        // Initialize request handler
        requestHandler = new TakaroRequestHandler(this, hytaleApi);

        // Initialize event listeners
        chatListener = new ChatEventListener(this);
        playerListener = new PlayerEventListener(this);

        // Register events (official pattern)
        registerEvents();

        // Register debug command (official pattern)
        HytaleServer.get().getCommandManager().register(new TakaroDebugCommand(this));

        getLogger().at(java.util.logging.Level.INFO).log("Configuration loaded");
        getLogger().at(java.util.logging.Level.INFO).log("Debug command registered: /takarodebug");
    }

    /**
     * Register event handlers using official Hytale pattern
     */
    private void registerEvents() {
        try {
            // Register chat event
            this.getEventRegistry().registerGlobal(
                com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent.class,
                chatListener::onPlayerChat
            );
            getLogger().at(java.util.logging.Level.INFO).log("Registered PlayerChatEvent handler");

            // Register player connect event
            this.getEventRegistry().registerGlobal(
                com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent.class,
                playerListener::onPlayerConnect
            );
            getLogger().at(java.util.logging.Level.INFO).log("Registered PlayerConnectEvent handler");

            // Register player disconnect event
            this.getEventRegistry().registerGlobal(
                com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent.class,
                playerListener::onPlayerDisconnect
            );
            getLogger().at(java.util.logging.Level.INFO).log("Registered PlayerDisconnectEvent handler");

        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.SEVERE).log("Failed to register events: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void start() {
        super.start();
        getLogger().at(java.util.logging.Level.INFO).log("Starting Takaro WebSocket connection...");

        // Check if items are loaded
        try {
            com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap();
            int itemCount = com.hypixel.hytale.server.core.asset.type.item.config.Item.getAssetMap().getAssetMap().size();
            getLogger().at(java.util.logging.Level.INFO).log("Items loaded: " + itemCount + " items available");
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.WARNING).log("Could not check items at startup: " + e.getMessage());
        }

        try {
            webSocket = new TakaroWebSocket(this, config);
            webSocket.connect();
            getLogger().at(java.util.logging.Level.INFO).log("Connecting to Takaro at " + config.getWsUrl());
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.SEVERE).log("Failed to start WebSocket connection: " + e.getMessage());
            e.printStackTrace();
        }

        // Start telemetry reporting to Hytale API (optional - only if token configured)
        if (!config.getHytaleApiToken().isEmpty()) {
            telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
            telemetryScheduler.scheduleAtFixedRate(this::reportTelemetry, 1, 5, TimeUnit.MINUTES);
            getLogger().at(java.util.logging.Level.INFO).log("Started Hytale telemetry reporting");
        }
    }

    private void reportTelemetry() {
        try {
            // TODO: Get actual player count from server
            int playerCount = 0; // getServer().getOnlinePlayers().size();

            JsonObject metadata = new JsonObject();
            metadata.addProperty("takaroEnabled", true);
            metadata.addProperty("version", VERSION);

            hytaleApi.reportTelemetry(playerCount, "online", metadata);
            getLogger().at(java.util.logging.Level.FINE).log("Telemetry reported to Hytale API");
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.WARNING).log("Failed to report telemetry: " + e.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        getLogger().at(java.util.logging.Level.INFO).log("Shutting down Takaro integration...");

        if (telemetryScheduler != null) {
            telemetryScheduler.shutdownNow();
        }

        if (webSocket != null) {
            webSocket.shutdown();
        }

        if (hytaleApi != null) {
            hytaleApi.shutdown();
        }
    }

    public void handleTakaroRequest(String requestId, String action, JsonObject payload) {
        requestHandler.handleRequest(requestId, action, payload);
    }

    public TakaroWebSocket getWebSocket() {
        return webSocket;
    }

    public TakaroConfig getConfig() {
        return config;
    }

    public String getVersion() {
        return VERSION;
    }
}
