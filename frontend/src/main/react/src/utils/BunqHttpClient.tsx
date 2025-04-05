import { ApiClient } from "./ApiClient";

const bunqClient = ApiClient();

/**
 * Interface for the OAuth status response from the backend.
 */
interface OAuthStatusResponse {
  authenticated: boolean;
}

/**
 * Fetches the OAuth status from the backend.
 * @returns A promise that resolves to the OAuth status.
 */
const getOAuthStatus = async (): Promise<OAuthStatusResponse> => {
  const response = await bunqClient.call("bank/bunq/oauth/status");
  return response as OAuthStatusResponse;
};

/**
 * Clears the OAuth authentication with Bunq.
 * @returns A promise that resolves when the authentication is cleared.
 */
const clearOAuthAuthentication = async (): Promise<void> => {
  await bunqClient.callWithBody(
    "bank/bunq/oauth/clear",
    {},
    { method: "POST" }
  );
};

/**
 * Gets the URL for the Bunq OAuth authorization page.
 * @returns The URL for the Bunq OAuth authorization page.
 */
const getAuthorizationUrl = (): string => {
  return "/api/bank/bunq/oauth/authorize";
};

/**
 * BunqHttpClient provides methods for interacting with the Bunq API.
 */
export const bunqHttpClient = {
  ...bunqClient,
  getOAuthStatus,
  clearOAuthAuthentication,
  getAuthorizationUrl,
};
