import { Navigate, useLocation } from "react-router-dom";
import React from "react";
import { authenticationManager } from "../utils/AuthenticationManager";

const isAuthenticated = () => {
  return authenticationManager.isAuthenticated() === true;
};

export const RequireAuth = ({ children, redirectTo = "/authenticate" }) => {
  const location = useLocation();
  return isAuthenticated() ? (
    children
  ) : (
    <Navigate to={redirectTo} state={{ from: location }} replace={true} />
  );
};
