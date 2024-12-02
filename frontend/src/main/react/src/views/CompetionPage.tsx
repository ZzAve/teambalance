import Grid from "@mui/material/Grid";
import {
  Button,
  Card,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import Typography from "@mui/material/Typography";
import PageTitle from "../components/PageTitle";
import { withLoading } from "../utils/util";
import { SpinnerWithText } from "../components/SpinnerWithText";
import { competitionApiClient } from "../utils/CompetitionApiClient";
import { Leaderboard } from "../utils/domain";

const CompetitionPage = (props: { refresh: boolean }) => {
  const navigate = useNavigate();

  const navigateBack = () => {
    navigate("../");
  };

  return (
    <>
      <PageTitle title="Teamleden" />
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
                <Typography variant="h5">Stand van de competitie</Typography>
              </Grid>
              <Grid item xs={12}>
                <LoaderBoard refresh={props.refresh} />
              </Grid>
            </Grid>
          </Card>
        </Grid>
      </Grid>
    </>
  );
};

/**
 * example contents:
 *
 * '<?xml version="1.0" encoding="UTF-8"?>\n<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:stand="https://www.nevobo.nl/competitie/">\n  <channel>\n    <title>RSS feed Nederlandse Volleybalbond</title>\n    <link>https://api.nevobo.nl/export/poule/regio-west/competitie-seniorencompetitie-1/regio-west-h2e-10/stand.rss</link>\n    <description>Stand van de Nederlandse volleybalcompetitie, georganiseerd door de Nederlandse Volleybalbond</description>\n    <lastBuildDate>Sat, 30 Nov 2024 21:08:33 +0100</lastBuildDate>\n    <atom:link href="https://api.nevobo.nl/export/poule/regio-west/competitie-seniorencompetitie-1/regio-west-h2e-10/stand.rss" rel="self" type="text/xml"/>\n    <item>\n      <title><![CDATA[Stand Heren 2e Klasse E Regio West]]></title>\n      <link>https://api.nevobo.nl/permalink/regio-west/competitie-seniorencompetitie-1/poule/regio-west-h2e-10</link>\n      <guid>https://api.nevobo.nl/permalink/regio-west/competitie-seniorencompetitie-1/poule/regio-west-h2e-10</guid>\n      <description><![CDATA[# Team<br />1. Gemini S HS 4, wedstr: 7, punten: 31<br />2. Voleem HS 2, wedstr: 5, punten: 21<br />3. Tovo HS 4, wedstr: 6, punten: 18<br />4. Lovoc HS 2, wedstr: 6, punten: 18<br />5. Forza Hoogland HS 4, wedstr: 6, punten: 17<br />6. Voleem HS 3, wedstr: 6, punten: 13<br />7. KdK A.G.A.V.S. - Soesterberg HS 1, wedstr: 7, punten: 12<br />8. UVV/Sphynx HS 2, wedstr: 6, punten: 5<br />9. VC Allvo HS 4, wedstr: 5, punten: 0<br />]]></description>\n      <pubDate>Sat, 30 Nov 2024 18:45:17 +0100</pubDate>\n    </item>\n    <stand:ranking>\n      <stand:nummer>1</stand:nummer>\n      <stand:team id="3261HS 4"><![CDATA[Gemini S HS 4]]></stand:team>\n      <stand:wedstrijden>7</stand:wedstrijden>\n      <stand:punten>31</stand:punten>\n      <stand:setsvoor>25</stand:setsvoor>\n      <stand:setstegen>3</stand:setstegen>\n      <stand:puntenvoor>699</stand:puntenvoor>\n      <stand:puntentegen>504</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>2</stand:nummer>\n      <stand:team id="3280HS 2"><![CDATA[Voleem HS 2]]></stand:team>\n      <stand:wedstrijden>5</stand:wedstrijden>\n      <stand:punten>21</stand:punten>\n      <stand:setsvoor>17</stand:setsvoor>\n      <stand:setstegen>4</stand:setstegen>\n      <stand:puntenvoor>501</stand:puntenvoor>\n      <stand:puntentegen>392</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>3</stand:nummer>\n      <stand:team id="6208HS 4"><![CDATA[Tovo HS 4]]></stand:team>\n      <stand:wedstrijden>6</stand:wedstrijden>\n      <stand:punten>18</stand:punten>\n      <stand:setsvoor>16</stand:setsvoor>\n      <stand:setstegen>11</stand:setstegen>\n      <stand:puntenvoor>605</stand:puntenvoor>\n      <stand:puntentegen>556</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>4</stand:nummer>\n      <stand:team id="3263HS 2"><![CDATA[Lovoc HS 2]]></stand:team>\n      <stand:wedstrijden>6</stand:wedstrijden>\n      <stand:punten>18</stand:punten>\n      <stand:setsvoor>16</stand:setsvoor>\n      <stand:setstegen>11</stand:setstegen>\n      <stand:puntenvoor>597</stand:puntenvoor>\n      <stand:puntentegen>555</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>5</stand:nummer>\n      <stand:team id="3276HS 4"><![CDATA[Forza Hoogland HS 4]]></stand:team>\n      <stand:wedstrijden>6</stand:wedstrijden>\n      <stand:punten>17</stand:punten>\n      <stand:setsvoor>14</stand:setsvoor>\n      <stand:setstegen>12</stand:setstegen>\n      <stand:puntenvoor>572</stand:puntenvoor>\n      <stand:puntentegen>558</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>6</stand:nummer>\n      <stand:team id="3280HS 3"><![CDATA[Voleem HS 3]]></stand:team>\n      <stand:wedstrijden>6</stand:wedstrijden>\n      <stand:punten>13</stand:punten>\n      <stand:setsvoor>11</stand:setsvoor>\n      <stand:setstegen>15</stand:setstegen>\n      <stand:puntenvoor>540</stand:puntenvoor>\n      <stand:puntentegen>583</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>7</stand:nummer>\n      <stand:team id="3255HS 1"><![CDATA[KdK A.G.A.V.S. - Soesterberg HS 1]]></stand:team>\n      <stand:wedstrijden>7</stand:wedstrijden>\n      <stand:punten>12</stand:punten>\n      <stand:setsvoor>10</stand:setsvoor>\n      <stand:setstegen>18</stand:setstegen>\n      <stand:puntenvoor>543</stand:puntenvoor>\n      <stand:puntentegen>639</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>8</stand:nummer>\n      <stand:team id="6212HS 2"><![CDATA[UVV/Sphynx HS 2]]></stand:team>\n      <stand:wedstrijden>6</stand:wedstrijden>\n      <stand:punten>5</stand:punten>\n      <stand:setsvoor>5</stand:setsvoor>\n      <stand:setstegen>20</stand:setstegen>\n      <stand:puntenvoor>501</stand:puntenvoor>\n      <stand:puntentegen>597</stand:puntentegen>\n    </stand:ranking>\n    <stand:ranking>\n      <stand:nummer>9</stand:nummer>\n      <stand:team id="3283HS 4"><![CDATA[VC Allvo HS 4]]></stand:team>\n      <stand:wedstrijden>5</stand:wedstrijden>\n      <stand:punten>0</stand:punten>\n      <stand:setsvoor>0</stand:setsvoor>\n      <stand:setstegen>20</stand:setstegen>\n      <stand:puntenvoor>327</stand:puntenvoor>\n      <stand:puntentegen>501</stand:puntentegen>\n    </stand:ranking>\n  </channel>\n</rss>\n'
 *
 */
const LoaderBoard = (props: { refresh: boolean }) => {
  const [leaderboardData, setLeaderboardData] = useState<Leaderboard | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    withLoading(setIsLoading, () =>
      competitionApiClient.getCompetitionRanking().then(setLeaderboardData)
    ).then();
  }, [props.refresh]);

  if (isLoading) {
    return <SpinnerWithText text="ophalen competitie" />;
  }

  return (
    <>
      <TableContainer component={Paper}>
        <Table aria-label="leaderboard table">
          <TableHead>
            <TableRow>
              <TableCell>Nummer</TableCell>
              <TableCell>Ploeg</TableCell>
              <TableCell align="right">Wedstrijden</TableCell>
              <TableCell align="right">Punten</TableCell>
              <TableCell align="right">Sets Voor</TableCell>
              <TableCell align="right">Sets Tegen</TableCell>
              <TableCell align="right">Punten Voor</TableCell>
              <TableCell align="right">Punten Tegen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leaderboardData?.entries.map((item) => (
              <TableRow key={item.team}>
                <TableCell component="th" scope="row">
                  {item.number}
                </TableCell>
                <TableCell>{item.team}</TableCell>
                <TableCell align="right">{item.matches}</TableCell>
                <TableCell align="right">{item.points}</TableCell>
                <TableCell align="right">{item.setsFor}</TableCell>
                <TableCell align="right">{item.setsAgainst}</TableCell>
                <TableCell align="right">{item.pointsFor}</TableCell>
                <TableCell align="right">{item.pointsAgainst}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      {leaderboardData?.webUrl && (
        <Typography
          variant="body1"
          sx={{ marginTop: "16px", textAlign: "center" }}
        >
          Meer details van de competitie op{" "}
          <a
            href={leaderboardData.webUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            Nevobo
          </a>
          . Laatst bijgewerkt op:{" "}
          {leaderboardData.lastUpdateTimestamp.toLocaleString()}
        </Typography>
      )}
    </>
  );
};

export default CompetitionPage;
