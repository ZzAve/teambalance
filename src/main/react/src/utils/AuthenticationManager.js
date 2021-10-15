import React from "react";
import Aes from "crypto-js/aes";
import Utf8 from "crypto-js/enc-utf8";
import { authenticationApiClient } from "./AuthenticationApiClient";
import { TimeoutError } from "./Exceptions";

const _key = "apiSecret";
const PRIVATE_ENCRYPTION_KEY =
  "ZtptKHnNIhr7gqycuyDlSE8mIpBGaJJsiU3IEB7dQvtsO9vUMGctHdZtzOzoLH8shjzSUoO6YMMz3elMGU16YDooJXgtxSObUXZkxh6XnPY6QSz1HwL5W1uwpFv4oqzIzHbp8lTYGS1mCmuUbuKu87S8fhMhyUJwr1NG00R1bCXDkemdVXvD4ChwZDrsehxGuu3EHYGXGTyp5Sf3Q8bZC28ktZcNnf78D57kRo2wIgPdbi2j60SNOAvR4g34WXFU";

let _secret = undefined;
let _authenticated = false;
let _authenticationCheck = new Promise((resolve) => {
  resolve(false);
});

const getSecretFromLocalStorage = () => {
  let item = localStorage.getItem(_key);
  if (item === null || item === "null") {
    return null;
  }

  // Decrypt
  let cipher = item.toString();
  let decrypt = Aes.decrypt(cipher, PRIVATE_ENCRYPTION_KEY);
  item = Utf8.stringify(decrypt);

  return item;
};

const storeSecret = (newSecret) => {
  try {
    if (newSecret === null) {
      localStorage.removeItem(_key);
    } else {
      const ciphertext = Aes.encrypt(newSecret, PRIVATE_ENCRYPTION_KEY);
      localStorage.setItem(_key, ciphertext);
    }

    // setSecret(newSecret);
  } catch (e) {
    console.error("Could not reach localstorage");
  }
};

const setSecret = (newSecret) => {
  storeSecret(newSecret);
  _secret = newSecret;
};

const getSecret = () => _secret;

const isAuthenticated = () => _authenticated;
const checkAuthentication = () => _authenticationCheck;

const _doAuthenticate = (passphrase) =>{
  setSecret(passphrase);
  return authenticationApiClient.authenticate(passphrase).then((result) => {
    console.log(`Successful authentication ${result.message}`);
  });
}

const authenticate = (passphrase) => {
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
  setSecret(null);
  _authenticated = false;
  _authenticationCheck = new Promise((resolve) => {
    resolve(_authenticated);
  });
};
const recursiveAuth = async (pass, number = 1) => {
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

const startupAuth = (passphrase) => {
  _authenticationCheck = new Promise((resolve) => {
    return recursiveAuth(passphrase)
      .then((_) => {
        _authenticated = true;
        resolve(true);
      })
      .catch((_) => {
        _authenticated = false;
        resolve(false);
      });
  });
};

setTimeout(() => {
  //on start fetch from localStorage
  _secret = getSecretFromLocalStorage("apiSecret");

  // on start check credentials
  startupAuth(_secret);
});

export const authenticationManager = {
  get: getSecret,
  set: setSecret,
  isAuthenticated, // boolean
  checkAuthentication, // promise
  authenticate,
  logout,
};
