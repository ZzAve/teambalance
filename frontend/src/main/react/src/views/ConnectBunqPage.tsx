import React, { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Button,
  Card,
  CardContent,
  Typography,
  Box,
  CircularProgress,
  Alert,
} from "@mui/material";
import { useSnackbar } from "notistack";
import { bunqHttpClient } from "../utils/BunqHttpClient";

/**
 * Page for connecting a Bunq bank account using OAuth.
 */
const ConnectBunqPage: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const location = useLocation();
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  // Check if we're returning from OAuth callback
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const success = params.get("success");

    if (success === "true") {
      enqueueSnackbar("Successfully connected to Bunq!", {
        variant: "success",
      });
      // Remove the query parameters from the URL
      navigate("/connect-bunq", { replace: true });
    } else if (success === "false") {
      enqueueSnackbar("Failed to connect to Bunq", { variant: "error" });
      setError("Failed to connect to Bunq. Please try again.");
      // Remove the query parameters from the URL
      navigate("/connect-bunq", { replace: true });
    }
  }, [location, navigate, enqueueSnackbar]);

  // Fetch OAuth status
  useEffect(() => {
    const fetchOAuthStatus = async () => {
      try {
        setLoading(true);
        const response = await bunqHttpClient.getOAuthStatus();
        setAuthenticated(response.authenticated);
        setError(null);
      } catch (err) {
        console.error("Error fetching OAuth status:", err);
        setError("Failed to fetch OAuth status. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchOAuthStatus();
  }, []);

  // Handle connect button click
  const handleConnect = () => {
    window.location.href = bunqHttpClient.getAuthorizationUrl();
  };

  // Handle disconnect button click
  const handleDisconnect = async () => {
    try {
      setLoading(true);
      await bunqHttpClient.clearOAuthAuthentication();
      setAuthenticated(false);
      enqueueSnackbar("Disconnected from Bunq", { variant: "success" });
    } catch (err) {
      console.error("Error disconnecting from Bunq:", err);
      setError("Failed to disconnect from Bunq. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        mt: 4,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
      }}
    >
      <Typography variant="h4" component="h1" gutterBottom>
        Connect Bunq Account
      </Typography>

      {loading ? (
        <CircularProgress />
      ) : (
        <Card sx={{ minWidth: 300, maxWidth: 600, width: "100%", mt: 2 }}>
          <CardContent>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            {authenticated ? (
              <>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Your Bunq account is connected!
                </Alert>
                <Button
                  variant="contained"
                  color="secondary"
                  onClick={handleDisconnect}
                  fullWidth
                >
                  Disconnect Bunq Account
                </Button>
              </>
            ) : (
              <>
                <Typography variant="body1" paragraph>
                  Connect your Bunq account to enable automatic balance and
                  transaction tracking.
                </Typography>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={handleConnect}
                  fullWidth
                >
                  Connect Bunq Account
                </Button>
              </>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default ConnectBunqPage;
