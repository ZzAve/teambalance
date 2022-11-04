import React, { useEffect, useState } from "react";
import { useSnackbar } from "notistack";
import { Button } from "@material-ui/core";
import CloseIcon from "@material-ui/icons/Close";

export const AlertLevel = {
  SUCCESS: "success",
  INFO: "info",
  WARN: "warning",
  ERROR: "error",
};

let alerts = [];
let setStates = [];

export const useAlerts = () => {
  const [myAlerts, setMyAlerts] = useState(alerts);
  const { enqueueSnackbar, closeSnackbar } = useSnackbar();

  useEffect(() => {
    setStates.push(setMyAlerts);

    return () => {
      const index = setStates.indexOf(setMyAlerts);
      setStates.splice(index, 1);
    };
  }, []);

  const addSnackbar = (alert) =>
    enqueueSnackbar(alert.message, {
      variant: alert.level,
      action: (snackbarId) => (
        <>{alert.canClose !== false ? closeButton(snackbarId) : ""}</>
      ),
    });

  const closeButton = (snackbarId) => (
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
   * @param alert
   * @type (alert: {message: string, level: "success" | "info" | "warn" | "error", canClose: boolean = false}) => void
   */
  const addAlert = (alert) => {
    console.debug("[useAlert] adding alert", alert);
    alerts = [...alerts, alert];
    alert.id = addSnackbar(alert);

    setStates.forEach((it) => it(alerts));
  };
  const removeAlert = (alert) => {
    console.debug("[useAlert] removing alert", alert);
    const index = alerts.indexOf(alert);
    alerts.splice(index, 1);

    closeSnackbar(alert.id);
    setStates.forEach((it) => it([...alerts]));
  };
  return { myAlerts, addAlert, removeAlert };
};
