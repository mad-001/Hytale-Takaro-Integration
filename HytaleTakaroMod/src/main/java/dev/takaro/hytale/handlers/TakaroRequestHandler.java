package dev.takaro.hytale.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.takaro.hytale.TakaroPlugin;
import dev.takaro.hytale.api.HytaleApiClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

public class TakaroRequestHandler {
    private final TakaroPlugin plugin;
    private final HytaleApiClient hytaleApi;
    private final Gson gson = new Gson();

    public TakaroRequestHandler(TakaroPlugin plugin, HytaleApiClient hytaleApi) {
        this.plugin = plugin;
        this.hytaleApi = hytaleApi;
    }

    public void handleRequest(String requestId, String action, JsonObject payload) {
        Object responsePayload;

        try {
            switch (action) {
                case "testReachability":
                    responsePayload = handleTestReachability();
                    break;
                case "getPlayers":
                    responsePayload = handleGetPlayers();
                    break;
                case "getServerInfo":
                    responsePayload = handleGetServerInfo();
                    break;
                case "sendMessage":
                    responsePayload = handleSendMessage(payload);
                    break;
                case "executeCommand":
                case "executeConsoleCommand":
                    responsePayload = handleExecuteCommand(payload);
                    break;
                case "giveItem":
                    responsePayload = handleGiveItem(payload);
                    break;
                case "kickPlayer":
                    responsePayload = handleKickPlayer(payload);
                    break;
                case "banPlayer":
                    responsePayload = handleBanPlayer(payload);
                    break;
                case "unbanPlayer":
                    responsePayload = handleUnbanPlayer(payload);
                    break;
                case "getPlayerLocation":
                    responsePayload = handleGetPlayerLocation(payload);
                    break;
                case "teleportPlayer":
                    responsePayload = handleTeleportPlayer(payload);
                    break;
                case "teleportPlayerToPlayer":
                    responsePayload = handleTeleportPlayerToPlayer(payload);
                    break;
                case "listCommands":
                    responsePayload = handleListCommands();
                    break;
                case "getAvailableActions":
                case "help":
                    responsePayload = handleGetAvailableActions();
                    break;
                case "getPlayerInventory":
                    responsePayload = handleGetPlayerInventory(payload);
                    break;
                case "listItems":
                    responsePayload = handleListItems();
                    break;
                case "listBans":
                case "listEntities":
                case "listLocations":
                    // Not implemented yet
                    responsePayload = new Object[0];
                    break;
                default:
                    plugin.getLogger().at(java.util.logging.Level.WARNING).log("Unknown action: " + action);
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Unknown action: " + action);
                    responsePayload = error;
            }
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling " + action + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            responsePayload = error;
        }

        plugin.getWebSocket().sendResponse(requestId, responsePayload);
    }

    private Object handleTestReachability() {
        Map<String, Object> result = new HashMap<>();
        result.put("connectable", true);
        result.put("reason", null);
        return result;
    }

