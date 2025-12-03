import React, { useState, useEffect } from 'react';
import { Button, Container, Card, Row, Col, Table } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { FaArrowRight } from 'react-icons/fa';
import '../styles/Dashboard.css';
import Sidebar from './Sidebar';
import authService from '../services/authService';
import accountService from '../services/accountService';
import transactionService from '../services/transactionService';

const Dashboard = () => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const initializeDashboard = async () => {
      console.log("=== Dashboard Initialization Started ===");
      
      // Wait a moment for any redirects/storage to complete
      await new Promise(resolve => setTimeout(resolve, 500));
      
      // Check authentication
      const isAuth = authService.isAuthenticated();
      console.log("Authentication check:", isAuth);
      
      if (!isAuth) {
        console.log("Not authenticated, redirecting to login");
        navigate('/login', { replace: true });
        return;
      }
      
      // Get user data from localStorage
      const userData = authService.getUser();
      console.log("User data from localStorage:", userData);
      
      if (!userData || !userData.id) {
        console.error("Invalid user data");
        setError("User data is missing. Please login again.");
        setLoading(false);
        setTimeout(() => {
          authService.logout();
        }, 2000);
        return;
      }
      
      // Set initial customer data
      setCustomer(userData);
      const customerId = userData.id;
      
      try {
        setLoading(true);
        
        // Fetch customer details from AUTH SERVICE
        console.log("Fetching customer details for ID:", customerId);
        try {
          const customerResponse = await authService.getCustomerDetails(customerId);
          if (customerResponse && customerResponse.data) {
            console.log("Customer data received:", customerResponse.data);
            setCustomer(customerResponse.data);
          } else {
            console.log("No detailed customer data, using localStorage data");
          }
        } catch (err) {
          console.warn("Error fetching customer details:", err);
          // Continue with localStorage data
        }
        
        // Fetch account details from ACCOUNT SERVICE
        console.log("Fetching accounts for customer ID:", customerId);
        let accountNumber = null;
        try {
          const accounts = await accountService.getAccountsByCustomerId(customerId);
          console.log("Accounts received:", accounts);
          
          if (accounts && accounts.length > 0) {
            setAccount(accounts[0]);
            accountNumber = accounts[0].accountNumber;
          } else {
            console.warn("No accounts found");
          }
        } catch (err) {
          console.error("Error fetching account data:", err);
        }
        
        // Fetch latest 5 transactions from TRANSACTION SERVICE (already sorted by transaction_id DESC)
        if (accountNumber) {
          console.log("Fetching latest transactions for account:", accountNumber);
          try {
            const latestTransactions = await transactionService.getLatestTransactions(accountNumber);
            console.log("Latest transactions received:", latestTransactions);
            setTransactions(latestTransactions);
          } catch (err) {
            console.error("Error fetching transactions:", err);
            // Set empty array on error so dashboard still loads
            setTransactions([]);
          }
        } else {
          console.warn("No account number available for fetching transactions");
          setTransactions([]);
        }
        
        setLoading(false);
        console.log("=== Dashboard Initialization Complete ===");
      } catch (err) {
        console.error("Error initializing dashboard:", err);
        setError("Failed to load dashboard data.");
        setLoading(false);
      }
    };
    
    initializeDashboard();
  }, [navigate]);

  if (loading) {
    return (
      <div className="loading-container" style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>Loading dashboard...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container" style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column'
      }}>
        <div className="error">{error}</div>
        <Button onClick={() => navigate('/login')} style={{ marginTop: '20px' }}>
          Go to Login
        </Button>
      </div>
    );
  }

  if (!customer) {
    return (
      <div className="error-container" style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>No customer data available</div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="main-content">
        <Container fluid>
          <div className="bank-header">
            <h1>Welcome to BingBank</h1>
          </div>
          
          {/* CARD 1: Customer Information from AUTH SERVICE */}
          <Card className="info-card mb-4">
            <Card.Body>
              <h2 className="customer-name">Hi, {customer.firstName || ''} {customer.lastName || ''}</h2>
              <Row>
                <Col md={6}>
                  <div className="info-item">
                    <span className="info-label">Customer ID</span>
                    <span className="info-value">{customer.id || customer.customerId || ''}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Email</span>
                    <span className="info-value">{customer.email || 'Not provided'}</span>
                  </div>
                </Col>
                <Col md={6}>
                  <div className="info-item">
                    <span className="info-label">Phone Number</span>
                    <span className="info-value">{customer.mobile || 'Not provided'}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Address</span>
                    <span className="info-value">{customer.address || 'Not provided'}</span>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>
          
          {/* CARD 2: Account Information from ACCOUNT SERVICE */}
          {account ? (
            <Card className="balance-card mb-4">
              <Card.Body>
                <div className="balance-container">
                  <div className="balance-amount">${Number(account.balance).toFixed(2)}</div>
                  <div className="balance-info">
                    <div className="info-item">
                      <span className="info-label">Account Number:</span>
                      <span className="info-value">{account.accountNumber}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Account Type:</span>
                      <span className="info-value">{account.accountType}</span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Branch Code:</span>
                      <span className="info-value">
                        {account.branch ? account.branch.branchCode : 'N/A'}
                      </span>
                    </div>
                    <div className="info-item">
                      <span className="info-label">Routing Number:</span>
                      <span className="info-value">{account.routingNumber}</span>
                    </div>
                  </div>
                </div>
              </Card.Body>
            </Card>
          ) : (
            <Card className="balance-card mb-4">
              <Card.Body>
                <div className="text-center py-4">
                  <h3>No Account Information Available</h3>
                  <p>Please contact customer support to link your account.</p>
                </div>
              </Card.Body>
            </Card>
          )}
          
          {/* CARD 3: Transaction History from TRANSACTION SERVICE */}
          <Card className="transaction-card">
            <Card.Header>
              <h3>Recent Transaction History</h3>
            </Card.Header>
            <Card.Body>
              {transactions.length > 0 ? (
                <>
                  <Table responsive className="transaction-table">
                    <thead>
                      <tr>
                        <th>Transaction ID</th>
                        <th>Amount</th>
                        <th>Transaction Type</th>
                        <th>Transaction Date</th>
                        <th>Source Account</th>
                        <th>Target Account</th>
                      </tr>
                    </thead>
                    <tbody>
                      {transactions.map(transaction => (
                        <tr 
                          key={transaction.transactionId} 
                          className={transaction.transactionType.toLowerCase()}
                        >
                          <td>{transaction.transactionId}</td>
                          <td className={transaction.transactionType === 'CREDIT' ? 'text-success' : 'text-danger'}>
                            {transaction.transactionType === 'CREDIT' ? '+' : '-'}${transaction.amount.toFixed(2)}
                          </td>
                          <td>
                            <span className={`transaction-type ${transaction.transactionType.toLowerCase()}`}>
                              {transaction.transactionType}
                            </span>
                          </td>
                          <td>{new Date(transaction.transactionDate).toLocaleDateString('en-GB')}</td>
                          <td>{transaction.sourceAccountNumber}</td>
                          <td>{transaction.targetAccountNumber || 'N/A'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </Table>
                  <div className="text-center">
                    <Link to="/transactions">
                      <Button variant="primary">
                        See All Transactions <FaArrowRight />
                      </Button>
                    </Link>
                  </div>
                </>
              ) : (
                <div className="text-center py-4">
                  <p>No transaction history available.</p>
                  {account && (
                    <p className="text-muted">
                      Your account is active, but no transactions have been recorded yet.
                    </p>
                  )}
                </div>
              )}
            </Card.Body>
          </Card>
        </Container>
      </div>
    </div>
  );
};

export default Dashboard;