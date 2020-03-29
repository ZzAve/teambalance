
export class InvalidSecretException extends Error {
    constructor (...args) {
        super(...args);
        Error.captureStackTrace(this, TimeoutError)

    }
}

export class TimeoutError extends Error {
    constructor (...args) {
        super(...args);
        Error.captureStackTrace(this, TimeoutError)

    }
}