    private Object handleGetPlayers() {
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Getting players list");

        try {
            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                plugin.getLogger().at(java.util.logging.Level.WARNING).log("Universe is null");
                return new Object[0];
            }

            List<PlayerRef> players = universe.getPlayers();

            List<Map<String, Object>> playerList = players.stream().map(player -> {
                Map<String, Object> playerData = new HashMap<>();
                String uuid = player.getUuid().toString();
                playerData.put("name", player.getUsername());
                playerData.put("gameId", uuid);
                playerData.put("platformId", "hytale:" + uuid);
                playerData.put("ip", "127.0.0.1"); // IP not directly accessible via API
                return playerData;
            }).collect(Collectors.toList());

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Found " + playerList.size() + " players");
            return playerList;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error getting players: " + e.getMessage());
            e.printStackTrace();
            return new Object[0];
        }
    }

    private Object handleGetServerInfo() {
        // TODO: Implement actual server info from Hytale API
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Hytale Server");
        info.put("version", "1.0");
        return info;
    }

    private Object handleSendMessage(JsonObject payload) {
        try {
            // Parse args if it exists, otherwise try direct message field
            String message;
            if (payload.has("args")) {
                String argsString = payload.get("args").getAsString();
                JsonObject args = gson.fromJson(argsString, JsonObject.class);
                message = args.get("message").getAsString();
            } else {
                message = payload.get("message").getAsString();
            }

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Sending message to all players: " + message);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                plugin.getLogger().at(java.util.logging.Level.WARNING).log("Universe is null");
                Map<String, Boolean> result = new HashMap<>();
                result.put("success", false);
                return result;
            }

            List<PlayerRef> players = universe.getPlayers();

            // Parse color codes in message from Takaro
            Message msg = ChatFormatter.parseColoredMessage(message);

            for (PlayerRef player : players) {
                player.sendMessage(msg);
            }

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Message sent to " + players.size() + " players");
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", true);
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error sending message: " + e.getMessage());
            e.printStackTrace();
            Map<String, Boolean> result = new HashMap<>();
            result.put("success", false);
            return result;
        }
    }

    private Object handleExecuteCommand(JsonObject payload) {
        try {
            // The payload structure is: {"args": "{\"command\":\"help\"}"}
            // We need to parse the args string as JSON
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String command = args.get("command").getAsString();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Executing console command: " + command);

            // Check if it's a request for help/documentation
            if (command.equalsIgnoreCase("getavailableactions") ||
                command.equalsIgnoreCase("takarohelp") ||
                command.equalsIgnoreCase("takaro")) {
                return buildHelpResponse();
            }

            // Check if it's a request for player locations
            if (command.equalsIgnoreCase("playerlocations") ||
                command.equalsIgnoreCase("locations") ||
                command.equalsIgnoreCase("whereis") ||
                command.equalsIgnoreCase("players")) {
                return buildPlayerLocationsResponse();
            }

            // Check if it's a give command
            if (command.toLowerCase().startsWith("give ")) {
                return handleGiveConsoleCommand(command);
            }

            // Check if it's a shutdown/stop command
            if (command.equalsIgnoreCase("shutdown") ||
                command.equalsIgnoreCase("stop")) {
                // Schedule shutdown after a delay to allow response to be sent
                new Thread(() -> {
                    try {
                        Thread.sleep(1000); // Wait 1 second to send response
                        HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command).join();
                    } catch (Exception e) {
                        plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error executing delayed shutdown: " + e.getMessage());
                    }
                }).start();

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("rawResult", "Server shutdown initiated");
                return result;
            }

            // Subscribe to logger to capture all console output
            CopyOnWriteArrayList<LogRecord> logCapture = new CopyOnWriteArrayList<>();
            HytaleLoggerBackend.subscribe(logCapture);

            try {
                // Execute command
                HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, command).join();

                // Give async messages time to arrive
                Thread.sleep(500);
            } finally {
                // Always unsubscribe
                HytaleLoggerBackend.unsubscribe(logCapture);
            }

            // Extract messages from captured logs
            StringBuilder output = new StringBuilder();
            for (LogRecord record : logCapture) {
                String message = record.getMessage();
                if (message != null && !message.isEmpty()) {
                    output.append(message).append("\n");
                }
            }

            String outputStr = output.toString().trim();
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Captured " + logCapture.size() + " log records");

            if (outputStr.isEmpty()) {
                outputStr = "Command executed (no output)";
            } else {
                plugin.getLogger().at(java.util.logging.Level.INFO).log("Output preview: " + outputStr.substring(0, Math.min(100, outputStr.length())));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("rawResult", outputStr);

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Command executed: " + command + " | Output length: " + outputStr.length());
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error executing command: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("rawResult", "Error: " + e.getMessage());
            return result;
        }
    }

    private Object handleGiveItem(JsonObject payload) {
        try {
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String gameId = args.get("gameId").getAsString();
            String itemId = args.get("item").getAsString();
            int amount = args.has("amount") ? args.get("amount").getAsInt() : 1;

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Giving item " + itemId + " x" + amount + " to player " + gameId);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Universe is null");
                return result;
            }

            UUID playerUuid = UUID.fromString(gameId);
            PlayerRef playerRef = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);

            if (playerRef == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Player not found");
                return result;
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Player not in world");
                return result;
            }

            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            CompletableFuture<Boolean> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    Player playerComponent = store.getComponent(ref, Player.getComponentType());
                    if (playerComponent == null) {
                        future.complete(false);
                        return;
                    }

                    Item item = Item.getAssetMap().getAsset(itemId);
                    if (item == null) {
                        plugin.getLogger().at(java.util.logging.Level.WARNING).log("Item not found: " + itemId);
                        future.complete(false);
                        return;
                    }

                    ItemStackTransaction transaction = playerComponent.getInventory()
                        .getCombinedHotbarFirst()
                        .addItemStack(new ItemStack(item.getId(), amount, null));

                    ItemStack remainder = transaction.getRemainder();
                    future.complete(remainder == null || remainder.isEmpty());
                } catch (Exception e) {
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error giving item: " + e.getMessage());
                    e.printStackTrace();
                    future.complete(false);
                }
            });

            boolean success = future.get(5, TimeUnit.SECONDS);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Give item result: " + success);
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling giveItem: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object handleKickPlayer(JsonObject payload) {
        try {
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String gameId = args.get("gameId").getAsString();
            String reason = args.has("reason") ? args.get("reason").getAsString() : "You were kicked.";

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Kicking player: " + gameId);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Universe is null");
                return result;
            }

            UUID playerUuid = UUID.fromString(gameId);
            PlayerRef playerRef = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);

            if (playerRef == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Player not found");
                return result;
            }

            playerRef.getPacketHandler().disconnect(reason);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Player kicked: " + gameId);
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error kicking player: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object handleBanPlayer(JsonObject payload) {
        // TODO: Implement player ban
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Banning player: " + payload.toString());
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private Object handleUnbanPlayer(JsonObject payload) {
        // TODO: Implement player unban
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Unbanning player: " + payload.toString());
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private Object handleGetPlayerLocation(JsonObject payload) {
        try {
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String gameId = args.get("gameId").getAsString();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Getting player location: " + gameId);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("x", 0);
                result.put("y", 0);
                result.put("z", 0);
                result.put("error", "Universe is null");
                return result;
            }

            UUID playerUuid = UUID.fromString(gameId);
            PlayerRef playerRef = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);

            if (playerRef == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("x", 0);
                result.put("y", 0);
                result.put("z", 0);
                result.put("error", "Player not found");
                return result;
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("x", 0);
                result.put("y", 0);
                result.put("z", 0);
                result.put("error", "Player not in world");
                return result;
            }

            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
                    if (transform == null) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("x", 0);
                        result.put("y", 0);
                        result.put("z", 0);
                        result.put("error", "Transform component not found");
                        future.complete(result);
                        return;
                    }

                    Vector3d position = transform.getPosition();
                    Map<String, Object> result = new HashMap<>();
                    result.put("x", position.getX());
                    result.put("y", position.getY());
                    result.put("z", position.getZ());
                    future.complete(result);
                } catch (Exception e) {
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error getting position: " + e.getMessage());
                    e.printStackTrace();
                    Map<String, Object> result = new HashMap<>();
                    result.put("x", 0);
                    result.put("y", 0);
                    result.put("z", 0);
                    result.put("error", e.getMessage());
                    future.complete(result);
                }
            });

            Map<String, Object> result = future.get(5, TimeUnit.SECONDS);
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Player location: " + result.get("x") + "," + result.get("y") + "," + result.get("z"));
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling getPlayerLocation: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("x", 0);
            result.put("y", 0);
            result.put("z", 0);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object handleTeleportPlayerToPlayer(JsonObject payload) {
        try {
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String sourceGameId = args.get("gameId").getAsString();
            String targetGameId = args.get("targetGameId").getAsString();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Teleporting player " + sourceGameId + " to player " + targetGameId);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Universe is null");
                return result;
            }

            UUID sourceUuid = UUID.fromString(sourceGameId);
            UUID targetUuid = UUID.fromString(targetGameId);

            PlayerRef sourcePlayer = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(sourceUuid))
                .findFirst()
                .orElse(null);

            PlayerRef targetPlayer = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(targetUuid))
                .findFirst()
                .orElse(null);

            if (sourcePlayer == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Source player not found");
                return result;
            }

            if (targetPlayer == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Target player not found");
                return result;
            }

            Ref<EntityStore> sourceRef = sourcePlayer.getReference();
            Ref<EntityStore> targetRef = targetPlayer.getReference();

            if (sourceRef == null || !sourceRef.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Source player not in world");
                return result;
            }

            if (targetRef == null || !targetRef.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Target player not in world");
                return result;
            }

            Store<EntityStore> targetStore = targetRef.getStore();
            World targetWorld = targetStore.getExternalData().getWorld();

            // Get target player's position
            CompletableFuture<Vector3d> positionFuture = new CompletableFuture<>();

            targetWorld.execute(() -> {
                try {
                    TransformComponent targetTransform = targetStore.getComponent(targetRef, TransformComponent.getComponentType());
                    if (targetTransform == null) {
                        positionFuture.completeExceptionally(new Exception("Target transform not found"));
                        return;
                    }
                    positionFuture.complete(new Vector3d(targetTransform.getPosition()));
                } catch (Exception e) {
                    positionFuture.completeExceptionally(e);
                }
            });

            Vector3d targetPosition = positionFuture.get(5, TimeUnit.SECONDS);

            // Now teleport source player to target position
            Store<EntityStore> sourceStore = sourceRef.getStore();
            World sourceWorld = sourceStore.getExternalData().getWorld();

            CompletableFuture<Boolean> teleportFuture = new CompletableFuture<>();

            sourceWorld.execute(() -> {
                try {
                    Vector3f rotation = new Vector3f(0, 0, 0);
                    Teleport teleport = new Teleport(targetWorld, targetPosition, rotation);
                    sourceStore.addComponent(sourceRef, Teleport.getComponentType(), teleport);
                    teleportFuture.complete(true);
                } catch (Exception e) {
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error teleporting: " + e.getMessage());
                    e.printStackTrace();
                    teleportFuture.complete(false);
                }
            });

            boolean success = teleportFuture.get(5, TimeUnit.SECONDS);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Teleport to player result: " + success);
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling teleportPlayerToPlayer: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object handleTeleportPlayer(JsonObject payload) {
        try {
            String argsString = payload.get("args").getAsString();
            JsonObject args = gson.fromJson(argsString, JsonObject.class);
            String gameId = args.get("gameId").getAsString();
            double x = args.get("x").getAsDouble();
            double y = args.get("y").getAsDouble();
            double z = args.get("z").getAsDouble();

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Teleporting player " + gameId + " to " + x + "," + y + "," + z);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Universe is null");
                return result;
            }

            UUID playerUuid = UUID.fromString(gameId);
            PlayerRef playerRef = universe.getPlayers().stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);

            if (playerRef == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Player not found");
                return result;
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Player not in world");
                return result;
            }

            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            CompletableFuture<Boolean> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    Vector3d position = new Vector3d(x, y, z);
                    Vector3f rotation = new Vector3f(0, 0, 0);
                    Teleport teleport = new Teleport(world, position, rotation);
                    store.addComponent(ref, Teleport.getComponentType(), teleport);
                    future.complete(true);
                } catch (Exception e) {
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error teleporting: " + e.getMessage());
                    e.printStackTrace();
                    future.complete(false);
                }
            });

            boolean success = future.get(5, TimeUnit.SECONDS);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Teleport result: " + success);
            return result;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling teleportPlayer: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private Object handleListCommands() {
        try {
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Fetching available commands from CommandManager");

            java.util.Map<String, com.hypixel.hytale.server.core.command.system.AbstractCommand> commands =
                HytaleServer.get().getCommandManager().getCommandRegistration();

            List<Map<String, Object>> commandList = new ArrayList<>();
            for (java.util.Map.Entry<String, com.hypixel.hytale.server.core.command.system.AbstractCommand> entry : commands.entrySet()) {
                Map<String, Object> commandInfo = new HashMap<>();
                commandInfo.put("name", entry.getKey());
                commandInfo.put("description", entry.getValue().getDescription());
                commandInfo.put("aliases", entry.getValue().getAliases());
                commandList.add(commandInfo);
            }

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Returning " + commandList.size() + " commands");
            return commandList;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error listing commands: " + e.getMessage());
            e.printStackTrace();
            return new Object[0];
        }
    }

    private Object handleGetPlayerInventory(JsonObject payload) {
        // TODO: Implement player inventory fetching when API is available
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Getting player inventory (not implemented): " + payload.toString());
        return new Object[0];
    }

    private Object handleListItems() {
        try {
            plugin.getLogger().at(java.util.logging.Level.INFO).log("Fetching all items from AssetMap");

            Map<String, Item> items = Item.getAssetMap().getAssetMap();
            List<Map<String, Object>> itemList = new ArrayList<>();

            for (Map.Entry<String, Item> entry : items.entrySet()) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("code", entry.getKey());
                itemInfo.put("name", entry.getValue().getTranslationKey());
                itemInfo.put("description", entry.getValue().getTranslationKey());
                itemList.add(itemInfo);
            }

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Returning " + itemList.size() + " items");
            return itemList;
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error listing items: " + e.getMessage());
            e.printStackTrace();
            return new Object[0];
        }
    }

    private Map<String, Object> handleGiveConsoleCommand(String command) {
        try {
            // Parse: give <player> <item> [amount]
            String[] parts = command.split("\\s+");

            if (parts.length < 3) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("rawResult", "Usage: give <player> <item> [amount]\nExample: give Mad001 Wood_Oak_Trunk 10");
                return result;
            }

            final String playerName = parts[1];
            final String itemName = parts[2];
            final int amount;

            if (parts.length >= 4) {
                try {
                    amount = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("rawResult", "Invalid amount: " + parts[3] + "\nUsage: give <player> <item> [amount]");
                    return result;
                }
            } else {
                amount = 1;
            }

            plugin.getLogger().at(java.util.logging.Level.INFO).log("Console give command: " + playerName + " " + itemName + " x" + amount);

            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("rawResult", "Universe is null");
                return result;
            }

            // Find player by name
            PlayerRef playerRef = universe.getPlayers().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);

            if (playerRef == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("rawResult", "Player not found: " + playerName);
                return result;
            }

            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("rawResult", "Player not in world: " + playerName);
                return result;
            }

            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();

            CompletableFuture<String> future = new CompletableFuture<>();

            world.execute(() -> {
                try {
                    Player playerComponent = store.getComponent(ref, Player.getComponentType());
                    if (playerComponent == null) {
                        future.complete("Player component not found");
                        return;
                    }

                    Item item = Item.getAssetMap().getAsset(itemName);
                    if (item == null) {
                        future.complete("Item not found: " + itemName);
                        return;
                    }

                    ItemStackTransaction transaction = playerComponent.getInventory()
                        .getCombinedHotbarFirst()
                        .addItemStack(new ItemStack(item.getId(), amount, null));

                    ItemStack remainder = transaction.getRemainder();
                    if (remainder != null && !remainder.isEmpty()) {
                        future.complete("Gave " + (amount - remainder.getQuantity()) + " " + itemName + " to " + playerName + " (inventory full, " + remainder.getQuantity() + " dropped)");
                    } else {
                        future.complete("Gave " + amount + " " + itemName + " to " + playerName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error giving item: " + e.getMessage());
                    e.printStackTrace();
                    future.complete("Error: " + e.getMessage());
                }
            });

            String resultMessage = future.get(5, TimeUnit.SECONDS);

            Map<String, Object> result = new HashMap<>();
            result.put("success", !resultMessage.startsWith("Error") && !resultMessage.contains("not found"));
            result.put("rawResult", resultMessage);
            return result;

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error handling give command: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("rawResult", "Error: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> buildPlayerLocationsResponse() {
        try {
            com.hypixel.hytale.server.core.universe.Universe universe =
                com.hypixel.hytale.server.core.universe.Universe.get();

            if (universe == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("rawResult", "Universe is null - cannot get player locations");
                return result;
            }

            List<PlayerRef> players = universe.getPlayers();

            if (players.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("rawResult", "No players online");
                return result;
            }

            StringBuilder output = new StringBuilder();
            output.append("=== ONLINE PLAYERS & LOCATIONS ===\n\n");
            output.append(String.format("Total players: %d\n\n", players.size()));

            for (PlayerRef player : players) {
                String playerName = player.getUsername();
                UUID playerUuid = player.getUuid();

                Ref<EntityStore> ref = player.getReference();
                if (ref == null || !ref.isValid()) {
                    output.append(String.format("%-20s - Not in world\n", playerName));
                    continue;
                }

                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();

                // Get location synchronously
                CompletableFuture<String> locationFuture = new CompletableFuture<>();

                world.execute(() -> {
                    try {
                        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
                        if (transform == null) {
                            locationFuture.complete("Unknown location");
                            return;
                        }

                        Vector3d position = transform.getPosition();
                        String location = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                            position.getX(), position.getY(), position.getZ());
                        locationFuture.complete(location);
                    } catch (Exception e) {
                        locationFuture.complete("Error: " + e.getMessage());
                    }
                });

                String location = locationFuture.get(2, TimeUnit.SECONDS);
                output.append(String.format("%-20s - %s\n", playerName, location));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("rawResult", output.toString());
            return result;

        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error building player locations: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("rawResult", "Error getting player locations: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> buildHelpResponse() {
        StringBuilder help = new StringBuilder();
        help.append("=== TAKARO API ACTIONS ===\n\n");

        help.append("1. testReachability\n");
        help.append("   Description: Test if the server is reachable\n");
        help.append("   Payload: {}\n\n");

        help.append("2. getPlayers\n");
        help.append("   Description: Get list of online players\n");
        help.append("   Payload: {}\n\n");

        help.append("3. getServerInfo\n");
        help.append("   Description: Get server information\n");
        help.append("   Payload: {}\n\n");

        help.append("4. sendMessage\n");
        help.append("   Description: Send message to all players (supports [red]text[-] or [ff0000]text[-])\n");
        help.append("   Payload: {\"args\": \"{\\\"message\\\":\\\"[red]Hello[-]\\\"}\"}}\n\n");

        help.append("5. executeCommand / executeConsoleCommand\n");
        help.append("   Description: Execute a console command\n");
        help.append("   Payload: {\"args\": \"{\\\"command\\\":\\\"who\\\"}\"}\n\n");

        help.append("6. giveItem\n");
        help.append("   Description: Give an item to a player\n");
        help.append("   Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\",\\\"item\\\":\\\"Wood_Oak_Trunk\\\",\\\"amount\\\":10}\"}\n\n");

        help.append("7. kickPlayer\n");
        help.append("   Description: Kick a player from the server\n");
        help.append("   Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\",\\\"reason\\\":\\\"kicked\\\"}\"}\n\n");

        help.append("8. banPlayer (not implemented)\n");
        help.append("   Description: Ban a player\n");
        help.append("   Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\"}\"}\n\n");

        help.append("9. unbanPlayer (not implemented)\n");
        help.append("   Description: Unban a player\n");
        help.append("   Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\"}\"}\n\n");

        help.append("10. getPlayerLocation\n");
        help.append("    Description: Get player's current position\n");
        help.append("    Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\"}\"}\n");
        help.append("    Returns: {\"x\": 123.45, \"y\": 67.89, \"z\": -234.56}\n\n");

        help.append("11. teleportPlayer\n");
        help.append("    Description: Teleport player to coordinates\n");
        help.append("    Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\",\\\"x\\\":100,\\\"y\\\":64,\\\"z\\\":200}\"}\n\n");

        help.append("12. teleportPlayerToPlayer\n");
        help.append("    Description: Teleport player to another player\n");
        help.append("    Payload: {\"args\": \"{\\\"gameId\\\":\\\"source-uuid\\\",\\\"targetGameId\\\":\\\"target-uuid\\\"}\"}\n\n");

        help.append("13. listCommands\n");
        help.append("    Description: Get all available Hytale server commands\n");
        help.append("    Payload: {}\n\n");

        help.append("14. getPlayerInventory (not implemented)\n");
        help.append("    Description: Get player's inventory\n");
        help.append("    Payload: {\"args\": \"{\\\"gameId\\\":\\\"uuid\\\"}\"}\n\n");

        help.append("15. getAvailableActions / help\n");
        help.append("    Description: Get this list (API version)\n");
        help.append("    Payload: {}\n\n");

        help.append("=== CONSOLE COMMANDS ===\n");
        help.append("Type these in Takaro console:\n");
        help.append("  - getavailableactions / takarohelp / takaro (shows this help)\n");
        help.append("  - playerlocations / locations / whereis / players (shows all online players & locations)\n");
        help.append("  - give <player> <item> [amount] (give items to players, e.g. 'give Mad001 Wood_Oak_Trunk 10')\n");
        help.append("  - who, version, kick, etc. (standard Hytale commands)\n\n");

        help.append("For Hytale commands, use 'listCommands' API action to see all available.\n");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rawResult", help.toString());
        return result;
    }

    private Object handleGetAvailableActions() {
        List<Map<String, Object>> actions = new ArrayList<>();

        // testReachability
        Map<String, Object> testReachability = new HashMap<>();
        testReachability.put("action", "testReachability");
        testReachability.put("description", "Test if the server is reachable");
        testReachability.put("payload", "{}");
        testReachability.put("returns", "{\"connectable\": true, \"reason\": null}");
        actions.add(testReachability);

        // getPlayers
        Map<String, Object> getPlayers = new HashMap<>();
        getPlayers.put("action", "getPlayers");
        getPlayers.put("description", "Get list of online players");
        getPlayers.put("payload", "{}");
        getPlayers.put("returns", "[{\"name\": \"PlayerName\", \"gameId\": \"uuid\", \"platformId\": \"hytale:uuid\", \"ip\": \"127.0.0.1\"}]");
        actions.add(getPlayers);

        // getServerInfo
        Map<String, Object> getServerInfo = new HashMap<>();
        getServerInfo.put("action", "getServerInfo");
        getServerInfo.put("description", "Get server information");
        getServerInfo.put("payload", "{}");
        getServerInfo.put("returns", "{\"name\": \"Hytale Server\", \"version\": \"1.0\"}");
        actions.add(getServerInfo);

        // sendMessage
        Map<String, Object> sendMessage = new HashMap<>();
        sendMessage.put("action", "sendMessage");
        sendMessage.put("description", "Send a message to all players (supports color codes: [red]text[-] or [ff0000]text[-])");
        sendMessage.put("payload", "{\"args\": \"{\\\"message\\\":\\\"[red]Hello[-] everyone!\\\"}\"}");
        sendMessage.put("returns", "{\"success\": true}");
        actions.add(sendMessage);

        // executeCommand / executeConsoleCommand
        Map<String, Object> executeCommand = new HashMap<>();
        executeCommand.put("action", "executeCommand / executeConsoleCommand");
        executeCommand.put("description", "Execute a console command and capture output");
        executeCommand.put("payload", "{\"args\": \"{\\\"command\\\":\\\"who\\\"}\"}");
        executeCommand.put("returns", "{\"success\": true, \"rawResult\": \"Console executed command: who\\ndefault (1): : PlayerName (PlayerName)\"}");
        actions.add(executeCommand);

        // giveItem
        Map<String, Object> giveItem = new HashMap<>();
        giveItem.put("action", "giveItem");
        giveItem.put("description", "Give an item to a player");
        giveItem.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\",\\\"item\\\":\\\"Wood_Deadwood_Decorative\\\",\\\"amount\\\":10}\"}");
        giveItem.put("returns", "{\"success\": true}");
        actions.add(giveItem);

        // kickPlayer
        Map<String, Object> kickPlayer = new HashMap<>();
        kickPlayer.put("action", "kickPlayer");
        kickPlayer.put("description", "Kick a player from the server");
        kickPlayer.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\",\\\"reason\\\":\\\"You were kicked\\\"}\"}");
        kickPlayer.put("returns", "{\"success\": true}");
        actions.add(kickPlayer);

        // banPlayer
        Map<String, Object> banPlayer = new HashMap<>();
        banPlayer.put("action", "banPlayer");
        banPlayer.put("description", "Ban a player (not implemented yet)");
        banPlayer.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\"}\"}");
        banPlayer.put("returns", "{\"success\": true}");
        actions.add(banPlayer);

        // unbanPlayer
        Map<String, Object> unbanPlayer = new HashMap<>();
        unbanPlayer.put("action", "unbanPlayer");
        unbanPlayer.put("description", "Unban a player (not implemented yet)");
        unbanPlayer.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\"}\"}");
        unbanPlayer.put("returns", "{\"success\": true}");
        actions.add(unbanPlayer);

        // getPlayerLocation
        Map<String, Object> getPlayerLocation = new HashMap<>();
        getPlayerLocation.put("action", "getPlayerLocation");
        getPlayerLocation.put("description", "Get a player's current position");
        getPlayerLocation.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\"}\"}");
        getPlayerLocation.put("returns", "{\"x\": 123.45, \"y\": 67.89, \"z\": -234.56}");
        actions.add(getPlayerLocation);

        // teleportPlayer
        Map<String, Object> teleportPlayer = new HashMap<>();
        teleportPlayer.put("action", "teleportPlayer");
        teleportPlayer.put("description", "Teleport a player to coordinates");
        teleportPlayer.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\",\\\"x\\\":100,\\\"y\\\":64,\\\"z\\\":200}\"}");
        teleportPlayer.put("returns", "{\"success\": true}");
        actions.add(teleportPlayer);

        // teleportPlayerToPlayer
        Map<String, Object> teleportPlayerToPlayer = new HashMap<>();
        teleportPlayerToPlayer.put("action", "teleportPlayerToPlayer");
        teleportPlayerToPlayer.put("description", "Teleport a player to another player");
        teleportPlayerToPlayer.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"source-uuid\\\",\\\"targetGameId\\\":\\\"target-uuid\\\"}\"}");
        teleportPlayerToPlayer.put("returns", "{\"success\": true}");
        actions.add(teleportPlayerToPlayer);

        // listCommands
        Map<String, Object> listCommands = new HashMap<>();
        listCommands.put("action", "listCommands");
        listCommands.put("description", "Get list of all available Hytale server commands");
        listCommands.put("payload", "{}");
        listCommands.put("returns", "[{\"name\": \"help\", \"description\": \"Shows help\", \"aliases\": [\"?\"]}]");
        actions.add(listCommands);

        // getPlayerInventory
        Map<String, Object> getPlayerInventory = new HashMap<>();
        getPlayerInventory.put("action", "getPlayerInventory");
        getPlayerInventory.put("description", "Get a player's inventory (not implemented yet)");
        getPlayerInventory.put("payload", "{\"args\": \"{\\\"gameId\\\":\\\"player-uuid\\\"}\"}");
        getPlayerInventory.put("returns", "[]");
        actions.add(getPlayerInventory);

        // getAvailableActions / help
        Map<String, Object> help = new HashMap<>();
        help.put("action", "getAvailableActions / help");
        help.put("description", "Get this list of available actions");
        help.put("payload", "{}");
        help.put("returns", "[{\"action\": \"...\", \"description\": \"...\", \"payload\": \"...\", \"returns\": \"...\"}]");
        actions.add(help);

        plugin.getLogger().at(java.util.logging.Level.INFO).log("Returning " + actions.size() + " available actions");
        return actions;
    }
}
