import React, { FormEvent, useEffect, useState } from "react";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";
import LockOpenIcon from "@mui/icons-material/LockOpen";
import PageItem from "../components/PageItem";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import { withLoading } from "../utils/util";
import Loading from "./Loading";
import { Navigate, useLocation } from "react-router-dom";
import { authenticationManager } from "../utils/AuthenticationManager";

import { LocationState } from "../components/utils";

const Login = (opts: { handleRefresh: () => void }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const location = useLocation();

  useEffect(() => {
    //On startup , try with current value in authenticationManager
    setTimeout(() => {
      authenticationManager
        .checkAuthentication()
        .then((isAuth) => {
          console.debug(
            `Checked authentiation: user is ${
              isAuth ? "" : "NOT"
            } authenticated (${isAuth})`
          );
          setIsLoading(false);
          setIsAuthenticated(isAuth);
        })
        .catch(() => {
          setInput(authenticationManager.get() || "");
        });
    });
  }, []);

  useEffect(() => {
    console.debug(`Current state:
            authenticated: ${isAuthenticated}
            input: ${randomChars(Math.max((input || "").length, 0))}
            isLoading: ${isLoading}
      `);
  }, [input, isAuthenticated, isLoading]);

  const authenticate = (passphrase: string) =>
    withLoading(setIsLoading, () =>
      authenticationManager.authenticate(passphrase)
    )
      .then((_) => {
        setIsAuthenticated(true);
      })
      .catch((e) => {
        console.error("Login did not work", e);
      });

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    await authenticate(input);
  };

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) =>
    setInput(e.target.value);

  if (isAuthenticated) {
    const { from } = (location.state as LocationState) || {
      from: { pathname: "/" },
    };
    opts.handleRefresh();
    return <Navigate to={from} replace />;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <PageItem xs={12} title="Login" pageTitle="Login" dataTestId="login">
      <form onSubmit={handleLogin}>
        <Grid item container spacing={2}>
          <Grid item xs={12}>
            <Typography>omdat we niet graag onze namen delen</Typography>
          </Grid>
          <Grid item>
            <TextField
              variant="standard"
              sx={{ display: "none" }}
              type="text"
              autoComplete="username"
              value="tovo67"
            />
            <TextField
              variant="standard"
              id="secret"
              type="password"
              autoComplete="password"
              value={input}
              onChange={handleInput}
              placeholder="******"
              autoFocus
            />
          </Grid>
          <Grid item>
            <Button variant="contained" color="primary" type="submit">
              <LockOpenIcon /> Login
            </Button>
          </Grid>
        </Grid>
      </form>
    </PageItem>
  );
};

/**
 * Some fun and giggles
 * @param number
 * @returns {string}
 */
const randomChars = (number: number) => {
  let char = () => Math.floor(Math.random() * 36).toString(36);
  let outStr = "";
  while (outStr.length < number) {
    outStr += char();
  }
  return `=== ${number} = ${outStr} ===`;
};

export default Login;
