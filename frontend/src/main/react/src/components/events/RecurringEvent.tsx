import {
  AffectedRecurringEvents,
  Day,
  label,
  RecurringEventProperties,
  RecurringInterval,
} from "../../utils/domain";
import React, { useEffect, useReducer, useState } from "react";
import dayjs, { Dayjs } from "dayjs";
import Grid from "@mui/material/Grid";
import {
  Alert,
  AlertTitle,
  FormControl,
  FormLabel,
  InputAdornment,
  MenuItem,
  RadioGroup,
  Select,
  ToggleButton,
} from "@mui/material";
import Typography from "@mui/material/Typography";
import TextField from "@mui/material/TextField";
import { MobileDatePicker } from "@mui/x-date-pickers";
import FormControlLabel from "@mui/material/FormControlLabel";
import Radio from "@mui/material/Radio";

/**
 * Describes how an event should re-occur / repeat:
 * An event is repeated every {@code intervalAmount} {@code intervalTimeUnit},
 * until {@code limit} is reached, on {@code selected days}
 */
interface RecurringEventPropertiesInternal {
  /* id */
  teamBalanceId?: string;
  /* to repeat every x amount of time */
  intervalAmount: number;
  /* interval sizing to combine 'every' with */
  intervalTimeUnit: RecurringInterval;
  selectedDays: Record<Day, boolean>;

  /* the amount of occurrences to occur, bound by a number of events */
  amountLimit?: number;
  /* the amount of occurrences to occur,  bound by an end date (inclusive)*/
  dateLimit?: Dayjs;
}

type RecurringLimitType = "EndDate" | "AmountOfEvents";
const internalize = (
  r: RecurringEventProperties | undefined
): RecurringEventPropertiesInternal | undefined => {
  if (r === undefined) return undefined;

  const selectedDays: Record<Day, boolean> = r.selectedDays.reduce(
    (acc, cur) => ({
      ...acc,
      [cur]: true,
    }),
    NO_DAYS
  );
  return {
    teamBalanceId: r.teamBalanceId,
    amountLimit: r.amountLimit || 10,
    dateLimit: r.dateLimit || dayjs(new Date()).add(3, "months"),
    intervalAmount: r.intervalAmount,
    intervalTimeUnit: r.intervalTimeUnit,
    selectedDays: selectedDays,
  };
};
const NO_DAYS = {
  MONDAY: false,
  TUESDAY: false,
  WEDNESDAY: false,
  THURSDAY: false,
  FRIDAY: false,
  SATURDAY: false,
  SUNDAY: false,
};

export const RecurringEvent = (props: {
  initialValue?: RecurringEventProperties;
  onChange: (props: RecurringEventProperties) => void;
  initialAffectedEvents: AffectedRecurringEvents;
  onAffectedEventsChange: (props: AffectedRecurringEvents) => void;
  readOnly: boolean;
  isCreateEvent: boolean;
}) => {
  return (
    <>
      {props.isCreateEvent ? (
        <CreateRecurringEvent
          initialValue={props.initialValue}
          onChange={props.onChange}
          readOnly={props.readOnly}
        />
      ) : (
        <AffectedRecurringEvent
          initialValue={props.initialAffectedEvents}
          onChange={props.onAffectedEventsChange}
        />
      )}
    </>
  );
};

export const AffectedRecurringEvent = (props: {
  initialValue?: AffectedRecurringEvents;
  onChange: (props: AffectedRecurringEvents) => void;
}) => {
  const [affectedRecurringEvents, setAffectedRecurringEvents] =
    useState<AffectedRecurringEvents>(props.initialValue ?? "ALL");

  useEffect(() => {
    props.onChange(affectedRecurringEvents);
  }, [affectedRecurringEvents, props.onChange]);

  return (
    <Grid container item>
      <FormControl>
        <FormLabel id="affected-recurring-events-label">
          Welke events wil je aanpassen?
        </FormLabel>
        <RadioGroup
          row
          aria-labelledby="affected-recurring-events-label"
          value={affectedRecurringEvents}
          onChange={(x) =>
            setAffectedRecurringEvents(
              (x.target as HTMLInputElement).value as AffectedRecurringEvents
            )
          }
          name="affected-recurring-events-radio-group"
        >
          <FormControlLabel
            value="CURRENT"
            control={<Radio />}
            label="Dit event"
          />
          <FormControlLabel
            value="CURRENT_AND_FUTURE"
            control={<Radio />}
            label="Dit en toekomstige events"
          />
          <FormControlLabel
            value="ALL"
            control={<Radio />}
            label="Alle events"
          />
        </RadioGroup>
      </FormControl>
    </Grid>
  );
};

