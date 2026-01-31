# Hytale-Takaro Integration Mod

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-24+-orange.svg)](https://adoptium.net/)
[![Hytale](https://img.shields.io/badge/Hytale-Server%20Plugin-blue.svg)](https://hytale.com/)
[![Takaro](https://img.shields.io/badge/Takaro-Integration-green.svg)](https://takaro.dev/)

A Hytale server plugin that integrates with Takaro for server management, chat relay, and player tracking.

> **Note**: This plugin runs **inside** the Hytale server process (not as a separate bridge). Players don't need to install anything client-side.

## 🎯 Put Your Server on the Billboards!

> **✨ NEW: HytaleCharts Integration Built-In!**
>
> This mod now includes **HytaleCharts** integration for automatic server status reporting and player tracking. Get your server listed on the public server directory and attract more players!
>
> **📊 [Get started at HytaleCharts.com →](https://hytalecharts.com/)**
>
> Features:
> - 📈 **Automatic heartbeat** - Server status updates every 5 minutes
> - 👥 **Player tracking** - Live player count and online player lists
> - 🔗 **Promotional links** - Customizable clickable messages for players
> - 📣 **Server visibility** - Get discovered by new players
>
> Simply add your `HYTALECHARTS_SECRET` to the config (see [Configuration](#configuration) below)!

## 📚 Documentation

- **[Installation Guide](#installation)** - How to set up the plugin
- **[Configuration](#configuration)** - Config file options
- **[Hytale API Integration](HYTALE_API_INTEGRATION.md)** - Complete API reference
- **[Implementation Status](IMPLEMENTATION_STATUS.md)** - Current status & roadmap
- **[Contributing](CONTRIBUTING.md)** - How to contribute

## ⚡ Quick Start

```bash
# 1. Build the plugin
mvn clean package

# 2. Copy to your Hytale server
cp target/HytaleTakaroMod-X.X.X.jar /path/to/hytale/mods/

# 3. Start server to generate config
# Config will be created at: mods/TakaroConfig.properties

# 4. Edit config with your tokens
# IDENTITY_TOKEN, REGISTRATION_TOKEN, HYTALECHARTS_SECRET (optional)

# 5. Restart server
```

## Features

**Takaro Integration:**
- WebSocket connection to Takaro platform
- Real-time chat message relay
- Player connect/disconnect events
- Server command execution
- Player management (kick, ban, teleport)
- Server info reporting
- Customizable command prefix and response messages

**HytaleCharts Integration:**
- Automatic heartbeat to HytaleCharts.com (every 5 minutes)
- Real-time player count and max players reporting
- Online player list with usernames, UUIDs, and worlds
- Configurable promotional link system (on-login and periodic broadcasts)
- Server visibility on public server directory

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
2. Copy `target/HytaleTakaroMod-X.X.X.jar` to your Hytale server's `mods` directory
3. Start your Hytale server (config will be auto-generated)
4. Edit the generated `mods/TakaroConfig.properties` file:
   ```properties
   # Takaro Settings (required)
   IDENTITY_TOKEN=MyHytaleServer
   REGISTRATION_TOKEN=your_registration_token_here
   COMMAND_PREFIX=!
   COMMAND_RESPONSE=[cyan]Command Received[-] [green]{prefix}{command}[-]

   # HytaleCharts (optional - get your secret at hytalecharts.com)
   HYTALECHARTS_SECRET=your_secret_here
   HYTALECHARTS_DEBUG=false
   HYTALECHARTS_PROMO_ON_LOGIN=true
   HYTALECHARTS_PROMO_ENABLED=false
   ```
5. Restart your Hytale server
6. **(Optional)** Set up HytaleCharts:
   - Visit [hytalecharts.com](https://hytalecharts.com/)
   - Generate a heartbeat secret
   - Add it to `HYTALECHARTS_SECRET` in your config
   - Restart to start sending heartbeats!

## Configuration

The configuration file is automatically created at `mods/TakaroConfig.properties`:

**Takaro Settings:**
- `IDENTITY_TOKEN`: Your Takaro identity token (required) - Choose a name for your server
- `REGISTRATION_TOKEN`: Your Takaro registration token (get from Takaro dashboard)
- `COMMAND_PREFIX`: Prefix for Takaro commands (default: `!`) - Can be changed to `.`, `-`, etc. (do not use `/` - reserved by Hytale)
- `COMMAND_RESPONSE`: Private message sent when command received. Use `{prefix}` and `{command}` placeholders. Supports color codes like `[cyan]text[-]`.
  - Example: `[cyan]Command Received[-] [green]{prefix}{command}[-]`

**HytaleCharts Integration:**
- `HYTALECHARTS_SECRET`: Your heartbeat secret from [hytalecharts.com](https://hytalecharts.com/) (required to enable)
- `HYTALECHARTS_DEBUG`: Enable debug logging (default: `false`)
- `HYTALECHARTS_PROMO_ON_LOGIN`: Send promo link when player joins (default: `true`)
- `HYTALECHARTS_PROMO_ENABLED`: Enable periodic promo link broadcasts (default: `false`)
- `HYTALECHARTS_PROMO_INTERVAL_MINUTES`: How often to broadcast promo link in minutes (default: `15`)
- `HYTALECHARTS_PROMO_PREFIX`: Prefix before promo message (default: `[hytalecharts.com] `)
- `HYTALECHARTS_PROMO_MESSAGE`: Promo link message text (default: `Vote for our server on HytaleCharts!`)
- `HYTALECHARTS_PROMO_URL`: URL for the clickable link (default: `https://hytalecharts.com`)

**Hytale API Settings (Optional):**
- `HYTALE_API_URL`: Hytale first-party API URL (default: `https://api.hytale.com`)
- `HYTALE_API_TOKEN`: Your authenticated server token from Hytale (required for API features)

**Dev Takaro (Optional - for developers only):**
- `DEV_ENABLED`: Enable secondary dev Takaro connection (default: `false`)
- `DEV_IDENTITY_TOKEN`: Dev server identity token
- `DEV_REGISTRATION_TOKEN`: Dev server registration token

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
