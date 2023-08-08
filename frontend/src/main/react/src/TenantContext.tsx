import React, { createContext } from "react";

export enum TeamBalanceTheme {
  LIGHT = "light",
  DARK = "dark",
}

export type TenantInfo = {
  type: "tenantInfo";
  title: string;
  tenant: string;
  bunqMeBaseUrl: string;
};

export type TenantError = {
  type: "tenantError";
  details: string;
};

export const UNKNOWN_TENANT_CONTEXT: TenantInfo = {
  type: "tenantInfo",
  title: "Unknown tenant",
  tenant: "unknown",
  bunqMeBaseUrl: "https://bunq.com",
};
export const TenantContext: React.Context<TenantInfo> = createContext(
  UNKNOWN_TENANT_CONTEXT
);

export const getTenantInfo: () => Promise<
  TenantInfo | TenantError
> = async () => {
  try {
    const response = await fetch("/api/tenants/me");
    if (response.status < 299) {
      const body = await response.json();
      return {
        type: "tenantInfo",
        title: body.title,
        tenant: body.domain,
        bunqMeBaseUrl: body.bunqMeBaseUrl,
      };
    } else {
      const tenantError: TenantError = {
        type: "tenantError",
        details: `Current tenant information responded with status ${
          response.status
        }: ${await response.text()}`,
      };
      console.warn("Could not fetch tenant information. ", tenantError);
      return tenantError;
    }
  } catch (e) {
    const tenantError: TenantError = {
      type: "tenantError",
      details: `Could not fetch tenant information. ${e}`,
    };
    console.error("Something went wrong trying to to fetch the tenant", e);
    return tenantError;
  }
};
