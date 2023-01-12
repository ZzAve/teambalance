import React from "react";
import Button from "@material-ui/core/Button";
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import DialogContent from "@material-ui/core/DialogContent";
import DialogTitle from "@material-ui/core/DialogTitle";

export default function AlertDialog(props: {
  onResult: (confirmed: boolean) => void;
  title: string;
  body?: string | JSX.Element;
  confirmText?: string;
  cancelText?: string;
}) {
  const { body = "", confirmText = "Ok", cancelText = "Cancel" } = props;
  const handleClose = (shouldDelete: boolean) => {
    props.onResult(shouldDelete);
  };

  return (
    <Dialog
      open={true}
      onClose={() => handleClose(false)}
      aria-labelledby="alert-dialog-title"
      aria-describedby="alert-dialog-description"
    >
      <DialogTitle id="alert-dialog-title">{props.title}</DialogTitle>
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
