import React from "react";
import Button from "@material-ui/core/Button";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogTitle from "@material-ui/core/DialogTitle";

export default function AlertDialog({
  onResult,
  title,
  body = "",
  confirmText = "Ok",
  cancelText = "Cancel",
}) {
  const handleClose = (shouldDelete) => {
    onResult(shouldDelete);
  };

  return (
    <Dialog
      open={true}
      onClose={() => handleClose(false)}
      aria-labelledby="alert-dialog-title"
      aria-describedby="alert-dialog-description"
    >
      <DialogTitle id="alert-dialog-title">{title}</DialogTitle>
      <DialogContent id="alert-dialog-description">{body}</DialogContent>
      <DialogActions>
        <Button onClick={() => handleClose(false)} color="primary">
          {cancelText}
        </Button>
        <Button onClick={() => handleClose(true)} color="primary" autoFocus>
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
