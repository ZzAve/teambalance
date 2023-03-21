import {
  Day,
  label,
  RecurringEventProperties,
  RecurringInterval,
} from "../../utils/domain";
import React, { useEffect, useState } from "react";
import dayjs, { Dayjs } from "dayjs";
import Grid from "@mui/material/Grid";
import {
  Alert,
  AlertTitle,
  FormControl,
  MenuItem,
  Select,
} from "@mui/material";
import Typography from "@mui/material/Typography";
import TextField from "@mui/material/TextField";
import { DatePicker } from "@mui/x-date-pickers";
import FormControlLabel from "@mui/material/FormControlLabel";
import CheckBox from "@mui/material/Checkbox";

export type RecurringLimitType = "EndDate" | "AmountOfEvents";
export const RecurringEvent = (props: {
  setRecurringEventProperties: (props: RecurringEventProperties) => void;
}) => {
  const [selectedDays, setSelectedDays] = useState<Record<Day, boolean>>({
    MONDAY: false,
    TUESDAY: false,
    WEDNESDAY: false,
    THURSDAY: false,
    FRIDAY: false,
    SATURDAY: false,
    SUNDAY: false,
  });
  const [recurringLimitType, setRecurringLimitType] =
    useState<RecurringLimitType>("EndDate");
  const [intervalTimeunit, setIntervalTimeunit] =
    useState<RecurringInterval>("WEEK");
  const [intervalAmount, setIntervalAmount] = useState<number>(1);
  const [amountLimit, setAmountLimit] = useState<number>(10);
  const [dateLimit, setDateLimit] = useState<Dayjs>(dayjs(new Date()));

  useEffect(() => {
    console.debug("Recurring event update!!");

    const recurringEventProperties: RecurringEventProperties = {
      amountLimit: amountLimit,
      dateLimit: dateLimit,
      intervalAmount: intervalAmount,
      intervalTimeUnit: intervalTimeunit,
      selectedDays: Object.entries(selectedDays)
        .filter(([_, value]) => value)
        .map(([key, _]) => key) as Day[],
    };
    props.setRecurringEventProperties(
      recurringLimitType === "EndDate"
        ? { ...recurringEventProperties, amountLimit: undefined }
        : { ...recurringEventProperties, dateLimit: undefined }
    );
  }, [
    recurringLimitType,
    intervalTimeunit,
    intervalAmount,
    amountLimit,
    dateLimit,
    selectedDays,
    props.setRecurringEventProperties,
  ]);
  const selectedFrequencySuffix = () => {
    switch (intervalTimeunit) {
      case "WEEK":
        return intervalAmount === 1 ? "week" : "weken";
      case "MONTH":
        return intervalAmount === 1 ? "maand" : "maanden";
    }
  };

  return (
    <Grid container item spacing={2} alignItems="center" marginY="10px">
      {/*<Grid item xs={12}></Grid>*/}
      <Grid item xs={12}>
        <Alert severity="warning">
          <AlertTitle>Ontbrekende link tussen gecreeerde events</AlertTitle>
          <Typography>
            De 'herhaal event' optie is nog vrij nieuw en niet helemaal af. Je
            kunt herhalende events creeeren, maar ze daarna als groep aanpassen
            of verwijderen kan helaas nog niet.
          </Typography>
          <Typography>
            Er wordt aan gewerkt om óók dat mogelijk te maken.
          </Typography>
        </Alert>
      </Grid>
      <Grid item xs={12}>
        <FormControl variant="standard">
          <Select
            variant="standard"
            labelId="interval-time-unit"
            value={intervalTimeunit}
            onChange={(x) =>
              setIntervalTimeunit(x.target.value as RecurringInterval)
            }
          >
            <MenuItem key={"WEEK"} value={"WEEK"}>
              herhaal wekelijks
            </MenuItem>
            <MenuItem key={"MONTH"} value={"MONTH"}>
              herhaal maandelijks
            </MenuItem>
          </Select>
        </FormControl>
      </Grid>
      <Grid item>
        Elke&nbsp;
        <TextField
          InputLabelProps={{ shrink: true }}
          variant="standard"
          id="interval-amount"
          type="number"
          size="small"
          sx={{ width: "80px" }}
          value={intervalAmount}
          onChange={(x) => setIntervalAmount(+x.target.value)}
        />
        &nbsp;{selectedFrequencySuffix()}
      </Grid>
      <Grid item>
        <FormControl variant="standard">
          <Select
            variant="standard"
            labelId="interval-limit"
            value={recurringLimitType}
            onChange={(x) => {
              return setRecurringLimitType(
                x.target.value as RecurringLimitType
              );
            }}
          >
            <MenuItem key={"EndDate"} value={"EndDate"}>
              herhaal tot datum
            </MenuItem>
            <MenuItem key={"AmountOfEvents"} value={"AmountOfEvents"}>
              herhaal een aantal keer
            </MenuItem>
          </Select>
        </FormControl>
      </Grid>
      <Grid item>
        {recurringLimitType === "EndDate" ? (
          <DatePicker
            renderInput={(props) => (
              <TextField variant="standard" {...props}></TextField>
            )}
            minDate={dayjs("2020-01-01")}
            maxDate={dayjs("2030-01-01")}
            value={dateLimit}
            onChange={(x) => setDateLimit(x || dayjs(new Date()))}
          />
        ) : (
          <>
            <TextField
              variant="standard"
              id="interval-limit"
              name="title"
              type="number"
              value={amountLimit}
              onChange={(x) => setAmountLimit(+x.target.value)}
            />{" "}
            events
          </>
        )}
      </Grid>
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <FormControlLabel
            label="Alle dagen"
            control={
              <CheckBox
                checked={Object.values(selectedDays).every((x) => x)}
                indeterminate={
                  Object.values(selectedDays).some((x) => x) &&
                  Object.values(selectedDays).some((x) => !x)
                }
                onChange={(it) => {
                  const isChecked = it.target.checked;
                  const newSelectedDays: Partial<Record<Day, boolean>> =
                    Object.keys(Day).reduce(
                      (previousValue, day) => ({
                        ...previousValue,
                        [day]: isChecked,
                      }),
                      {}
                    );

                  setSelectedDays(newSelectedDays as Record<Day, boolean>);
                }}
              />
            }
          />
        </Grid>
        <Grid item xs={12}>
          {Object.entries(Day).map(([key, value]) => {
            const dayType = key as keyof typeof Day;
            return (
              <FormControlLabel
                key={"day-" + key}
                label={label(value)}
                control={
                  <CheckBox
                    inputProps={{ "aria-label": "controlled" }}
                    checked={selectedDays[dayType]}
                    onChange={(it) => {
                      setSelectedDays({
                        ...selectedDays,
                        [key]: it.target.checked,
                      });
                    }}
                  />
                }
              />
            );
          })}
        </Grid>
      </Grid>
    </Grid>
  );
};
