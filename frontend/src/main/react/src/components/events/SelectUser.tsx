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
import { useAlerts } from "../../hooks/alertsHook";
import { Availability } from "../../utils/domain";
import { AttendeeButton } from "../Attendees";

export type SelectedUserOption = {
  id: number;
  name: string;
  state: Availability;
};

/**
 * Small UI component to select an option from a list of options
 *
 * Has 2 UI states:
 * <ul>
 *     <li>Showing a single option, as a piece of text. This is the initialOption initially; the selected option after changing the option</li>
 *     <li>Showing a dropdown with a list of options</li>
 * </ul>
 *
 */
export const SelectUser = <T extends SelectedUserOption>(props: {
  label: string;
  icon: string;
  options: T[];
  initialOption?: T;
  selectedUserCallback: (selected: T | undefined) => Promise<boolean>;
}) => {
  const [isOptionSelectionLocked, setIsOptionSelectionLocked] = useState(true);
  const [selectedOption, setSelectedOption] = useState<T | undefined>(
    props.initialOption
  );
  const { addAlert } = useAlerts();

  const handleOptionSelection = async (event: SelectChangeEvent<number>) => {
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
    const newOptionId = event.target.value;
    const previousOption = selectedOption;
    let newOption = props.options.find((x) => x.id === newOptionId);
    console.log(
      `New option selected: ${newOption?.name}. Was ${previousOption?.name}`
    );
    setSelectedOption(newOption);
    const success = await props.selectedUserCallback(newOption);

    if (!success) {
      //update failed, revert update
      setSelectedOption(previousOption);

      console.warn(
        `Could not update user to ${newOption?.name} for event ${event}, reverting to ${previousOption?.name}`
      );
    }

    setIsOptionSelectionLocked(success);
  };

  const toggleLock = () => {
    setIsOptionSelectionLocked(!isOptionSelectionLocked);
  };

  return (
    <Box display="flex" alignItems="center">
      <Typography variant="body1">
        {props.icon}&nbsp;{props.label}:&nbsp;
      </Typography>

      {isOptionSelectionLocked ? (
        <AttendeeButton
          key="empty"
          attendee={{
            state: selectedOption?.state || "NOT_RESPONDED",
            user: {
              name: selectedOption?.name || "niemand",
              id: 0,
              role: "OTHER",
              isActive: false,
            },
            id: -1,
            eventId: -1,
          }}
          onSelection={() => {}}
        ></AttendeeButton>
      ) : (
        <FormControl variant="standard" fullWidth>
          <Select
            variant="standard"
            labelId="user-select"
            value={selectedOption?.id || -1}
            onChange={handleOptionSelection}
          >
            <MenuItem key={-1} value={-1}>
              {props.label}
            </MenuItem>
            {props.options.map((it) => (
              <MenuItem key={it.id} value={it.id}>
                {it.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      )}
      <IconButton onClick={toggleLock}>
        {isOptionSelectionLocked ? <LockOpenIcon /> : <LockIcon />}
      </IconButton>
    </Box>
  );
};
