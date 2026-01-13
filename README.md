# Hytale-Takaro Integration Mod

A Hytale server plugin that integrates with Takaro for server management, chat relay, and player tracking.

## Features

**Takaro Integration:**
- WebSocket connection to Takaro platform
- Real-time chat message relay
- Player connect/disconnect events
- Server command execution
- Player management (kick, ban, teleport)
- Server info reporting

**Hytale First-Party API Integration:**
- UUID ↔ Name lookups (single and bulk)
- Player profile fetching (cosmetics, avatars, public data)
- Game version checking
- Automatic server telemetry reporting (every 5 minutes)
- Player reporting for ToS violations
- Payment processing via Hytale's payment gate
- Global sanctions checking (when available)
- Friends list retrieval (when available)
- Webhook subscriptions (when available)

See [HYTALE_API_INTEGRATION.md](HYTALE_API_INTEGRATION.md) for detailed API documentation.

## Building

### Prerequisites
- Java 24 or higher
- Maven 3.6+
- Hytale Server installed

### Build Steps

```bash
# Clone/navigate to project
cd HytaleTakaroMod

# Build the plugin JAR
mvn clean package

# Output will be in target/HytaleTakaroMod-1.0.0.jar
```

## Installation

1. Build the plugin as described above
2. Copy `target/HytaleTakaroMod-1.0.0.jar` to your Hytale server's `plugins` directory
3. Start your Hytale server
4. Edit the generated `plugins/TakaroPlugin/config.properties` file:
   ```properties
   TAKARO_WS_URL=wss://connect.next.takaro.dev/
   IDENTITY_TOKEN=your_identity_token_here
   REGISTRATION_TOKEN=your_registration_token_here
   ```
5. Restart your Hytale server

## Configuration

The configuration file is automatically created at `plugins/TakaroPlugin/config.properties`:

**Takaro Settings:**
- `TAKARO_WS_URL`: WebSocket URL for Takaro connection (default: `wss://connect.next.takaro.dev/`)
- `IDENTITY_TOKEN`: Your Takaro identity token (required)
- `REGISTRATION_TOKEN`: Your Takaro registration token (optional)

**Hytale API Settings:**
- `HYTALE_API_URL`: Hytale first-party API URL (default: `https://api.hytale.com`)
- `HYTALE_API_TOKEN`: Your authenticated server token from Hytale (required for API features)

## Architecture

This plugin runs **inside** the Hytale server process (not as a separate bridge):

```
Hytale Server
  └── TakaroPlugin.jar
        ├── WebSocket Client (connects to Takaro)
        ├── Event Listeners (chat, players, etc.)
        └── Request Handlers (commands from Takaro)
```

### Key Components

- **TakaroPlugin**: Main plugin class, manages lifecycle
- **TakaroWebSocket**: WebSocket client for Takaro communication
- **TakaroConfig**: Configuration file handler
- **ChatEventListener**: Listens for chat messages and forwards to Takaro
- **PlayerEventListener**: Listens for player join/leave/death events
- **TakaroRequestHandler**: Handles incoming requests from Takaro

## Development

### Project Structure

```
src/main/java/dev/takaro/hytale/
├── TakaroPlugin.java              # Main plugin class
├── config/
│   └── TakaroConfig.java          # Configuration handler
├── websocket/
│   └── TakaroWebSocket.java       # WebSocket client
├── events/
│   ├── ChatEventListener.java     # Chat event handling
│   └── PlayerEventListener.java   # Player event handling
└── handlers/
    └── TakaroRequestHandler.java  # Takaro request handling
```

### Supported Takaro Actions

- `testReachability` - Check if server is reachable
- `getPlayers` - Get list of online players
- `getServerInfo` - Get server information
- `sendMessage` - Send message to game chat
- `executeCommand` - Execute server command
- `kickPlayer` - Kick a player
- `banPlayer` - Ban a player
- `unbanPlayer` - Unban a player
- `getPlayerLocation` - Get player coordinates
- `teleportPlayer` - Teleport player

### Events Sent to Takaro

- `chat-message` - Player chat messages
- `player-connected` - Player joins server
- `player-disconnected` - Player leaves server
- `player-death` - Player dies

## Troubleshooting

### Plugin not loading
- Check that HytaleServer.jar is in the correct location (`../libs/HytaleServer.jar`)
- Verify Java version is 24+
- Check server logs for errors

### Not connecting to Takaro
- Verify `IDENTITY_TOKEN` is set in config.properties
- Check network connectivity to `connect.next.takaro.dev`
- Review plugin logs for WebSocket errors

### Events not being sent
- Ensure WebSocket connection is established (check logs for "Successfully identified with Takaro")
- Verify event listeners are properly registered with Hytale's event system

## License

[Add your license here]

## Credits

Based on the Palworld-Takaro Bridge architecture.
