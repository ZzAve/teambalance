import React, { useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid2";
import Typography from "@mui/material/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";
import { TeamBalanceTheme, TENANT } from "../TenantContext";
import { ThemeToggler } from "./ThemeToggler";

const TopBar = (props: {
  handleRefresh: () => void;
  refresh: boolean;
  theme: TeamBalanceTheme;
  setTheme: (theme: TeamBalanceTheme) => void;
}) => {
  const [isAuth, setIsAuth] = useState(false);

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
        <Grid container spacing={2} alignItems="center" sx={{ width: "100%" }}>
          <Grid size="grow">
            <Typography variant="h6">{TENANT.title} Team balance</Typography>
          </Grid>

          <Grid>
            <ThemeToggler theme={props.theme} setTheme={props.setTheme} />
          </Grid>
          {isAuth ? (
            <>
              <Grid>
                <Refresh handleRefresh={props.handleRefresh} />
              </Grid>
              <Grid>
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
