# Installation Guide

## Quick Start (5 minutes)

### Step 1: Get Takaro Access

**To use Takaro, follow these steps:**

1. **Fill out the survey** at [takaro.io](https://takaro.io)
2. **Join the Takaro Discord**: [https://discord.gg/pwenDRrtnA](https://discord.gg/pwenDRrtnA)
3. **Request an invite** in the Discord to get access to your Takaro dashboard

Once you have access to your dashboard, you can proceed with the mod installation.

### Step 2: Download the Mod
Download `HytaleTakaroMod-1.0.0.jar` from CurseForge

### Step 3: Install on Server
1. Locate your Hytale server directory
2. Navigate to the `mods` folder (create if it doesn't exist)
3. Copy `HytaleTakaroMod-1.0.0.jar` into the `mods` folder

### Step 3: Generate Config
1. Start your Hytale server
2. The mod will create `config/HytaleTakaroMod/config.properties`
3. Stop the server

### Step 5: Get Takaro Credentials
1. Once you have dashboard access, log in to your Takaro dashboard
2. Navigate to **Settings → Game Servers**
3. Click **"Add Game Server"**
4. Select **"Generic"** as the game type
5. **Copy the Registration Token** that is displayed

### Step 6: Configure the Mod
Edit `config/HytaleTakaroMod/config.properties`:

```properties
IDENTITY_TOKEN=MyHytaleServer
REGISTRATION_TOKEN=paste-your-registration-token-here
```

**Configuration:**
- `IDENTITY_TOKEN` - **Choose a name for your server** (e.g., "MyHytaleServer", "SurvivalServer", etc.)
- `REGISTRATION_TOKEN` - **Paste the Registration Token** you copied from Takaro in Step 5

### Step 7: Start Server
1. Start your Hytale server
2. Check the logs for:
   ```
   [INFO] Hytale-Takaro Integration v1.0.0 initializing...
   [INFO] Connecting to Takaro at wss://...
   [INFO] Successfully identified with Takaro
   ```

### Step 8: Verify Connection
1. In Takaro dashboard, go to your game server
2. You should see "Connected" status
3. Have a player join to test events

## Advanced Configuration

### Log Levels
To change log verbosity, edit your server's logging configuration.

## Troubleshooting

### Config file not generating
**Problem:** Config file isn't created after starting server

**Solution:**
1. Check server logs for errors
2. Verify the JAR is in the correct `mods` folder
3. Ensure you have write permissions

### Cannot connect to Takaro
**Problem:** "Failed to start WebSocket connection" in logs

**Solutions:**
- Verify `wsUrl` starts with `wss://` (not `ws://` or `http://`)
- Check tokens are correct (re-copy from Takaro)
- Ensure server has internet access
- Check firewall isn't blocking WebSocket connections
- Verify Takaro instance is running

### Events not showing in Discord
**Problem:** Player joins/leaves don't appear in Discord

**Solutions:**
1. Check Takaro is connected (green status in dashboard)
2. Verify Discord integration is enabled in Takaro
3. Configure event hooks in Takaro dashboard
4. Check server logs for event messages
5. Test with `/takarodebug` command in-game

### "Takaro error" messages
**Problem:** Seeing error messages about Takaro

**Solutions:**
- This usually indicates a temporary Takaro service issue
- Check Takaro status page
- Wait a few minutes and it should reconnect automatically
- Check Takaro logs in the dashboard

## File Locations

```
HytaleServer/Server
├── mods/
│   └── HytaleTakaroMod-1.0.0.jar
└── config/
    └── HytaleTakaroMod/
        └── config.properties
```

## Updating the Mod

1. Stop your server
2. Delete old JAR from `mods` folder
3. Download new version
4. Copy new JAR to `mods` folder
5. Start server
6. Check changelog for any config changes

## Getting Help

- **Discord**: [Takaro Discord](https://discord.gg/pwenDRrtnA) - Primary support channel
- **Documentation**: [Takaro Docs](https://docs.takaro.io)
- **Issues**: [GitHub](https://github.com/mad-001/Hytale-Takaro-Integration)
