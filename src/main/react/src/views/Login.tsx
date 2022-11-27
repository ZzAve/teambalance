import React, { FormEvent, useEffect, useState } from "react";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import LockOpenIcon from "@material-ui/icons/LockOpen";
import PageItem from "../components/PageItem";
import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import { withLoading } from "../utils/util";
import Loading from "./Loading";
import { Navigate, useLocation } from "react-router-dom";
import { authenticationManager } from "../utils/AuthenticationManager";
import { createStyles, makeStyles } from "@material-ui/core";
import { LocationState } from "../components/utils";

const useStyles = makeStyles(() =>
  createStyles({
    hidden: {
      display: "none",
    },
  })
);

const Login = (opts: { handleRefresh: () => void }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const location = useLocation();

  const classes = useStyles();

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
  }, [input]);

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

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>) => setInput(e.target.value);

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
    <Grid item xs={12}>
      <Grid container spacing={2}>
        <PageItem title="Login" pageTitle="Login">
          <form onSubmit={handleLogin}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography>omdat we niet graag onze namen delen</Typography>
              </Grid>
              <Grid item>
                <TextField
                  className={classes.hidden}
                  type="text"
                  autoComplete="username"
                  value="tovoheren5"
                />
                <TextField
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
      </Grid>
    </Grid>
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
