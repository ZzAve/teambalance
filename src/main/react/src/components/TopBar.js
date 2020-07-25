import React, { useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";

const TopBar = ({ handleRefresh, refresh }) => {
  const [isAuth, setIsAuth] = useState(true);

  useEffect(() => {
    let checkAuthentication = authenticationManager.checkAuthentication();
    checkAuthentication.then(it => {
      console.log(`Result: ${it}`);
    });
    checkAuthentication.then(it => {
      setIsAuth(it);
    });
  }, [refresh]);

  return (
    <AppBar position="static">
      <Toolbar>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs>
            <Typography variant="h6">Tovo Heren 5 Teampot</Typography>
          </Grid>

          {isAuth ? (
            <>
              <Grid item>
                <Refresh handleRefresh={handleRefresh} />
              </Grid>
              <Grid item>
                <Logout handleRefresh={handleRefresh} />
              </Grid>
            </>
          ) : null}
        </Grid>
      </Toolbar>
    </AppBar>
  );
};

export default TopBar;
