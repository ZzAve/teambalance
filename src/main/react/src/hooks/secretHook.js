import React, { useEffect, useState } from "react";
import Aes from "crypto-js/aes";
import CryptoJS from "crypto-js";
import {useSharedState} from "./SharedState";
const _key = "apiSecret";

const PRIVATE_ENCRYPTION_KEY =
  "ZtptKHnNIhr7gqycuyDlSE8mIpBGaJJsiU3IEB7dQvtsO9vUMGctHdZtzOzoLH8shjzSUoO6YMMz3elMGU16YDooJXgtxSObUXZkxh6XnPY6QSz1HwL5W1uwpFv4oqzIzHbp8lTYGS1mCmuUbuKu87S8fhMhyUJwr1NG00R1bCXDkemdVXvD4ChwZDrsehxGuu3EHYGXGTyp5Sf3Q8bZC28ktZcNnf78D57kRo2wIgPdbi2j60SNOAvR4g34WXFU";
export function useSecretStore(key = Math.random()) {
  const [secret, setSecret] = useSharedState(key, null);

  useEffect(() => {
    let item = localStorage.getItem(_key);
    if (item === null || item === "null") {
      setSecret(null);
      return;
    }

    // Decrypt
    let cipher = item.toString();
    // console.log(`Decrypting '${cipher}'`);
    let decrypt = Aes.decrypt(cipher, PRIVATE_ENCRYPTION_KEY);
    item = decrypt.toString(CryptoJS.enc.Utf8);

    // console.log(`item ${item} (${typeof item})`);
    setSecret(item);
  }, []);

  const handleSecret = newSecret => {
      try {
        if (newSecret === null) {
          localStorage.removeItem(_key);
        } else {
          const ciphertext = Aes.encrypt(newSecret, PRIVATE_ENCRYPTION_KEY);
          localStorage.setItem(_key, ciphertext);
        }

        setSecret(newSecret);
      } catch (e) {
        console.error("Could not reach localstorage");
      }
  };

  return [secret, handleSecret];
}
