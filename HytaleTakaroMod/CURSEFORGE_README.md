# Hytale-Takaro Integration

**Integrate your Hytale server with the Takaro game server management platform**

## What is This?

This mod connects your Hytale server to [Takaro](https://takaro.io), giving you:
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

### Information
- ✅ Player list with locations
- ✅ Server info and status
- ✅ Complete items database
- ✅ Player inventory viewing

## Installation

### Prerequisites
- Hytale dedicated server
- **Takaro account** - Get access by:
  1. Filling out the survey at [takaro.io](https://takaro.io)
  2. Joining the [Takaro Discord](https://discord.gg/pwenDRrtnA)
  3. Requesting an invite in Discord

### Server Setup

1. **Download** the mod JAR file
2. **Copy** to your Hytale server's `mods` folder:
   ```
   YourHytaleServer/mods/HytaleTakaroMod-1.0.0.jar
   ```
3. **Start** your server to generate the config file
4. **Stop** the server

### Configuration

Edit `config/HytaleTakaroMod/config.properties`:

```properties
# Choose a name for your server
IDENTITY_TOKEN=MyHytaleServer

# Registration token from Takaro dashboard
REGISTRATION_TOKEN=paste-your-registration-token-here
```

**How to configure:**

First, get Takaro access:
1. Fill out the survey at [takaro.io](https://takaro.io)
2. Join the [Takaro Discord](https://discord.gg/pwenDRrtnA)
3. Request an invite in Discord

Then get your Registration Token:
1. Log in to your Takaro dashboard
2. Navigate to Settings → Game Servers
3. Click "Add Game Server"
4. Select "Generic" as the game type
5. **Copy the Registration Token** displayed

Finally, configure your mod:
- `IDENTITY_TOKEN` - **Choose any name for your server** (e.g., "MyHytaleServer")
- `REGISTRATION_TOKEN` - Paste the token you copied above

### Start Server

Start your Hytale server. You should see:
```
[INFO] Hytale-Takaro Integration v1.0.0 initializing...
[INFO] Configuration loaded
[INFO] Connecting to Takaro at wss://...
[INFO] Successfully identified with Takaro
```

## Configuration Options

| Property | Description | Required |
|----------|-------------|----------|
| `IDENTITY_TOKEN` | Your chosen server name | Yes |
| `REGISTRATION_TOKEN` | Registration token from Takaro dashboard | Yes (first connection) |

## Commands

### In-Game Commands
- `/takarodebug` - Show connection status and debug info

### From Takaro Console
All Hytale console commands can be executed remotely through Takaro!

## Troubleshooting

### Mod not loading
- Check `logs/latest.log` for errors
- Verify the JAR is in the `mods` folder
- Ensure you're running a compatible Hytale server version

### Connection Issues
- Verify your `wsUrl` is correct (should start with `wss://`)
- Check that tokens are copied correctly (no extra spaces)
- Ensure your server can reach the internet
- Check firewall settings

### Events not appearing in Discord
- Verify Discord integration is enabled in Takaro
- Check Takaro's event hooks are configured
- Look for errors in server logs

## Support

- **Discord**: [Takaro Discord](https://discord.gg/pwenDRrtnA) - Primary support and community
- **Issues/Bugs**: [GitHub Issues](https://github.com/gettakaro/Hytale-Takaro-Integration/issues)
- **Documentation**: [Takaro Docs](https://docs.takaro.io)

## Links

- **Takaro Platform**: https://takaro.io
- **Source Code**: https://github.com/gettakaro/Hytale-Takaro-Integration
- **Takaro Documentation**: https://docs.takaro.io

## License

Licensed under the MIT License. See LICENSE file for details.

## Credits

Developed by the Takaro team for the Hytale community.
