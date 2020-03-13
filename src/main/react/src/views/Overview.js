import Grid from "@material-ui/core/Grid";
import Balance from "../components/Balance";
import Topup from "../components/Topup";
import PageItem from "../components/PageItem";
import {Card, CardHeader} from "@material-ui/core";
import Transactions from "../components/Transactions";
import React from "react";
import Trainings from "../components/Trainings";

const Overview = ({ state, loadingState }) => {
    return (
        <>
            <Grid item xs={12} md={6}>
                <Grid container spacing={2}>
                    {/*<PageItem title="Huidig saldo">*/}
                    {/*    <Balance balance={state.balance} isLoading={loadingState.loadingBalance} />*/}
                    {/*</PageItem>*/}
                    {/*<PageItem title="Bijpotten">*/}
                    {/*    <Topup baseURL={state.bunqMeUrl} />*/}
                    {/*</PageItem>*/}
                    <PageItem title="Aanstaande trainingen">
                        <Trainings trainings={state.trainings} isLoading={loadingState.loadingTrainings}/>
                    </PageItem>
                </Grid>
            </Grid>
            {/*<Grid item xs={12} md={6}>*/}
            {/*    <Grid container spacing={2}>*/}
            {/*        <Grid item xs={12}>*/}
            {/*            <Card>*/}
            {/*                <CardHeader title="Transacties"/>*/}
            {/*            </Card>*/}
            {/*            <Card>*/}
            {/*                <Transactions transactions={state.transactions} isLoading={loadingState.loadingTransactions} />*/}
            {/*            </Card>*/}
            {/*        </Grid>*/}
            {/*    </Grid>*/}
            {/*</Grid>*/}
        </>
    );
};

export default Overview