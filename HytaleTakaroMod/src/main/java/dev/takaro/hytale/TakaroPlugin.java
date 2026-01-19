package dev.takaro.hytale;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import dev.takaro.hytale.api.HytaleApiClient;
import dev.takaro.hytale.commands.TakaroDebugCommand;
import dev.takaro.hytale.config.TakaroConfig;
import dev.takaro.hytale.events.ChatEventListener;
import dev.takaro.hytale.events.PlayerDeathSystem;
import dev.takaro.hytale.events.PlayerEventListener;
import dev.takaro.hytale.events.TakaroLogHandler;
import dev.takaro.hytale.handlers.TakaroRequestHandler;
import dev.takaro.hytale.websocket.TakaroWebSocket;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TakaroPlugin extends JavaPlugin {
    private static final String VERSION = "1.8.5";
    private TakaroConfig config;
    private TakaroWebSocket webSocket;
    private TakaroWebSocket devWebSocket; // Optional dev Takaro connection
    private TakaroRequestHandler requestHandler;
    private HytaleApiClient hytaleApi; // Hidden feature - not in user config yet
    private ChatEventListener chatListener;
    private PlayerEventListener playerListener;
    private PlayerDeathSystem deathSystem;
    private TakaroLogHandler logHandler;
    private ScheduledExecutorService telemetryScheduler;

    // Cache for player name colors (UUID -> color code)
    // Set by Takaro via setPlayerNameColor action
    private final ConcurrentHashMap<String, String> playerNameColors = new ConcurrentHashMap<>();

    public TakaroPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().at(java.util.logging.Level.INFO).log("Hytale-Takaro Integration v" + VERSION + " initializing...");

        // Load configuration - store in mods folder directly, not in subdirectory
        File configFile = getFile().getParent().resolve("TakaroConfig.properties").toFile();
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
        deathSystem = new PlayerDeathSystem(this);
        logHandler = new TakaroLogHandler(this);

        // Register events (official pattern)
        registerEvents();

        // Register ECS systems
        registerEcsSystems();

        // Subscribe to Hytale's logging system
        HytaleLoggerBackend.subscribe(logHandler.getLogBuffer());

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

    /**
     * Register ECS systems using official Hytale pattern
     */
    private void registerEcsSystems() {
        try {
            // Register player death system with entity store registry
            this.getEntityStoreRegistry().registerSystem(deathSystem);
            getLogger().at(java.util.logging.Level.INFO).log("Registered PlayerDeathSystem");

        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.SEVERE).log("Failed to register ECS systems: " + e.getMessage());
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

        // Connect to production Takaro
        try {
            webSocket = new TakaroWebSocket(this, config, false); // false = production
            webSocket.connect();
            getLogger().at(java.util.logging.Level.INFO).log("Connecting to Takaro at " + config.getWsUrl());
        } catch (Exception e) {
            getLogger().at(java.util.logging.Level.SEVERE).log("Failed to start WebSocket connection: " + e.getMessage());
            e.printStackTrace();
        }

        // Connect to dev Takaro (if enabled)
        if (config.isDevEnabled()) {
            try {
                devWebSocket = new TakaroWebSocket(this, config, true); // true = dev
                devWebSocket.connect();
                getLogger().at(java.util.logging.Level.INFO).log("Connecting to Dev Takaro at " + config.getDevWsUrl());
            } catch (Exception e) {
                getLogger().at(java.util.logging.Level.SEVERE).log("Failed to start Dev WebSocket connection: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Start telemetry reporting to Hytale API (optional - only if token configured)
        if (!config.getHytaleApiToken().isEmpty()) {
            telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
            telemetryScheduler.scheduleAtFixedRate(this::reportTelemetry, 1, 5, TimeUnit.MINUTES);
            getLogger().at(java.util.logging.Level.INFO).log("Started Hytale telemetry reporting");
        }

        // Start log forwarding to Takaro
        logHandler.start();
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

        if (logHandler != null) {
            HytaleLoggerBackend.unsubscribe(logHandler.getLogBuffer());
            logHandler.stop();
        }

        if (telemetryScheduler != null) {
            telemetryScheduler.shutdownNow();
        }

        if (webSocket != null) {
            webSocket.shutdown();
        }

        if (devWebSocket != null) {
            devWebSocket.shutdown();
        }

        if (hytaleApi != null) {
            hytaleApi.shutdown();
        }
    }

    public void handleTakaroRequest(TakaroWebSocket sourceWebSocket, String requestId, String action, JsonObject payload) {
        requestHandler.handleRequest(sourceWebSocket, requestId, action, payload);
    }

    public TakaroWebSocket getWebSocket() {
        return webSocket;
    }

    public TakaroWebSocket getDevWebSocket() {
        return devWebSocket;
    }

    public TakaroConfig getConfig() {
        return config;
    }

    public String getVersion() {
        return VERSION;
    }

    /**
     * Send game event to all active Takaro connections (production and dev if enabled)
     * @param eventType Type of event
     * @param data Event data
     */
    public void sendGameEventToAll(String eventType, Map<String, Object> data) {
        // Send to production
        if (webSocket != null) {
            webSocket.sendGameEvent(eventType, data);
        }
        // Send to dev (if enabled and connected)
        // Dev Takaro doesn't support log events, skip those
        if (devWebSocket != null && !eventType.equals("log")) {
            devWebSocket.sendGameEvent(eventType, data);
        }
    }

    /**
     * Get a player's name color from cache
     * @param uuid Player UUID
     * @return Color code (e.g., "gold", "ff0000") or null if not set
     */
    public String getPlayerNameColor(String uuid) {
        return playerNameColors.get(uuid);
    }

    /**
     * Set a player's name color in cache
     * Called by Takaro via setPlayerNameColor action
     * @param uuid Player UUID
     * @param colorCode Color code (e.g., "gold", "ff0000")
     */
    public void setPlayerNameColor(String uuid, String colorCode) {
        if (colorCode == null || colorCode.isEmpty()) {
            playerNameColors.remove(uuid);
            getLogger().at(java.util.logging.Level.INFO).log("Removed name color for player: " + uuid);
        } else {
            playerNameColors.put(uuid, colorCode);
            getLogger().at(java.util.logging.Level.INFO).log("Set name color for player " + uuid + ": " + colorCode);
        }
    }
}
