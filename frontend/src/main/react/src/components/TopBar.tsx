import React, { useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";

const TopBar = (props: { handleRefresh: () => void; refresh: boolean }) => {
  const [isAuth, setIsAuth] = useState(true);

  useEffect(() => {
    authenticationManager.checkAuthentication().then((it) => {
      console.debug(`[Topbar] Authentication result: ${it}`);
      setIsAuth(it);
    });
  }, [props.refresh]);

  console.debug("[Topbar] render");
  return (
    <AppBar position="static">
      <Toolbar>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs>
            <Typography variant="h6">Tovo Heren 5 Team balance</Typography>
          </Grid>

          {isAuth ? (
            <>
              <Grid item>
                <Refresh handleRefresh={props.handleRefresh} />
              </Grid>
              <Grid item>
                <Logout handleRefresh={props.handleRefresh} />
              </Grid>
            </>
          ) : null}
        </Grid>
      </Toolbar>
    </AppBar>
  );
};

export default TopBar;
