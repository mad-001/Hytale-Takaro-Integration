# Hytale-Takaro Integration

This mod connects your Hytale server to Takaro, giving you:
- **Discord Integration** - Player join/leave notifications, chat relay
- **Remote Management** - Execute commands, give items, teleport players
- **Advanced Automation** - Hooks, cronjobs, and custom commands
- **Player Tracking** - Real-time player locations and statistics
- **Item Management** - Complete item database integration

## Features

### Events
- ✅ Player join/leave notifications
- ✅ Chat message relay
- ✅ Real-time player tracking

### Commands
- ✅ Execute console commands remotely
- ✅ Send messages to players
- ✅ Give items to players
- ✅ Teleport players
- ✅ Kick/ban/unban players
- ✅ Execute ANY console command

### Information
- ✅ Player list with locations
- ✅ Server info and status
- ✅ Complete items database
- ✅ Player inventory viewing

## Installation

### Prerequisites
- Hytale dedicated server
- [Takaro account](https://takaro.io) (free)

### Get a Takaro Account
1. Go to [Takaro](https://takaro.io) and fill out the survey
2. Join [Discord](https://discord.gg/pwenDRrtnA) and ask for an invite

### Server Setup
1. Download the mod JAR file
2. Copy to your Hytale server's `mods` folder
3. Start your server to generate the config file
4. Stop the server

### Configuration
Edit `\Hytale\Server\mods\dev.takaro_HytaleTakaroIntegration\config.properties`:

```properties
# Get these from your Takaro dashboard
IDENTITY_TOKEN=NAME-YOUR-SERVER-WHATEVER-YOU-WANT
REGISTRATION_TOKEN=GET_THIS_FROM_THE_DIRECTIONS_BELOW
```

**Where to get tokens:**
1. Go to your [Takaro dashboard](https://takaro.io)
2. Navigate to Settings → Game Servers
3. Click "Add Game Server"
4. Select "Generic" as the game type
5. Copy the Registration Token
6. Paste into `dev.takaro_HytaleTakaroIntegration` and replace `GET_THIS_FROM_THE_DIRECTIONS_BELOW`

### Start Server
Start your Hytale server and the mod will automatically connect to Takaro!

## Support
- **Issues/Bugs**: [GitHub Issues](https://github.com/gettakaro/Hytale-Takaro-Integration/issues)
- **Discord**: [Takaro Discord](https://discord.gg/pwenDRrtnA)
- **Documentation**: [Takaro Docs](https://docs.takaro.io)

## Links
- **Takaro Platform**: https://takaro.io
- **Source Code**: https://github.com/mad-001/Hytale-Takaro-Integration
- **Takaro Documentation**: https://docs.takaro.io

## Credits
Developed by the Takaro team for the Hytale community.
