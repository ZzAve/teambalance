import Grid from "@material-ui/core/Grid";
import React, { useEffect, useState } from "react";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import Checkbox from "@material-ui/core/Checkbox";
import Typography from "@material-ui/core/Typography";
import Button from "@material-ui/core/Button";
import { Link } from "react-router-dom";

export const TrainingUsers = ({ training, users }) => {
  const [singleUserCheck, setSingleUserCheck] = useState([]);
  const [allUsersCheckBox, setAllUsersCheckBox] = useState(false);

  useEffect(() => {
    let attendeeUserIds = (training.attendees || []).map(it => it.user.id);

    const selectedUserMap = {};
    users.forEach(user => {
      let checked = attendeeUserIds.some(it => it === user.id);
      console.log(`Setting ${user.id} to ${checked}`);
      selectedUserMap[user.id] = checked;
    });

    console.log(selectedUserMap);
    setSingleUserCheck(selectedUserMap);
    setAllUsersCheckBox(attendeeUserIds.length === users.length);
  }, [users, training]);

  useEffect(() => {
    let hasUncheckedUsers = Object.values(singleUserCheck).some(
      it => it === false
    );
    setAllUsersCheckBox(!hasUncheckedUsers);
  }, [singleUserCheck]);

  const setAllUsersCheckedStateTo = isChecked => {
    console.log(`Setting  all users to ${isChecked}`);
    const selectedUserMap = {};
    users.forEach(user => {
      selectedUserMap[user.id] = isChecked;
    });
    return selectedUserMap;
  };

  const handleAllAttendeesChange = x => {
    let isChecked = x.target.checked;
    let allUsersCheckedStateTo = setAllUsersCheckedStateTo(isChecked);
    setSingleUserCheck(allUsersCheckedStateTo);
  };

  const handleSingleAttendeeChange = x => {
    const userId = x.target.name;
    const isChecked = x.target.checked;

    setSingleUserCheck(prevState => ({
      ...prevState,
      [userId]: isChecked
    }));
  };

  const handleSaveAttendees = x => {};

  const attendingPeople = ({ users }) => (
    <Grid item container spacing={5}>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            <Checkbox
              checked={allUsersCheckBox}
              onChange={handleAllAttendeesChange}
              name="all"
              // color="primary"
            />
          }
          label="Iedereen"
        />
      </Grid>
      <Grid item xs={12}>
        {users.map(it => (
          <FormControlLabel
            key={it.id}
            control={
              <Checkbox
                checked={!!singleUserCheck[it.id]}
                onChange={handleSingleAttendeeChange}
                name={it.id.toString()}
              />
            }
            label={it.name}
          />
        ))}
      </Grid>
    </Grid>
  );

  return (
    <Grid container>
      <Grid item xs={12} sm={6}>
        <Typography variant="h6">Teamgenoten </Typography>
        {attendingPeople({ users: users })}
      </Grid>
      <Grid item container spacing={5} alignItems="flex-end" justify="flex-end">
        <Grid item>
          <Button
            variant="contained"
            color="primary"
            onClick={handleSaveAttendees}
          >
            Opslaan
          </Button>
        </Grid>
        <Grid item>
          <Link to={"/admin/trainings"}>
            <Button variant="contained" color="secondary">
              Annuleren
            </Button>
          </Link>
        </Grid>
      </Grid>
    </Grid>
  );
};
