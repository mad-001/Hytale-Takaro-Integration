# Hytale Takaro Integration

Hytale server plugin that integrates with Takaro (https://takaro.io/) for server management and automation.

## Features

- **Player Events**: Forwards player connect/disconnect events to Takaro
- **Chat Integration**: Forwards chat messages and supports color formatting with `[color]text[-]` syntax
- **Console Commands**: Execute Hytale commands from Takaro console
- **API Actions**:
  - `getPlayers` - Get list of online players
  - `giveItem` - Give items to players
  - `kickPlayer` - Kick players from server
  - `teleportPlayer` - Teleport players to coordinates
  - `teleportPlayerToPlayer` - Teleport one player to another
  - `getPlayerLocation` - Get player coordinates
  - `sendMessage` - Send messages to all players
  - `listItems` - List all available items for Takaro shop
  - `listCommands` - List available console commands
  - And more...

## Setup

1. Build the mod: `mvn clean package`
2. Copy `target/HytaleTakaroMod-1.0.0.jar` to your Hytale server's mods folder
3. Configure `config.properties` with your Takaro connection details
4. Restart your Hytale server

## Configuration

Create `config.properties` in the mod's data directory with:

```properties
TAKARO_WS_URL=wss://connect.takaro.io/
TAKARO_IDENTITY_TOKEN=your_token_here
TAKARO_REGISTRATION_TOKEN=your_registration_token
HYTALE_API_URL=https://api.hytale.com
HYTALE_API_TOKEN=your_hytale_token
```

## Player Identification

Players are identified using the `platformId` format: `hytale:<uuid>`

This ensures proper player tracking without interfering with Steam/Epic/Xbox integrations.

## Color Chat Formatting

Players can use color codes in chat:
- Named colors: `[red]text[-]`, `[blue]text[-]`, etc.
- Hex colors: `[ff0000]text[-]`

Supported named colors: red, green, blue, yellow, orange, pink, white, black, gray, cyan, magenta, purple, gold, lime, aqua

## Development

Built with:
- Java 21
- Maven 3.9.9
- Hytale Server API

## License

MIT
