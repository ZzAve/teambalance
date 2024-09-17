import React, { useEffect, useState } from "react";
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
import { Availability, TeamBalanceId } from "../../utils/domain";
import { AttendeeButton } from "../Attendees";

export type SelectedUserOption = {
  index: number;
  id: TeamBalanceId;
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
export const SelectUser = (props: {
  label: string;
  icon: string;
  options: SelectedUserOption[];
  initialOption?: SelectedUserOption;
  selectedUserCallback: (
    selected: SelectedUserOption | undefined
  ) => Promise<boolean>;
}) => {
  const [isOptionSelectionLocked, setIsOptionSelectionLocked] = useState(true);
  const [selectedOption, setSelectedOption] = useState<
    SelectedUserOption | undefined
  >(props.initialOption);

  useEffect(() => {
    // console.debug("Updating selectedOption");
    setSelectedOption(props.options.find((it) => it.id === selectedOption?.id));
  }, [props.options]);

  const handleOptionSelection = async (event: SelectChangeEvent) => {
    const newOptionId: string = event.target.value;
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
          size="small"
          key="empty"
          attendee={{
            state: selectedOption?.state || "NOT_RESPONDED",
            user: {
              name: selectedOption?.name || "niemand",
              id: "no-user",
              role: "OTHER",
              isActive: false,
            },
            id: "no-attendee",
            eventId: "no-event",
          }}
          onSelection={() => {}}
        ></AttendeeButton>
      ) : (
        <FormControl variant="standard" fullWidth>
          <Select
            variant="standard"
            labelId="user-select"
            value={selectedOption?.id || "no-user"}
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
