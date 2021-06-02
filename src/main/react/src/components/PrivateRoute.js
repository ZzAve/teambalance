import { Redirect, Route } from "react-router-dom";
import React from "react";
import { authenticationManager } from "../utils/AuthenticationManager";

const isAuthenticated = () => {
  return authenticationManager.isAuthenticated() === true;
};
export const PrivateRoute = ({ component: Component, ...rest }) => (
  <Route
    {...rest}
    children={({ ...props }) =>
      isAuthenticated() ? (
        <Component {...rest} />
      ) : (
        <Redirect
          to={{
            pathname: "/authenticate",
            state: { from: props.location },
          }}
        />
      )
    }
  />
);
