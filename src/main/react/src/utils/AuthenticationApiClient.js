import { ApiClient } from "./ApiClient";
import { toBase64 } from "./util";

const authenticationClient = ApiClient("auth");

const authenticate = (password) => {
  return authenticationClient.call(
    `authentication`,
    {
      method: "GET",
      headers: { "X-Secret": toBase64(password) },
    },
    authenticationClient.defaultTimeout,
    1000
  );
};

export const authenticationApiClient = {
  ...authenticationClient,
  authenticate,
};
