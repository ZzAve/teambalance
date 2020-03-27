import Grid from "@material-ui/core/Grid";
import Balance from "../components/Balance";
import Topup from "../components/Topup";
import PageItem from "../components/PageItem";
import {Card, CardHeader} from "@material-ui/core";
import Transactions from "../components/Transactions";
import React from "react";
import Trainings from "../components/Trainings";

const Overview = ({ refresh }) => {
    return (
        <>
            <Grid item xs={12} md={6}>
                <Grid container spacing={2}>
                    <PageItem title="De bierstand">
                        <Grid item container spacing={3} xs={12}>
                            <Grid item xs={12}><Balance refresh={refresh} /></Grid>
                            <Grid item xs={12}><Topup /></Grid>
                        </Grid>
                    </PageItem>
                    <Grid item xs={12}>
                        <Card>
                            <CardHeader title="Transacties"/>
                        </Card>
                        <Card>
                            <Transactions refresh={refresh} />
                        </Card>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12} md={6}>
                <Grid container spacing={2}>
                    <PageItem title="Aanstaande trainingen">
                        <Trainings refresh={refresh}/>
                    </PageItem>

                </Grid>
            </Grid>
        </>
    );
};

export default Overview