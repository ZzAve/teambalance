import { ApiClient } from "./ApiClient";
import { UsersResponse } from "./CommonApiResponses";

const usersClient = ApiClient();

const getActiveUsers = () =>
  usersClient.call(`users`).then((x) => x as UsersResponse);

export const usersApiClient = {
  ...usersClient,
  getActiveUsers,
};
