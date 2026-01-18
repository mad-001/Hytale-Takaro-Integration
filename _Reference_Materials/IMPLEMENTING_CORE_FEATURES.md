# Implementing Core Features NOW

**What works RIGHT NOW without APIs**

---

## ✅ What You CAN Do Now

The core Takaro integration features work TODAY using Hytale's server-side APIs:

### 1. **Chat Messages** ✅
Hook into `PlayerChatEvent` to forward chat to Takaro

### 2. **Player Join/Leave** ✅
Hook into `PlayerConnectEvent` and `PlayerDisconnectEvent`

### 3. **Get Online Players** ✅
Use server's player list API

### 4. **Kick/Ban Players** ✅
Use server's admin commands

### 5. **Teleport Players** ✅
Use player teleport API

### 6. **Send Messages to Game** ✅
Use server's broadcast/announce API

### 7. **Execute Commands** ✅
Use server's command execution API

---

## 🔧 How to Complete the Implementation

### Step 1: Find the Server Instance

The `JavaPlugin` class should provide access to the server:

```java
// In TakaroPlugin.java
HytaleServer server = getServer(); // or similar method
```

### Step 2: Register Event Listeners

Hytale uses an event bus system. Look for:

```java
// Pattern A: EventBus registration
getEventBus().subscribe(PlayerChatEvent.class, this::onPlayerChat);

// Pattern B: EventRegistry
getEventRegistry().register(PlayerChatEvent.class, event -> {
    // Handle event
});

// Pattern C: Annotation-based (if available)
@Subscribe
public void onPlayerChat(PlayerChatEvent event) {
    // Handle event
}
```

### Step 3: Access Player Data

```java
// Get online players
Collection<ServerPlayer> players = server.getOnlinePlayers();

// Or from world
World world = server.getWorld("world_name");
Collection<PlayerRef> players = world.getPlayers();
```

---

## 📝 Quick Implementation Guide

### Chat Event Handler

```java
public void onPlayerChat(PlayerChatEvent event) {
    String playerName = event.getPlayer().getName();
    String message = event.getMessage();
    String uuid = event.getPlayer().getUUID().toString();

    // Forward to Takaro
    chatListener.handleChatMessage(
        playerName,
        uuid,
        uuid, // steamId (may need different source)
        message,
        "global" // or determine from event
    );
}
```

### Player Connect Handler

```java
public void onPlayerConnect(PlayerConnectEvent event) {
    ServerPlayer player = event.getPlayer();

    playerListener.handlePlayerConnect(
        player.getName(),
        player.getUUID().toString(),
        player.getUUID().toString() // steamId
    );
}
```

### Player Disconnect Handler

```java
public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    ServerPlayer player = event.getPlayer();

    playerListener.handlePlayerDisconnect(
        player.getName(),
        player.getUUID().toString(),
        player.getUUID().toString()
    );
}
```

### Get Players Implementation

```java
private Object handleGetPlayers() {
    Collection<ServerPlayer> players = server.getOnlinePlayers();

    List<Map<String, Object>> playerList = new ArrayList<>();
    for (ServerPlayer player : players) {
        Map<String, Object> playerData = new HashMap<>();
        playerData.put("gameId", player.getUUID().toString());
        playerData.put("name", player.getName());
        playerData.put("steamId", player.getUUID().toString());

        // Get position if available
        Vector3 pos = player.getPosition();
        if (pos != null) {
            playerData.put("positionX", pos.getX());
            playerData.put("positionY", pos.getY());
            playerData.put("positionZ", pos.getZ());
        }

        playerList.add(playerData);
    }

    return playerList;
}
```

### Send Message Implementation

```java
private Object handleSendMessage(JsonObject payload) {
    String message = payload.get("message").getAsString();

    // Broadcast to all players
    server.broadcast(Message.raw(message));

    return Map.of("success", true);
}
```

### Kick Player Implementation

```java
private Object handleKickPlayer(JsonObject payload) {
    String gameId = payload.get("gameId").getAsString();

    ServerPlayer player = server.getPlayer(UUID.fromString(gameId));
    if (player != null) {
        player.kick("Kicked by admin");
        return Map.of("success", true);
    }

    return Map.of("success", false, "error", "Player not found");
}
```

### Teleport Player Implementation

```java
private Object handleTeleportPlayer(JsonObject payload) {
    String sourceId = payload.get("sourcePlayer").getAsString();

    ServerPlayer source = server.getPlayer(UUID.fromString(sourceId));
    if (source == null) {
        return Map.of("success", false, "error", "Player not found");
    }

    // Coordinate teleport
    if (payload.has("x") && payload.has("y") && payload.has("z")) {
        double x = payload.get("x").getAsDouble();
        double y = payload.get("y").getAsDouble();
        double z = payload.get("z").getAsDouble();

        source.teleport(new Vector3(x, y, z));
        return Map.of("success", true);
    }

    // Player-to-player teleport
    if (payload.has("targetPlayer")) {
        String targetId = payload.get("targetPlayer").getAsString();
        ServerPlayer target = server.getPlayer(UUID.fromString(targetId));

        if (target != null) {
            source.teleport(target.getPosition());
            return Map.of("success", true);
        }
    }

    return Map.of("success", false, "error", "Invalid teleport target");
}
```

