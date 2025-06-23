import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Signup from './components/Signup';
import ShoppingCart from './components/ShoppingCart';
import { Button, AppBar, Toolbar, Typography, Box, Link } from '@mui/material';
import axios from 'axios';

const API_URL = process.env.REACT_APP_AUTH_API_URL || "";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showSignup, setShowSignup] = useState(false);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    if (token) {
      setIsLoggedIn(true);
    }
  }, []);

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
  };

  const handleLogout = async () => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        await axios.post(`${API_URL}/api/auth/logout`, {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
      }
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      setIsLoggedIn(false);
    }
  };

  const toggleSignup = () => {
    setShowSignup(!showSignup);
  };

  return (
    <div className="App">
      {isLoggedIn ? (
        <>
          <AppBar position="static">
            <Toolbar>
              <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                Shopping App
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Link 
                  href="http://localhost:8081/swagger-ui/index.html" 
                  target="_blank" 
                  sx={{ color: 'white', mx: 2 }}
                >
                  Shopping API
                </Link>
                <Link 
                  href="http://localhost:8082/swagger-ui/index.html" 
                  target="_blank" 
                  sx={{ color: 'white', mx: 2 }}
                >
                  Auth API
                </Link>
                <Button color="inherit" onClick={handleLogout}>Logout</Button>
              </Box>
            </Toolbar>
          </AppBar>
          <ShoppingCart />
        </>
      ) : (
        showSignup ? (
          <Signup onBackToLogin={toggleSignup} />
        ) : (
          <Login onLoginSuccess={handleLoginSuccess} onSignupClick={toggleSignup} />
        )
      )}
    </div>
  );
}

export default App;