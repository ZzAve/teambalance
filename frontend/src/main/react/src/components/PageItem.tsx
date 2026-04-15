import Grid from "@mui/material/Grid2";
import { Card, CardContent, CardHeader } from "@mui/material";
import PageTitle from "./PageTitle";

const PageItem = (props: {
  title: string;
  children: any;
  dataTestId: string;
  xs?: number;
  md?: number;
  pageTitle?: string;
}) => {
  const { children, md = 12, pageTitle, title, xs = 12 } = props;
  return (
    <>
      {!!pageTitle && <PageTitle title={pageTitle} />}
      <Grid size={{ xs, md }} data-testid={props.dataTestId}>
        <Card>
          <CardHeader title={title} />
          <CardContent sx={{ width: "100%" }}>{children}</CardContent>
        </Card>
      </Grid>
    </>
  );
};

export default PageItem;
