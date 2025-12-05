import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Button, Modal, Form, Alert, Spinner } from 'react-bootstrap';
import cardsService from '../services/cardsService';
import accountService from '../services/accountService';
import logo from '../assets/logo.png';

const DebitCard = ({ customerId }) => {
  const [debitCard, setDebitCard] = useState(null);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notFound, setNotFound] = useState(false);

  // Modal states
  const [showPinModal, setShowPinModal] = useState(false);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);

  // Form states
  const [newPin, setNewPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');

  // Loading states
  const [processing, setProcessing] = useState(false);
  const [validationError, setValidationError] = useState('');

  useEffect(() => {
    loadDebitCard();
    loadAccount();
  }, [customerId]);

  const loadAccount = async () => {
    try {
      const accounts = await accountService.getAccountsByCustomerId(customerId);
      if (accounts && accounts.length > 0) {
        setAccount(accounts[0]);
      }
    } catch (err) {
      console.error("Error loading account:", err);
    }
  };

  const loadDebitCard = async () => {
    try {
      setLoading(true);
      setError(null);
      setNotFound(false);
      
      const response = await cardsService.getDebitCard(customerId);
      
      // Check if card was found
      if (response && response.found === false) {
        setNotFound(true);
        setDebitCard(null);
      } else if (response) {
        setDebitCard(response);
        setNotFound(false);
      }
      
      setLoading(false);
    } catch (err) {
      console.error("Error loading debit card:", err);
      setError("Failed to load debit card");
      setLoading(false);
    }
  };

  const handleChangePinClick = () => {
    setNewPin('');
    setConfirmPin('');
    setValidationError('');
    setShowPinModal(true);
  };

  const handleChangePinSubmit = async (e) => {
    e.preventDefault();
    setValidationError('');

    // Validate PIN
    if (newPin.length !== 4 || !/^\d+$/.test(newPin)) {
      setValidationError('PIN must be exactly 4 digits');
      return;
    }

    if (newPin !== confirmPin) {
      setValidationError('PINs do not match');
      return;
    }

    // Execute change PIN directly without OTP
    await executeChangePin(newPin, null);
  };

  const executeChangePin = async (pin, otpValue) => {
    try {
      setProcessing(true);
      const request = {
        customerId: customerId,
        cardId: debitCard.cardId,
        newPin: pin,
        otp: otpValue
      };

      await cardsService.changeAtmPin(request);
      
      setShowPinModal(false);
      setNewPin('');
      setConfirmPin('');
      setProcessing(false);
      
      alert('ATM PIN changed successfully!');
    } catch (err) {
      console.error("Error changing PIN:", err);
      setValidationError(err.response?.data?.error || "Failed to change PIN");
      setProcessing(false);
    }
  };

  const handleDeactivateClick = () => {
    setValidationError('');
    setShowDeactivateModal(true);
  };

  const handleDeactivateConfirm = async () => {
    // Execute deactivate directly without OTP
    await executeDeactivate(null);
  };

  const executeDeactivate = async (otpValue) => {
    try {
      setProcessing(true);
      const request = {
        customerId: customerId,
        cardId: debitCard.cardId,
        cardType: 'DEBIT',
        otp: otpValue
      };

      await cardsService.deactivateCard(request);
      
      setShowDeactivateModal(false);
      setProcessing(false);
      
      await loadDebitCard();
      
      alert('Debit card deactivated successfully!');
    } catch (err) {
      console.error("Error deactivating card:", err);
      setValidationError(err.response?.data?.error || "Failed to deactivate card");
      setProcessing(false);
    }
  };

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
        <p className="mt-3">Loading debit card...</p>
      </div>
    );
  }

  if (notFound) {
    return (
      <div className="text-center py-5">
        <Alert variant="info">
          <Alert.Heading>No Debit Card Found</Alert.Heading>
          <p className="mb-0">
            No debit card on file for Account Number: <strong>{account ? account.accountNumber : 'N/A'}</strong>
          </p>
          <hr />
          <p className="mb-0 text-muted">
            Please contact customer support to apply for a debit card.
          </p>
        </Alert>
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

  if (!debitCard) {
    return (
      <Alert variant="info">
        No debit card found for your account.
      </Alert>
    );
  }

  return (
    <div className="debit-card-container">
      <Row>
        <Col lg={6}>
          {/* Beautiful Card Design */}
          <div className="card-3d-wrapper">
            <div className={`card-3d ${debitCard.cardStatus === 'INACTIVE' ? 'inactive-card' : ''}`}>
              <div className="card-front">
                <div className="card-bg"></div>
                <div className="card-content">
                  <div className="card-header-row">
                    <img src={logo} alt="BingBank" className="card-logo" />
                    <div className="card-chip"></div>
                  </div>
                  
                  <div className="card-number">
                    {debitCard.cardNumber.match(/.{1,4}/g).join(' ')}
                  </div>
                  
                  <div className="card-details-row">
                    <div className="card-detail">
                      <div className="card-label">CARDHOLDER NAME</div>
                      <div className="card-value">{debitCard.cardholderName}</div>
                    </div>
                    <div className="card-detail">
                      <div className="card-label">EXPIRES</div>
                      <div className="card-value">{debitCard.expiryMonth}/{debitCard.expiryYear}</div>
                    </div>
                    <div className="card-detail">
                      <div className="card-label">CVV</div>
                      <div className="card-value">{debitCard.cvv}</div>
                    </div>
                  </div>
                  
                  <div className="card-type">DEBIT CARD</div>
                </div>
              </div>
            </div>
          </div>

          {debitCard.cardStatus === 'INACTIVE' && (
            <Alert variant="warning" className="mt-3">
              This card is currently inactive.
            </Alert>
          )}
        </Col>

        <Col lg={6}>
          <Card>
            <Card.Header>
              <h5>Card Management</h5>
            </Card.Header>
            <Card.Body>
              <div className="card-info mb-4">
                <h6>Card Information</h6>
                <div className="info-row">
                  <span>Card Number:</span>
                  <strong>**** **** **** {debitCard.cardNumber.slice(-4)}</strong>
                </div>
                <div className="info-row">
                  <span>Account Number:</span>
                  <strong>{debitCard.accountNumber}</strong>
                </div>
                <div className="info-row">
                  <span>Status:</span>
                  <strong className={debitCard.cardStatus === 'ACTIVE' ? 'text-success' : 'text-danger'}>
                    {debitCard.cardStatus}
                  </strong>
                </div>
              </div>

              <h6>Card Actions</h6>
              <div className="d-grid gap-2">
                <Button 
                  variant="primary" 
                  onClick={handleChangePinClick}
                  disabled={debitCard.cardStatus !== 'ACTIVE'}
                >
                  Change ATM PIN
                </Button>
                <Button 
                  variant="outline-danger"
                  onClick={handleDeactivateClick}
                  disabled={debitCard.cardStatus !== 'ACTIVE'}
                >
                  Deactivate Card
                </Button>
              </div>

              <Alert variant="info" className="mt-3">
                <small>
                  <strong>Note:</strong> Debit card transactions appear in your account transactions.
                </small>
              </Alert>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Change PIN Modal */}
      <Modal show={showPinModal} onHide={() => setShowPinModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Change ATM PIN</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {validationError && (
            <Alert variant="danger" dismissible onClose={() => setValidationError('')}>
              {validationError}
            </Alert>
          )}
          <Form onSubmit={handleChangePinSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>New PIN</Form.Label>
              <Form.Control
                type="password"
                placeholder="Enter 4-digit PIN"
                value={newPin}
                onChange={(e) => setNewPin(e.target.value)}
                maxLength={4}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Confirm PIN</Form.Label>
              <Form.Control
                type="password"
                placeholder="Re-enter PIN"
                value={confirmPin}
                onChange={(e) => setConfirmPin(e.target.value)}
                maxLength={4}
                required
              />
            </Form.Group>

            <div className="d-grid gap-2">
              <Button variant="primary" type="submit" disabled={processing}>
                {processing ? 'Processing...' : 'Change PIN'}
              </Button>
              <Button variant="secondary" onClick={() => setShowPinModal(false)}>
                Cancel
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>

      {/* Deactivate Confirmation Modal */}
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
          <p>Are you sure you want to deactivate this debit card?</p>
          <Alert variant="warning">
            <strong>Warning:</strong> This action cannot be undone. You will need to contact customer service to get a new card.
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
    </div>
  );
};

export default DebitCard;