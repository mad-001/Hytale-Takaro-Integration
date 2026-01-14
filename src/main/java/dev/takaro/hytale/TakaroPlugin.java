package dev.takaro.hytale;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.takaro.hytale.api.HytaleApiClient;
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
import java.util.logging.Logger;

public class TakaroPlugin extends JavaPlugin {
    private static final String VERSION = "1.0.0";
    private TakaroConfig config;
    private TakaroWebSocket webSocket;
    private TakaroRequestHandler requestHandler;
    private HytaleApiClient hytaleApi;
    private ChatEventListener chatListener;
    private PlayerEventListener playerListener;
    private ScheduledExecutorService telemetryScheduler;
    private Logger logger;

    public TakaroPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.logger = getLogger();
    }

    @Override
    protected void setup() {
        logger.info("Hytale-Takaro Integration v" + VERSION + " initializing...");

        // Load configuration
        File configFile = new File(getDataFolder(), "config.properties");
        config = new TakaroConfig(configFile);

        // Initialize Hytale API client
        hytaleApi = new HytaleApiClient(this, config.getHytaleApiUrl());
        if (!config.getHytaleApiToken().isEmpty()) {
            hytaleApi.setAuthToken(config.getHytaleApiToken());
            logger.info("Hytale API client initialized");
        } else {
            logger.warning("No Hytale API token configured - some features may be limited");
        }

        // Initialize request handler
        requestHandler = new TakaroRequestHandler(this, hytaleApi);

        // Initialize event listeners
        chatListener = new ChatEventListener(this);
        playerListener = new PlayerEventListener(this);

        logger.info("Configuration loaded");
    }

    @Override
    protected void start() {
        super.start();
        logger.info("Starting Takaro WebSocket connection...");

        try {
            webSocket = new TakaroWebSocket(this, config);
            webSocket.connect();
            logger.info("Connecting to Takaro at " + config.getWsUrl());
        } catch (Exception e) {
            logger.severe("Failed to start WebSocket connection: " + e.getMessage());
            e.printStackTrace();
        }

        // Start telemetry reporting to Hytale API (every 5 minutes)
        if (!config.getHytaleApiToken().isEmpty()) {
            telemetryScheduler = Executors.newSingleThreadScheduledExecutor();
            telemetryScheduler.scheduleAtFixedRate(this::reportTelemetry, 1, 5, TimeUnit.MINUTES);
            logger.info("Started Hytale telemetry reporting");
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
            logger.fine("Telemetry reported to Hytale API");
        } catch (Exception e) {
            logger.warning("Failed to report telemetry: " + e.getMessage());
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        logger.info("Shutting down Takaro integration...");

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
}
