import React, { useState } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Spinner } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import '../styles/ForgotPassword.css';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1: Email, 2: OTP, 3: New Password
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Step 1: Request OTP
  const handleRequestOTP = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!email) {
      setError('Please enter your email address');
      return;
    }

    try {
      setLoading(true);
      await authService.requestPasswordReset(email);
      setSuccess('OTP has been sent to your email address');
      setStep(2);
      setLoading(false);
    } catch (err) {
      console.error('Error requesting password reset:', err);
      setError(err.response?.data?.message || err.message || 'Failed to send OTP. Please try again.');
      setLoading(false);
    }
  };

  // Step 2: Verify OTP
  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!otp || otp.length !== 6) {
      setError('Please enter a valid 6-digit OTP');
      return;
    }

    try {
      setLoading(true);
      await authService.verifyPasswordResetOTP(email, otp);
      setSuccess('OTP verified successfully. Please enter your new password.');
      setStep(3);
      setLoading(false);
    } catch (err) {
      console.error('Error verifying OTP:', err);
      setError(err.response?.data?.message || err.message || 'Invalid OTP. Please try again.');
      setLoading(false);
    }
  };

  // Step 3: Reset Password
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!newPassword || newPassword.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    try {
      setLoading(true);
      await authService.resetPassword(email, otp, newPassword);
      setSuccess('Password reset successfully! Redirecting to login...');
      setLoading(false);
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      console.error('Error resetting password:', err);
      setError(err.response?.data?.message || err.message || 'Failed to reset password. Please try again.');
      setLoading(false);
    }
  };

  return (
    <Container className="forgot-password-container">
      <Row className="justify-content-center">
        <Col md={6} lg={5}>
          <div className="text-center mb-4">
            <h1 className="brand-name">BingBank</h1>
            <p className="brand-tagline">Reset Your Password</p>
          </div>

          <Card className="forgot-password-card">
            <Card.Body>
              <h2 className="text-center mb-4">
                {step === 1 && 'Forgot Password'}
                {step === 2 && 'Verify OTP'}
                {step === 3 && 'Set New Password'}
              </h2>

              {error && <Alert variant="danger" dismissible onClose={() => setError('')}>{error}</Alert>}
              {success && <Alert variant="success">{success}</Alert>}

              {/* Step 1: Enter Email */}
              {step === 1 && (
                <Form onSubmit={handleRequestOTP}>
                  <p className="text-center text-muted mb-4">
                    Enter your email address and we'll send you an OTP to reset your password.
                  </p>

                  <Form.Group className="mb-3">
                    <Form.Label>Email Address</Form.Label>
                    <Form.Control
                      type="email"
                      placeholder="Enter your email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                    />
                  </Form.Group>

                  <div className="d-grid gap-2">
                    <Button variant="primary" type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          Sending OTP...
                        </>
                      ) : (
                        'Send OTP'
                      )}
                    </Button>
                    <Link to="/login">
                      <Button variant="outline-secondary" className="w-100">
                        Back to Login
                      </Button>
                    </Link>
                  </div>
                </Form>
              )}

              {/* Step 2: Verify OTP */}
              {step === 2 && (
                <Form onSubmit={handleVerifyOTP}>
                  <p className="text-center mb-4">
                    We've sent a 6-digit OTP to <strong>{email}</strong>
                  </p>

                  <Form.Group className="mb-3">
                    <Form.Label>Enter OTP</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder="Enter 6-digit OTP"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                      maxLength="6"
                      className="text-center"
                      required
                    />
                    <Form.Text className="text-muted">
                      Please check your email for the verification code
                    </Form.Text>
                  </Form.Group>

                  <div className="d-grid gap-2">
                    <Button variant="primary" type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          Verifying...
                        </>
                      ) : (
                        'Verify OTP'
                      )}
                    </Button>
                    <Button 
                      variant="outline-secondary" 
                      onClick={() => {
                        setStep(1);
                        setOtp('');
                        setError('');
                        setSuccess('');
                      }}
                    >
                      Back
                    </Button>
                  </div>
                </Form>
              )}

              {/* Step 3: Set New Password */}
              {step === 3 && (
                <Form onSubmit={handleResetPassword}>
                  <p className="text-center text-muted mb-4">
                    Enter your new password below
                  </p>

                  <Form.Group className="mb-3">
                    <Form.Label>New Password</Form.Label>
                    <Form.Control
                      type="password"
                      placeholder="Enter new password (min 6 characters)"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-3">
                    <Form.Label>Confirm Password</Form.Label>
                    <Form.Control
                      type="password"
                      placeholder="Re-enter new password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                    />
                  </Form.Group>

                  <div className="d-grid gap-2">
                    <Button variant="primary" type="submit" disabled={loading}>
                      {loading ? (
                        <>
                          <Spinner animation="border" size="sm" className="me-2" />
                          Resetting Password...
                        </>
                      ) : (
                        'Reset Password'
                      )}
                    </Button>
                  </div>
                </Form>
              )}
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default ForgotPassword;