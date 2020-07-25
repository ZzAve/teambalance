import { Route } from "react-router-dom";
import React from "react";

export const PublicRoute = ({ component: Component, ...rest }) => (
  <Route {...rest} children={({ ...props }) => <Component {...rest} />} />
);
