import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Switch from "@material-ui/core/Switch";
import { attendeesApiClient } from "../../utils/AttendeesApiClient";
import Checkbox from "@material-ui/core/Checkbox";
import { AlertLevel, useAlerts } from "../../hooks/alertsHook";

export const ControlType = {
  CHECKBOX: "CHECKBOX",
  SWITCH: "SWITCH",
};

export const EventUsers = ({ event, users, controlType, setUserSelection }) => {
  const [singleUserCheck, setSingleUserCheck] = useState([]);
  const [allUsersCheckBox, setAllUsersCheckBox] = useState(false);
  const { addAlert } = useAlerts();

  useEffect(() => {
    let attendeeUserIds = (event.attendees || []).map((it) => it.user.id);

    const selectedUserMap = {};
    users.forEach((user) => {
      const checked = attendeeUserIds.some((it) => it === user.id);
      selectedUserMap[user.id] = checked;
    });

    setSingleUserCheck(selectedUserMap);
    setUserSelection(selectedUserMap);
    setAllUsersCheckBox(attendeeUserIds.length === users.length);
  }, [users, event, setUserSelection]);

  useEffect(() => {
    const hasUncheckedUsers = Object.values(singleUserCheck).some(
      (it) => it === false
    );
    setAllUsersCheckBox(!hasUncheckedUsers);
    setUserSelection(singleUserCheck);
  }, [singleUserCheck, setUserSelection]);

  const setAllUsersCheckedStateTo = async (isChecked) => {
    console.log(`Setting  all users to ${isChecked}`);
    const selectedUserMap = {};
    for (const user of users) {
      selectedUserMap[user.id] = isChecked;
      await handleSingleAttendeeChange({
        target: {
          name: user.id,
          checked: isChecked,
        },
      });
    }
    return selectedUserMap;
  };

  const handleAllAttendeesChange = async (x) => {
    let isChecked = x.target.checked;
    await setAllUsersCheckedStateTo(isChecked);
  };

  const handleSingleAttendeeChange = async (x) => {
    const userId = x.target.name;
    const isChecked = x.target.checked;

    if (singleUserCheck[userId] === isChecked) return;

    setSingleUserCheck((prevState) => ({
      ...prevState,
      [userId]: isChecked,
    }));

    if (controlType === ControlType.SWITCH) {
      try {
        if (isChecked) {
          await attendeesApiClient.addAttendee({
            eventId: event.id,
            userId: userId,
          });
        } else {
          await attendeesApiClient.removeAttendee({
            eventId: event.id,
            userId: userId,
          });
        }

        addAlert({
          message: `💪 Geüpdate `,
          level: AlertLevel.SUCCESS,
        });
      } catch (e) {
        console.error(e);
        addAlert({
          message: `Er ging iets mis met het updaten van de speler. `,
          level: AlertLevel.ERROR,
        });

        //Revert state
        setSingleUserCheck((prevState) => ({
          ...prevState,
          [userId]: !isChecked,
        }));
      }
    }
  };

  const attendingPeople = ({ users }) => (
    <Grid item container spacing={2}>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            controlType === ControlType.CHECKBOX ? (
              <Checkbox
                checked={allUsersCheckBox}
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
      {users.map((it) => (
        <Grid item key={it.id}>
          <FormControlLabel
            key={it.id}
            control={
              controlType === ControlType.CHECKBOX ? (
                <Checkbox
                  checked={!!singleUserCheck[it.id]}
                  onChange={handleSingleAttendeeChange}
                  name={it.id.toString()}
                />
              ) : (
                <Switch
                  checked={!!singleUserCheck[it.id]}
                  onChange={handleSingleAttendeeChange}
                  name={it.id.toString()}
                />
              )
            }
            label={it.name}
          />
        </Grid>
      ))}
    </Grid>
  );

  return (
    <Grid container>
      <Grid item xs={12}>
        {attendingPeople({ users: users })}
      </Grid>
      {/*<Grid item container spacing={5} alignItems="flex-end" justify="flex-end">*/}
      {/*  <Grid item>*/}
      {/*    <Button*/}
      {/*      variant="contained"*/}
      {/*      color="primary"*/}
      {/*      onClick={handleSaveAttendees}*/}
      {/*    >*/}
      {/*      Opslaan*/}
      {/*    </Button>*/}
      {/*  </Grid>*/}
      {/*  <Grid item>*/}
      {/*    <Link to={"/admin/trainings"}>*/}
      {/*      <Button variant="contained" color="secondary">*/}
      {/*        Annuleren*/}
      {/*      </Button>*/}
      {/*    </Link>*/}
      {/*  </Grid>*/}
      {/*</Grid>*/}
    </Grid>
  );
};
