# Required Downloads & Links for Hytale-Takaro Integration

## ⚠️ IMPORTANT: Downloads Needed

This file lists all external resources you need to download to run the Hytale-Takaro Integration mod.

---

## 1. Java 25 (REQUIRED)

**What**: Java Development Kit 25 or later
**Why**: Hytale requires Java 25 to run
**Download**: https://adoptium.net/

**Installation Steps**:
1. Visit https://adoptium.net/
2. Select "Temurin 25 (LTS)"
3. Choose your OS (Windows/Linux/macOS)
4. Download and install
5. Verify installation:
   ```bash
   java --version
   ```
   Should show: `openjdk 25.0.1` or later

**Status**: ⬜ Not Downloaded | ✅ Downloaded & Installed

---

## 2. Hytale Server Files (REQUIRED)

You need EITHER Option A OR Option B:

### Option A: Manual Copy from Launcher (Quick Testing)

**What**: Server files from your Hytale game installation
**Location**:
- **Windows**: `%appdata%\Hytale\install\release\package\game\latest`
- **Linux**: `$XDG_DATA_HOME/Hytale/install/release/package/game/latest`
- **MacOS**: `~/Application Support/Hytale/install/release/package/game/latest`

**Files to Copy**:
- `Server/` folder (entire directory)
- `Assets.zip` file

**Copy To**: Your server directory (e.g., `/home/zmedh/hytale-server/`)

**Status**: ⬜ Not Copied | ✅ Copied

---

### Option B: Hytale Downloader CLI (Production Servers)

**What**: Official CLI tool to download Hytale server files
**Download**: `hytale-downloader.zip` (Linux & Windows)
**Source**: Official Hytale Support/Docs

⚠️ **UPDATE (Jan 2026)**: Download link not yet publicly available. Community members have confirmed the link doesn't work. Use **Option A (Manual Copy)** for now.

**Usage** (when available):
```bash
./hytale-downloader                    # Download latest release
./hytale-downloader -print-version     # Show version
./hytale-downloader -check-update      # Check for updates
```

**Status**: ⚠️ Link Not Available Yet | Use Option A Instead

---

## 3. Takaro Tokens (REQUIRED for Takaro Integration)

**What**: Authentication tokens from Takaro platform
**Where**: Takaro Dashboard (https://takaro.dev/)

**Required Values**:
- `IDENTITY_TOKEN` - Your Takaro server identity token
- `REGISTRATION_TOKEN` - Your Takaro registration token (optional)

**How to Get**:
1. Go to https://takaro.dev/
2. Create account / Login
3. Create a new server connection
4. Copy the tokens provided

**Configure In**: `plugins/TakaroPlugin/config.properties`

**Status**: ⬜ Not Obtained | ✅ Obtained

---

## 4. Hytale Server Authentication (REQUIRED)

**What**: Authenticate your Hytale server with Hytale platform
**URL**: https://accounts.hytale.com/device

**Process**:
1. Start your Hytale server
2. Run in-game command: `/auth login device`
3. Server will display a code (e.g., `ABCD-1234`)
4. Visit: https://accounts.hytale.com/device
5. Enter the code
6. Authorize the server
7. Server will confirm: "Authentication successful!"

**Note**:
- Required to enable API access and player connections
- Limit of 100 servers per Hytale game license
- For more than 100 servers, apply for Server Provider account

**Status**: ⬜ Not Authenticated | ✅ Authenticated

---

## 5. Hytale API Token (OPTIONAL but Recommended)

**What**: Authentication token for Hytale's first-party API
**Purpose**: Enables UUID lookups, player profiles, telemetry, payments, etc.
**Where**: Obtained during server authentication (see #4)

**Configure In**: `plugins/TakaroPlugin/config.properties`
```properties
HYTALE_API_TOKEN=your_token_here
```

**Status**: ⬜ Not Configured | ✅ Configured

---

## 6. HytaleServer.jar (FOR DEVELOPMENT)

**What**: Hytale server JAR file for building the plugin
**Location**: Copy from your server installation OR Hytale launcher
**Copy To**: `../libs/HytaleServer.jar` (relative to plugin project)

**Full Path**: `/home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration/libs/HytaleServer.jar`

**Why**: The plugin needs this as a compile-time dependency

**Status**: ✅ Already Copied (from your installation)

---

## 7. Maven (FOR BUILDING)

**What**: Build tool for compiling the Java plugin
**Install**:

**Windows**:
```bash
winget install Maven.Maven
```

**Linux/WSL**:
```bash
sudo apt-get update
sudo apt-get install maven
```

**macOS**:
```bash
brew install maven
```

**Verify**:
```bash
mvn --version
```

**Status**: ⬜ Not Installed | ✅ Installed

---

## Optional Resources

### Recommended Plugins (Optional)

These are optional plugins that can enhance your server:

1. **Nitrado:WebServer** - Web server plugin
2. **Nitrado:Query** - Server status queries
3. **Nitrado:PerformanceSaver** - Dynamic view distance
4. **ApexHosting:PrometheusExporter** - Metrics export

**Source**: Maintained by Nitrado and Apex Hosting (check their websites)

**Status**: ⬜ Not Needed | ✅ Downloaded

---

## Links Reference

| Resource | URL |
|----------|-----|
| Java 25 (Adoptium) | https://adoptium.net/ |
| Hytale Official | https://hytale.com/ |
| Hytale Device Auth | https://accounts.hytale.com/device |
| Takaro Platform | https://takaro.dev/ |
| GitHub Repository | https://github.com/mad-001/Hytale-Takaro-Integration |
| JVM Parameters Guide | https://www.baeldung.com/jvm-parameters |
| JEP-514 (AOT Cache) | https://openjdk.org/jeps/514 |

---

## Checklist Summary

Use this to track what you've completed:

- [ ] Java 25 installed
- [ ] Hytale server files obtained
- [ ] Takaro tokens obtained
- [ ] Hytale server authenticated
- [ ] Hytale API token configured
- [ ] HytaleServer.jar in libs folder
- [ ] Maven installed
- [ ] Plugin built successfully
- [ ] Plugin installed on server
- [ ] Config.properties edited with tokens
- [ ] Server started and connected to Takaro

---

## Build & Deploy Quick Commands

Once all downloads are complete:

```bash
# Navigate to plugin directory
cd /home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration/HytaleTakaroMod

# Build the plugin
mvn clean package

# Copy to server (adjust path to your server)
cp target/HytaleTakaroMod-1.0.0.jar /path/to/your/hytale/server/mods/

# Start server
cd /path/to/your/hytale/server
java -jar HytaleServer.jar --assets PathToAssets.zip

# Authenticate server (in-game)
/auth login device

# Edit config
nano plugins/TakaroPlugin/config.properties

# Restart server
```

---

## Support & Troubleshooting

If you're missing any downloads or having issues:

1. **Java issues**: Make sure Java 25+ is installed and in PATH
2. **Server files**: Must have exact server version matching your Hytale client
3. **Tokens**: Both Takaro and Hytale tokens are required for full functionality
4. **Build errors**: Ensure HytaleServer.jar is in `../libs/` directory
5. **Connection issues**: Verify firewall allows UDP port 5520

**Documentation**:
- [README.md](../HytaleTakaroMod/README.md)
- [Hytale Server Manual](./Hytale_Server_Manual.md)
- [Implementation Status](../HytaleTakaroMod/IMPLEMENTATION_STATUS.md)
- [API Integration Guide](../HytaleTakaroMod/HYTALE_API_INTEGRATION.md)
