# Hytale First-Party API Integration

## Overview

The Hytale-Takaro mod now includes full integration with Hytale's official first-party API endpoints. These endpoints provide authoritative data directly from Hytale and reduce the need for third-party services.

## Configuration

Add to your `config.properties`:

```properties
# Hytale API Settings
HYTALE_API_URL=https://api.hytale.com
HYTALE_API_TOKEN=your_authenticated_server_token_here
```

## Implemented API Endpoints

### ✅ UUID ↔ Name Lookup
**Status**: Fully Implemented

Resolve player names to UUIDs and vice versa. Supports single and bulk lookups.

```java
// Single lookup by name
JsonObject player = hytaleApi.lookupPlayerByName("PlayerName");

// Single lookup by UUID
JsonObject player = hytaleApi.lookupPlayerByUUID("uuid-here");

// Bulk lookup
String[] names = {"Player1", "Player2", "Player3"};
JsonObject results = hytaleApi.bulkLookupPlayers(names);
```

**Use Cases**:
- Convert Takaro gameIds to Hytale UUIDs
- Validate player identities
- Bulk player data enrichment

### ✅ Game Version
**Status**: Fully Implemented

Query current game version, protocol version, and check for updates.

```java
JsonObject version = hytaleApi.getGameVersion();
```

**Use Cases**:
- Server compatibility checks
- Update notifications
- Protocol version validation

### ✅ Player Profile
**Status**: Fully Implemented

Fetch player profile data including cosmetics, avatar renders, and public profile information.

```java
JsonObject profile = hytaleApi.getPlayerProfile(uuid);
```

**Use Cases**:
- Rich player information for Takaro dashboard
- Avatar display
- Player statistics and achievements

### ✅ Server Telemetry
**Status**: Fully Implemented + Auto-Reporting

Report server status, player count, and metadata for discovery integration.

```java
JsonObject metadata = new JsonObject();
metadata.addProperty("takaroEnabled", true);

hytaleApi.reportTelemetry(playerCount, "online", metadata);
```

**Auto-Reporting**:
- Automatically reports every 5 minutes
- Includes player count, server status, Takaro metadata
- Helps with server discovery

### ✅ Report
**Status**: Fully Implemented

Report players for ToS violations.

```java
hytaleApi.reportPlayer(reportedUuid, "Griefing", "Evidence description");
```

**Use Cases**:
- Integrate with Takaro moderation tools
- Platform-level player reports
- Automated abuse detection

### ✅ Payments
**Status**: Fully Implemented

Process payments using Hytale's built-in payment gate.

```java
JsonObject result = hytaleApi.processPayment(
    playerUuid,
    9.99,
    "USD",
    "Premium Server Access"
);
```

**Use Cases**:
- Server monetization
- In-game purchases
- Donation processing

## Under Consideration Endpoints

These endpoints may not be available yet but are implemented and ready:

### ⏳ Global Sanctions
Query whether a player has platform-level sanctions (not server-specific bans).

```java
JsonObject sanctions = hytaleApi.checkGlobalSanctions(uuid);
```

**Use Cases**:
- Pre-emptive ban checking
- Platform-wide moderation coordination

### ⏳ Friends List
Retrieve a player's friends list (with appropriate permissions) for social features.

```java
JsonObject friends = hytaleApi.getFriendsList(uuid);
```

**Use Cases**:
- Social features in Takaro
- Friend-based permissions
- Party systems

### ⏳ Webhook Subscriptions
Subscribe to push notifications for events like player name changes or sanction updates.

```java
String[] events = {"player.nameChange", "player.sanctions"};
hytaleApi.subscribeWebhook("https://your-server.com/webhooks", events);
```

**Use Cases**:
- Real-time updates without polling
- Event-driven architecture
- Reduced API load

## Integration with Takaro

The Hytale API client works seamlessly with Takaro:

### Player Data Enrichment

```java
// In TakaroRequestHandler.handleGetPlayers()
List<Player> hytaleServerPlayers = server.getOnlinePlayers();

for (Player player : hytaleServerPlayers) {
    // Enrich with Hytale API data
    JsonObject profile = hytaleApi.getPlayerProfile(player.getUUID());
    JsonObject sanctions = hytaleApi.checkGlobalSanctions(player.getUUID());

    // Send enriched data to Takaro
    // ...
}
```

### Moderation Integration

