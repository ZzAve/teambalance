import React, {useEffect, useState} from "react";
import {SnackbarKey, useSnackbar} from "notistack";
import {Button} from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";

export type AlertLevelType = "success" | "info" | "warning" | "error";

export interface Alert {
  message: string;
  level: AlertLevelType;
  canClose?: boolean;
}

type InternalAlert = Alert & { id: SnackbarKey };

let alerts: InternalAlert[] = [];
let setStates: React.Dispatch<React.SetStateAction<InternalAlert[]>>[] = [];

export const useAlerts = () => {
  const [myAlerts, setMyAlerts] = useState<InternalAlert[]>(alerts);
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();

  useEffect(() => {
    setStates.push(setMyAlerts);

    return () => {
      const index = setStates.indexOf(setMyAlerts);
      setStates.splice(index, 1);
    };
  }, []);

  const addSnackbar = (alert: Alert) =>
    enqueueSnackbar(alert.message, {
      variant: alert.level,
      action: (snackbarId) => (
        <>{alert.canClose !== false ? closeButton(snackbarId) : ""}</>
      ),
    });

  const closeButton = (snackbarId: SnackbarKey) => (
    <Button
      variant="text"
      color="default"
      onClick={() => closeSnackbar(snackbarId)}
    >
      <CloseIcon />
    </Button>
  );

  /**
   *
   */
  const addAlert = (alert: Alert) => {
    const alertId = addSnackbar(alert);
    alerts = [...alerts, { ...alert, id: alertId }];

    setStates.forEach((it) => it(alerts));
  };
  const removeAlert = (alertId: string | number) => {
    const alertToRemove = alerts.find((a) => a.id === alertId);

    if (alertToRemove !== undefined) {
      const index = alerts.indexOf(alertToRemove);
      alerts.splice(index, 1);
      closeSnackbar(alertToRemove.id);
      setStates.forEach((it) => it([...alerts]));
    }
  };
  return { myAlerts, addAlert, removeAlert };
};
