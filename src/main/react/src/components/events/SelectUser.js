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

  const handleUserSelection = (event) => {
    const newUser = event.target.value;
    console.log("User selected:", newUser);
    setSelectedUser(newUser);
    selectedUserCallback(newUser === noUser ? undefined : newUser);
    setIsChanging(false);
  };

  const toggleLock = () => {
    setIsChanging(!isChanging);
  };

  console.log("Initial user:", initialUser, "vs selectedUser:", selectedUser);
  const attendee = attendees.find((it) => it.user.id === selectedUser);
  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">🏐&nbsp;{label}:&nbsp;</Typography>

      {isChanging ? (
        <FormControl fullWidth>
          <InputLabel id="user-select">{label}</InputLabel>
          <Select
            labelId="user-select"
            value={selectedUser}
            label={label}
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