```java
// In TakaroRequestHandler.handleBanPlayer()
// 1. Ban on local server
server.banPlayer(uuid);

// 2. Report to Hytale platform
hytaleApi.reportPlayer(uuid, "Banned via Takaro", evidence);

// 3. Notify Takaro
webSocket.sendResponse(requestId, {success: true});
```

### Monetization Support

```java
// Custom Takaro module can trigger payments
// Payment processed through Hytale's official gateway
JsonObject payment = hytaleApi.processPayment(
    playerUuid,
    amount,
    currency,
    description
);

// Notify player and grant access
// ...
```

## Error Handling

All API methods gracefully handle errors:

- **404 Not Found**: Endpoint not yet available (for "under consideration" features)
- **401 Unauthorized**: Invalid or missing API token
- **500 Server Error**: Logged and returns null
- **Network Errors**: Timeout after 30 seconds, logged and returns null

```java
try {
    JsonObject player = hytaleApi.lookupPlayerByName("PlayerName");
    if (player == null) {
        // Handle error - endpoint unavailable or player not found
    }
} catch (IOException e) {
    // Network error
    logger.warning("API request failed: " + e.getMessage());
}
```

## Rate Limiting

**Important**: Hytale API endpoints may have rate limits. The implementation includes:

- Connection pooling (OkHttp)
- 30-second timeouts
- Automatic error handling

**Best Practices**:
- Cache player lookups when possible
- Use bulk lookups instead of multiple single lookups
- Respect any rate limit headers returned by API

## Security

**Authentication**:
- All requests include `Authorization: Bearer <token>` header
- Token stored securely in config.properties
- Token required for authenticated endpoints

**Token Management**:
```java
// Set token at startup
hytaleApi.setAuthToken(config.getHytaleApiToken());

// Token can be updated at runtime if needed
hytaleApi.setAuthToken(newToken);
```

## Architecture

```
TakaroPlugin
  │
  ├─ HytaleApiClient ──────────→ Hytale API (api.hytale.com)
  │   │                           │
  │   ├─ Player Lookups           ├─ UUID ↔ Name
  │   ├─ Profile Data             ├─ Player Profiles
  │   ├─ Telemetry Reporting      ├─ Server Status
  │   └─ Moderation Tools         └─ Reports & Sanctions
  │
  ├─ TakaroWebSocket ──────────→ Takaro Platform
  │   │
  │   └─ Send enriched player data from Hytale API
  │
  └─ TakaroRequestHandler
      │
      └─ Uses both Hytale API and server API for requests
```

## Future Enhancements

When "Under Consideration" endpoints become available:

1. **Webhook Integration**:
   - Receive real-time player updates
   - Reduce polling for player changes
   - Event-driven architecture

2. **Social Features**:
   - Friends-based permissions in Takaro
   - Social graphs for analytics
   - Party/group systems

3. **Enhanced Moderation**:
   - Global sanctions checking
   - Cross-server ban coordination
   - Platform-level reputation systems

## Performance Considerations

**Connection Pooling**: OkHttp automatically pools connections for efficiency.

**Caching Strategy**: Consider caching:
- Player UUIDs (rarely change)
- Player profiles (update periodically)
- Game version (check hourly)

**Async Operations**: All API calls are blocking. Consider wrapping in CompletableFuture for async:

```java
CompletableFuture.supplyAsync(() -> {
    try {
        return hytaleApi.lookupPlayerByName("Player");
    } catch (IOException e) {
        return null;
    }
}).thenAccept(result -> {
    // Handle result
});
```

## Testing

To test API integration:

1. **Set up authentication**:
   ```properties
   HYTALE_API_TOKEN=test_token_here
   ```

2. **Monitor logs**:
   ```
   [INFO] Hytale API client initialized
   [INFO] Started Hytale telemetry reporting
   [FINE] Telemetry reported to Hytale API
   ```

3. **Test endpoints**:
   - Create a test command to trigger API calls
   - Verify responses in logs
   - Check error handling with invalid tokens

## Documentation

For official Hytale API documentation, see:
- Hytale Developer Portal (when available)
- API Reference: `https://api.hytale.com/docs`
- Authentication Guide: Server token generation

## Support

If API endpoints are not working:
1. Verify HYTALE_API_TOKEN is set correctly
2. Check server logs for error codes
3. Verify server is authenticated with Hytale
4. Test with curl to isolate issues:
   ```bash
   curl -H "Authorization: Bearer YOUR_TOKEN" \
        https://api.hytale.com/version
   ```
