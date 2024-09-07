import Grid from "@mui/material/Grid";
import React, { useEffect, useState } from "react";
import FormControlLabel from "@mui/material/FormControlLabel";
import Switch from "@mui/material/Switch";
import { attendeesApiClient } from "../../utils/AttendeesApiClient";
import Checkbox from "@mui/material/Checkbox";
import { useAlerts } from "../../hooks/alertsHook";
import { TeamBalanceId, TeamEvent, User } from "../../utils/domain";
import { formattedDate } from "../../utils/util";

export type ControlType = "CHECKBOX" | "SWITCH";

export const EventUsers = (props: {
  event?: TeamEvent;
  users: User[];
  controlType: ControlType;
  initialValue?: { [p: string]: boolean };
  onChange: (userSelection: { [p: string]: boolean }) => void;
}) => {
  const [singleUserCheck, setSingleUserCheck] = useState<{
    [userId: string]: boolean;
  }>({});
  const [allUsersCheckBox, setAllUsersCheckBox] = useState(false);
  const { addAlert } = useAlerts();

  useEffect(() => {
    if (props.initialValue !== undefined) {
      //TODO add validation?
      setSingleUserCheck(props.initialValue);
    } else {
      const attendeeUserIds = (props.event?.attendees || []).map(
        (it) => it.user.id
      );

      const selectedUserMap: { [userId: string]: boolean } = {};
      props.users.forEach((user) => {
        selectedUserMap[user.id] = attendeeUserIds.some((it) => it === user.id);
      });

      setSingleUserCheck(selectedUserMap);
    }
  }, [props.users]);

  useEffect(() => {
    const hasUncheckedUsers = Object.values(singleUserCheck).some((it) => !it);
    setAllUsersCheckBox(!hasUncheckedUsers);
    props.onChange(singleUserCheck);
  }, [singleUserCheck]);

  const setAllUsersCheckedStateTo = async (isChecked: boolean) => {
    const selectedUserMap: { [p: string]: boolean } = {};
    for (const user of props.users) {
      selectedUserMap[user.id] = isChecked;
      await handleSingleAttendeeChange(user.id, isChecked);
    }
    return selectedUserMap;
  };

  const handleAllAttendeesChange = async (
    _: React.ChangeEvent<HTMLInputElement>,
    checked: boolean
  ) => {
    await setAllUsersCheckedStateTo(checked);
  };

  const handleSingleAttendeeChange = async (
    userId: TeamBalanceId,
    checked: boolean
  ) => {
    if (singleUserCheck[userId] === checked) return;

    setSingleUserCheck((prevState) => ({
      ...prevState,
      [userId]: checked,
    }));

    if (props.controlType === "SWITCH" && props.event !== undefined) {
      const updatedUserName = props.users.find((u) => u.id === userId)?.name;
      try {
        if (checked) {
          await attendeesApiClient.addAttendee({
            eventId: props.event.id,
            userId: "" + userId,
          });
        } else {
          await attendeesApiClient.removeAttendee({
            eventId: props.event.id,
            userId: "" + userId,
          });
        }

        addAlert({
          message: `${updatedUserName} ${
            checked ? " toegevoegd aan " : " verwijderd van"
          } event van ${formattedDate(props.event.startTime)}`,
          level: "success",
        });
      } catch (e) {
        console.error(e);
        addAlert({
          message: `Er ging iets mis met het updaten van ${updatedUserName}. Probeer het nog eens... `,
          level: "warning",
        });

        //Revert state
        setSingleUserCheck((prevState) => ({
          ...prevState,
          [userId]: !checked,
        }));
      }
    }
  };

  const isChecked: (user: User) => boolean = (user) =>
    singleUserCheck[user.id] || false;

  return (
    <Grid container>
      <Grid item xs={12}>
        <Grid item container spacing={2}>
          <Grid item xs={12}>
            <FormControlLabel
              control={
                props.controlType === "CHECKBOX" ? (
                  <Checkbox
                    checked={allUsersCheckBox}
                    indeterminate={
                      Object.values(singleUserCheck).some((it) => it) &&
                      Object.values(singleUserCheck).some((it) => !it)
                    }
                    onChange={handleAllAttendeesChange}
                    name="all"
                  />
                ) : (
                  <Switch
                    checked={allUsersCheckBox}
                    onChange={handleAllAttendeesChange}
                    name="all"
                  />
                )
              }
              label="Iedereen"
            />
          </Grid>
          {props.users.map((it) => (
            <Grid item key={it.id}>
              <FormControlLabel
                key={it.id}
                control={
                  props.controlType === "CHECKBOX" ? (
                    <Checkbox
                      checked={isChecked(it)}
                      onChange={async (
                        event: React.ChangeEvent<HTMLInputElement>
                      ) => {
                        await handleSingleAttendeeChange(
                          event.target.name,
                          event.target.checked
                        );
                      }}
                      name={it.id.toString()}
                    />
                  ) : (
                    <Switch
                      checked={isChecked(it)}
                      onChange={async (
                        event: React.ChangeEvent<HTMLInputElement>
                      ) => {
                        await handleSingleAttendeeChange(
                          event.target.name,
                          event.target.checked
                        );
                      }}
                      name={it.id.toString()}
                    />
                  )
                }
                label={it.name}
              />
            </Grid>
          ))}
        </Grid>
      </Grid>
    </Grid>
  );
};
