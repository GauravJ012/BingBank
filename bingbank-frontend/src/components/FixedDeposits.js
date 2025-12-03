import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Button, Modal, Form, Badge, ProgressBar, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import Sidebar from './Sidebar';
import authService from '../services/authService';
import accountService from '../services/accountService';
import fixedDepositService from '../services/fixedDepositService';
import '../styles/FixedDeposits.css';

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

const FixedDeposits = () => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [account, setAccount] = useState(null);
  const [fds, setFds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showGrowthModal, setShowGrowthModal] = useState(false);
  const [showCloseConfirm, setShowCloseConfirm] = useState(false);

  // Form states
  const [createForm, setCreateForm] = useState({
    principalAmount: '',
    tenureYears: 1
  });
  const [selectedFD, setSelectedFD] = useState(null);
  const [growthData, setGrowthData] = useState(null);
  const [fdToClose, setFdToClose] = useState(null);

  // Loading states
  const [creatingFD, setCreatingFD] = useState(false);
  const [closingFD, setClosingFD] = useState(false);
  const [loadingGrowth, setLoadingGrowth] = useState(false);

  useEffect(() => {
    initializeFixedDeposits();
  }, [navigate]);

  const initializeFixedDeposits = async () => {
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
      setCustomer(userData);

      // Get account details
      const accounts = await accountService.getAccountsByCustomerId(userData.id);
      if (accounts && accounts.length > 0) {
        setAccount(accounts[0]);

        // Get FDs
        const fixedDeposits = await fixedDepositService.getActiveFDs(userData.id);
        setFds(fixedDeposits);
      } else {
        setError("No account found");
      }

      setLoading(false);
    } catch (err) {
      console.error("Error loading fixed deposits:", err);
      setError("Failed to load fixed deposits");
      setLoading(false);
    }
  };

  const handleCreateFD = async (e) => {
    e.preventDefault();
    
    if (!account || !customer) {
      alert("Account information not available");
      return;
    }

    const amount = parseFloat(createForm.principalAmount);
    
    if (amount < 100) {
      alert("Minimum FD amount is $100");
      return;
    }

    if (amount > parseFloat(account.balance)) {
      alert("Insufficient balance in your account");
      return;
    }

    try {
      setCreatingFD(true);
      
      const fdRequest = {
        customerId: customer.id,
        accountNumber: account.accountNumber,
        principalAmount: amount,
        tenureYears: parseInt(createForm.tenureYears)
      };

      await fixedDepositService.createFD(fdRequest);
      
      // Refresh data
      await initializeFixedDeposits();
      
      setShowCreateModal(false);
      setCreateForm({ principalAmount: '', tenureYears: 1 });
      setCreatingFD(false);
      
      alert("Fixed Deposit created successfully!");
    } catch (err) {
      console.error("Error creating FD:", err);
      alert(err.response?.data || "Failed to create Fixed Deposit");
      setCreatingFD(false);
    }
  };

  const handleCloseFD = async () => {
    if (!fdToClose) return;

    try {
      setClosingFD(true);
      
      const closeRequest = {
        fdId: fdToClose.fdId,
        customerId: customer.id,
        accountNumber: fdToClose.accountNumber
      };

      await fixedDepositService.closeFD(closeRequest);
      
      // Refresh data
      await initializeFixedDeposits();
      
      setShowCloseConfirm(false);
      setFdToClose(null);
      setClosingFD(false);
      
      alert("Fixed Deposit closed successfully! Amount credited to your account.");
    } catch (err) {
      console.error("Error closing FD:", err);
      alert(err.response?.data || "Failed to close Fixed Deposit");
      setClosingFD(false);
    }
  };

  const handleTrackGrowth = async (fd) => {
    setSelectedFD(fd);
    setShowGrowthModal(true);
    setLoadingGrowth(true);

    try {
      const data = await fixedDepositService.getGrowthData(fd.fdId);
      setGrowthData(data);
      setLoadingGrowth(false);
    } catch (err) {
      console.error("Error fetching growth data:", err);
      alert("Failed to load growth data");
      setShowGrowthModal(false);
      setLoadingGrowth(false);
    }
  };

  const calculateMaturityAmount = (principal, years) => {
    const rate = 0.07; // 7% interest
    return principal * Math.pow(1 + rate, years);
  };

  const getProgressPercentage = (fd) => {
    if (!fd.daysElapsed || !fd.totalDays) return 0;
    return Math.min((fd.daysElapsed / fd.totalDays) * 100, 100);
  };

  // Prepare chart data with better date formatting
  const getChartData = () => {
    if (!growthData) return null;

    return {
      labels: growthData.growthPoints.map(point => {
        const date = new Date(point.date);
        // Format based on tenure for better readability
        if (growthData.tenureYears === 1) {
          return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
        } else if (growthData.tenureYears <= 3) {
          return date.toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
        } else {
          return date.toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
        }
      }),
      datasets: [
        {
          label: 'FD Value',
          data: growthData.growthPoints.map(point => point.value),
          borderColor: 'rgb(102, 126, 234)',
          backgroundColor: 'rgba(102, 126, 234, 0.1)',
          fill: true,
          tension: 0.4,
          pointRadius: 5,
          pointHoverRadius: 7,
          pointBackgroundColor: 'rgb(102, 126, 234)',
          pointBorderColor: '#fff',
          pointBorderWidth: 2
        }
      ]
    };
  };

  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Fixed Deposit Growth Over Time',
        font: {
          size: 16,
          weight: 'bold'
        }
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            return '$' + context.parsed.y.toFixed(2);
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        ticks: {
          callback: function(value) {
            return '$' + value.toLocaleString();
          }
        }
      },
      x: {
        ticks: {
          maxRotation: 45,
          minRotation: 45
        }
      }
    }
  };

  if (loading) {
    return (
      <div className="loading-container" style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <div className="ms-3">Loading fixed deposits...</div>
      </div>
    );
  }

  if (error && !account) {
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
            <h1>Fixed Deposits</h1>
            <Button 
              variant="primary" 
              size="lg"
              onClick={() => setShowCreateModal(true)}
            >
              + Create New FD
            </Button>
          </div>

          {/* Account Balance Card */}
          {account && (
            <Card className="mb-4">
              <Card.Body>
                <Row>
                  <Col md={4}>
                    <strong>Account Number:</strong> {account.accountNumber}
                  </Col>
                  <Col md={4}>
                    <strong>Account Type:</strong> {account.accountType}
                  </Col>
                  <Col md={4}>
                    <strong>Available Balance:</strong> <span className="text-success fw-bold">${Number(account.balance).toFixed(2)}</span>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          )}

          {/* FD Summary */}
          <Row className="mb-4">
            <Col md={4}>
              <Card className="stat-card">
                <Card.Body>
                  <h6 className="text-muted">Active Fixed Deposits</h6>
                  <h2 className="mb-0">{fds.length}</h2>
                </Card.Body>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="stat-card">
                <Card.Body>
                  <h6 className="text-muted">Total Investment</h6>
                  <h2 className="mb-0 text-primary">
                    ${fds.reduce((sum, fd) => sum + parseFloat(fd.principalAmount), 0).toFixed(2)}
                  </h2>
                </Card.Body>
              </Card>
            </Col>
            <Col md={4}>
              <Card className="stat-card">
                <Card.Body>
                  <h6 className="text-muted">Current Total Value</h6>
                  <h2 className="mb-0 text-success">
                    ${fds.reduce((sum, fd) => sum + parseFloat(fd.currentValue), 0).toFixed(2)}
                  </h2>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Fixed Deposits List */}
          <Card>
            <Card.Header>
              <h5>Your Fixed Deposits</h5>
            </Card.Header>
            <Card.Body>
              {fds.length > 0 ? (
                <Row>
                  {fds.map(fd => (
                    <Col md={6} lg={4} key={fd.fdId} className="mb-4">
                      <Card className="fd-card h-100">
                        <Card.Body>
                          <div className="d-flex justify-content-between align-items-center mb-3">
                            <h6 className="mb-0">FD #{fd.fdId}</h6>
                            <Badge bg="success">Active</Badge>
                          </div>

                          <div className="fd-details mb-3">
                            <div className="detail-row">
                              <span className="label">Principal Amount:</span>
                              <span className="value">${parseFloat(fd.principalAmount).toFixed(2)}</span>
                            </div>
                            <div className="detail-row">
                              <span className="label">Start Date:</span>
                              <span className="value">{new Date(fd.startDate).toLocaleDateString('en-GB')}</span>
                            </div>
                            <div className="detail-row">
                              <span className="label">Maturity Date:</span>
                              <span className="value">{new Date(fd.maturityDate).toLocaleDateString('en-GB')}</span>
                            </div>
                            <div className="detail-row">
                              <span className="label">ROI:</span>
                              <span className="value">{(parseFloat(fd.interestRate) * 100).toFixed(2)}%</span>
                            </div>
                            <div className="detail-row">
                              <span className="label">Current Value:</span>
                              <span className="value text-success fw-bold">${parseFloat(fd.currentValue).toFixed(2)}</span>
                            </div>
                            <div className="detail-row">
                              <span className="label">Maturity Value:</span>
                              <span className="value text-primary fw-bold">${parseFloat(fd.maturityAmount).toFixed(2)}</span>
                            </div>
                          </div>

                          {/* Progress Bar */}
                          <div className="mb-3">
                            <div className="d-flex justify-content-between mb-1">
                              <small>Progress</small>
                              <small>{getProgressPercentage(fd).toFixed(1)}%</small>
                            </div>
                            <ProgressBar 
                              now={getProgressPercentage(fd)} 
                              variant="success"
                              animated
                            />
                            <small className="text-muted">
                              {fd.daysElapsed} of {fd.totalDays} days elapsed
                            </small>
                          </div>

                          {/* Action Buttons */}
                          <div className="d-grid gap-2">
                            <Button 
                              variant="outline-primary" 
                              size="sm"
                              onClick={() => handleTrackGrowth(fd)}
                            >
                              📈 Track Growth
                            </Button>
                            <Button 
                              variant="outline-danger" 
                              size="sm"
                              onClick={() => {
                                setFdToClose(fd);
                                setShowCloseConfirm(true);
                              }}
                            >
                              Close FD
                            </Button>
                          </div>
                        </Card.Body>
                      </Card>
                    </Col>
                  ))}
                </Row>
              ) : (
                <div className="text-center py-5">
                  <h5>No Fixed Deposits Found</h5>
                  <p className="text-muted">Create your first fixed deposit to start earning interest!</p>
                  <Button variant="primary" onClick={() => setShowCreateModal(true)}>
                    Create FD
                  </Button>
                </div>
              )}
            </Card.Body>
          </Card>

          {/* Create FD Modal */}
          <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)} centered>
            <Modal.Header closeButton>
              <Modal.Title>Create New Fixed Deposit</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <Form onSubmit={handleCreateFD}>
                <Form.Group className="mb-3">
                  <Form.Label>Principal Amount ($)</Form.Label>
                  <Form.Control
                    type="number"
                    placeholder="Enter amount"
                    value={createForm.principalAmount}
                    onChange={(e) => setCreateForm({ ...createForm, principalAmount: e.target.value })}
                    min="100"
                    max={account ? account.balance : 0}
                    step="100"
                    required
                  />
                  <Form.Text className="text-muted">
                    Minimum: $100 | Available Balance: ${account ? parseFloat(account.balance).toFixed(2) : 0}
                  </Form.Text>
                </Form.Group>

                <Form.Group className="mb-3">
                  <Form.Label>Tenure (Years)</Form.Label>
                  <Form.Select
                    value={createForm.tenureYears}
                    onChange={(e) => setCreateForm({ ...createForm, tenureYears: e.target.value })}
                    required
                  >
                    <option value="1">1 Year</option>
                    <option value="2">2 Years</option>
                    <option value="3">3 Years</option>
                    <option value="5">5 Years</option>
                    <option value="10">10 Years</option>
                  </Form.Select>
                </Form.Group>

                <Card className="bg-light mb-3">
                  <Card.Body>
                    <h6>Estimated Returns</h6>
                    <div className="d-flex justify-content-between">
                      <span>Interest Rate:</span>
                      <strong>7% per annum</strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span>Maturity Amount:</span>
                      <strong className="text-success">
                        ${createForm.principalAmount ? 
                          calculateMaturityAmount(parseFloat(createForm.principalAmount), parseInt(createForm.tenureYears)).toFixed(2) 
                          : '0.00'}
                      </strong>
                    </div>
                    <div className="d-flex justify-content-between">
                      <span>Total Interest:</span>
                      <strong className="text-primary">
                        ${createForm.principalAmount ? 
                          (calculateMaturityAmount(parseFloat(createForm.principalAmount), parseInt(createForm.tenureYears)) - parseFloat(createForm.principalAmount)).toFixed(2)
                          : '0.00'}
                      </strong>
                    </div>
                  </Card.Body>
                </Card>

                <div className="d-grid gap-2">
                  <Button variant="primary" type="submit" disabled={creatingFD}>
                    {creatingFD ? 'Creating...' : 'Create Fixed Deposit'}
                  </Button>
                  <Button variant="secondary" onClick={() => setShowCreateModal(false)} disabled={creatingFD}>
                    Cancel
                  </Button>
                </div>
              </Form>
            </Modal.Body>
          </Modal>

          {/* Growth Tracking Modal */}
          <Modal show={showGrowthModal} onHide={() => setShowGrowthModal(false)} size="lg" centered>
            <Modal.Header closeButton>
              <Modal.Title>FD Growth Tracker - FD #{selectedFD?.fdId}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {loadingGrowth ? (
                <div className="text-center py-5">
                  <Spinner animation="border" />
                  <p className="mt-3">Loading growth data...</p>
                </div>
              ) : growthData ? (
                <>
                  <Row className="mb-4">
                    <Col md={4}>
                      <Card className="text-center">
                        <Card.Body>
                          <small className="text-muted">Starting Value</small>
                          <h5 className="mb-0">${parseFloat(growthData.principalAmount).toFixed(2)}</h5>
                        </Card.Body>
                      </Card>
                    </Col>
                    <Col md={4}>
                      <Card className="text-center">
                        <Card.Body>
                          <small className="text-muted">Current Value</small>
                          <h5 className="mb-0 text-success">${parseFloat(growthData.currentValue).toFixed(2)}</h5>
                        </Card.Body>
                      </Card>
                    </Col>
                    <Col md={4}>
                      <Card className="text-center">
                        <Card.Body>
                          <small className="text-muted">Maturity Value</small>
                          <h5 className="mb-0 text-primary">${parseFloat(growthData.maturityAmount).toFixed(2)}</h5>
                        </Card.Body>
                      </Card>
                    </Col>
                  </Row>

                  <div style={{ height: '400px' }}>
                    <Line data={getChartData()} options={chartOptions} />
                  </div>

                  <Card className="mt-4 bg-light">
                    <Card.Body>
                      <Row>
                        <Col md={6}>
                          <small className="text-muted">Interest Rate:</small>
                          <div className="fw-bold">{(parseFloat(growthData.interestRate) * 100).toFixed(2)}% per annum</div>
                        </Col>
                        <Col md={6}>
                          <small className="text-muted">Tenure:</small>
                          <div className="fw-bold">{growthData.tenureYears} years</div>
                        </Col>
                      </Row>
                    </Card.Body>
                  </Card>
                </>
              ) : null}
            </Modal.Body>
          </Modal>

          {/* Close FD Confirmation Modal */}
          <Modal show={showCloseConfirm} onHide={() => setShowCloseConfirm(false)} centered>
            <Modal.Header closeButton>
              <Modal.Title>Confirm FD Closure</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {fdToClose && (
                <>
                  <p>Are you sure you want to close this Fixed Deposit?</p>
                  <Card className="bg-light">
                    <Card.Body>
                      <div className="d-flex justify-content-between mb-2">
                        <span>FD Number:</span>
                        <strong>#{fdToClose.fdId}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Principal Amount:</span>
                        <strong>${parseFloat(fdToClose.principalAmount).toFixed(2)}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Current Value:</span>
                        <strong className="text-success">${parseFloat(fdToClose.currentValue).toFixed(2)}</strong>
                      </div>
                      <div className="d-flex justify-content-between">
                        <span>Interest Earned:</span>
                        <strong className="text-primary">
                          ${(parseFloat(fdToClose.currentValue) - parseFloat(fdToClose.principalAmount)).toFixed(2)}
                        </strong>
                      </div>
                    </Card.Body>
                  </Card>
                  <p className="mt-3 text-muted">
                    The current value will be credited to your account ending with {fdToClose.accountNumber}.
                  </p>
                </>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={() => setShowCloseConfirm(false)} disabled={closingFD}>
                Cancel
              </Button>
              <Button variant="danger" onClick={handleCloseFD} disabled={closingFD}>
                {closingFD ? 'Closing...' : 'Confirm Closure'}
              </Button>
            </Modal.Footer>
          </Modal>
        </Container>
      </div>
    </div>
  );
};

export default FixedDeposits;