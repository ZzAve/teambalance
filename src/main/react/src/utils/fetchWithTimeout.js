import { TimeoutError } from "./Exceptions";

export const fetchWithTimeout = (uri, options = {}, time = 5000) => {
  // Lets set up our `AbortController`, and create a request options object
  // that includes the controller's `signal` to pass to `fetch`.
  const controller = new AbortController();
  const config = { ...options, signal: controller.signal };
  // Set a timeout limit for the request using `setTimeout`. If the body of this
  // timeout is reached before the request is completed, it will be cancelled.
  const timeout = setTimeout(() => {
    controller.abort();
  }, time);
  return fetch(uri, config).catch((error) => {
    // When we abort our `fetch`, the controller conveniently throws a named
    // error, allowing us to handle them separately from other errors.
    if (error.name === "AbortError") {
      throw new TimeoutError("Response timed out");
    }
    throw new Error(error.message);
  });
};
