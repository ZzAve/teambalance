import React, { useContext, useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";
import { TeamBalanceTheme, TenantContext } from "../TenantContext";
import { ThemeToggler } from "./ThemeToggler";

const TopBar = (props: {
  handleRefresh: () => void;
  refresh: boolean;
  theme: TeamBalanceTheme;
  setTheme: (theme: TeamBalanceTheme) => void;
}) => {
  const [isAuth, setIsAuth] = useState(false);
  const tenantContext = useContext(TenantContext);

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
            <Typography variant="h6">
              {tenantContext.title} Team balance
            </Typography>
          </Grid>

          <Grid item>
            <ThemeToggler theme={props.theme} setTheme={props.setTheme} />
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
