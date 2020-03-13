import {fetchWithTimeout} from "./fetchWithTimeout";
import {delay} from "./util";


const DEFAULT_TIMEOUT = 5000; //ms
const DEFAULT_MIN_DELAY = 750; //ms

const _getFetchOptions = (method, secret) => ({
    method: method,
    headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "X-Secret": btoa(secret)
    }
});

const _throwIfNotOk = (res) => {
    if (!res.ok) {
        //TODO Should be moved to a higher level where AUTH state is reachable
        if (res.status === 403) {
            console.log("Status was 403");
            // setAuthenticated(false);
        }

        throw Error(
            `Call to '${path}' returned an erroneous response (code ${res.status})`
        );
    }
};


const _resultWithMinDelay = (result, minDelay) =>
    Promise.all([result, delay(minDelay)])
        .then(([result, _]) => result);


export const ApiClient = () => {
    let apiSecret = null;

    const performApiCallWithBody = (path, body, method = "POST", timeout = DEFAULT_TIMEOUT, minDelay = DEFAULT_MIN_DELAY) => {
        // debugger
        const apiResult = fetchWithTimeout(
            `/api/${path}`,
            {
                ..._getFetchOptions(method),
                body: JSON.stringify(body)
            },
            timeout
        ).then(res => {
            _throwIfNotOk(res);
            return res.json();
        });

        return _resultWithMinDelay(apiResult, minDelay);
    };


    const performApiCall = (path, method = "GET", timeout = DEFAULT_TIMEOUT, minDelay = DEFAULT_MIN_DELAY) => {
        const apiResult = fetchWithTimeout(
            `/api/${path}`,
            _getFetchOptions(method),
            timeout
        ).then(res => {
            _throwIfNotOk(res);
            return res.json();
        });

        return _resultWithMinDelay(apiResult, minDelay);
    };

    const setSecret = (secret) => {
        apiSecret = btoa(secret);
    };
    return {
        callWithBody: performApiCallWithBody,
        call: performApiCall,
        defaultTimeout: DEFAULT_TIMEOUT,
        defaultMinDelay: DEFAULT_MIN_DELAY,
        setSecret
    };
};
