# Bunq OAuth Integration

This document provides instructions for setting up and using the Bunq OAuth integration in the Team Balance application.

## Overview

The Bunq OAuth integration allows users to connect their Bunq bank accounts to the Team Balance application using OAuth 2.0 authentication. This provides a more secure and user-friendly way to access Bunq account data compared to API key authentication.

## Configuration

### Backend Configuration

The Bunq OAuth integration requires the following configuration properties in the `application.yml` file:

```yaml
app:
  bank:
    bunq:
      oauth-client-id: ${BUNQ_OAUTH_CLIENT_ID:}
      oauth-client-secret: ${BUNQ_OAUTH_CLIENT_SECRET:}
      oauth-redirect-uri: ${BUNQ_OAUTH_REDIRECT_URI:}
```

These properties can be set using environment variables:

- `BUNQ_OAUTH_CLIENT_ID`: The client ID obtained from the Bunq Developer Portal
- `BUNQ_OAUTH_CLIENT_SECRET`: The client secret obtained from the Bunq Developer Portal
- `BUNQ_OAUTH_REDIRECT_URI`: The redirect URI registered in the Bunq Developer Portal

For local development, you can set these values directly in the `application-local.yml` file:

```yaml
app:
  bank:
    bunq:
      oauth-client-id: "your_client_id_here"
      oauth-client-secret: "your_client_secret_here"
      oauth-redirect-uri: "http://localhost:8080/api/bank/bunq/oauth/callback"
```

### Bunq Developer Portal Setup

To use the Bunq OAuth integration, you need to register your application in the Bunq Developer Portal:

1. Go to the [Bunq Developer Portal](https://developer.bunq.com/)
2. Create a new OAuth client
3. Set the redirect URI to match the `oauth-redirect-uri` configuration property
4. Note the client ID and client secret for configuration

## Usage

### Connecting a Bunq Account

1. Log in to the Team Balance application
2. Navigate to the "Connect Bunq Account" page from the "Danger zone" section on the Overview page
3. Click the "Connect Bunq Account" button
4. You will be redirected to the Bunq website to authorize the application
5. After authorization, you will be redirected back to the Team Balance application
6. If successful, you will see a confirmation message

### Disconnecting a Bunq Account

1. Log in to the Team Balance application
2. Navigate to the "Connect Bunq Account" page
3. Click the "Disconnect Bunq Account" button
4. Your Bunq account will be disconnected from the application

## Implementation Details

The Bunq OAuth integration consists of the following components:

### Backend

- `BunqOAuthService`: Service for handling OAuth authentication with Bunq
- `BunqOAuthController`: Controller for exposing OAuth endpoints
- Configuration properties in `BankBunqConfig`

### Frontend

- `ConnectBunqPage`: React component for the Connect Bunq page
- Route in `App.tsx` for the Connect Bunq page
- Button in the Overview page to navigate to the Connect Bunq page

## Troubleshooting

If you encounter issues with the Bunq OAuth integration:

1. Check that the OAuth configuration properties are set correctly
2. Verify that the redirect URI matches the one registered in the Bunq Developer Portal
3. Check the application logs for error messages
4. Ensure that the Bunq API is available and responding

## References

- [Bunq OAuth Documentation](https://doc.bunq.com/#/oauth)
- [OAuth 2.0 Specification](https://oauth.net/2/)