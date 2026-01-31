package dev.takaro.hytale.handlers;

import com.google.gson.JsonObject;
import dev.takaro.hytale.TakaroPlugin;
import dev.takaro.hytale.api.HytaleApiClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TakaroRequestHandler {
    private final TakaroPlugin plugin;
    private final HytaleApiClient hytaleApi;

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
                case "listBans":
                case "listItems":
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
        // TODO: Implement actual player fetching from Hytale server API
        // Example approach:
        // 1. Get players from server: server.getOnlinePlayers()
        // 2. Enrich with Hytale API data if needed (profiles, UUIDs)
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Getting players list");

        try {
            // Example: Use Hytale API to enrich player data
            // JsonObject playerData = hytaleApi.lookupPlayerByName("PlayerName");
            // JsonObject profile = hytaleApi.getPlayerProfile(uuid);

            // For now, return empty until Hytale server API is integrated
            return new Object[0];
        } catch (Exception e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error getting players: " + e.getMessage());
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
        // TODO: Implement sending message to game chat
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Sending message: " + payload.toString());
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private Object handleExecuteCommand(JsonObject payload) {
        // TODO: Implement command execution
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Executing command: " + payload.toString());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("rawResult", "Command executed");
        return result;
    }

    private Object handleKickPlayer(JsonObject payload) {
        // TODO: Implement player kick
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Kicking player: " + payload.toString());
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
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
        // TODO: Implement player location fetching
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Getting player location: " + payload.toString());
        Map<String, Integer> location = new HashMap<>();
        location.put("x", 0);
        location.put("y", 0);
        location.put("z", 0);
        return location;
    }

    private Object handleTeleportPlayer(JsonObject payload) {
        // TODO: Implement player teleportation
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Teleporting player: " + payload.toString());
        Map<String, Boolean> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
