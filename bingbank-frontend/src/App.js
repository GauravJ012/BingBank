import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';

import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Transactions from './components/Transactions';
import FixedDeposits from './components/FixedDeposits'; 
import FundTransfer from './components/FundTransfer';
import Cards from './components/Cards';
import ForgotPassword from './components/ForgotPassword';
import PrivateRoute from './components/PrivateRoute';
import authService from './services/authService';

function App() {
  const [isLoading, setIsLoading] = useState(true);
  
  // Check authentication on app load
  useEffect(() => {
    console.log("App initialization - checking auth");
    // Give a small delay to ensure localStorage is read correctly
    setTimeout(() => {
      setIsLoading(false);
    }, 300);
  }, []);

  if (isLoading) {
    return <div className="app-loading">Loading application...</div>;
  }

  return (
    <Router>
      <div className="App">
        <ToastContainer 
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
        
        <Routes>
          <Route path="/login" element={
            authService.isAuthenticated() ? <Navigate to="/dashboard" /> : <Login />
          } />
          <Route path="/register" element={
            authService.isAuthenticated() ? <Navigate to="/dashboard" /> : <Register />
          } />
          <Route 
            path="/" 
            element={<Navigate to="/dashboard" />} 
          />
          <Route 
            path="/dashboard" 
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            } 
          />
          <Route
            path="/transactions"
            element={
              <PrivateRoute>
                <Transactions />
              </PrivateRoute>
            }
          />
          <Route
            path="/fixed-deposits"
            element={
              <PrivateRoute>
                <FixedDeposits />
              </PrivateRoute>
            }
          />
          <Route
            path="/fund-transfer"
            element={
              <PrivateRoute>
                <FundTransfer />
              </PrivateRoute>
            }
          />
          <Route
            path="/cards"
            element={
              <PrivateRoute>
                <Cards />
              </PrivateRoute>
            }
          />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          {/* Add routes for other pages here, all protected with PrivateRoute */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;