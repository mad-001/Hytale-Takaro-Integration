# Quick Reference - Hytale-Takaro Integration

**Quick lookup guide for common tasks and important information**

---

## 🚀 Quick Start

```bash
# 1. Build plugin
cd HytaleTakaroMod
mvn clean package

# 2. Start Hytale server
java -jar HytaleServer.jar --assets Assets.zip

# 3. Authenticate (in-game)
/auth login device
# Visit: https://accounts.hytale.com/device

# 4. Configure plugin
nano plugins/TakaroPlugin/config.properties

# 5. Restart server
```

---

## 📍 Important Paths

### Windows
- **Hytale Launcher Files**: `%appdata%\Hytale\install\release\package\game\latest`
- **Server Install**: `C:\Users\zmedh\AppData\Roaming\Hytale\install\release\package\game\latest\Server`

### Linux/WSL
- **Project**: `/home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration`
- **Plugin Source**: `./HytaleTakaroMod`
- **Built Plugin**: `./HytaleTakaroMod/target/HytaleTakaroMod-1.0.0.jar`
- **Reference Docs**: `./_Reference_Materials`
- **HytaleServer JAR**: `./libs/HytaleServer.jar`

---

## 🔌 Server Configuration

### Default Port
- **Port**: `5520` (UDP)
- **Protocol**: QUIC (not TCP!)
- **Change Port**: `java -jar HytaleServer.jar --bind 0.0.0.0:25565 --assets Assets.zip`

### Firewall Rules

**Windows**:
```powershell
New-NetFirewallRule -DisplayName "Hytale Server" -Direction Inbound -Protocol UDP -LocalPort 5520 -Action Allow
```

**Linux**:
```bash
sudo ufw allow 5520/udp
```

### Server Structure
```
Server/
├── .cache/              # Optimized files cache
├── logs/                # Server logs
├── mods/                # Plugin JARs (put plugin here!)
├── universe/            # World saves
│   └── worlds/          # Individual world folders
├── bans.json            # Banned players
├── config.json          # Server config
├── permissions.json     # Permissions
└── whitelist.json       # Whitelist
```

---

## ⚙️ Plugin Configuration

**Location**: `Server/plugins/TakaroPlugin/config.properties`

```properties
# Takaro Connection
TAKARO_WS_URL=wss://connect.next.takaro.dev/
IDENTITY_TOKEN=your_takaro_identity_token
REGISTRATION_TOKEN=your_takaro_registration_token

# Hytale API
HYTALE_API_URL=https://api.hytale.com
HYTALE_API_TOKEN=your_hytale_server_token
```

---

## 🔐 Authentication

### Hytale Server Auth
```bash
# In-game command
/auth login device

# Visit in browser
https://accounts.hytale.com/device

# Enter code shown in server console
```

### Takaro Tokens
1. Visit: https://takaro.dev/
2. Login/Register
3. Create server connection
4. Copy tokens to `config.properties`

---

## 🛠️ Development Commands

### Build Plugin
```bash
cd HytaleTakaroMod
mvn clean package
```

### Build with Specific Java
```bash
mvn clean package -DskipTests
```

### Clean Build
```bash
mvn clean
rm -rf target/
mvn package
```

### Check Dependencies
```bash
mvn dependency:tree
```

---

## 🚦 Server Launch Options

### Basic Launch
```bash
java -jar HytaleServer.jar --assets Assets.zip
```

### With AOT Cache (Faster Startup)
```bash
java -XX:AOTCache=HytaleServer.aot -jar HytaleServer.jar --assets Assets.zip
```

### Development Mode (Disable Sentry)
```bash
java -jar HytaleServer.jar --assets Assets.zip --disable-sentry
```

### Custom Port
```bash
java -jar HytaleServer.jar --assets Assets.zip --bind 0.0.0.0:25565
```

### Set Memory Limits
```bash
java -Xms4G -Xmx8G -jar HytaleServer.jar --assets Assets.zip
```

### Full Production Example
```bash
java -Xms4G -Xmx8G \
  -XX:AOTCache=HytaleServer.aot \
  -jar HytaleServer.jar \
  --assets Assets.zip \
  --bind 0.0.0.0:5520 \
  --backup \
  --backup-frequency 60
```

---

## 📊 Resource Recommendations

| Players | RAM | CPU | View Distance |
|---------|-----|-----|---------------|
| 1-10 | 4GB | 2 cores | 8 chunks |
| 10-30 | 8GB | 4 cores | 10 chunks |
| 30-50 | 12GB | 6 cores | 12 chunks |
| 50+ | 16GB+ | 8+ cores | 12 chunks (max) |

**Note**: View distance is the biggest RAM consumer. Default is 24 chunks (384 blocks) - we recommend 12 chunks max.

---

## 🔗 API Endpoints (Hytale)

