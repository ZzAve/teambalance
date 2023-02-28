import Grid, { GridSize } from "@mui/material/Grid";
import { Card, CardContent, CardHeader } from "@mui/material";
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
          <CardContent sx={{ width: "100%" }}>{children}</CardContent>
        </Card>
      </Grid>
    </>
  );
};

export default PageItem;
