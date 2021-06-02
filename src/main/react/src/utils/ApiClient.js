import { fetchWithTimeout } from "./fetchWithTimeout";
import { delay } from "./util";
import { authenticationManager } from "./AuthenticationManager";
import { InvalidSecretException } from "./Exceptions";

const DEFAULT_TIMEOUT = 5000; //ms
const DEFAULT_MIN_DELAY = 250; //ms

const _mergeFetchOptions = (options, secret) => ({
  ...options,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Secret": btoa(secret),
    ...options.headers,
  },
});

const _throwIfNotOk = (path, res) => {
  if (!res.ok) {
    //TODO Should be moved to a higher level where AUTH state is reachable
    if (res.status === 403) {
      console.log("Status was 403");
      // setAuthenticated(false);
      throw new InvalidSecretException("nope");
    }

    throw Error(
      `Call to '${path}' returned an erroneous response (code ${res.status})`
    );
  }
};

const _resultWithMinDelay = async (result, minDelay) => {
  await delay(minDelay);
  return result;
};

export const ApiClient = () => {
  const performApiCallWithBody = (
    path,
    payload,
    options = { method: "POST" },
    timeout = DEFAULT_TIMEOUT,
    minDelay = DEFAULT_MIN_DELAY
  ) => {
    const apiResult = fetchWithTimeout(
      `/api/${path}`,
      {
        ..._mergeFetchOptions(options, authenticationManager.get()),
        body: JSON.stringify(payload),
      },
      timeout
    ).then((res) => {
      _throwIfNotOk(path, res);
      if (res.status === 204) {
        return {};
      }

      return res.json();
    });

    return _resultWithMinDelay(apiResult, minDelay);
  };

  const performApiCall = (
    path,
    options = { method: "GET" },
    timeout = DEFAULT_TIMEOUT,
    minDelay = DEFAULT_MIN_DELAY
  ) => {
    const apiResult = fetchWithTimeout(
      `/api/${path}`,
      _mergeFetchOptions(options, authenticationManager.get()),
      timeout
    ).then((res) => {
      _throwIfNotOk(path, res);
      if (res.status === 204) {
        return {};
      }
      return res.json();
    });

    return _resultWithMinDelay(apiResult, minDelay);
  };

  const externalizeDateTime = (t) => {
    const externalDateTime = new Date(t);
    // Very dirty hack(!)
    externalDateTime.setMinutes(t.getMinutes() - t.getTimezoneOffset());
    return externalDateTime.toISOString().slice(0, -1);
  };

  return {
    callWithBody: performApiCallWithBody,
    call: performApiCall,
    externalizeDateTime,
    defaultTimeout: DEFAULT_TIMEOUT,
    defaultMinDelay: DEFAULT_MIN_DELAY,
  };
};
