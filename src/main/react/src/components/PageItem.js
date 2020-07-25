import Grid from "@material-ui/core/Grid";
import { Card, CardContent, CardHeader } from "@material-ui/core";
import React from "react";

const PageItem = ({ title, children }) => {
  return (
    <Grid item xs={12}>
      <Card>
        <CardHeader title={title} />
        <CardContent>{children}</CardContent>
      </Card>
    </Grid>
  );
};

export default PageItem;
