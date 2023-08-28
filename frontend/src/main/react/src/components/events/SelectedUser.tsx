import React, { useState } from "react";
import {
  Box,
  FormControl,
  IconButton,
  MenuItem,
  Select,
  SelectChangeEvent,
} from "@mui/material";
import LockIcon from "@mui/icons-material/Lock";
import LockOpenIcon from "@mui/icons-material/LockOpen";
import Typography from "@mui/material/Typography";
import { AttendeeButton } from "../Attendees";
import { useAlerts } from "../../hooks/alertsHook";
import { Attendee as AttendeeType, User } from "../../utils/domain";

const noUser = -1;
const noUserExternal = undefined;

const internalize = (id: number | undefined) =>
  id === noUserExternal ? noUser : id;
const externalize = (id: number) => (id === noUser ? noUserExternal : id);

/**
 * Small UI component to select a user from a list of Attendees.
 *
 * Has 2 UI states:
 * <ul>
 *     <li>Showing a single user, as an {@link AttendeeButton}. This is the initialUser initially; the selected users after changing the user</li>
 *     <li>Showing a dropdown with a list of users (/attendees)</li>
 * </ul>
 *
 */
export const SelectedUser = (props: {
  label: string;
  attendees: AttendeeType[];
  initialUser?: User;
  selectedUserCallback: (userId: number | undefined) => Promise<boolean>;
}) => {
  const [isUserSelectionLocked, setIsUserSelectionLocked] = useState(true);
  const [selectedUser, setSelectedUser] = useState<number>(
    internalize(props.initialUser?.id)
  );

  const { addAlert } = useAlerts();
  const handleUserSelection = async (event: SelectChangeEvent<number>) => {
    if (typeof event.target.value !== "number") {
      console.error("Something is horribly wrong");
      addAlert({
        message:
          "Iets of iemand heeft teambalance kapot gemaakt... Tijd om de hulpdiensten te bellen?",
        level: "error",
        canClose: false,
      });
      return;
    }
    const newUser = event.target.value;
    const previousUser = selectedUser;
    console.log("New user selected:", newUser, "was", previousUser);
    setSelectedUser(newUser);
    const success = await props.selectedUserCallback(externalize(newUser));

    if (!success) {
      //update failed, revert update
      setSelectedUser(previousUser);

      console.warn(
        `Could not update user to ${newUser} for event ${event}, reverting to ${previousUser}`
      );
    }

    setIsUserSelectionLocked(success);
  };

  const toggleLock = () => {
    setIsUserSelectionLocked(!isUserSelectionLocked);
  };

  const showSelectedUser = (attendee?: AttendeeType) =>
    attendee ? (
      <AttendeeButton
        key={attendee.state}
        disabled={false}
        attendee={attendee}
        onSelection={() => {}}
      ></AttendeeButton>
    ) : (
      <AttendeeButton
        key="empty"
        disabled={true}
        attendee={{
          state: "NOT_RESPONDED",
          user: { name: "niemand", id: 0, role: "OTHER", isActive: false },
          id: -1,
          eventId: -1,
        }}
        onSelection={() => {}}
      ></AttendeeButton>
    );

  const attendee = props.attendees.find((it) => it.user.id === selectedUser);

  const showUserSelectionDropdown = () => (
    <FormControl variant="standard" fullWidth>
      <Select
        variant="standard"
        labelId="user-select"
        value={selectedUser}
        onChange={handleUserSelection}
      >
        <MenuItem key={-1} value={-1}>
          {props.label}
        </MenuItem>
        {props.attendees.map((it) => (
          <MenuItem key={it.user.id} value={it.user.id}>
            {it.user.name}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );

  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">🏐&nbsp;{props.label}:&nbsp;</Typography>

      {isUserSelectionLocked
        ? showSelectedUser(attendee)
        : showUserSelectionDropdown()}
      <IconButton onClick={toggleLock}>
        {isUserSelectionLocked ? <LockOpenIcon /> : <LockIcon />}
      </IconButton>
    </Box>
  );
};