### Available Now
- UUID ↔ Name Lookup: Single and bulk
- Player Profile: Cosmetics, avatars, public data
- Game Version: Current version and protocol
- Server Telemetry: Auto-reported every 5 minutes
- Report: ToS violation reporting
- Payments: Built-in payment processing

### Coming Soon
- Global Sanctions
- Friends List
- Webhook Subscriptions

**Base URL**: `https://api.hytale.com`
**Auth**: `Authorization: Bearer <HYTALE_API_TOKEN>`

---

## 🐛 Troubleshooting

### Plugin Not Loading
```bash
# Check logs
tail -f Server/logs/latest.log

# Verify plugin in correct folder
ls -la Server/mods/

# Check Java version
java --version
```

### Connection Issues
```bash
# Test port
nc -vuz localhost 5520  # Linux
Test-NetConnection localhost -Port 5520  # Windows

# Check firewall
sudo ufw status  # Linux
Get-NetFirewallRule -DisplayName "Hytale*"  # Windows
```

### Takaro Not Connecting
1. Verify `IDENTITY_TOKEN` in config
2. Check WebSocket URL: `wss://connect.next.takaro.dev/`
3. Review logs for "Successfully identified with Takaro"
4. Test network: `curl -I https://connect.next.takaro.dev`

### Build Errors
```bash
# Verify HytaleServer.jar location
ls -la ../libs/HytaleServer.jar

# Clean and rebuild
mvn clean package

# Check Java version
mvn --version
```

---

## 📚 Documentation Links

### Local Files
- [README.md](../HytaleTakaroMod/README.md) - Main documentation
- [Implementation Status](../HytaleTakaroMod/IMPLEMENTATION_STATUS.md) - Development status
- [Hytale API Integration](../HytaleTakaroMod/HYTALE_API_INTEGRATION.md) - API reference
- [Contributing](../HytaleTakaroMod/CONTRIBUTING.md) - How to contribute
- [Hytale Server Manual](./Hytale_Server_Manual.md) - Full server docs
- [Required Downloads](./REQUIRED_DOWNLOADS.md) - Download checklist

### Online Resources
- **GitHub Repo**: https://github.com/mad-001/Hytale-Takaro-Integration
- **Takaro**: https://takaro.dev/
- **Hytale**: https://hytale.com/
- **Java 25**: https://adoptium.net/

---

## 🎯 Common Tasks

### Installing the Plugin
```bash
# 1. Build
cd HytaleTakaroMod && mvn clean package

# 2. Copy to server
cp target/HytaleTakaroMod-1.0.0.jar /path/to/server/mods/

# 3. Restart server
```

### Updating the Plugin
```bash
# 1. Pull latest code
git pull

# 2. Rebuild
mvn clean package

# 3. Stop server
# 4. Replace old JAR with new one
# 5. Start server
```

### Viewing Logs
```bash
# Real-time logs
tail -f Server/logs/latest.log

# Search for errors
grep -i "error" Server/logs/latest.log

# Search for Takaro connection
grep -i "takaro" Server/logs/latest.log
```

### Backup World
```bash
# Manual backup
cp -r Server/universe Server/universe.backup.$(date +%Y%m%d)

# With server running (if enabled)
# /save-all (in-game command)
```

---

## 🔢 Default Values

| Setting | Default Value |
|---------|---------------|
| Port | 5520 (UDP) |
| View Distance | 24 chunks (384 blocks) |
| Memory | 4GB minimum |
| Java Version | 25+ required |
| Protocol | QUIC over UDP |
| Max Servers | 100 per license |
| Backup Frequency | 30 minutes (if enabled) |
| Telemetry Interval | 5 minutes |

---

## 💡 Pro Tips

1. **Use AOT Cache**: Significantly faster startup times
2. **Limit View Distance**: 12 chunks max for better performance
3. **Monitor RAM**: Use `-Xmx` to set memory limits
4. **Disable Sentry in Dev**: Avoid submitting dev errors
5. **Test Locally First**: Use offline mode for initial testing
6. **Enable Backups**: `--backup --backup-frequency 60`
7. **Use Hytale Downloader**: Easier to keep server updated
8. **Check Logs Often**: `tail -f` is your friend
9. **Document Tokens**: Keep track of all auth tokens
10. **Read Error Messages**: Hytale has good error reporting

---

## 🆘 Emergency Commands

### Stop Server Gracefully
```bash
# In server console
/stop
```

### Force Kill Server
```bash
# Linux
pkill -9 java

# Windows
taskkill /F /IM java.exe
```

### Reset Authentication
```bash
# In-game
/auth logout
/auth login device
```

### Clear Plugin Config
```bash
rm Server/plugins/TakaroPlugin/config.properties
# Restart server to regenerate
```

---

**Last Updated**: January 2026
**Project**: Hytale-Takaro Integration
**Maintainer**: https://github.com/mad-001/Hytale-Takaro-Integration
