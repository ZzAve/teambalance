import {ApiClient} from "./ApiClient";

const authenticationClient = ApiClient("auth");

const authenticate = (password ) => {
    return authenticationClient.call(`authentication`, {method:"GET", headers:{"X-Secret": btoa(password)}})
};

export const authenticationApiClient = {
    ...authenticationClient,
    authenticate

};