import {fetchWithTimeout} from "./fetchWithTimeout";
import {delay} from "./util";
import {authenticationManager} from "./AuthenticationManager";


const DEFAULT_TIMEOUT = 5000; //ms
const DEFAULT_MIN_DELAY = 500; //ms

const _mergeFetchOptions = (options, secret) => ({
    ...options,
    headers: {
        "Content-Type": "application/json",
        "Accept": "application/json",
        "X-Secret": btoa(secret),
        ...options.headers
    }
});

const _throwIfNotOk = (path, res) => {
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

    const performApiCallWithBody = (path, payload, options = {method:"POST"},  timeout = DEFAULT_TIMEOUT, minDelay = DEFAULT_MIN_DELAY) => {
        // debugger
        const apiResult = fetchWithTimeout(
            `/api/${path}`,
            {
                ..._mergeFetchOptions(options, authenticationManager.get()),
                body: JSON.stringify(payload)
            },
            timeout
        ).then(res => {
            _throwIfNotOk(path, res);
            return res.json();
        });

        return _resultWithMinDelay(apiResult, minDelay);
    };


    const performApiCall = (path, options = {method: "GET"}, timeout = DEFAULT_TIMEOUT, minDelay = DEFAULT_MIN_DELAY) => {
        const apiResult = fetchWithTimeout(
            `/api/${path}`,
            _mergeFetchOptions(options, authenticationManager.get()),
            timeout
        ).then(res => {
            _throwIfNotOk(path, res);
            return res.json();
        });

        return _resultWithMinDelay(apiResult, minDelay);
    };

    return {
        callWithBody: performApiCallWithBody,
        call: performApiCall,
        defaultTimeout: DEFAULT_TIMEOUT,
        defaultMinDelay: DEFAULT_MIN_DELAY
    };
};
