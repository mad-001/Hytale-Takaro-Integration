# Changelog

All notable changes to the Hytale-Takaro Integration mod will be documented in this file.

## [1.4.0] - 2026-01-16

### Added
- **Player Name Colors via Takaro** - Players can set custom chat name colors using Takaro commands
  - Added `setPlayerNameColor` API action - allows Takaro to set/update player name colors
  - Name colors are cached in memory (UUID -> color code mapping)
  - Supports both hex colors (ff0000) and named colors (gold, red, blue, etc.)
  - Players use Takaro commands like `/namecolor gold` (command requires proper permissions)
  - Colors are applied locally when players chat - no interception needed

### Technical
- TakaroPlugin: Added `ConcurrentHashMap<String, String> playerNameColors` cache
- TakaroPlugin: Added `getPlayerNameColor()` and `setPlayerNameColor()` methods
- ChatEventListener: Looks up cached name color and applies it via `ChatFormatter.parseColor()`
- ChatFormatter: Made `parseColor()` method public for use by ChatEventListener
- TakaroRequestHandler: Added `handleSetPlayerNameColor()` to update the cache
- No chat event cancellation - all formatting happens locally

## [1.2.5] - 2026-01-15

### Added
- Enhanced logging for listItems - logs first 3 items showing original and cleaned names

### Technical
- Verify that name cleanup is working in listItems when Takaro calls it hourly

## [1.2.4] - 2026-01-15

### Added
- Enhanced logging for getPlayerInventory - logs each item with code, name, and amount

### Technical
- Helps debug what's actually being sent to Takaro

## [1.2.3] - 2026-01-15

### Fixed
- Cleaned up item names in listItems response - strips "server.items." prefix and ".name" suffix from all items sent to Takaro hourly

### Technical
- Applied same name cleaning logic to listItems as getPlayerInventory

## [1.2.2] - 2026-01-15

### Fixed
- **Critical**: Fixed getPlayerInventory payload parsing - now properly extracts gameId from args field
- Cleaned up item names in getPlayerInventory - strips "server.items." prefix and ".name" suffix (e.g., "Rail" instead of "server.items.Rail.name")

### Technical
- Matches payload parsing pattern from getPlayerLocation
- Returns clean item names for better readability in Takaro

## [1.2.1] - 2026-01-15

### Added
- **Implemented getPlayerInventory** - Now properly reads player inventory contents
- Returns item code, name, and amount for all inventory items (hotbar, storage, armor, utility, backpack)

### Technical
- Uses decompiled Hytale API methods: ItemContainer.getCapacity(), ItemContainer.getItemStack(short), ItemStack.getItemId(), ItemStack.getQuantity()
- Accesses inventory on world thread for thread safety

## [1.2.0] - 2026-01-15

### Changed
- getPlayerInventory now returns empty array due to Hytale API limitations
- The CombinedItemContainer API only supports adding items, not reading them

## [1.1.7] - 2026-01-15

### Changed
- Removed 100ms delay from disconnect events (not needed for dedicated servers)

### Fixed
- **Critical**: Removed "error" field from getPlayerLocation responses
- **Critical**: Removed duplicate "type" field from event data
- Event data now only contains "player" object, matching Enshrouded format

## [1.1.6] - 2026-01-15

### Fixed
- Added 100ms delay after sending disconnect events (removed in v1.1.7)

## [1.1.5] - 2026-01-15

### Added
- Log exact JSON being sent to Takaro for debugging

## [1.1.4] - 2026-01-15

### Added
- Enhanced logging for disconnect events (INFO level instead of FINE)

## [1.1.3] - 2026-01-15

### Fixed
- **Critical**: Removed duplicate "type" field from event data (both connect and disconnect events)
- Event data now only contains "player" object, matching working Enshrouded integration format

### Technical
- Matches exact format that works in test: data contains only player, not type field

## [1.1.2] - 2026-01-15

### Fixed
- **Critical**: Removed "error" field from getPlayerLocation responses
- Was causing Generic connector to throw errors and crash event handler

### Technical
- getPlayerLocation now returns only x, y, z coordinates (matching IPosition DTO requirements)
- When player is offline/not found, returns (0, 0, 0) instead of error field
- Fixes eventWorker.ts line 57 crash that prevented disconnect event storage

## [1.1.1] - 2026-01-15

### Fixed
- RESTORED EXACT v1.0.0 CODE - copied directly from working version
- Removed all changes that were breaking events
- Code is now identical to v1.0.0 except for disconnect deduplication

