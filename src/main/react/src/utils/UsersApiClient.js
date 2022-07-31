import { ApiClient } from "./ApiClient";

const usersClient = ApiClient("users");

const getActiveUsers = () => usersClient.call(`users`);

export const usersApiClient = {
  ...usersClient,
  getActiveUsers,
};
