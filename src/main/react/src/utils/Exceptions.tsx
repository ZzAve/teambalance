export class InvalidSecretException extends Error {
  constructor(...args: string[]) {
    super(...args);
    Error.captureStackTrace(this, TimeoutError);
  }
}

export class TimeoutError extends Error {
  constructor(...args: string[]) {
    super(...args);
    Error.captureStackTrace(this, TimeoutError);
  }
}
