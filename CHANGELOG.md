# Changelog

All notable features added to the Hytale-Takaro Integration Mod.

---

## ⚠️ IMPORTANT: Configuration File Changes

**The configuration file has changed location and format!**

**Old Location:** `AppData/Roaming/Hytale/UserData/Saves/<WorldName>/mods/HytaleTakaroMod/TakaroConfig.properties`

**New Location:** `AppData/Roaming/Hytale/UserData/Saves/<WorldName>/mods/TakaroConfig.properties`

### Migration Steps:
1. **Copy your old config file** from the HytaleTakaroMod subdirectory to the mods folder directly
2. **Transfer your settings**:
   - Copy your `IDENTITY_TOKEN` value
   - Copy your `REGISTRATION_TOKEN` value
3. **Delete the old config** and HytaleTakaroMod subdirectory
4. **The new config will be auto-created** on first run with new settings (COMMAND_PREFIX, COMMAND_RESPONSE)

### ⚠️ CRITICAL WARNING - DEV Configuration
**DO NOT ENABLE DEV CONFIGURATION UNLESS YOU ARE A DEVELOPER!**

```properties
# NEVER enable these on production servers:
# DEV_ENABLED=true
# DEV_IDENTITY_TOKEN=...
# DEV_REGISTRATION_TOKEN=...
```

**Enabling dev mode on production servers will cause crashes and instability!** Only use dev configuration if you're actively developing Takaro integrations with a dev Takaro instance.

---

## [1.12.4] - 2026-01-26

### Fixed
- **Automatic Reconnection on Internal Errors**: Detects "Internal error handling game event" from Takaro and automatically reconnects with fresh websocket connection
  - Closes existing connection when internal error detected
  - Reconnects with fresh identity and registration tokens
  - Reinitializes connection cleanly in Takaro
  - Prevents connection state corruption

## [1.12.3] - 2026-01-23

### Added
- **Configurable Command Prefix**: Set custom prefix for Takaro commands (default: `!`)
  - Configure via `COMMAND_PREFIX` in TakaroConfig.properties
  - Supports any custom prefix (avoid `/` - reserved by Hytale)
- **Configurable Command Response**: Private confirmation message when player uses command
  - Configure via `COMMAND_RESPONSE` in TakaroConfig.properties
  - Default: `[cyan]Command[-] [green]{prefix}{command}[-]`
  - Supports `{prefix}` and `{command}` placeholders
  - Full color code support
- **Command Interception**: Commands no longer appear in public chat (behaves like Hytale's `/` commands)

### Changed
- **Configuration File Location**: Moved from `mods/HytaleTakaroMod/TakaroConfig.properties` to `mods/TakaroConfig.properties` (see migration steps above)

## [1.11.3] - 2026-01-19

### Added
- **Clickable Links in Messages**: URLs in Takaro messages are automatically detected and made clickable
  - Supports `http://`, `https://`, and `www.` URLs
  - Links appear in cyan color and open in browser when clicked

## [1.10.0] - 2026-01-19

### Added
- **Player Death Events**: Track and report player deaths to Takaro
  - Uses Hytale's ECS system (RefChangeSystem) to detect deaths
  - Sends death events with player info, position, and damage source
  - Enables death-based stats and respawn commands

## [1.8.5] - 2026-01-18

### Added
- **Dev Takaro Connection**: Optional secondary connection for development/testing
  - Configure via `DEV_ENABLED=true` in TakaroConfig.properties
  - Separate dev identity and registration tokens
  - Production and dev connections run simultaneously
- **Friendly Item Names**: Item lists now show display names (e.g., "Crude Arrow") instead of codes
  - Uses Hytale's I18n system for proper localization
  - Fallback to item code if translation unavailable

## [1.7.5] - 2026-01-18

### Added
- **Console Commands for All API Actions**: Execute any API action via console
  - `testReachability` - Test connection to Takaro
  - `getPlayers` - List all online players
  - `getServerInfo` - Display server information
  - `listItems` - Show all available items
  - `sendMessage <message>` - Broadcast message to all players
  - `getPlayerLocation <player>` - Get player coordinates
  - `getPlayerInventory <player>` - View player inventory
  - `kickPlayer <player> [reason]` - Kick player from server
  - `banPlayer <player>` - Ban player
  - `unbanPlayer <player>` - Unban player
- **Enhanced Help System**: Categorized commands with examples and argument descriptions

## [1.4.0] - 2026-01-17

### Added
- **Player Name Colors**: Custom chat name colors via Takaro permissions
  - Takaro can set player name colors via `setPlayerNameColor` API action
  - Supports hex colors (e.g., `ff0000`) and named colors (e.g., `gold`, `red`, `blue`)
  - Colors cached in memory for instant application
  - Console command: `setcolor <player> <color>`
- **Color Code System**: Rich text formatting in messages
  - Format: `[color]text[-]` (e.g., `[red]Warning[-]`)
  - Supports hex: `[ff0000]text[-]`
  - Supports named colors: `[red]`, `[green]`, `[blue]`, `[yellow]`, `[orange]`, `[pink]`, `[cyan]`, `[magenta]`, `[purple]`, `[gold]`, `[white]`, `[gray]`, `[black]`

## [1.3.1] - 2026-01-16

### Added
- **Player Bed Locations**: Track and query player bed/spawn positions
  - API action: `getPlayerBed` returns bed coordinates
  - Console command: `beds` lists all player bed locations

## [1.3.0] - 2026-01-16

### Added
- **Enhanced Console Commands**: Execute Takaro actions directly from console
  - Core commands for testing and administration
  - Detailed help menu with examples

## [1.2.5] - 2026-01-15

### Added
- **Hytale-Takaro Integration**: Complete integration between Hytale server and Takaro platform
  - WebSocket connection to Takaro (wss://connect.takaro.io/)
  - Real-time bidirectional communication
  - Automatic reconnection on disconnect
- **Player Event Tracking**: Monitor player activity
  - Player connect/disconnect events
  - Player position tracking
  - Player inventory monitoring
- **Chat Integration**: Full chat relay system
  - Send chat messages from Takaro to game
  - Forward game chat to Takaro
  - Support for private (DM) messages
- **Server Management API**: Remote server control
  - Get server information
  - List online players
  - Get player location and inventory
  - Execute console commands
  - Teleport players
- **Item Management**: Complete item system integration
  - List all available items with categories
  - Give items to players
  - Check item existence
- **Player Moderation**: Administrative actions
  - Kick players
  - Ban/unban players
  - Execute commands as console
- **Hytale API Client**: Optional integration with official Hytale API
  - Server telemetry reporting
  - Authentication support
  - Prepared for future Hytale API features
- **Configuration System**: Simple properties-based config
  - Server identity token
  - Registration token for Takaro
  - Optional Hytale API configuration
- **Logging Integration**: Forward server logs to Takaro
  - Real-time log streaming
  - Filtered log levels
  - Configurable log buffer

---

## Version Numbering

- **Major** (X.0.0): Breaking changes or major feature overhauls
- **Minor** (0.X.0): New features and capabilities
- **Patch** (0.0.X): Bug fixes and minor improvements

## Links

- [GitHub Repository](https://github.com/yourusername/Hytale-Takaro-Integration)
- [Takaro Platform](https://takaro.io/)
- [Hytale Website](https://hytale.com/)
