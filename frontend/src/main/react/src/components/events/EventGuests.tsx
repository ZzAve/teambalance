import React, { useEffect, useState } from "react";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import Chip from "@mui/material/Chip";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import { EventGuest, guestsApiClient } from "../../utils/GuestsApiClient";
import { useAlerts } from "../../hooks/alertsHook";
import { TeamBalanceId } from "../../utils/domain";

export const EventGuests = (props: { eventId: TeamBalanceId }) => {
  const [guests, setGuests] = useState<EventGuest[]>([]);
  const [newGuestName, setNewGuestName] = useState("");
  const { addAlert } = useAlerts();

  const fetchGuests = async () => {
    try {
      const data = await guestsApiClient.getEventGuests(props.eventId);
      setGuests(data);
    } catch (e) {
      console.error(e);
      addAlert({
        message: "Er ging iets mis met het ophalen van gasten",
        level: "warning",
      });
    }
  };

  useEffect(() => {
    fetchGuests();
  }, [props.eventId]);

  const handleAddGuest = async () => {
    const name = newGuestName.trim();
    if (!name) return;

    try {
      await guestsApiClient.addEventGuest(props.eventId, name);
      setNewGuestName("");
      await fetchGuests();
      addAlert({ message: `${name} toegevoegd als gast`, level: "success" });
    } catch (e) {
      console.error(e);
      addAlert({
        message: `Er ging iets mis met het toevoegen van ${name}`,
        level: "warning",
      });
    }
  };

  const handleDeleteGuest = async (guest: EventGuest) => {
    try {
      await guestsApiClient.deleteEventGuest(props.eventId, guest.id);
      await fetchGuests();
      addAlert({
        message: `${guest.name} verwijderd als gast`,
        level: "success",
      });
    } catch (e) {
      console.error(e);
      addAlert({
        message: `Er ging iets mis met het verwijderen van ${guest.name}`,
        level: "warning",
      });
    }
  };

  return (
    <Grid container spacing={1}>
      <Grid item xs={12}>
        <Typography variant="subtitle2" color="text.secondary">
          Gasten
        </Typography>
      </Grid>
      <Grid item xs={12} container spacing={1}>
        {guests.map((guest) => (
          <Grid item key={guest.id}>
            <Chip
              label={guest.name}
              onDelete={() => handleDeleteGuest(guest)}
              size="small"
              variant="outlined"
            />
          </Grid>
        ))}
      </Grid>
      <Grid item xs={12} container spacing={1} alignItems="center">
        <Grid item>
          <TextField
            variant="standard"
            size="small"
            label="Naam gast"
            value={newGuestName}
            onChange={(e) => setNewGuestName(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") handleAddGuest();
            }}
          />
        </Grid>
        <Grid item>
          <Button
            size="small"
            variant="outlined"
            onClick={handleAddGuest}
            disabled={!newGuestName.trim()}
          >
            Toevoegen
          </Button>
        </Grid>
      </Grid>
    </Grid>
  );
};
