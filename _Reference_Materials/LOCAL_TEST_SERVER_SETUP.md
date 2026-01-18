# Local Hytale Test Server Setup Guide

**Complete step-by-step guide to set up a local Hytale server and test the Takaro mod**

Last Updated: 2026-01-13

---

## Prerequisites Checklist

Before starting, you need:

- [ ] Hytale game installed and working
- [ ] Java 21+ installed (check with `java --version` in PowerShell)
- [ ] Maven installed (we'll help you install if needed)
- [ ] Administrator access to your PC

---

## Step 1: Install Maven (Build Tool)

Maven is needed to compile the mod from source code.

### Check if Maven is Already Installed

Open PowerShell and run:
```powershell
mvn --version
```

If you see version info, **skip to Step 2**. If not, continue below.

### Install Maven on Windows

**Option A: Using Chocolatey (Easiest)**
```powershell
# Run PowerShell as Administrator
choco install maven
```

**Option B: Using Winget**
```powershell
# Run PowerShell as Administrator
winget install Apache.Maven
```

**Option C: Manual Download**
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Maven`
3. Add to PATH:
   - Open System Properties → Environment Variables
   - Edit "Path" system variable
   - Add: `C:\Program Files\Maven\bin`
4. Restart PowerShell and verify with `mvn --version`

---

## Step 2: Build the Takaro Mod

Now we'll compile the mod into a JAR file.

### Open PowerShell and Navigate to Project

```powershell
cd C:\Users\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\HytaleTakaroMod
```

**If that path doesn't work**, use the WSL path:
```powershell
cd \\wsl.localhost\Ubuntu\home\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\HytaleTakaroMod
```

### Build the Mod

```powershell
mvn clean package
```

**What this does:**
- `clean` - Removes old build files
- `package` - Compiles Java code and creates JAR file

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
```

**Output Location:**
The compiled mod will be at:
```
target\HytaleTakaroMod-1.0.0.jar
```

---

## Step 3: Locate Your Hytale Game Files

You need to find where Hytale is installed on your system.

### Find Hytale Installation

**Default Locations:**

**Windows:**
```
%APPDATA%\Hytale\install\release\package\game\latest\
```

**Full Path (Usually):**
```
C:\Users\zmedh\AppData\Roaming\Hytale\install\release\package\game\latest\
```

### Verify Files Exist

Open File Explorer and navigate there. You should see:
- `Server/` folder
- `Assets.zip` file

---

## Step 4: Create Local Test Server Directory

Create a dedicated folder for your test server.

### Create Server Folder

```powershell
# Create server directory
mkdir C:\HytaleTestServer
cd C:\HytaleTestServer
```

---

## Step 5: Copy Server Files

Copy the required files from your Hytale installation to your test server.

### Copy Server Files

```powershell
# Copy Server folder
xcopy "%APPDATA%\Hytale\install\release\package\game\latest\Server\*" "C:\HytaleTestServer\Server\" /E /I /H

# Copy Assets.zip
copy "%APPDATA%\Hytale\install\release\package\game\latest\Assets.zip" "C:\HytaleTestServer\Assets.zip"
```

### Verify Files Copied

Check that `C:\HytaleTestServer\` contains:
- `Server\` folder with `HytaleServer.jar` inside
- `Assets.zip` file

---

## Step 6: Create Mods Folder and Install Mod

Hytale servers load mods from a `mods/` folder.

### Create Mods Folder

```powershell
cd C:\HytaleTestServer
mkdir mods
```

### Copy the Compiled Mod

```powershell
# If using Windows path
copy "C:\Users\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\HytaleTakaroMod\target\HytaleTakaroMod-1.0.0.jar" "C:\HytaleTestServer\mods\"

# OR if using WSL path
copy "\\wsl.localhost\Ubuntu\home\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\HytaleTakaroMod\target\HytaleTakaroMod-1.0.0.jar" "C:\HytaleTestServer\mods\"
```

### Verify Mod Installed

Check that `C:\HytaleTestServer\mods\HytaleTakaroMod-1.0.0.jar` exists.

---

## Step 7: First Server Start (Without Takaro)

Let's start the server once to generate config files.

### Start Server

```powershell
cd C:\HytaleTestServer\Server
java -jar HytaleServer.jar --assets ..\Assets.zip
```

**What Happens:**
1. Server starts up
2. Creates `plugins\` folder
3. Loads the TakaroPlugin
4. Generates `plugins\TakaroPlugin\config.properties`
5. May show warnings about missing Takaro tokens (this is expected)

### Watch for These Messages

Look for log messages like:
```
[TakaroPlugin|P] Hytale-Takaro Integration v1.0.0 initializing...
[TakaroPlugin|P] Configuration loaded
[TakaroPlugin|P] Debug command registered: /takarodebug
[TakaroPlugin|P] Registered PlayerChatEvent handler
[TakaroPlugin|P] Registered PlayerConnectEvent handler
[TakaroPlugin|P] Registered PlayerDisconnectEvent handler
```

### Authenticate the Server (IMPORTANT)

The server will display a message like:
```
To authenticate this server, visit: https://accounts.hytale.com/device
Enter code: ABCD-1234
```

**Do This:**
1. Open browser: https://accounts.hytale.com/device
2. Enter the code shown (e.g., `ABCD-1234`)
3. Log in with your Hytale account
4. Authorize the server

**Server will confirm:**
```
[INFO] Authentication successful!
```

### Stop the Server

Press `Ctrl+C` or type `stop` in console to stop the server.

---

## Step 8: Get Takaro Tokens

Now we need to get authentication tokens from Takaro.

### Create Takaro Account

1. Visit: https://takaro.dev/
2. Sign up or log in
3. Go to Settings → Servers
4. Click "Add Server"
5. Select "Custom" or "Other"

### Get Tokens

You'll receive two tokens:
- **IDENTITY_TOKEN** - Main authentication token
- **REGISTRATION_TOKEN** - (Optional) Registration token

**IMPORTANT:** Copy these tokens somewhere safe. You'll need them in the next step.

---

## Step 9: Configure the Mod

Edit the config file to add your Takaro tokens.

### Open Config File

```powershell
notepad C:\HytaleTestServer\plugins\TakaroPlugin\config.properties
```

### Edit Configuration

The file should look like this:
```properties
# Takaro WebSocket Configuration
TAKARO_WS_URL=wss://connect.next.takaro.dev/
IDENTITY_TOKEN=your_identity_token_here
REGISTRATION_TOKEN=your_registration_token_here

# Hytale First-Party API Configuration
HYTALE_API_URL=https://api.hytale.com
HYTALE_API_TOKEN=
```

**Replace:**
- `your_identity_token_here` with your actual IDENTITY_TOKEN from Takaro
- `your_registration_token_here` with your actual REGISTRATION_TOKEN from Takaro

**Save and close** the file.

---

## Step 10: Start Server with Takaro Integration

Now start the server with full Takaro integration enabled.

### Start Server

```powershell
cd C:\HytaleTestServer\Server
java -jar HytaleServer.jar --assets ..\Assets.zip
```

### Watch for Connection

Look for these messages:
```
[TakaroPlugin|P] Starting Takaro WebSocket connection...
[TakaroPlugin|P] Connecting to Takaro at wss://connect.next.takaro.dev/
[TakaroPlugin|P] WebSocket connected
[TakaroPlugin|P] Sent identify message to Takaro
[TakaroPlugin|P] Successfully identified with Takaro
```

**If you see these messages, the integration is WORKING! 🎉**

---

## Step 11: Connect with Hytale Client

Now test the integration by connecting with your game client.

### Start Hytale Game

1. Launch Hytale
2. Go to Multiplayer
3. Click "Direct Connect"
4. Enter: `localhost` or `127.0.0.1`
5. Click "Connect"

### Test Integration

**Test Chat:**
1. Type a message in game chat
2. Check Takaro dashboard - message should appear
3. Try sending a message from Takaro - should appear in game

**Test Join/Leave:**
1. Join the server
2. Check Takaro dashboard - should show player connected
3. Leave the server
4. Check Takaro dashboard - should show player disconnected

---

## Step 12: Test Debug Commands (Optional)

The mod includes debug commands to check status.

### In-Game Commands

Type these in game chat:

```
/takarodebug info
```
Shows plugin version and connection status.

```
/takarodebug ws
```
Shows WebSocket connection details.

```
/takarodebug server
```
Shows server information.

---

## Troubleshooting

### Build Fails: "Cannot find HytaleServer.jar"

**Problem:** Maven can't find the HytaleServer.jar dependency.

**Solution:**
```powershell
# Check if file exists
Test-Path "\\wsl.localhost\Ubuntu\home\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\libs\HytaleServer.jar"

# If missing, copy it
copy "%APPDATA%\Hytale\install\release\package\game\latest\Server\HytaleServer.jar" "\\wsl.localhost\Ubuntu\home\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\libs\"
```

### Server Won't Start: "Port 5520 already in use"

**Problem:** Another Hytale server is already running.

**Solution:**
```powershell
# Find process using port 5520
netstat -ano | findstr :5520

# Kill the process (replace XXXX with PID from above)
taskkill /PID XXXX /F
```

### Mod Not Loading: "Plugin not found"

**Problem:** Mod JAR not in correct location.

**Solution:**
```powershell
# Verify mod exists
dir C:\HytaleTestServer\mods\

# Should show: HytaleTakaroMod-1.0.0.jar
```

### WebSocket Won't Connect

**Problem:** Can't connect to Takaro.

**Solutions:**
1. Check tokens are correct in config.properties
2. Check firewall isn't blocking outbound HTTPS
3. Verify Takaro service is online: https://status.takaro.dev/

### Events Not Forwarding to Takaro

**Problem:** Chat/join/leave events not appearing in Takaro.

**Check:**
1. Server logs show: "Successfully identified with Takaro"
2. Tokens are correct
3. Player events are being logged (check for `[EVENT]` messages)

---

## Server Management Commands

### Stop Server Gracefully
```powershell
# In server console, type:
stop
```

### View Server Logs
```powershell
# Logs are in:
C:\HytaleTestServer\Server\logs\
```

### Reload Mod (After Changes)
1. Stop server
2. Delete old JAR from `mods\`
3. Copy new JAR to `mods\`
4. Start server

---

## Development Workflow

When making changes to the mod:

```powershell
# 1. Make code changes in your IDE

# 2. Rebuild mod
cd C:\Users\zmedh\Takaro-Projects\Hytale\Hytale-Takaro-Integration\HytaleTakaroMod
mvn clean package

# 3. Stop test server (Ctrl+C in server console)

# 4. Copy new JAR
copy "target\HytaleTakaroMod-1.0.0.jar" "C:\HytaleTestServer\mods\" /Y

# 5. Restart server
cd C:\HytaleTestServer\Server
java -jar HytaleServer.jar --assets ..\Assets.zip
```

---

## Quick Reference

### Server Start Command
```powershell
cd C:\HytaleTestServer\Server
java -jar HytaleServer.jar --assets ..\Assets.zip
```

### Config Location
```
C:\HytaleTestServer\plugins\TakaroPlugin\config.properties
```

### Mod Location
```
C:\HytaleTestServer\mods\HytaleTakaroMod-1.0.0.jar
```

### Logs Location
```
C:\HytaleTestServer\Server\logs\latest.log
```

---

## Next Steps

Once your local test server is working:

1. **Test all features** - Chat, join, leave, commands
2. **Monitor logs** - Watch for errors or warnings
3. **Test Takaro commands** - Try kicking, messaging from Takaro
4. **Performance testing** - Join with multiple clients
5. **Deploy to production** - Follow production deployment guide

---

## Additional Resources

- [API Reference](./API_REFERENCE.md)
- [Implementation Status](../HytaleTakaroMod/IMPLEMENTATION_STATUS.md)
- [Hytale Server Manual](./Hytale_Server_Manual.md)
- [Required Downloads](./REQUIRED_DOWNLOADS.md)

---

## Support

If you encounter issues:

1. Check server logs for error messages
2. Enable debug logging in config
3. Test without Takaro tokens first
4. Report issues on GitHub: https://github.com/mad-001/Hytale-Takaro-Integration/issues