export const CreateRecurringEvent = (props: {
  initialValue?: RecurringEventProperties;
  onChange: (props: RecurringEventProperties) => void;
  readOnly: boolean;
}) => {
  const [recurringLimitType, setRecurringLimitType] =
    useState<RecurringLimitType>(
      props.initialValue?.amountLimit !== undefined
        ? "AmountOfEvents"
        : "EndDate"
    );
  const [recProps, setRecProps] = useReducer(
    (
      state: RecurringEventPropertiesInternal,
      action: Partial<RecurringEventPropertiesInternal>
    ) => ({ ...state, ...action }),
    internalize(props.initialValue) || {
      teamBalanceId: undefined,
      amountLimit: 10,
      dateLimit: dayjs(new Date()).add(3, "months"),
      intervalAmount: 1,
      intervalTimeUnit: "WEEK",
      selectedDays: NO_DAYS,
    }
  );

  useEffect(() => {
    const recurringEventProperties: RecurringEventProperties = {
      teamBalanceId: recProps.teamBalanceId,
      amountLimit: recProps.amountLimit,
      dateLimit: recProps.dateLimit,
      intervalAmount: recProps.intervalAmount,
      intervalTimeUnit: recProps.intervalTimeUnit,
      selectedDays: Object.entries(recProps.selectedDays)
        .filter(([_, value]) => value === true)
        .map(([key, _]) => key) as Day[],
    };

    props.onChange(
      recurringLimitType === "EndDate"
        ? { ...recurringEventProperties, amountLimit: undefined }
        : { ...recurringEventProperties, dateLimit: undefined }
    );
  }, [recProps, recurringLimitType, props.onChange]);

  return (
    <Grid container item spacing={2} alignItems="center" marginY="10px">
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
      <Grid item container xs={12}>
        <Grid container item spacing={2}>
          <Grid item>
            <Typography
              sx={{
                display: "inline-block",
                verticalAlign: "-webkit-baseline-middle",
              }}
              variant="body1"
            >
              Elke
            </Typography>
          </Grid>
          <Grid item>
            <FormControl variant="standard">
              <TextField
                InputLabelProps={{ shrink: true }}
                InputProps={{
                  sx: {
                    "& input": {
                      textAlign: "center",
                    },
                  },
                }}
                disabled={props.readOnly}
                variant="standard"
                id="interval-amount"
                type="number"
                // size="small"
                sx={{
                  width: "50px",
                }}
                value={recProps.intervalAmount}
                onChange={(e) =>
                  setRecProps({ intervalAmount: Math.max(1, +e.target.value) })
                }
              />
            </FormControl>
          </Grid>
          <Grid item>
            <FormControl variant="standard">
              <Select
                variant="standard"
                labelId="interval-time-unit"
                disabled={props.readOnly}
                value={recProps.intervalTimeUnit}
                onChange={(x) =>
                  setRecProps({
                    intervalTimeUnit: x.target.value as RecurringInterval,
                  })
                }
              >
                <MenuItem key={"WEEK"} value={"WEEK"}>
                  {recProps.intervalAmount === 1 ? "week" : "weken"}
                </MenuItem>
                <MenuItem key={"MONTH"} value={"MONTH"}>
                  {recProps.intervalAmount === 1 ? "maand" : "maanden"}
                </MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Grid>
      <Grid item container xs={12} spacing={1}>
        <Grid item xs={12}>
          <Typography variant="body1">Herhaal op:</Typography>
        </Grid>
        <Grid item xs={12}>
          {Object.entries(Day).map(([key, value]) => {
            const dayType = key as keyof typeof Day;
            return (
              <ToggleButton
                color="primary"
                selected={recProps.selectedDays[dayType]}
                disabled={props.readOnly}
                onChange={() =>
                  setRecProps({
                    selectedDays: {
                      ...recProps.selectedDays,
                      [key]: !recProps.selectedDays[dayType],
                    },
                  })
                }
                key={key}
                value={key}
              >
                {label(value).slice(0, 2)}
              </ToggleButton>
            );
          })}
        </Grid>
      </Grid>
      <Grid item>
        <Typography variant="body1">Eindigt</Typography>
      </Grid>
      <Grid container item>
        <Grid container item columnSpacing={3}>
          <Grid item>
            <FormControl>
              <FormControlLabel
                value="EndDate"
                control={<Radio />}
                label="Op"
                disabled={props.readOnly}
                checked={recurringLimitType === "EndDate"}
                onChange={(_) => setRecurringLimitType("EndDate")}
              />
            </FormControl>
          </Grid>
          <Grid item>
            <MobileDatePicker
              renderInput={(props) => (
                <TextField variant="standard" {...props}></TextField>
              )}
              minDate={dayjs("2020-01-01")}
              maxDate={dayjs("2030-01-01")}
              value={recProps.dateLimit}
              disabled={props.readOnly || recurringLimitType !== "EndDate"}
              onChange={(x) =>
                setRecProps({ dateLimit: x || dayjs(new Date()) })
              }
            />
          </Grid>
        </Grid>
        <Grid container item columnSpacing={3}>
          <Grid item>
            <FormControl>
              <FormControlLabel
                value="AmountOfEvents"
                control={<Radio />}
                label="Na"
                checked={
                  props.readOnly || recurringLimitType === "AmountOfEvents"
                }
                onChange={(_) => setRecurringLimitType("AmountOfEvents")}
              />
            </FormControl>
          </Grid>
          <Grid
            item
            container
            xs
            direction="column"
            justifyContent="space-evenly"
          >
            <Grid item>
              <TextField
                variant="standard"
                id="interval-limit"
                name="title"
                type="number"
                InputProps={{
                  // readOnly: props.readOnly,
                  endAdornment: (
                    <InputAdornment position="end">keer</InputAdornment>
                  ),
                }}
                value={recProps.amountLimit}
                disabled={
                  props.readOnly || recurringLimitType !== "AmountOfEvents"
                }
                onChange={(x) => setRecProps({ amountLimit: +x.target.value })}
              />
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </Grid>
  );
};
