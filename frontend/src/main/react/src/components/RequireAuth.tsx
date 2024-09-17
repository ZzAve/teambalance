import { Navigate, useLocation } from "react-router-dom";
import React from "react";
import { authenticationManager } from "../utils/AuthenticationManager";

const isAuthenticated = () => authenticationManager.isAuthenticated();

export const RequireAuth = (props: { children: any; redirectTo?: string }) => {
  const { redirectTo = "/authenticate" } = props;
  const location = useLocation();
  return isAuthenticated() ? (
    props.children
  ) : (
    <Navigate to={redirectTo} state={{ from: location }} replace={true} />
  );
};