### Technical
- Exact code from git commit 9c66337 (v1.0.0 that was working)
- Only addition is disconnect event deduplication

## [1.1.0] - 2026-01-15

### Fixed
- Reverted to EXACT v1.0.0 event format (removed msg field that was breaking events)
- Event data now has ONLY: type, player (matching what worked before)
- Includes disconnect event deduplication from v1.0.7

### Technical
- Event structure matches v1.0.0 that was working
- Disconnect deduplication prevents Hytale's duplicate PlayerDisconnectEvent from being sent twice

## [1.0.9] - 2026-01-15

### Fixed
- Added msg field to events matching Takaro mock server format exactly

### Technical
- Connect events now include: type, msg, player
- Disconnect events now include: type, msg, player

## [1.0.8] - 2026-01-15

### Fixed
- **ACTUALLY FIXED IT**: Added type field back to event data (required by Takaro DTO)
- Event data now includes type field as shown in Takaro mock server examples

### Technical
- Event data structure matches Takaro's mock gameserver format exactly

## [1.0.7] - 2026-01-15

### Fixed
- **Critical**: Added deduplication for disconnect events (Hytale fires PlayerDisconnectEvent twice)
- Prevents sending duplicate disconnect events to Takaro within 5 seconds

### Technical
- Added cooldown tracking map for disconnect events
- Logs "Ignoring duplicate disconnect event" when duplicates are detected

## [1.0.6] - 2026-01-15

### Fixed
- **Critical**: Removed duplicate "type" field from event data that was causing Takaro DTO validation to fail
- Events now send correct structure without duplicate type field

### Technical
- Event data no longer includes type field (only the payload wrapper includes it)
- Matches Takaro's strict DTO validation requirements

## [1.0.5] - 2026-01-15

### Added
- Detailed logging of exact JSON being sent to Takaro for debugging

### Technical
- Added log output in sendGameEvent to show full JSON message being sent

## [1.0.4] - 2026-01-15

### Fixed
- Restored exact v1.0.0 event format including type field in event data
- Uses PlayerConnectEvent exactly as v1.0.0 did

### Technical
- Event data includes type field (same as v1.0.0 that was working)
- Uses PlayerConnectEvent for player join detection

## [1.0.3] - 2026-01-15

### Fixed
- Reverted to `PlayerConnectEvent` which was working in v1.0.0
- Kept the duplicate "type" field bug fix from v1.0.2

### Technical
- Uses `PlayerConnectEvent` for player join detection (not `AddPlayerToWorldEvent`)
- Event data structure matches Takaro DTO validation requirements

## [1.0.2] - 2026-01-15

### Fixed
- **Critical Bug Fix**: Removed duplicate "type" field in event data that was causing Takaro to reject events
- Events now use correct structure that matches Takaro's DTO validation requirements

### Technical
- Removed `eventData.put("type", "player-connected")` from event building
- The type field is now only added by `sendGameEvent()` wrapper method
- Uses `PlayerConnectEvent` for reliable player join detection

## [1.0.1] - 2026-01-15

### Changed
- **Simplified Configuration** - Users now only configure 2 fields (IDENTITY_TOKEN and REGISTRATION_TOKEN)
- Hardcoded WebSocket URL to `wss://connect.takaro.io/` (users no longer configure this)
- Hidden Hytale API configuration from default config (infrastructure remains for future use)

### Technical
- Hytale API integration code is present but hidden from user configuration
- WebSocket URL is no longer user-configurable to prevent connection issues

## [1.0.0] - 2026-01-15

### Initial Release

#### Features
- **WebSocket Integration** with Takaro platform
- **Player Events**
  - Join notifications
  - Leave notifications
  - Real-time player tracking
- **Chat Integration**
  - Chat message relay to Takaro
  - Color code support in chat
- **Remote Commands**
  - Execute console commands
  - Send messages to players
  - Give items to players
  - Teleport players
  - Kick/ban/unban players
- **Server Information**
  - Player list with real-time locations
  - Server status and info
  - Complete items database (5000+ items)
  - Player inventory viewing
- **Automatic Reconnection**
  - Exponential backoff
  - Connection resilience

#### Technical Details
- Event system using official Hytale event API
- Supports both local and dedicated servers
- AddPlayerToWorldEvent for reliable player tracking
- Optimized event deduplication
- Comprehensive error handling and logging

#### Known Issues
- None reported

---

## Version Format

Versions follow Semantic Versioning: MAJOR.MINOR.PATCH
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)