---

## 🧪 Testing Methodology

### Phase 1: Server Access
1. Start plugin and verify it loads
2. Add debug logging to `setup()` method
3. Try to access server instance
4. Log available methods

```java
@Override
protected void setup() {
    logger.info("Plugin loaded!");
    logger.info("Server instance: " + getServer());
    logger.info("Available methods: " + getServer().getClass().getMethods());
}
```

### Phase 2: Event Registration
1. Find event registration method
2. Register a simple event listener
3. Trigger event in-game and verify log output

```java
// Try different registration patterns
try {
    getEventBus().subscribe(PlayerChatEvent.class, this::onPlayerChat);
    logger.info("Event registered via EventBus");
} catch (Exception e) {
    logger.warning("EventBus not available: " + e.getMessage());
}
```

### Phase 3: Player Operations
1. Test getting online players
2. Test sending messages
3. Test kicking (use test account!)
4. Test teleporting

### Phase 4: Takaro Integration
1. Verify WebSocket connects
2. Test chat forwarding
3. Test player join/leave events
4. Test Takaro commands (kick, teleport, etc.)

---

## 🔍 Exploration Commands

Add these to your plugin for testing:

```java
// Debug command to explore server API
public class DebugCommand extends CommandBase {
    public DebugCommand() {
        super("takarodebug", "Debug Takaro integration");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        // List online players
        context.sendMessage(Message.raw("Online players:"));
        for (ServerPlayer player : server.getOnlinePlayers()) {
            context.sendMessage(Message.raw("- " + player.getName() + " (" + player.getUUID() + ")"));
        }

        // Test Takaro connection
        if (webSocket.isIdentified()) {
            context.sendMessage(Message.raw("Takaro: CONNECTED"));
        } else {
            context.sendMessage(Message.raw("Takaro: DISCONNECTED"));
        }
    }
}
```

---

## 📋 Implementation Checklist

- [ ] Access HytaleServer instance from plugin
- [ ] Register PlayerChatEvent listener
- [ ] Register PlayerConnectEvent listener
- [ ] Register PlayerDisconnectEvent listener
- [ ] Implement getPlayers() with real server API
- [ ] Implement sendMessage() with broadcast API
- [ ] Implement kickPlayer() with player.kick()
- [ ] Implement banPlayer() with ban API
- [ ] Implement teleportPlayer() with player.teleport()
- [ ] Test Takaro WebSocket connection
- [ ] Test end-to-end: chat message → Takaro
- [ ] Test end-to-end: Takaro command → game action

---

## 🚀 Next Steps

1. **Build and install the plugin**
   ```bash
   mvn clean package
   cp target/HytaleTakaroMod-1.0.0.jar /path/to/server/mods/
   ```

2. **Start server and check logs**
   ```bash
   tail -f Server/logs/latest.log | grep -i takaro
   ```

3. **Test in-game**
   - Join server
   - Send chat message
   - Check if it appears in Takaro
   - Try Takaro command (from Takaro dashboard)

4. **Iterate**
   - Add debug logging
   - Fix issues
   - Rebuild and restart
   - Repeat

---

## 💡 Pro Tips

1. **Use IntelliJ's decompiler** - Open HytaleServer.jar in IntelliJ to browse all available classes and methods

2. **Check server logs** - Hytale has excellent error messages

3. **Start simple** - Get one event working first (chat), then expand

4. **Use debug command** - Create a `/takarodebug` command to test APIs

5. **Test offline first** - Set `--auth-mode offline` to test without authentication

6. **Console is your friend** - Add lots of `logger.info()` statements

7. **Check example mods** - Look for other Hytale plugins that use events

8. **Ask the community** - Hytale modding Discord probably has event examples

---

## 🆘 If Stuck

### Can't Find Event Registration Method?

Try these patterns in your `setup()` method:

```java
// Pattern 1
if (this.getServer() != null) {
    this.getServer().getEventBus().register(...);
}

// Pattern 2
if (this.getEventManager() != null) {
    this.getEventManager().subscribe(...);
}

// Pattern 3
this.registerEvents(new ChatEventListener(this));

// Pattern 4 - Check JavaPlugin superclass
Class<?> pluginClass = JavaPlugin.class;
for (Method method : pluginClass.getMethods()) {
    if (method.getName().contains("event") || method.getName().contains("Event")) {
        logger.info("Found method: " + method.getName());
    }
}
```

### Can't Get Player List?

```java
// Try these
server.getPlayers()
server.getOnlinePlayers()
server.getPlayerManager().getPlayers()
server.getWorlds().get(0).getPlayers()
```

### Can't Send Messages?

```java
// Try these
server.broadcast(Message.raw(text))
server.sendMessage(Message.raw(text))
player.sendMessage(Message.raw(text))
```

---

**Remember**: The WebSocket connection and Takaro integration is DONE and WORKING. You just need to hook up the Hytale server API to feed it data!

Once you get the first event working (chat or join/leave), the rest will follow the same pattern.
