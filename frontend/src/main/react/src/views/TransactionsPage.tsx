import Grid from "@mui/material/Grid2";
import PageItem from "../components/PageItem";
import { Button } from "@mui/material";
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
      <Grid container spacing={2}>
        <Grid size={12}>
          <Button variant="contained" color="primary" onClick={navigateBack}>
            <ArrowBackIcon />
            <Typography
              variant={"button"}
              sx={{ display: { xs: "none", sm: "block" } }}
            >
              Terug
            </Typography>
          </Button>
        </Grid>

        <Grid size={12}>
          <PageItem title="Transacties" dataTestId="transactions">
            <Grid size={12}>
              <Transactions refresh={props.refresh} withPagination={true} />
            </Grid>
          </PageItem>
        </Grid>
        <Grid size={12}>
          <PageItem title="Potters" dataTestId="potters">
            <Potters
              refresh={props.refresh}
              limit={20}
              showFloppers={false}
              showSupportRoles={true}
            />
          </PageItem>
        </Grid>
      </Grid>
    </>
  );
};

export default TransactionsPage;
