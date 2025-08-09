import React, { useState, useEffect } from 'react';
import { Button, Container, Card, Row, Col, Table } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { FaArrowRight } from 'react-icons/fa';
import '../styles/Dashboard.css';
import Sidebar from './Sidebar';
import authService from '../services/authService';

// Mock data only for account and transactions until those microservices are ready
const MOCK_ACCOUNT = {
  accountNumber: "556704",
  accountType: "Saving",
  balance: 950.00,
  branch: "Binghamton",
  routingNumber: "1003005"
};

const MOCK_TRANSACTIONS = [
  {
    id: 21,
    amount: 50.00,
    type: "Withdraw",
    date: "2023-07-23",
    sourceAccount: "556704",
    targetAccount: "N/A"
  },
  {
    id: 20,
    amount: 70.00,
    type: "Transfer",
    date: "2023-07-23",
    sourceAccount: "556704",
    targetAccount: "707290"
  },
  {
    id: 19,
    amount: 70.00,
    type: "Credited",
    date: "2023-07-23",
    sourceAccount: "552398",
    targetAccount: "556704"
  },
  {
    id: 18,
    amount: 590.00,
    type: "Transfer",
    date: "2023-07-23",
    sourceAccount: "556704",
    targetAccount: "552398"
  },
  {
    id: 17,
    amount: 0.00,
    type: "Credited",
    date: "2023-07-23",
    sourceAccount: "236480",
    targetAccount: "556704"
  }
];

const Dashboard = () => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [authChecked, setAuthChecked] = useState(false);

  // First check if user is authenticated
  useEffect(() => {
    const checkAuth = () => {
      const isAuth = authService.isAuthenticated();
      console.log("Auth check on dashboard:", isAuth);
      
      if (!isAuth) {
        console.log("User not authenticated, redirecting to login");
        navigate('/login');
        return false;
      }
      
      setAuthChecked(true);
      return true;
    };
    
    checkAuth();
  }, [navigate]);

  // Then fetch data if authenticated
  useEffect(() => {
    const fetchData = async () => {
      if (!authChecked) return;
      
      try {
        setLoading(true);
        console.log("Fetching dashboard data...");
        
        // Get authenticated user data
        const userData = authService.getUser();
        console.log("User data from localStorage:", userData);
        
        if (!userData || !userData.id) {
          console.error("Invalid user data");
          setError("User data is missing or invalid");
          setLoading(false);
          return;
        }
        
        // Set initial user data from localStorage
        setCustomer(userData);
        
        // Get more detailed customer data if needed
        try {
          // Make an API call to get full customer details
          console.log("Fetching detailed customer info for ID:", userData.id);
          const customerResponse = await authService.getCustomerDetails(userData.id);
          console.log("Detailed customer data:", customerResponse.data);
          setCustomer(prevCustomer => ({
            ...prevCustomer,
            ...customerResponse.data
          }));
        } catch (customerErr) {
          // If API fails, keep using data from localStorage
          console.warn("Couldn't fetch detailed customer info:", customerErr);
          // We already set customer from localStorage, so no need to do it again
        }
        
        // Until account service is ready, use mock data
        setAccount(MOCK_ACCOUNT);
        setTransactions(MOCK_TRANSACTIONS);
        
        setLoading(false);
      } catch (err) {
        console.error("Error loading dashboard data:", err);
        setError("Failed to load dashboard data. Please try again later.");
        setLoading(false);
      }
    };
    
    fetchData();
  }, [authChecked]);

  if (!authChecked) return <div className="loading">Checking authentication...</div>;
  if (loading) return <div className="loading">Loading dashboard data...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!customer) return <div className="error">No customer data available</div>;

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="main-content">
        <Container fluid>
          <div className="bank-header">
            <h1>Welcome to BingBank</h1>
          </div>
          
          {/* Customer Information Card */}
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
                    <span className="info-value">{customer.mobile || customer.phoneNumber || 'Not provided'}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Address</span>
                    <span className="info-value">{customer.address || 'Not provided'}</span>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>
          
          {/* Account Details Card */}
          <Card className="balance-card mb-4">
            <Card.Body>
              <div className="balance-container">
                <div className="balance-amount">${account.balance.toFixed(2)}</div>
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
                    <span className="info-label">Branch:</span>
                    <span className="info-value">{account.branch}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Routing Number:</span>
                    <span className="info-value">{account.routingNumber}</span>
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>
          
          {/* Transaction History Card */}
          <Card className="transaction-card">
            <Card.Header>
              <h3>Transaction History</h3>
            </Card.Header>
            <Card.Body>
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
                    <tr key={transaction.id} className={transaction.type.toLowerCase()}>
                      <td>{transaction.id}</td>
                      <td>${transaction.amount.toFixed(2)}</td>
                      <td>
                        <span className={`transaction-type ${transaction.type.toLowerCase()}`}>
                          {transaction.type}
                        </span>
                      </td>
                      <td>{new Date(transaction.date).toLocaleDateString()}</td>
                      <td>{transaction.sourceAccount}</td>
                      <td>{transaction.targetAccount}</td>
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
            </Card.Body>
          </Card>
        </Container>
      </div>
    </div>
  );
};

export default Dashboard;