import React from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import CircularProgress from "@material-ui/core/CircularProgress";
import Typography from "@material-ui/core/Typography";
import Grid from "@material-ui/core/Grid";


const useStyles = makeStyles({
    root: {
        width: '100%',
        overflowX: 'auto',
    },
    table: {
        // minWidth: 650,
        // maxWidth: 1024
    },
    alignCenter: {
        // flexDirection:"column",
        alignItems:"center",
        justifyContent:"center",
        display:"flex",
        minHeight: 150
    }

});

const Transactions = ({transactions}) => {
    const classes = useStyles();
    if (transactions.length === 0) {
        return (
            <div className={classes.alignCenter}>
                {/*<Grid item xs={12} >*/}
                    {/*<Typography variant="h6">Transactions</Typography>*/}
                    <CircularProgress/>
                {/*</Grid>*/}
            </div>
        )
    }
    return (

        <TableContainer component={Paper}>
            <Table className={classes.table} aria-label="simple table">
                <TableHead>
                    <TableRow>
                        <TableCell>Datum</TableCell>
                        <TableCell align="right">Wie</TableCell>
                        <TableCell align="right">Bedrag</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {transactions.map((row) => (
                        <TableRow key={row.id}>
                            <TableCell component="th" scope="row">
                                {row.date.toLocaleString()}
                            </TableCell>
                            <TableCell align="right">{row.counterParty}</TableCell>
                            <TableCell align="right">{row.amount}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>

    )
};

export default Transactions


