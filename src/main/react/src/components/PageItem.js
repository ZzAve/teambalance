import Grid from "@material-ui/core/Grid";
import { Card, CardContent, CardHeader } from "@material-ui/core";
import React from "react";
import PageTitle from "./PageTitle";

const PageItem = ({ title, children, xs = 12, md = 12, pageTitle }) => {
  return (
    <>
      {!!pageTitle && <PageTitle title={pageTitle} />}
      <Grid item xs={xs} md={md}>
        <Card>
          <CardHeader title={title} />
          <CardContent>{children}</CardContent>
        </Card>
      </Grid>
    </>
  );
};

export default PageItem;
