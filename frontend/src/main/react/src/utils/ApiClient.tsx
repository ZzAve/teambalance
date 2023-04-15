import { fetchWithTimeout } from "./fetchWithTimeout";
import { delay, toBase64 } from "./util";
import { authenticationManager } from "./AuthenticationManager";
import { InvalidSecretException, TimeoutError } from "./Exceptions";

const DEFAULT_TIMEOUT = 5000; //ms
const DEFAULT_MIN_DELAY = 100; //ms

const _mergeFetchOptions = (options: RequestInit, secret?: string) => ({
  ...options,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Secret": toBase64(secret || "nope"),
    ...options.headers,
  },
});

const _throwIfNotOk: (path: string, res: Response) => Promise<Response> = (
  path: string,
  res: Response
) => {
  if (res.ok) {
    return new Promise((resolve) => {
      resolve(res);
    });
  }

  //TODO Should be moved to a higher level where AUTH state is reachable
  if (res.status === 403) {
    console.log("Status was 403");
    // setAuthenticated(false);
    throw new InvalidSecretException("nope");
  }
  if (res.status === 504) {
    throw new TimeoutError("Response timed out");
  }
  return res
    .json()
    .catch((x) => {
      throw Error(
        `Call to '${path}' returned an erroneous response (code ${res.status})`
      );
    })
    .then((data) => {
      throw Error(`${data.reason}`);
    });
};

const _resultWithMinDelay = async (
  result: Promise<object>,
  minDelay: number
) => {
  await delay(minDelay);
  return result;
};

export const ApiClient = () => {
  const performApiCallWithBody = (
    path: string,
    payload: object,
    options: RequestInit = { method: "POST" },
    timeout: number = DEFAULT_TIMEOUT,
    minDelay: number = DEFAULT_MIN_DELAY
  ) => {
    const apiResult = fetchWithTimeout(
      `/api/${path}`,
      {
        ..._mergeFetchOptions(options, authenticationManager.get()),
        body: JSON.stringify(payload),
      },
      timeout
    )
      .then((res) => _throwIfNotOk(path, res))
      .then((res) => {
        if (res.status === 204) {
          return {};
        }

        return res.json();
      });

    return _resultWithMinDelay(apiResult, minDelay);
  };

  const performApiCall = (
    path: string,
    options: RequestInit = { method: "GET" },
    timeout: number = DEFAULT_TIMEOUT,
    minDelay: number = DEFAULT_MIN_DELAY
  ) => {
    const apiResult = fetchWithTimeout(
      `/api/${path}`,
      _mergeFetchOptions(options, authenticationManager.get()),
      timeout
    )
      .then((res) => _throwIfNotOk(path, res))
      .then((res) => {
        if (res.status === 204) {
          return {};
        }
        return res.json();
      });

    return _resultWithMinDelay(apiResult, minDelay);
  };

  const externalizeDateTime = (t?: Date) => {
    if (!t) return undefined;

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
