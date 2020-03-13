// src/App.js

import React, { useEffect, useState } from "react";
import {Transactions} from "./components/Transactions";
import Topup from "./components/Topup";
import Login from "./views/Login";
import Balance from "./components/Balance";
import "./App.css";
import {
  Card,
  CardContent,
  CardHeader,
  Container,
  CssBaseline,
  makeStyles
} from "@material-ui/core";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import { useSecretStore } from "./hooks/secretHook";
import { fetchWithTimeout } from "./utils/fetchWithTimeout";
import { Logout } from "./components/Logout";
import { Refresh } from "./components/Refresh";
import Overview from "./views/Overview";
import PageItem from "./components/PageItem";

const TopBar = ({ authenticated, handleRefresh, setSecret }) => {
  return (
    <AppBar position="static">
      <Toolbar>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs>
            <Typography variant="h6">Tovo Heren 5 Teampot</Typography>
          </Grid>
          {authenticated ? (
            <>
              <Grid item>
                <Refresh handleRefresh={handleRefresh} />
              </Grid>
              <Grid item>
                <Logout setSecret={setSecret} />
              </Grid>
            </>
          ) : null}
        </Grid>
      </Toolbar>
    </AppBar>
  );
};

const Wrapper = ({ authenticated, setSecret, handleRefresh, children }) => {
  return (
    <>
      <CssBaseline />
      <Container maxWidth="lg">
        <TopBar
          authenticated={authenticated}
          handleRefresh={handleRefresh}
          setSecret={setSecret}
        />
        {children}
      </Container>
    </>
  );
};

const LoginGrid = ({ isLoading, setSecret }) => {
  return (
      <Login loading={isLoading} setSecret={setSecret} />
      );
};

const delay = ms => new Promise(res => setTimeout(res, ms));


const useStyles = makeStyles({
  alignCenter: {
    // flexDirection:"column",
    alignItems: "center",
    justifyContent: "center",
    display: "flex",
    minHeight: 150
  }
});

const initialState = {
  balance: "€ XX,XX",
  transactions: [],
  bunqMeUrl: "https://bunq.me/tovoheren5",
  trainings: []
};
const initialApiState = {
  loadingAuthentication: false,
  loadingTransactions: false,
  loadingBalance: false,
  loadingTrainings: false
};

const App = () => {
  const [state, setState] = useState(initialState);
  const [loadingState, setLoadingState] = useState(initialApiState);
  const [secret, setSecret] = useSecretStore();
  const [authenticated, setAuthenticated] = useState(false);

  const classes = useStyles();

  useEffect(() => {
    //Login user -- with retry for timeouts
    // console.log(`Secret has changed to ${secret}`);
    if (secret === null) {
      setAuthenticated(false);
      return;
    }

    // console.log("Secret has changed -- Authentication user");
    setLoadingState(state => ({
      ...state,
      loadingAuthentication: true
    }));
    authenticate();
  }, [secret]);

  useEffect(() => {
    //Do API calls
    // console.log(`Authenticated has changed, is '${authenticated}`);
    if (authenticated === false) {
      return;
    }
    if (secret == null) {
      console.error(
        "Authenticated user but no secret has been stored. Deauthenticating user"
      );
      setAuthenticated(false);
    }

    // callBankAPI();
    callTrainingApi()
  }, [authenticated]);

  const apiCall = (path, method = "GET", timeout= 5000, minDelay = 750) => {
    // console.log(`Calling  ${method} ${path}`);
    const apiResult = fetchWithTimeout(
        `/api/${path}`,
        {
          method: method,
          headers: {
            Accept: "application/json",
            "X-Secret": btoa(secret)
          }
        },
        timeout
    ).then(res => {
      if (!res.ok) {
        if (res.status === 403) {
          // console.log("Status was 403");
          setAuthenticated(false);
        }

        throw Error(
          `Call to '${path}' returned an erroneous response (code ${res.status})`
        );
      }

      return res.json();
    });

    return Promise.all([apiResult,delay(minDelay)])
        .then(([result,_]) => result
        );
  };

  const authenticate = _ => {
    apiCall("authentication")
      .then(result => {
        // console.log(`Authenticated: '${result.message}'`);
        setAuthenticated(true);
        setLoadingState(state => ({
          ...state,
          loadingAuthentication: false
        }));
      })
      .catch(e => {
        console.log(e);
        if (e.message === "Response timed out") {
          setTimeout(_ => {
            authenticate();
          }, 2000);
        } else {
          setLoadingState(state => ({
            ...state,
            loadingAuthentication: false
          }));
        }
      });
  };

  const callTrainingApi = _ => {
      setLoadingState(state => ({
          ...state,
          loadingTrainings: true
      }));

      let hours = new Date();

      apiCall(`trainings?since=${hours.toJSON()}&includeAttendees=true`)
          .then(data => {
              setState(state => ({
                  ...state,
                  trainings: data.trainings
              }))
          })
          .catch(e => {
              console.error(e)
          })
          .finally(_ => {
              setLoadingState(state => ({
                  ...state,
                  loadingTrainings: false
              }))
          });
  };

  const callBankAPI = _ => {
    setLoadingState(state => ({
      ...state,
      loadingTransactions: true,
      loadingBalance: true
    }));

    apiCall("bank/balance")
      .then(data => {
        setState(state => ({
          ...state,
          balance: data.balance || initialState.balance
        }));
      })
      .catch(e => {
        console.error(e);
      })
      .finally(_ => {
        setLoadingState(state => ({
          ...state,
          loadingBalance: false
        }));
      });

    apiCall("bank/transactions")
      .then(data => {
        const transactions =
          internalize(data.transactions) || initialState.transactions;
        setState(state => ({ ...state, transactions }));
      })
      .catch(e => {
        console.error(e);
      })
      .finally(_ => {
        setLoadingState(state => ({
          ...state,
          loadingTransactions: false
        }));
      });
  };

  const internalize = transactions => {
    return transactions.map(transaction => ({
      id: transaction.id,
      amount: transaction.amount,
      counterParty: transaction.counterParty,
      date: new Date(transaction.timestamp * 1000)
    }));
  };

  const handleRefresh = _ => {
    callBankAPI();
  };

  return (
    <>
      <CssBaseline />
      <Container maxWidth="lg">
        <TopBar
          authenticated={authenticated}
          handleRefresh={handleRefresh}
          setSecret={setSecret}
        />

        <Grid container spacing={2} alignItems="flex-start">
          {authenticated ? (
            <Overview state={state} loadingState={loadingState} />
          ) : (
            <Login loading={loadingState.loadingAuthentication} setSecret={setSecret} />
          )}
        </Grid>
      </Container>
    </>
  );
};

export default App;
