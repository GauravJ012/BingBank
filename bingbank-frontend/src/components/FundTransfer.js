import React, { useState, useEffect } from 'react';
import { Container, Card, Row, Col, Form, Button, Table, Badge, Modal, Spinner, Alert } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import authService from '../services/authService';
import accountService from '../services/accountService';
import fundTransferService from '../services/fundTransferService';
import '../styles/FundTransfer.css';

const FundTransfer = () => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [transferHistory, setTransferHistory] = useState([]);

  // Transfer form state
  const [transferForm, setTransferForm] = useState({
    targetAccountNumber: '',
    amount: '',
    remarks: ''
  });

  // UI states
  const [submitting, setSubmitting] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [transferResult, setTransferResult] = useState(null);
  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    initializeFundTransfer();
  }, [navigate]);

  const initializeFundTransfer = async () => {
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

        // Get transfer history
        const history = await fundTransferService.getTransferHistory(userData.id);
        setTransferHistory(history);
      } else {
        setError("No account found");
      }

      setLoading(false);
    } catch (err) {
      console.error("Error loading fund transfer page:", err);
      setError("Failed to load page data");
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setTransferForm(prev => ({
      ...prev,
      [name]: value
    }));
    setValidationError('');
  };

  const validateTransfer = () => {
    if (!transferForm.targetAccountNumber) {
      setValidationError('Please enter target account number');
      return false;
    }

    if (transferForm.targetAccountNumber === account.accountNumber) {
      setValidationError('Cannot transfer to the same account');
      return false;
    }

    const amount = parseFloat(transferForm.amount);
    if (!amount || amount <= 0) {
      setValidationError('Please enter a valid amount');
      return false;
    }

    if (amount < 1) {
      setValidationError('Minimum transfer amount is $1');
      return false;
    }

    if (amount > parseFloat(account.balance)) {
      setValidationError('Insufficient balance');
      return false;
    }

    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!validateTransfer()) {
      return;
    }

    setShowConfirmModal(true);
  };

  const confirmTransfer = async () => {
    setShowConfirmModal(false);
    setSubmitting(true);

    try {
      const transferRequest = {
        customerId: customer.id,
        sourceAccountNumber: account.accountNumber,
        targetAccountNumber: transferForm.targetAccountNumber,
        amount: parseFloat(transferForm.amount),
        remarks: transferForm.remarks || 'Fund Transfer'
      };

      const result = await fundTransferService.initiateTransfer(transferRequest);
      
      setTransferResult(result);
      setShowSuccessModal(true);
      
      // Reset form
      setTransferForm({
        targetAccountNumber: '',
        amount: '',
        remarks: ''
      });

      // Refresh data
      await initializeFundTransfer();
      
      setSubmitting(false);
    } catch (err) {
      console.error("Error initiating transfer:", err);
      const errorMessage = err.response?.data?.error || err.message || "Failed to initiate transfer";
      setValidationError(errorMessage);
      setSubmitting(false);
    }
  };

  const getStatusBadge = (status) => {
    const variants = {
      'PENDING': 'warning',
      'PROCESSING': 'info',
      'COMPLETED': 'success',
      'FAILED': 'danger'
    };
    return <Badge bg={variants[status] || 'secondary'}>{status}</Badge>;
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
        <div className="ms-3">Loading...</div>
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
            <h1>Fund Transfer</h1>
          </div>

          {/* Account Info Card */}
          {account && (
            <Card className="mb-4">
              <Card.Body>
                <Row>
                  <Col md={4}>
                    <strong>Your Account Number:</strong> {account.accountNumber}
                  </Col>
                  <Col md={4}>
                    <strong>Account Type:</strong> {account.accountType}
                  </Col>
                  <Col md={4}>
                    <strong>Available Balance:</strong> 
                    <span className="text-success fw-bold ms-2">
                      ${Number(account.balance).toFixed(2)}
                    </span>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
          )}

          <Row>
            {/* Transfer Form */}
            <Col lg={6}>
              <Card className="transfer-form-card mb-4">
                <Card.Header>
                  <h5>Transfer Funds</h5>
                </Card.Header>
                <Card.Body>
                  {validationError && (
                    <Alert variant="danger" onClose={() => setValidationError('')} dismissible>
                      {validationError}
                    </Alert>
                  )}

                  <Form onSubmit={handleSubmit}>
                    <Form.Group className="mb-3">
                      <Form.Label>From Account</Form.Label>
                      <Form.Control
                        type="text"
                        value={account ? account.accountNumber : ''}
                        disabled
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>To Account Number *</Form.Label>
                      <Form.Control
                        type="text"
                        name="targetAccountNumber"
                        placeholder="Enter target account number"
                        value={transferForm.targetAccountNumber}
                        onChange={handleFormChange}
                        required
                      />
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>Amount ($) *</Form.Label>
                      <Form.Control
                        type="number"
                        name="amount"
                        placeholder="Enter amount"
                        value={transferForm.amount}
                        onChange={handleFormChange}
                        min="1"
                        step="0.01"
                        max={account ? account.balance : 0}
                        required
                      />
                      <Form.Text className="text-muted">
                        Available Balance: ${account ? parseFloat(account.balance).toFixed(2) : '0.00'}
                      </Form.Text>
                    </Form.Group>

                    <Form.Group className="mb-3">
                      <Form.Label>Remarks (Optional)</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={2}
                        name="remarks"
                        placeholder="Enter remarks (optional)"
                        value={transferForm.remarks}
                        onChange={handleFormChange}
                        maxLength={255}
                      />
                    </Form.Group>

                    <div className="d-grid">
                      <Button 
                        variant="primary" 
                        type="submit" 
                        size="lg"
                        disabled={submitting}
                      >
                        {submitting ? 'Processing...' : 'Transfer Funds'}
                      </Button>
                    </div>
                  </Form>
                </Card.Body>
              </Card>
            </Col>

            {/* Transfer Info */}
            <Col lg={6}>
              <Card className="info-card mb-4">
                <Card.Header>
                  <h5>Transfer Information</h5>
                </Card.Header>
                <Card.Body>
                  <h6 className="mb-3">Important Notes:</h6>
                  <ul className="transfer-info-list">
                    <li>Minimum transfer amount is $1.00</li>
                    <li>Transfers are processed instantly via Kafka messaging</li>
                    <li>You cannot transfer to your own account</li>
                    <li>Ensure you have sufficient balance before transferring</li>
                    <li>Transfer history is available below</li>
                    <li>Both accounts will receive transaction records</li>
                  </ul>

                  <div className="mt-4 p-3 bg-light rounded">
                    <h6>Transfer Status Guide:</h6>
                    <div className="mb-2">
                      <Badge bg="warning" className="me-2">PENDING</Badge>
                      Transfer request received
                    </div>
                    <div className="mb-2">
                      <Badge bg="info" className="me-2">PROCESSING</Badge>
                      Transfer is being processed
                    </div>
                    <div className="mb-2">
                      <Badge bg="success" className="me-2">COMPLETED</Badge>
                      Transfer completed successfully
                    </div>
                    <div>
                      <Badge bg="danger" className="me-2">FAILED</Badge>
                      Transfer failed (amount refunded)
                    </div>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          {/* Transfer History */}
          <Card className="history-card">
            <Card.Header>
              <h5>Transfer History</h5>
            </Card.Header>
            <Card.Body>
              {transferHistory.length > 0 ? (
                <Table responsive hover>
                  <thead>
                    <tr>
                      <th>Transfer ID</th>
                      <th>Date & Time</th>
                      <th>From Account</th>
                      <th>To Account</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Remarks</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transferHistory.map(transfer => (
                      <tr key={transfer.transferId}>
                        <td>#{transfer.transferId}</td>
                        <td>
                          {new Date(transfer.transferDate).toLocaleString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </td>
                        <td>{transfer.sourceAccountNumber}</td>
                        <td>{transfer.targetAccountNumber}</td>
                        <td className="fw-bold">${parseFloat(transfer.amount).toFixed(2)}</td>
                        <td>{getStatusBadge(transfer.status)}</td>
                        <td>{transfer.remarks || '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              ) : (
                <div className="text-center py-5">
                  <h5>No Transfer History</h5>
                  <p className="text-muted">Your transfer history will appear here</p>
                </div>
              )}
            </Card.Body>
          </Card>

          {/* Confirm Transfer Modal */}
          <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)} centered>
            <Modal.Header closeButton>
              <Modal.Title>Confirm Transfer</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              <p>Please confirm the following transfer details:</p>
              <Card className="bg-light">
                <Card.Body>
                  <div className="d-flex justify-content-between mb-2">
                    <span>From Account:</span>
                    <strong>{account?.accountNumber}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>To Account:</span>
                    <strong>{transferForm.targetAccountNumber}</strong>
                  </div>
                  <div className="d-flex justify-content-between mb-2">
                    <span>Amount:</span>
                    <strong className="text-success">${parseFloat(transferForm.amount || 0).toFixed(2)}</strong>
                  </div>
                  {transferForm.remarks && (
                    <div className="d-flex justify-content-between">
                      <span>Remarks:</span>
                      <strong>{transferForm.remarks}</strong>
                    </div>
                  )}
                </Card.Body>
              </Card>
              <p className="mt-3 mb-0 text-muted">
                <small>This action cannot be undone. Please verify all details before confirming.</small>
              </p>
            </Modal.Body>
            <Modal.Footer>
              <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
                Cancel
              </Button>
              <Button variant="primary" onClick={confirmTransfer}>
                Confirm Transfer
              </Button>
            </Modal.Footer>
          </Modal>

          {/* Success Modal */}
          <Modal 
            show={showSuccessModal} 
            onHide={() => {
              setShowSuccessModal(false);
              setTransferResult(null);
            }} 
            centered
          >
            <Modal.Header closeButton className="bg-success text-white">
              <Modal.Title>✓ Transfer Initiated</Modal.Title>
            </Modal.Header>
            <Modal.Body>
              {transferResult && (
                <>
                  <div className="text-center mb-3">
                    <div className="success-icon mb-3">
                      <span style={{ fontSize: '48px', color: '#28a745' }}>✓</span>
                    </div>
                    <h5>{transferResult.message}</h5>
                  </div>
                  <Card className="bg-light">
                    <Card.Body>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Transfer ID:</span>
                        <strong>#{transferResult.transferId}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>Amount:</span>
                        <strong>${parseFloat(transferResult.amount).toFixed(2)}</strong>
                      </div>
                      <div className="d-flex justify-content-between mb-2">
                        <span>To Account:</span>
                        <strong>{transferResult.targetAccountNumber}</strong>
                      </div>
                      <div className="d-flex justify-content-between">
                        <span>Status:</span>
                        {getStatusBadge(transferResult.status)}
                      </div>
                    </Card.Body>
                  </Card>
                  <p className="mt-3 mb-0 text-muted text-center">
                    <small>Your transfer is being processed and will be completed shortly.</small>
                  </p>
                </>
              )}
            </Modal.Body>
            <Modal.Footer>
              <Button 
                variant="success" 
                onClick={() => {
                  setShowSuccessModal(false);
                  setTransferResult(null);
                }}
              >
                Done
              </Button>
            </Modal.Footer>
          </Modal>
        </Container>
      </div>
    </div>
  );
};

export default FundTransfer;