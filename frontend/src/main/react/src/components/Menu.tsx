import React from "react";
import {
  BottomNavigation,
  BottomNavigationAction,
  Box,
  Divider,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Paper,
  SwipeableDrawer,
} from "@mui/material";
import List from "@mui/material/List";
import SportsVolleyballIcon from "@mui/icons-material/SportsVolleyball";
import ScoreboardIcon from "@mui/icons-material/Scoreboard";
import EventIcon from "@mui/icons-material/Event";
import SavingsIcon from "@mui/icons-material/Savings";
import PaymentsIcon from "@mui/icons-material/Payments";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import GroupsIcon from "@mui/icons-material/Groups";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import MenuIcon from "@mui/icons-material/Menu";
import HomeIcon from "@mui/icons-material/Home";
import SettingsIcon from "@mui/icons-material/Settings";
import { useNavigate } from "react-router-dom";

export const DrawerMenu = () => {
  const [isOpen, setIsOpen] = React.useState(false);

  const toggleDrawer =
    (open: boolean) => (event: React.KeyboardEvent | React.MouseEvent) => {
      if (
        event &&
        event.type === "keydown" &&
        ((event as React.KeyboardEvent).key === "Tab" ||
          (event as React.KeyboardEvent).key === "Shift")
      ) {
        return;
      }

      setIsOpen(open);
    };

  const menuList = () => (
    <Box
      sx={{ width: 250 }}
      role="presentation"
      onClick={toggleDrawer(false)}
      onKeyDown={toggleDrawer(false)}
    >
      <List>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <SportsVolleyballIcon />
            </ListItemIcon>
            <ListItemText primary="Trainingen" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <ScoreboardIcon />
            </ListItemIcon>
            <ListItemText primary="Wedstrijden" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <EventIcon />
            </ListItemIcon>
            <ListItemText primary="Overige Events" />
          </ListItemButton>
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <SavingsIcon />
            </ListItemIcon>
            <ListItemText primary="Balans" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <PaymentsIcon />
            </ListItemIcon>
            <ListItemText primary="Transacties" />
          </ListItemButton>
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <DarkModeIcon />
              <LightModeIcon />
            </ListItemIcon>
            <ListItemText primary="Toggle mode" />
          </ListItemButton>
        </ListItem>
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <GroupsIcon />
            </ListItemIcon>
            <ListItemText primary="Team" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton>
            <ListItemIcon>
              <AdminPanelSettingsIcon />
            </ListItemIcon>
            <ListItemText primary="Admin" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  );
  return (
    <React.Fragment key="left">
      <MenuIcon onClick={toggleDrawer(true)} />
      <SwipeableDrawer
        anchor="left"
        open={isOpen}
        onClose={toggleDrawer(false)}
        onOpen={toggleDrawer(true)}
      >
        {menuList()}
      </SwipeableDrawer>
    </React.Fragment>
  );
};

export const BottomMenu = () => {
  const [value, setValue] = React.useState(undefined);
  const navigate = useNavigate();
  React.useEffect(() => {
    // (ref.current as HTMLDivElement).ownerDocument.body.scrollTop = 0;
    console.log("Updated value to", value);
    if (value) navigate(value);
  }, [value]);

  return (
    <Paper
      sx={{ position: "fixed", bottom: 0, left: 0, right: 0 }}
      elevation={3}
    >
      <BottomNavigation
        value={value}
        onChange={(event, newValue) => {
          setValue(newValue);
        }}
      >
        <BottomNavigationAction
          label="Overzicht"
          value="/"
          icon={<HomeIcon />}
        />
        <BottomNavigationAction
          label="Events"
          value="/events"
          icon={<SportsVolleyballIcon />}
        />
        <BottomNavigationAction
          label="Munneys"
          value="/transactions"
          icon={<SavingsIcon />}
        />
        {/*<BottomNavigationAction*/}
        {/*  label="Admin"*/}
        {/*  value="/misc-events"*/}
        {/*  icon={<AdminPanelSettingsIcon />}*/}
        {/*/>*/}
        <BottomNavigationAction
          label="Settings"
          value="/admin/"
          icon={<SettingsIcon />}
        />
      </BottomNavigation>
    </Paper>
  );
};
