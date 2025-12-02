import React, { useState, useEffect } from 'react';
import { Container, Card, Table, Form, Row, Col, Button, Badge } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import authService from '../services/authService';
import accountService from '../services/accountService';
import transactionService from '../services/transactionService';
import '../styles/Transactions.css';

const Transactions = () => {
  const navigate = useNavigate();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filter states
  const [filters, setFilters] = useState({
    startDate: '',
    endDate: '',
    minAmount: '',
    maxAmount: '',
    transactionType: '',
    otherAccountNumber: '',
    limit: '',
    sortBy: 'transactionDate',
    sortDirection: 'DESC'
  });

  useEffect(() => {
    const initializeTransactions = async () => {
      const isAuth = authService.isAuthenticated();
      if (!isAuth) {
        navigate('/login', { replace: true });
        return;
      }

      const userData = authService.getUser();
      if (!userData || !userData.id) {
        setError("User data is missing. Please login again.");
        setLoading(false);
        return;
      }

      try {
        setLoading(true);

        // Get account details
        const accounts = await accountService.getAccountsByCustomerId(userData.id);
        if (accounts && accounts.length > 0) {
          setAccount(accounts[0]);
          
          // Get all transactions
          const allTransactions = await transactionService.getAllTransactions(accounts[0].accountNumber);
          setTransactions(allTransactions);
          setFilteredTransactions(allTransactions);
        } else {
          setError("No account found");
        }

        setLoading(false);
      } catch (err) {
        console.error("Error loading transactions:", err);
        setError("Failed to load transactions");
        setLoading(false);
      }
    };

    initializeTransactions();
  }, [navigate]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const applyFilters = async () => {
    if (!account) return;

    try {
      setLoading(true);
      const filterRequest = {
        accountNumber: account.accountNumber,
        startDate: filters.startDate || null,
        endDate: filters.endDate || null,
        minAmount: filters.minAmount ? parseFloat(filters.minAmount) : null,
        maxAmount: filters.maxAmount ? parseFloat(filters.maxAmount) : null,
        transactionType: filters.transactionType || null,
        otherAccountNumber: filters.otherAccountNumber || null,
        limit: filters.limit ? parseInt(filters.limit) : null,
        sortBy: filters.sortBy,
        sortDirection: filters.sortDirection
      };

      const filtered = await transactionService.getFilteredTransactions(filterRequest);
      setFilteredTransactions(filtered);
      setSelectedTransactions([]); // Clear selections when filtering
      setLoading(false);
    } catch (err) {
      console.error("Error filtering transactions:", err);
      setError("Failed to filter transactions");
      setLoading(false);
    }
  };

  const resetFilters = async () => {
    setFilters({
      startDate: '',
      endDate: '',
      minAmount: '',
      maxAmount: '',
      transactionType: '',
      otherAccountNumber: '',
      limit: '',
      sortBy: 'transactionDate',
      sortDirection: 'DESC'
    });
    setFilteredTransactions(transactions);
    setSelectedTransactions([]);
  };

  const handleSort = (column) => {
    const newDirection = filters.sortBy === column && filters.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    setFilters(prev => ({
      ...prev,
      sortBy: column,
      sortDirection: newDirection
    }));
    // Automatically apply filters when sorting
    setTimeout(() => applyFilters(), 100);
  };

  const handleSelectTransaction = (transactionId) => {
    setSelectedTransactions(prev => {
      if (prev.includes(transactionId)) {
        return prev.filter(id => id !== transactionId);
      } else {
        return [...prev, transactionId];
      }
    });
  };

  const handleSelectAll = () => {
    if (selectedTransactions.length === filteredTransactions.length) {
      setSelectedTransactions([]);
    } else {
      setSelectedTransactions(filteredTransactions.map(t => t.transactionId));
    }
  };

  const downloadStatement = async () => {
    if (!account) return;

    const userData = authService.getUser();
    const statementRequest = {
      customerId: userData.id,
      accountNumber: account.accountNumber,
      transactionIds: selectedTransactions.length > 0 ? selectedTransactions : 
                      filteredTransactions.map(t => t.transactionId)
    };

    try {
      await transactionService.downloadStatement(statementRequest);
    } catch (err) {
      console.error("Error downloading statement:", err);
      alert("Failed to download statement");
    }
  };

  const getSortIcon = (column) => {
    if (filters.sortBy !== column) return '⇅';
    return filters.sortDirection === 'ASC' ? '↑' : '↓';
  };

  if (loading) {
    return (
      <div className="loading-container" style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>Loading transactions...</div>
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
        <Button onClick={() => navigate('/dashboard')} style={{ marginTop: '20px' }}>
          Go to Dashboard
        </Button>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="main-content">
        <Container fluid>
          <div className="bank-header">
            <h1>Transaction History</h1>
          </div>

          {/* Account Info */}
          {account && (
            <Card className="mb-4">
              <Card.Body>
                <Row>
                  <Col md={3}>
                    <strong>Account Number:</strong> {account.accountNumber}
                  </Col>
                  <Col md={3}>
                    <strong>Account Type:</strong> {account.accountType}
                  </Col>
                  <Col md={3}>
                    <strong>Current Balance:</strong> ${Number(account.balance).toFixed(2)}
                  </Col>
                  <Col md={3}>
                    <strong>Total Transactions:</strong> {filteredTransactions.length}
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          )}

          {/* Filters */}
          <Card className="mb-4">
            <Card.Header>
              <h5>Filter Transactions</h5>
            </Card.Header>
            <Card.Body>
              <Form>
                <Row className="mb-3">
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Start Date</Form.Label>
                      <Form.Control
                        type="date"
                        name="startDate"
                        value={filters.startDate}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>End Date</Form.Label>
                      <Form.Control
                        type="date"
                        name="endDate"
                        value={filters.endDate}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Min Amount</Form.Label>
                      <Form.Control
                        type="number"
                        name="minAmount"
                        placeholder="0.00"
                        value={filters.minAmount}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Max Amount</Form.Label>
                      <Form.Control
                        type="number"
                        name="maxAmount"
                        placeholder="0.00"
                        value={filters.maxAmount}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <Row className="mb-3">
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Transaction Type</Form.Label>
                      <Form.Select
                        name="transactionType"
                        value={filters.transactionType}
                        onChange={handleFilterChange}
                      >
                        <option value="">All Types</option>
                        <option value="DEBIT">Debit</option>
                        <option value="CREDIT">Credit</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Other Account</Form.Label>
                      <Form.Control
                        type="text"
                        name="otherAccountNumber"
                        placeholder="Account Number"
                        value={filters.otherAccountNumber}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3}>
                    <Form.Group>
                      <Form.Label>Limit Results</Form.Label>
                      <Form.Control
                        type="number"
                        name="limit"
                        placeholder="All"
                        value={filters.limit}
                        onChange={handleFilterChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={3} className="d-flex align-items-end">
                    <Button variant="primary" onClick={applyFilters} className="me-2">
                      Apply Filters
                    </Button>
                    <Button variant="secondary" onClick={resetFilters}>
                      Reset
                    </Button>
                  </Col>
                </Row>
              </Form>
            </Card.Body>
          </Card>

          {/* Actions */}
          <Card className="mb-4">
            <Card.Body>
              <Row>
                <Col md={6}>
                  <Button 
                    variant="success" 
                    onClick={downloadStatement}
                    disabled={filteredTransactions.length === 0}
                  >
                    📄 Download Statement ({selectedTransactions.length > 0 ? `${selectedTransactions.length} Selected` : 'All Displayed'})
                  </Button>
                </Col>
                <Col md={6} className="text-end">
                  <Form.Check
                    type="checkbox"
                    label="Select All"
                    checked={selectedTransactions.length === filteredTransactions.length && filteredTransactions.length > 0}
                    onChange={handleSelectAll}
                    inline
                  />
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* Transactions Table */}
          <Card className="transaction-card">
            <Card.Header>
              <h5>Transactions</h5>
            </Card.Header>
            <Card.Body>
              {filteredTransactions.length > 0 ? (
                <Table responsive hover className="transaction-table">
                  <thead>
                    <tr>
                      <th style={{ width: '50px' }}>Select</th>
                      <th style={{ cursor: 'pointer' }} onClick={() => handleSort('transactionDate')}>
                        Date {getSortIcon('transactionDate')}
                      </th>
                      <th style={{ cursor: 'pointer' }} onClick={() => handleSort('transactionId')}>
                        ID {getSortIcon('transactionId')}
                      </th>
                      <th style={{ cursor: 'pointer' }} onClick={() => handleSort('transactionType')}>
                        Type {getSortIcon('transactionType')}
                      </th>
                      <th style={{ cursor: 'pointer' }} onClick={() => handleSort('amount')}>
                        Amount {getSortIcon('amount')}
                      </th>
                      <th>From Account</th>
                      <th>To Account</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredTransactions.map(transaction => (
                      <tr key={transaction.transactionId}>
                        <td>
                          <Form.Check
                            type="checkbox"
                            checked={selectedTransactions.includes(transaction.transactionId)}
                            onChange={() => handleSelectTransaction(transaction.transactionId)}
                          />
                        </td>
                        <td>{new Date(transaction.transactionDate).toLocaleDateString('en-GB')}</td>
                        <td>{transaction.transactionId}</td>
                        <td>
                          <Badge bg={transaction.transactionType === 'CREDIT' ? 'success' : 'danger'}>
                            {transaction.transactionType}
                          </Badge>
                        </td>
                        <td className={transaction.transactionType === 'CREDIT' ? 'text-success' : 'text-danger'}>
                          {transaction.transactionType === 'CREDIT' ? '+' : '-'}${transaction.amount.toFixed(2)}
                        </td>
                        <td>{transaction.sourceAccountNumber}</td>
                        <td>{transaction.targetAccountNumber || 'N/A'}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              ) : (
                <div className="text-center py-4">
                  <p>No transactions found matching the criteria.</p>
                </div>
              )}
            </Card.Body>
          </Card>
        </Container>
      </div>
    </div>
  );
};

export default Transactions;