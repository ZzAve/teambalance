import Aes from "crypto-js/aes";
import Utf8 from "crypto-js/enc-utf8";
import { authenticationApiClient } from "./AuthenticationApiClient";
import { TimeoutError } from "./Exceptions";

const _key = "apiSecret";
const PRIVATE_ENCRYPTION_KEY =
  "ZtptKHnNIhr7gqycuyDlSE8mIpBGaJJsiU3IEB7dQvtsO9vUMGctHdZtzOzoLH8shjzSUoO6YMMz3elMGU16YDooJXgtxSObUXZkxh6XnPY6QSz1HwL5W1uwpFv4oqzIzHbp8lTYGS1mCmuUbuKu87S8fhMhyUJwr1NG00R1bCXDkemdVXvD4ChwZDrsehxGuu3EHYGXGTyp5Sf3Q8bZC28ktZcNnf78D57kRo2wIgPdbi2j60SNOAvR4g34WXFU";

let _secret: string | undefined = undefined;
let _authenticated = false;
let _authenticationCheck: Promise<boolean> = new Promise((resolve) => {
  resolve(false);
});

const getSecretFromLocalStorage: () => string | undefined = () => {
  let item = localStorage.getItem(_key);
  if (item === null || item === "null") {
    return undefined;
  }

  // Decrypt
  let cipher = item.toString();
  let decrypt = Aes.decrypt(cipher, PRIVATE_ENCRYPTION_KEY);
  item = Utf8.stringify(decrypt);

  return item;
};

const storeSecret = (newSecret?: string) => {
  try {
    if (newSecret === undefined) {
      localStorage.removeItem(_key);
    } else {
      const ciphertext = Aes.encrypt(newSecret, PRIVATE_ENCRYPTION_KEY);
      localStorage.setItem(_key, ciphertext.toString());
    }
  } catch (e) {
    console.error("Could not reach localstorage");
  }
};

const setSecret = (newSecret?: string) => {
  storeSecret(newSecret);
  _secret = newSecret;
};

const getSecret = () => _secret;

const isAuthenticated = () => _authenticated;
const checkAuthentication: () => Promise<boolean> = () => _authenticationCheck;

const _doAuthenticate = async (passphrase: string) => {
  setSecret(passphrase);
  const result = await authenticationApiClient.authenticate(passphrase);
  console.debug(`Successful authentication`, result);
};

const authenticate = (passphrase: string) => {
  let isAuthenticated = recursiveAuth(passphrase);

  _authenticationCheck = new Promise((resolve) =>
    isAuthenticated
      .then((_) => true)
      .catch((_) => false)
      .then((it) => {
        _authenticated = it;
        resolve(it);
      })
  );

  return isAuthenticated;
};

const logout = () => {
  setSecret(undefined);
  _authenticated = false;
  _authenticationCheck = new Promise((resolve) => {
    resolve(_authenticated);
  });
};
const recursiveAuth = async (pass: string, number: number = 1) => {
  if (number > 10) {
    throw Error("Too many tries");
  }

  try {
    console.debug(`Trying to recursive auth ${number}`);
    await _doAuthenticate(pass);
  } catch (e) {
    if (e instanceof TimeoutError) {
      await recursiveAuth(pass, ++number);
    } else {
      console.debug("Unknown error occured", e);
      throw e;
    }
  }
};

const startupAuth = (passphrase: string) => {
  _authenticationCheck = new Promise(async (resolve) => {
    try {
      const _ = await recursiveAuth(passphrase);
      _authenticated = true;
      resolve(true);
    } catch (__1) {
      _authenticated = false;
      resolve(false);
    }
  });
};

setTimeout(() => {
  console.log("Startup authentication check");
  //on start fetch from localStorage
  _secret = getSecretFromLocalStorage();

  // on start check credentials
  if (_secret !== undefined) {
    startupAuth(_secret);
  }
});

export const authenticationManager = {
  get: getSecret,
  set: setSecret,
  isAuthenticated, // boolean
  checkAuthentication, // promise
  authenticate,
  logout,
};
