# Hytale API Integration - Developer Notes

## Current Status: HIDDEN BUT IMPLEMENTED

The Hytale API integration is **fully implemented in the code** but **NOT exposed to users** in the configuration or documentation.

## Why?

The Hytale official API doesn't exist yet for regular users. When it becomes available, we can simply update the documentation and default config to expose these fields - all the code infrastructure is already there.

## What's Hidden?

### Configuration Fields (Not in default config.properties):
- `HYTALE_API_URL` - Hytale official API URL (default: https://api.hytale.com)
- `HYTALE_API_TOKEN` - API authentication token

These methods exist in `TakaroConfig.java` but are NOT written to the default config file that users see.

### Code That Uses It:
1. **TakaroPlugin.java**
   - `HytaleApiClient hytaleApi` field
   - Initialization in `setup()` method
   - Telemetry reporting in `start()` method
   - Shutdown in `shutdown()` method

2. **TakaroRequestHandler.java**
   - `HytaleApiClient hytaleApi` field
   - Constructor takes HytaleApiClient parameter

3. **TakaroDebugCommand.java**
   - Shows "Hytale API: Enabled (hidden feature)" if token is configured

## How to Enable in the Future

When Hytale releases their official server API:

1. **Update TakaroConfig.java** `createDefault()` method to include:
   ```java
   properties.setProperty("HYTALE_API_URL", "https://api.hytale.com");
   properties.setProperty("HYTALE_API_TOKEN", "");
   ```

2. **Update INSTALLATION.md** to add section:
   ```markdown
   ### Optional: Hytale API Integration
   If you have access to Hytale's official server API:

   ```properties
   HYTALE_API_URL=https://api.hytale.com
   HYTALE_API_TOKEN=your-api-token-here
   ```

   This enables:
   - Server telemetry reporting
   - Extended server statistics
   ```

3. **Update CURSEFORGE_README.md** configuration table to include those fields

That's it! All the code is already there and working.

## User-Facing Config (Current)

Users only see these 2 fields:
```properties
IDENTITY_TOKEN=MyHytaleServer
REGISTRATION_TOKEN=paste-your-registration-token-here
```

The `wsUrl` is hardcoded to `wss://connect.takaro.io/` and never shown to users.
