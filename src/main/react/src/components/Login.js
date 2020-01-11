import React, { useEffect, useState } from "react";
import { Button, TextField } from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";
import LockOpenIcon from "@material-ui/icons/LockOpen";
import { SpinnerWithText } from "./SpinnerWithText";

const Texts = [
  "Netten aan het opspannen",
  "Muntjes poetsen",
  "🏐 TOVO TOVO 🏐 🥇",
  "De fluim van de maand verzinnen",
  "De eentjes van de nulletjes scheiden",
  "Wachtwoord controleren",
  "💵 💲 💰" // emojis
];
const initialState = {
  isUpdating: false,
  shouldUpdate: false
};

const Login = ({ loading, setSecret }) => {
  const [update, setUpdate] = useState(initialState);
  const [input, setInput] = useState("");
  const [text, setText] = useState("Inloggen ...");
  const [lastDelayedExecution, setLastDelayedExecution] = useState(-1);

  // Unmount cancellation effect
  useEffect(() => {
    // console.log("Registering setTimeout handle:", lastDelayedExecution);
    return () => {
      // console.log(
      //   `Cancelling previous delayed execution I guess? handle`,
      //   lastDelayedExecution
      // );
      clearTimeout(lastDelayedExecution);
    };
  }, [lastDelayedExecution]);

  useEffect(
    _ => {
      console.log("Loading state changed to ", loading);
      setUpdate(state => ({ ...state, shouldUpdate: loading }));
    },
    [loading]
  );

  useEffect(() => {
    if (update.shouldUpdate && !update.isUpdating) {
      const handle = updateText();
      setLastDelayedExecution(handle);
      setUpdate(state => ({
        ...state,
        isUpdating: true
      }));
    }
  }, [update]);

  const updateText = () =>
    setTimeout(() => {
      if (Math.random() < 0.5) {
        let index = Math.floor(Math.random() * Texts.length);
        setText(`${Texts[index]} ...`);
      }

      setUpdate(state => ({ ...state, isUpdating: false }));
    }, 500);

  const handleLogin = e => {
    e.preventDefault();
    setSecret(null);
    setTimeout(_ => {
      setSecret(input);
    });
  };

  const handleInput = e => {
    return setInput(e.target.value);
  };

  if (loading) {
    return <SpinnerWithText text={text} />;
  }

  return (
    <>
      <form onSubmit={handleLogin}>
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <Typography>omdat we niet graag onze namen delen</Typography>
          </Grid>
          <Grid item>
            <TextField
              id="secret"
              type="password"
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
    </>
  );
};

export default Login;
