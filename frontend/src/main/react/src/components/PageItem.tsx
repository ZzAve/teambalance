import Grid, { GridSize } from "@material-ui/core/Grid";
import { Card, CardContent, CardHeader } from "@material-ui/core";
import React from "react";
import PageTitle from "./PageTitle";

const PageItem = (props: {
  title: string;
  children: any;
  xs?: GridSize;
  md?: GridSize;
  pageTitle?: string;
}) => {
  const { children, md = 12, pageTitle, title, xs = 12 } = props;
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
