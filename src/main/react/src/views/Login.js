import React, { useEffect, useState } from "react";
import Button from "@material-ui/core/Button";
import Grid from "@material-ui/core/Grid";
import LockOpenIcon from "@material-ui/icons/LockOpen";
import PageItem from "../components/PageItem";
import TextField from "@material-ui/core/TextField";
import Typography from "@material-ui/core/Typography";
import { withLoading } from "../utils/util";
import Loading from "./Loading";
import { Redirect } from "react-router-dom";
import { authenticationManager } from "../utils/AuthenticationManager";

const Login = ({ location, handleRefresh }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(undefined);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    //On startup , try with current value in authenticationManager
    setTimeout(() => {
      authenticationManager.checkAuthentication().then((isAuth) => {
        console.debug(
          `Checked authentiation: user is ${
            isAuth ? "" : "NOT"
          } authenticated (${isAuth})`
        );
        setIsLoading(false);
        setIsAuthenticated(isAuth);
      });
    });
  }, []);

  useEffect(() => {
    console.debug(`Current state:
            authenticated: ${isAuthenticated}
            input: ${randomChars(Math.max(input.length, 0))}
            isLoading: ${isLoading}
      `);
  }, [input]);

  const authenticate = (passphrase) =>
    withLoading(setIsLoading, () =>
      authenticationManager.authenticate(passphrase)
    )
      .catch((e) => {
        console.error("Login did not work", e);
      })
      .then((_) => {
        setIsAuthenticated(true);
      });

  const handleLogin = async (e) => {
    e.preventDefault();
    await authenticate(input);
  };

  const handleInput = (e) => setInput(e.target.value);

  if (isAuthenticated) {
    const { from } = location.state || { from: { pathname: "/" } };
    handleRefresh();
    return <Redirect to={from} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <Grid item xs={12}>
      <Grid container spacing={2}>
        <PageItem title="Login">
          <form onSubmit={handleLogin}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography>omdat we niet graag onze namen delen</Typography>
              </Grid>
              <Grid item>
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
const randomChars = (number) => {
  let char = () => Math.floor(Math.random() * 36).toString(36);
  let outStr = "";
  while (outStr.length < number) {
    outStr += char();
  }
  return `=== ${number} = ${outStr} ===`;
};

export default Login;
