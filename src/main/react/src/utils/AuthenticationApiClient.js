import {ApiClient} from "./apiClient";

const authenticationClient = ApiClient("auth");

const authenticate = (password ) => {
    return authenticationClient.call(`authentication`, {method:"GET", headers:{"X-Secret": btoa(password)}})
};

export const authenticationApiClient = {
    ...authenticationClient,
    authenticate

};