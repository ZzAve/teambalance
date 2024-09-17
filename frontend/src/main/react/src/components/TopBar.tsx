import React, { useEffect, useState } from "react";
import { authenticationManager } from "../utils/AuthenticationManager";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import { Refresh } from "./Refresh";
import { Logout } from "./Logout";
import { TeamBalanceTheme, TENANT } from "../TenantContext";
import { ThemeToggler } from "./ThemeToggler";
import { Hidden, IconButton } from "@mui/material";
import { DrawerMenu } from "./Menu";
import { useNavigate } from "react-router-dom";

const TopBar = (props: {
  handleRefresh: () => void;
  refresh: boolean;
  theme: TeamBalanceTheme;
  setTheme: (theme: TeamBalanceTheme) => void;
}) => {
  const [isAuth, setIsAuth] = useState(false);
  const navigate = useNavigate();
  useEffect(() => {
    authenticationManager.checkAuthentication().then((it) => {
      console.debug(`[Topbar] Authentication result: ${it}`);
      setIsAuth(it);
    });
  }, [props.refresh]);

  console.debug("[Topbar] render");
  const navigateHome = () => {
    return navigate("/");
  };
  return (
    <AppBar position="static">
      <Toolbar>
        <Grid container spacing={2} alignItems="center">
          <Hidden mdDown>
            <Grid item>
              <IconButton
                size="large"
                edge="start"
                color="inherit"
                aria-label="menu"
                sx={{ mr: 2 }}
              >
                <DrawerMenu />
              </IconButton>
            </Grid>
          </Hidden>
          <Grid item xs>
            <Typography
              onClick={() => navigateHome()}
              variant="h6"
              style={{ cursor: "pointer" }}
            >
              {TENANT.title} Team balance
            </Typography>
          </Grid>

          <Hidden mdDown>
            <Grid item>
              <ThemeToggler theme={props.theme} setTheme={props.setTheme} />
            </Grid>
          </Hidden>
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
