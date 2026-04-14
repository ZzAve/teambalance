import React, { useEffect, useState } from "react";
import dayjs, { Dayjs } from "dayjs";
import "dayjs/locale/nl";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { MobileDateTimePicker } from "@mui/x-date-pickers";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Grid from "@mui/material/Grid";
import { Alert } from "@mui/material";
import PageItem from "../components/PageItem";
import { settingsApiClient } from "../utils/SettingsApiClient";
import { flushStartOfSeason } from "../components/events/Events";

const AdminSettings = () => {
  const [seasonStart, setSeasonStart] = useState<Dayjs | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    settingsApiClient
      .getSeasonStart()
      .then((result) => {
        setSeasonStart(dayjs(result));
      })
      .catch(() => {
        setError("Kon seizoensbegin niet laden");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const handleSave = () => {
    if (!seasonStart) return;
    setSaving(true);
    setError(null);
    setSuccessMessage(null);

    settingsApiClient
      .updateSeasonStart(seasonStart.format("YYYY-MM-DDTHH:mm:ss"))
      .then(() => {
        setSuccessMessage("Seizoensbegin opgeslagen");
        flushStartOfSeason();
      })
      .catch(() => {
        setError("Opslaan mislukt. Probeer het opnieuw.");
      })
      .finally(() => {
        setSaving(false);
      });
  };

  return (
    <PageItem title="Begin van het seizoen" dataTestId="admin-settings-page">
      <Grid container spacing={2} direction="column">
        {loading && <CircularProgress />}
        {error && <Alert severity="error">{error}</Alert>}
        {successMessage && <Alert severity="success">{successMessage}</Alert>}
        {!loading && (
          <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="nl">
            <Grid item>
              <MobileDateTimePicker
                renderInput={(props) => (
                  <TextField variant="standard" {...props} />
                )}
                label="Begin van het seizoen"
                value={seasonStart}
                onChange={(x) => {
                  if (x !== null) setSeasonStart(x);
                }}
                ampm={false}
                minutesStep={15}
              />
            </Grid>
            <Grid item>
              <Button
                variant="contained"
                color="primary"
                onClick={handleSave}
                disabled={saving || seasonStart === null}
              >
                {saving ? <CircularProgress size={20} /> : "Opslaan"}
              </Button>
            </Grid>
          </LocalizationProvider>
        )}
      </Grid>
    </PageItem>
  );
};

export default AdminSettings;
