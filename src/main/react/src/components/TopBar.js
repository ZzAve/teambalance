import React, { useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Grid from "@material-ui/core/Grid";
import Typography from "@material-ui/core/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";
import Hidden from "@material-ui/core/Hidden";

const TopBar = ({ handleRefresh, refresh }) => {
  const [isAuth, setIsAuth] = useState(true);

  useEffect(() => {
    authenticationManager.checkAuthentication().then((it) => {
      console.debug(`[Topbar] Authentication result: ${it}`);
      setIsAuth(it);
    });
  }, [refresh]);

  console.debug("[Topbar] render");
  return (
    <AppBar position="static">
      <Toolbar>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs>
            <Typography variant="h6">
              Tovo Heren 5 <Hidden smDown>Teampot</Hidden>
            </Typography>
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
