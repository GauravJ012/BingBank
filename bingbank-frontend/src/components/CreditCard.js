import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Modal, Form, Alert, Spinner, Table, Badge, ProgressBar } from 'react-bootstrap';
import cardsService from '../services/cardsService';
import accountService from '../services/accountService';
import logo from '../assets/logo.png';

const CreditCard = ({ customerId }) => {
  const [creditCard, setCreditCard] = useState(null);
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [filteredTransactions, setFilteredTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [transactionsLoading, setTransactionsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Modal states
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);
  const [showFilterModal, setShowFilterModal] = useState(false);

  // Form states
  const [paymentAmount, setPaymentAmount] = useState('');
  const [validationError, setValidationError] = useState('');
  const [processing, setProcessing] = useState(false);

  // Filter states
  const [filters, setFilters] = useState({
    startDate: '',
    endDate: '',
    minAmount: '',
    maxAmount: '',
    category: '',
    merchantName: '',
    transactionType: ''
  });

  useEffect(() => {
    loadCreditCard();
  }, [customerId]);

  const loadCreditCard = async () => {
    try {
      setLoading(true);
      
      // Load credit card
      const card = await cardsService.getCreditCard(customerId);
      setCreditCard(card);

      // Load account details
      const accounts = await accountService.getAccountsByCustomerId(customerId);
      if (accounts && accounts.length > 0) {
        setAccount(accounts[0]);
      }

      // Load transactions
      const txns = await cardsService.getCreditCardTransactions(card.cardId);
      setTransactions(txns);
      setFilteredTransactions(txns);
      
      setLoading(false);
    } catch (err) {
      console.error("Error loading credit card:", err);
      setError("Failed to load credit card");
      setLoading(false);
    }
  };

  const handlePaymentClick = () => {
    setPaymentAmount('');
    setValidationError('');
    setShowPaymentModal(true);
  };

  const handlePaymentSubmit = async (e) => {
    e.preventDefault();
    setValidationError('');

    const amount = parseFloat(paymentAmount);

    if (amount <= 0) {
      setValidationError('Payment amount must be greater than zero');
      return;
    }

    if (amount > parseFloat(creditCard.outstandingBalance)) {
      setValidationError('Payment amount cannot exceed outstanding balance');
      return;
    }

    if (amount > parseFloat(account.balance)) {
      setValidationError('Insufficient balance in your account');
      return;
    }

    try {
      setProcessing(true);
      
      const request = {
        customerId: customerId,
        cardId: creditCard.cardId,
        accountNumber: account.accountNumber,
        amount: amount
      };

      await cardsService.payCreditCardBill(request);
      
      setShowPaymentModal(false);
      setPaymentAmount('');
      setProcessing(false);
      
      await loadCreditCard();
      
      alert('Payment processed successfully!');
    } catch (err) {
      console.error("Error processing payment:", err);
      setValidationError(err.response?.data?.error || "Failed to process payment");
      setProcessing(false);
    }
  };

  const handleDeactivateConfirm = async () => {
    if (parseFloat(creditCard.outstandingBalance) > 0) {
      setValidationError('Cannot deactivate card with outstanding balance');
      return;
    }

    try {
      setProcessing(true);
      
      const request = {
        customerId: customerId,
        cardId: creditCard.cardId,
        cardType: 'CREDIT'
      };

      await cardsService.deactivateCard(request);
      
      setShowDeactivateModal(false);
      setProcessing(false);
      
      await loadCreditCard();
      
      alert('Credit card deactivated successfully!');
    } catch (err) {
      console.error("Error deactivating card:", err);
      setValidationError(err.response?.data?.error || "Failed to deactivate card");
      setProcessing(false);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleApplyFilters = async () => {
    try {
      setTransactionsLoading(true);
      setShowFilterModal(false);

      const filterRequest = {
        startDate: filters.startDate || null,
        endDate: filters.endDate || null,
        minAmount: filters.minAmount ? parseFloat(filters.minAmount) : null,
        maxAmount: filters.maxAmount ? parseFloat(filters.maxAmount) : null,
        category: filters.category || null,
        merchantName: filters.merchantName || null,
        transactionType: filters.transactionType || null,
        sortBy: 'date',
        sortDirection: 'desc'
      };

      const filtered = await cardsService.getFilteredTransactions(creditCard.cardId, filterRequest);
      setFilteredTransactions(filtered);
      setTransactionsLoading(false);
    } catch (err) {
      console.error("Error filtering transactions:", err);
      setTransactionsLoading(false);
    }
  };

  const handleResetFilters = async () => {
    setFilters({
      startDate: '',
      endDate: '',
      minAmount: '',
      maxAmount: '',
      category: '',
      merchantName: '',
      transactionType: ''
    });
    setFilteredTransactions(transactions);
    setShowFilterModal(false);
  };

  const handleDownloadStatement = async () => {
    try {
      // Download November 2025 statement
      await cardsService.downloadStatement(creditCard.cardId, 2025, 11);
      alert('Statement downloaded successfully!');
    } catch (err) {
      console.error("Error downloading statement:", err);
      alert('Failed to download statement');
    }
  };

  const getCreditUtilization = () => {
    if (!creditCard) return 0;
    return ((parseFloat(creditCard.usedCredit) / parseFloat(creditCard.creditLimit)) * 100).toFixed(1);
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
        <p className="mt-3">Loading credit card...</p>
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="danger">
        {error}
      </Alert>
    );
  }

  if (!creditCard) {
    return (
      <Alert variant="info">
        No credit card found for your account.
      </Alert>
    );
  }

  return (
    <div className="credit-card-container">
      <Row>
        <Col lg={6}>
          {/* Beautiful Card Design */}
          <div className="card-3d-wrapper">
            <div className={`card-3d credit-card-design ${creditCard.cardStatus === 'INACTIVE' ? 'inactive-card' : ''}`}>
              <div className="card-front">
                <div className="card-bg credit-gradient"></div>
                <div className="card-content">
                  <div className="card-header-row">
                    <img src={logo} alt="BingBank" className="card-logo" />
                    <div className="card-chip"></div>
                  </div>
                  
                  <div className="card-number">
                    {creditCard.cardNumber.match(/.{1,4}/g).join(' ')}
                  </div>
                  
                  <div className="card-details-row">
                    <div className="card-detail">
                      <div className="card-label">CARDHOLDER NAME</div>
                      <div className="card-value">{creditCard.cardholderName}</div>
                    </div>
                    <div className="card-detail">
                      <div className="card-label">EXPIRES</div>
                      <div className="card-value">{creditCard.expiryMonth}/{creditCard.expiryYear}</div>
                    </div>
                    <div className="card-detail">
                      <div className="card-label">CVV</div>
                      <div className="card-value">{creditCard.cvv}</div>
                    </div>
                  </div>
                  
                  <div className="card-type">CREDIT CARD</div>
                </div>
              </div>
            </div>
          </div>

          {creditCard.cardStatus === 'INACTIVE' && (
            <Alert variant="warning" className="mt-3">
              This card is currently inactive.
            </Alert>
          )}
        </Col>

        <Col lg={6}>
          <Card>
            <Card.Header>
              <h5>Credit Card Summary</h5>
            </Card.Header>
            <Card.Body>
              <div className="credit-summary mb-3">
                <Row>
                  <Col xs={6}>
                    <div className="summary-item">
                      <small className="text-muted">Credit Limit</small>
                      <h5 className="mb-0">${parseFloat(creditCard.creditLimit).toFixed(2)}</h5>
                    </div>
                  </Col>
                  <Col xs={6}>
                    <div className="summary-item">
                      <small className="text-muted">Available Credit</small>
                      <h5 className="mb-0 text-success">${parseFloat(creditCard.availableCredit).toFixed(2)}</h5>
                    </div>
                  </Col>
                </Row>
              </div>

              <div className="credit-usage mb-3">
                <div className="d-flex justify-content-between mb-1">
                  <small>Credit Used</small>
                  <small>{getCreditUtilization()}%</small>
                </div>
                <ProgressBar 
                  now={getCreditUtilization()} 
                  variant={getCreditUtilization() > 70 ? 'danger' : 'success'}
                />
                <small className="text-muted">
                  ${parseFloat(creditCard.usedCredit).toFixed(2)} of ${parseFloat(creditCard.creditLimit).toFixed(2)}
                </small>
              </div>

              <div className="payment-info mb-3">
                <div className="info-row">
                  <span>Outstanding Balance:</span>
                  <strong className="text-danger">${parseFloat(creditCard.outstandingBalance).toFixed(2)}</strong>
                </div>
                <div className="info-row">
                  <span>Payment Due Date:</span>
                  <strong>
                    {new Date(creditCard.paymentDueDate).toLocaleDateString('en-US', { 
                      month: 'short', 
                      day: 'numeric', 
                      year: 'numeric' 
                    })}
                  </strong>
                </div>
                <div className="info-row">
                  <span>Days Until Due:</span>
                  <strong className={creditCard.daysUntilDue <= 7 ? 'text-danger' : ''}>
                    {creditCard.daysUntilDue} days
                  </strong>
                </div>
              </div>

              <div className="d-grid gap-2">
                <Button 
                  variant="success" 
                  onClick={handlePaymentClick}
                  disabled={creditCard.cardStatus !== 'ACTIVE' || parseFloat(creditCard.outstandingBalance) === 0}
                >
                  Pay Bill
                </Button>
                <Button 
                  variant="outline-danger"
                  onClick={() => setShowDeactivateModal(true)}
                  disabled={creditCard.cardStatus !== 'ACTIVE'}
                >
                  Deactivate Card
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Transactions Section */}
      <Card className="mt-4">
        <Card.Header className="d-flex justify-content-between align-items-center">
          <h5 className="mb-0">Credit Card Transactions</h5>
          <div>
            <Button 
              variant="outline-primary" 
              size="sm" 
              className="me-2"
              onClick={() => setShowFilterModal(true)}
            >
              Filter
            </Button>
            <Button 
              variant="outline-success" 
              size="sm"
              onClick={handleDownloadStatement}
            >
              Download November Statement
            </Button>
          </div>
        </Card.Header>
        <Card.Body>
          {transactionsLoading ? (
            <div className="text-center py-3">
              <Spinner animation="border" size="sm" />
              <span className="ms-2">Loading transactions...</span>
            </div>
          ) : filteredTransactions.length > 0 ? (
            <div className="table-responsive">
              <Table hover>
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Merchant</th>
                    <th>Category</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredTransactions.map(txn => (
                    <tr key={txn.transactionId}>
                      <td>{new Date(txn.transactionDate).toLocaleDateString('en-US')}</td>
                      <td>{txn.transactionTime}</td>
                      <td>{txn.merchantName}</td>
                      <td>{txn.category || '-'}</td>
                      <td>
                        <Badge bg={txn.transactionType === 'PURCHASE' ? 'danger' : 'success'}>
                          {txn.transactionType}
                        </Badge>
                      </td>
                      <td className={txn.transactionType === 'PURCHASE' ? 'text-danger' : 'text-success'}>
                        ${parseFloat(txn.amount).toFixed(2)}
                      </td>
                      <td>
                        <Badge bg="success">{txn.status}</Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          ) : (
            <div className="text-center py-5">
              <p>No transactions found</p>
              <Button variant="outline-primary" size="sm" onClick={handleResetFilters}>
                Reset Filters
              </Button>
            </div>
          )}
        </Card.Body>
      </Card>

      {/* Pay Bill Modal */}
      <Modal show={showPaymentModal} onHide={() => setShowPaymentModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Pay Credit Card Bill</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {validationError && (
            <Alert variant="danger" dismissible onClose={() => setValidationError('')}>
              {validationError}
            </Alert>
          )}
          
          <Card className="bg-light mb-3">
            <Card.Body>
              <div className="d-flex justify-content-between mb-2">
                <span>Outstanding Balance:</span>
                <strong className="text-danger">${parseFloat(creditCard.outstandingBalance).toFixed(2)}</strong>
              </div>
              <div className="d-flex justify-content-between">
                <span>Available in Account:</span>
                <strong className="text-success">${account ? parseFloat(account.balance).toFixed(2) : '0.00'}</strong>
              </div>
            </Card.Body>
          </Card>

          <Form onSubmit={handlePaymentSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Payment Amount ($)</Form.Label>
              <Form.Control
                type="number"
                placeholder="Enter amount"
                value={paymentAmount}
                onChange={(e) => setPaymentAmount(e.target.value)}
                min="0.01"
                step="0.01"
                max={creditCard.outstandingBalance}
                required
              />
              <Form.Text className="text-muted">
                Maximum: ${parseFloat(creditCard.outstandingBalance).toFixed(2)}
              </Form.Text>
            </Form.Group>

            <div className="d-grid gap-2">
              <Button 
                variant="primary" 
                onClick={() => setPaymentAmount(creditCard.outstandingBalance)}
                type="button"
              >
                Pay Full Amount
              </Button>
              <Button variant="success" type="submit" disabled={processing}>
                {processing ? 'Processing...' : 'Pay Now'}
              </Button>
              <Button variant="secondary" onClick={() => setShowPaymentModal(false)}>
                Cancel
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>

      {/* Deactivate Modal */}
      <Modal show={showDeactivateModal} onHide={() => setShowDeactivateModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Confirm Deactivation</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {validationError && (
            <Alert variant="danger">
              {validationError}
            </Alert>
          )}
          <p>Are you sure you want to deactivate this credit card?</p>
          <Alert variant="warning">
            <strong>Warning:</strong> You can only deactivate a card with zero outstanding balance.
          </Alert>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowDeactivateModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleDeactivateConfirm} disabled={processing}>
            {processing ? 'Processing...' : 'Confirm Deactivation'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Filter Modal */}
      <Modal show={showFilterModal} onHide={() => setShowFilterModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Filter Transactions</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Start Date</Form.Label>
                  <Form.Control
                    type="date"
                    name="startDate"
                    value={filters.startDate}
                    onChange={handleFilterChange}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>End Date</Form.Label>
                  <Form.Control
                    type="date"
                    name="endDate"
                    value={filters.endDate}
                    onChange={handleFilterChange}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Min Amount ($)</Form.Label>
                  <Form.Control
                    type="number"
                    name="minAmount"
                    value={filters.minAmount}
                    onChange={handleFilterChange}
                    step="0.01"
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Max Amount ($)</Form.Label>
                  <Form.Control
                    type="number"
                    name="maxAmount"
                    value={filters.maxAmount}
                    onChange={handleFilterChange}
                    step="0.01"
                  />
                </Form.Group>
              </Col>
            </Row>

            <Form.Group className="mb-3">
              <Form.Label>Merchant Name</Form.Label>
              <Form.Control
                type="text"
                name="merchantName"
                placeholder="Search merchant"
                value={filters.merchantName}
                onChange={handleFilterChange}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Category</Form.Label>
              <Form.Select
                name="category"
                value={filters.category}
                onChange={handleFilterChange}
              >
                <option value="">All Categories</option>
                <option value="Shopping">Shopping</option>
                <option value="Food & Dining">Food & Dining</option>
                <option value="Fuel">Fuel</option>
                <option value="Groceries">Groceries</option>
                <option value="Entertainment">Entertainment</option>
                <option value="Electronics">Electronics</option>
                <option value="Transportation">Transportation</option>
                <option value="Payment">Payment</option>
              </Form.Select>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Transaction Type</Form.Label>
              <Form.Select
                name="transactionType"
                value={filters.transactionType}
                onChange={handleFilterChange}
              >
                <option value="">All Types</option>
                <option value="PURCHASE">Purchase</option>
                <option value="PAYMENT">Payment</option>
                <option value="REFUND">Refund</option>
              </Form.Select>
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleResetFilters}>
            Reset
          </Button>
          <Button variant="primary" onClick={handleApplyFilters}>
            Apply Filters
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default CreditCard;