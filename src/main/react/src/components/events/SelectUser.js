import React, { useState } from "react";
import {
  Box,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
} from "@material-ui/core";
import LockIcon from "@material-ui/icons/Lock";
import LockOpenIcon from "@material-ui/icons/LockOpen";
import Typography from "@material-ui/core/Typography";
import { Attendee } from "../Attendees";
import { AlertLevel, useAlerts } from "../../hooks/alertsHook";

const noUser = "";
export const SelectUser = ({
  label,
  attendees,
  initialUser,
  selectedUserCallback,
}) => {
  const [isChanging, setIsChanging] = useState(false);
  const [selectedUser, setSelectedUser] = useState(
    (attendees.find((a) => a.user.id === initialUser?.id) || noUser)?.user?.id
  );
  const { addAlert } = useAlerts();

  const handleUserSelection = async (event) => {
    const newUser = event.target.value;
    const previousUser = selectedUser;
    console.log("New user selected:", newUser, "was", previousUser);

    setSelectedUser(newUser);
    const success = await selectedUserCallback(
      newUser === noUser ? undefined : newUser
    );

    if (!success) {
      //update failed, revert update
      setSelectedUser(previousUser);
      addAlert({
        message: `Kon ${label} '${
          attendees.find((it) => it.user.id === newUser)?.name
        }' niet selecteren voor`,
        level: AlertLevel.ERROR,
      });
      console.warn(
        `Could not update user to ${newUser} for event ${event}, reverting to ${previousUser}`
      );
    }

    setIsChanging(!success);
  };

  const toggleLock = () => {
    setIsChanging(!isChanging);
  };

  const attendee = attendees.find((it) => it.user.id === selectedUser);
  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">🏐&nbsp;{label}:&nbsp;</Typography>

      {isChanging ? (
        <FormControl fullWidth>
          <Select
            labelId="user-select"
            value={selectedUser}
            onChange={handleUserSelection}
          >
            <MenuItem key={noUser} value={noUser}>
              {label}
            </MenuItem>
            {attendees.map((it) => (
              <MenuItem key={it.user.id} value={it.user.id}>
                {it.user.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      ) : attendee ? (
        <Attendee
          key={attendee.state}
          disabled={false}
          attendee={attendee}
          onSelection={() => {}}
        ></Attendee>
      ) : (
        <Attendee
          key="empty"
          disabled={true}
          attendee={{ state: "default", user: { name: "niemand" } }}
          onSelection={() => {}}
        ></Attendee>
      )}
      <IconButton variant="outlined" onClick={toggleLock}>
        {isChanging ? <LockIcon /> : <LockOpenIcon />}
      </IconButton>
    </Box>
  );
};
