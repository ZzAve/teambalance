import { ApiClient } from "./ApiClient";
import { internalizeUser, UsersResponse } from "./CommonApiResponses";
import { User } from "./domain";

const usersClient = ApiClient();

const getActiveUsers: () => Promise<User[]> = () =>
  usersClient
    .call('users')
    .then((it) => it as UsersResponse)
    .then((it) => it.users.map(internalizeUser));

export const usersApiClient = {
  ...usersClient,
  getActiveUsers,
};
