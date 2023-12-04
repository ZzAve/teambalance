import Grid from "@mui/material/Grid";
import PageItem from "../components/PageItem";
import { Button, Card } from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import Transactions from "../components/Transactions";
import Potters from "../components/Potters";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";

const TransactionsPage = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  const navigateBack = () => {
    navigate("../");
  };

  return (
    <>
      <PageTitle title="Transacties" />
      <Grid item container spacing={2}>
        <Grid container item xs={12}>
          <Button variant="contained" color="primary" onClick={navigateBack}>
            <ArrowBackIcon />
            <Typography
              variant={"button"}
              sx={{ display: { sm: "block", xs: "none" } }}
            >
              Terug
            </Typography>
          </Button>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <Grid container item xs={12}>
              <Grid item xs={12} sx={{ padding: "16px" }}>
                <Typography variant="h5">Transacties</Typography>
              </Grid>
              <Grid item xs={12}>
                <Transactions refresh={props.refresh} withPagination={true} />
              </Grid>
            </Grid>
          </Card>
        </Grid>
        <Grid container item xs={12}>
          <PageItem title="Potters">
            <Potters refresh={props.refresh} limit={20} showFloppers={false} />
          </PageItem>
        </Grid>
      </Grid>
    </>
  );
};

export default TransactionsPage;
