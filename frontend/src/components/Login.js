import React, { useState } from "react";
import { TextField, Button, Container, Paper, Typography } from "@mui/material";
import axios from "axios";

// Use environment variable for API URL
const API_URL = process.env.REACT_APP_AUTH_API_URL || "http://localhost:8082";

function Login({ onLoginSuccess, onSignupClick }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`${API_URL}/api/auth/login`, {
        username,
        password,
      }, {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: 5000
      });
      
      if (response.data && response.data.token) {
        localStorage.setItem("token", response.data.token);
        localStorage.setItem("userId", response.data.userId);
        onLoginSuccess();
      } else {
        console.error("Login response missing token:", response.data);
        alert("Login failed: Invalid response from server");
      }
    } catch (error) {
      console.error("Login failed:", error);
      alert("Login failed: " + (error.response?.data?.message || error.message || "Unknown error"));
    }
  };

  return (
    <Container maxWidth="sm">
      <Paper elevation={3} style={{ padding: "20px", marginTop: "50px" }}>
        <Typography variant="h4" align="center" gutterBottom>
          Login
        </Typography>
        <form onSubmit={handleLogin}>
          <TextField
            fullWidth
            label="Username"
            margin="normal"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <TextField
            fullWidth
            label="Password"
            type="password"
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Button
            fullWidth
            variant="contained"
            color="primary"
            type="submit"
            style={{ marginTop: "20px" }}
          >
            Login
          </Button>
          <Button
            fullWidth
            variant="outlined"
            color="secondary"
            onClick={onSignupClick}
            style={{ marginTop: "10px" }}
          >
            Sign Up
          </Button>
        </form>
      </Paper>
    </Container>
  );
}

export default Login;