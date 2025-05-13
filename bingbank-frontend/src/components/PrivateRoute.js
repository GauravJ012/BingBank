import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../services/authService';

const PrivateRoute = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  
  useEffect(() => {
    const checkAuth = () => {
      const authResult = authService.isAuthenticated();
      console.log("PrivateRoute auth check:", authResult);
      setIsAuthenticated(authResult);
    };
    
    checkAuth();
  }, []);
  
  // While checking, show loading
  if (isAuthenticated === null) {
    return <div className="loading">Authenticating...</div>;
  }
  
  // Once checked, either render children or redirect
  return isAuthenticated ? children : <Navigate to="/login" />;
};

export default PrivateRoute;