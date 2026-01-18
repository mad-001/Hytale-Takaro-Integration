# Hytale Server API Reference

This document contains the actual API structure discovered by decompiling HytaleServer.jar using Vineflower.

**Last Updated**: 2026-01-13
**Decompiler**: Vineflower 1.11.2
**Source**: HytaleServer.jar (from Hytale installation)

---

## Table of Contents

1. [Event System](#event-system)
2. [Player Events](#player-events)
3. [PlayerRef API](#playerref-api)
4. [Plugin Base API](#plugin-base-api)
5. [Server Access](#server-access)
6. [Command System](#command-system)

---

## Event System

### Event Registration

Events are registered via the `EventRegistry` obtained from the plugin base class:

```java
this.getEventRegistry().registerGlobal(EventClass.class, this::methodReference);
```

**Example:**
```java
this.getEventRegistry().registerGlobal(
    PlayerChatEvent.class,
    chatListener::onPlayerChat
);
```

### Event Types

Events are located in: `com.hypixel.hytale.server.core.event.events`

**Available Event Categories:**
- `player/` - Player-related events
- `entity/` - Entity events
- `permissions/` - Permission events
- `ecs/` - Entity Component System events

---

## Player Events

### PlayerChatEvent

**Location**: `com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent`

**Implements**: `IAsyncEvent<String>`, `ICancellable`

**Methods:**
```java
@Nonnull PlayerRef getSender()              // Get the player who sent the message
@Nonnull String getContent()                // Get the message content
@Nonnull List<PlayerRef> getTargets()      // Get message recipients
void setContent(@Nonnull String content)    // Modify message
void setCancelled(boolean cancelled)        // Cancel the event
boolean isCancelled()                       // Check if cancelled
```

**Handler Example:**
```java
public void onPlayerChat(PlayerChatEvent event) {
    String playerName = event.getSender().getUsername();
    String uuid = event.getSender().getUUID().toString();
    String message = event.getContent();

    // Optionally cancel or modify
    // event.setCancelled(true);
    // event.setContent("Modified message");
}
```

---

### PlayerConnectEvent

**Location**: `com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent`

**Implements**: `IEvent<Void>`

**Methods:**
```java
@Nonnull PlayerRef getPlayerRef()              // Get player reference
@Nullable World getWorld()                     // Get world player is connecting to
void setWorld(@Nullable World world)           // Set world
@Deprecated @Nullable Player getPlayer()       // Legacy method (use getPlayerRef)
Holder<EntityStore> getHolder()                // Get entity store holder
```

**Handler Example:**
```java
public void onPlayerConnect(PlayerConnectEvent event) {
    PlayerRef playerRef = event.getPlayerRef();
    String playerName = playerRef.getUsername();
    String uuid = playerRef.getUUID().toString();
    World world = event.getWorld();

    // Forward to Takaro or perform actions
}
```

---

### PlayerDisconnectEvent

**Location**: `com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent`

**Extends**: `PlayerRefEvent<Void>`

**Methods:**
```java
@Nonnull PlayerRef getPlayerRef()                           // Get player reference
@Nonnull PacketHandler.DisconnectReason getDisconnectReason()  // Get reason for disconnect
```

**Handler Example:**
```java
public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    PlayerRef playerRef = event.getPlayerRef();
    String playerName = playerRef.getUsername();
    String uuid = playerRef.getUUID().toString();

    PacketHandler.DisconnectReason reason = event.getDisconnectReason();
    // Forward to Takaro
}
```

---

### Other Player Events Available

From decompiled code, additional player events exist:

- `PlayerMouseButtonEvent` - Mouse button interactions
- `PlayerMouseMotionEvent` - Mouse movement
- `PlayerRefEvent` - Base class for player events
- `PlayerInteractEvent` - Player interaction events
- `PlayerReadyEvent` - Player ready state
- `PlayerSetupConnectEvent` - Pre-connection setup
- `PlayerSetupDisconnectEvent` - Pre-disconnection setup
- `PlayerCraftEvent` - Crafting events
- `AddPlayerToWorldEvent` - Player added to world
- `DrainPlayerFromWorldEvent` - Player removed from world

---

## PlayerRef API

**Location**: `com.hypixel.hytale.server.core.universe.PlayerRef`

**Implements**: `Component<EntityStore>`, `MetricProvider`, `IMessageReceiver`

### Key Methods

```java
// Identity
@Nonnull UUID getUUID()                    // Player's unique ID
@Nonnull String getUsername()              // Player's username
@Nonnull String getLanguage()              // Player's language setting

// Networking
@Nonnull PacketHandler getPacketHandler()  // Get packet handler
@Nonnull HostAddress getHostAddress()      // Get connection address

// Components
@Nonnull ChunkTracker getChunkTracker()    // Get chunk tracker
@Nullable Ref<EntityStore> getReference()  // Get entity reference

// Messaging
void sendMessage(@Nonnull Message message) // Send message to player
void sendServerMessage(@Nonnull ServerMessage message)
```

---

## Plugin Base API

**Location**: `com.hypixel.hytale.server.core.plugin.PluginBase`

### Lifecycle Methods

```java
protected abstract void setup()       // Called during plugin setup phase
protected void start()               // Called when plugin starts
protected void shutdown()            // Called when plugin shuts down
```

### Registry Access

```java
@Nonnull EventRegistry getEventRegistry()              // Event registration
@Nonnull CommandRegistry getCommandRegistry()          // Command registration (deprecated)
@Nonnull ClientFeatureRegistry getClientFeatureRegistry()
@Nonnull BlockStateRegistry getBlockStateRegistry()
@Nonnull EntityRegistry getEntityRegistry()
@Nonnull TaskRegistry getTaskRegistry()
@Nonnull ComponentRegistryProxy<EntityStore> getEntityStoreRegistry()
@Nonnull ComponentRegistryProxy<ChunkStore> getChunkStoreRegistry()
```

### Plugin Information

```java
@Nonnull HytaleLogger getLogger()           // Plugin logger
@Nonnull PluginIdentifier getIdentifier()   // Plugin ID
@Nonnull PluginManifest getManifest()       // Plugin manifest
@Nonnull Path getDataDirectory()            // Plugin data directory (not File!)
@Nonnull PluginState getState()             // Current plugin state
@Nonnull String getName()                   // Plugin name
```

---

## Server Access

### Getting Server Instance

**Use the static getter:**
```java
HytaleServer server = HytaleServer.get();
```

**NOT:**
```java
// ❌ WRONG - this method doesn't exist
this.getServer()
```

### Server Methods

**Location**: `com.hypixel.hytale.server.core.HytaleServer`

```java
// Managers
@Nonnull EventBus getEventBus()
@Nonnull PluginManager getPluginManager()
@Nonnull CommandManager getCommandManager()

// Configuration
@Nonnull HytaleServerConfig getConfig()

// Server Info
String getServerName()
int getMaxPlayers()
boolean isShuttingDown()
```

---

## Command System

### Registering Commands

**Correct Pattern:**
```java
HytaleServer.get().getCommandManager().registerCommand(new MyCommand());
```

**NOT:**
```java
// ❌ WRONG - deprecated pattern
this.getCommandRegistry().registerCommand(...)
```

### Command Location

Commands should extend or implement the command classes in:
`com.hypixel.hytale.server.core.command.system`

---

## Important Corrections from Template

### ❌ Wrong (from template/docs)

```java
// Event handler
event.getPlayer().getDisplayName()     // NO - doesn't exist
event.getMessage()                     // NO - wrong method name
this.getServer()                       // NO - doesn't exist
new File(getDataFolder(), "config")    // NO - getDataFolder() doesn't exist
this.getCommandRegistry()              // NO - deprecated/wrong pattern
```

### ✅ Correct (from decompiled API)

```java
// Event handler
event.getSender().getUsername()        // YES - correct for chat
event.getPlayerRef().getUsername()     // YES - correct for connect/disconnect
event.getContent()                     // YES - correct method name
HytaleServer.get()                     // YES - correct server access
getDataDirectory().resolve("config").toFile()  // YES - returns Path
HytaleServer.get().getCommandManager() // YES - correct command registration
```

---

## File Locations in Decompiled Code

All decompiled source is in: `/home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration/decompile/`

**Key Directories:**
- `com/hypixel/hytale/server/core/` - Core server API
- `com/hypixel/hytale/server/core/event/events/` - Event classes
- `com/hypixel/hytale/server/core/plugin/` - Plugin system
- `com/hypixel/hytale/server/core/command/` - Command system
- `com/hypixel/hytale/server/core/universe/` - Universe and player management

---

## Additional Notes

1. **Java Version**: Server requires Java 21+ (uses sealed classes and modern Java features)

2. **Component System**: Hytale uses an ECS (Entity Component System) architecture heavily

3. **Async Events**: Some events implement `IAsyncEvent` meaning they can be handled asynchronously

4. **Cancellable Events**: Events implementing `ICancellable` can be cancelled via `setCancelled(true)`

5. **Metrics**: Most core classes implement `MetricProvider` for monitoring

6. **Logging**: Uses custom `HytaleLogger` class, not standard Java logging

7. **QUIC Protocol**: Server uses QUIC over UDP (port 5520 default) for networking

---

## See Also

- [Hytale Server Manual](./Hytale_Server_Manual.md)
- [Implementation Status](../HytaleTakaroMod/IMPLEMENTATION_STATUS.md)
- [Required Downloads](./REQUIRED_DOWNLOADS.md)

---

## Decompilation Notes

**Tool Used**: Vineflower 1.11.2 (modern FernFlower fork)

**Command:**
```bash
java -jar vineflower.jar libs/HytaleServer.jar decompile/
```

**Output Size**: ~9.6MB of decompiled Java code

**Accuracy**: Decompiled code is highly accurate, showing actual method signatures, class hierarchies, and implementation details.
