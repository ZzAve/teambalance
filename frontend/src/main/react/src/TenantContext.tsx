import React, { createContext } from "react";

export enum TeamBalanceTheme {
  LIGHT = "light",
  DARK = "dark",
}

export type TenantInfo = {
  type: "tenantInfo";
  title: string;
};

// @ts-ignore
const envVariables = import.meta.env;
console.error("env variables :", envVariables);
export const TENANT: TenantInfo = {
  type: "tenantInfo",
  title: envVariables.VITE_TENANT,
};

export const UNKNOWN_TENANT_CONTEXT: TenantInfo = {
  type: "tenantInfo",
  title: "Unknown tenant",
};
export const TenantContext: React.Context<TenantInfo> = createContext(
  UNKNOWN_TENANT_CONTEXT
);
