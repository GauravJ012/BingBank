import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { FaLock, FaEnvelope } from 'react-icons/fa';
import '../styles/Login.css';
import authService from '../services/authService';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [showOtpField, setShowOtpField] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  // Check if user is already logged in
  useEffect(() => {
    if (authService.isAuthenticated()) {
      navigate('/dashboard');
    }
  }, [navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      const response = await authService.login(email, password);
      
      if (response.data.otpRequired) {
        setShowOtpField(true);
      } else {
        // If no OTP required (uncommon for banking)
        // Store the token and user data
        localStorage.setItem('token', response.data.accessToken);
        localStorage.setItem('user', JSON.stringify({
          id: response.data.customerId,
          email: response.data.email,
          firstName: response.data.firstName,
          lastName: response.data.lastName
        }));
        
        // Redirect to dashboard
        navigate('/dashboard');
      }
    } catch (err) {
      setError(typeof err === 'string' ? err : 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      await authService.verifyOTP(email, otp);
      navigate('/dashboard');
    } catch (err) {
      setError(typeof err === 'string' ? err : 'Invalid OTP code');
    } finally {
      setLoading(false);
    }
  };

  // If already authenticated, don't show login form
  if (authService.isAuthenticated()) {
    return null;
  }

  return (
    <div className="login-container">
      <Container>
        <Row className="justify-content-center">
          <Col md={6} lg={5}>
            <div className="text-center mb-4">
              <h1 className="brand-name">BingBank</h1>
              <p className="brand-tagline">Banking Redefined</p>
            </div>
            
            <Card className="login-card">
              <Card.Body>
                <h2 className="text-center mb-4">
                  {showOtpField ? 'Verify OTP' : 'Login to Your Account'}
                </h2>
                
                {error && <Alert variant="danger">{error}</Alert>}
                
                {!showOtpField ? (
                  <Form onSubmit={handleLogin}>
                    <Form.Group className="mb-3">
                      <div className="input-group">
                        <div className="input-group-prepend">
                          <span className="input-group-text">
                            <FaEnvelope />
                          </span>
                        </div>
                        <Form.Control
                          type="email"
                          placeholder="Email Address"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          required
                        />
                      </div>
                    </Form.Group>
                    
                    <Form.Group className="mb-4">
                      <div className="input-group">
                        <div className="input-group-prepend">
                          <span className="input-group-text">
                            <FaLock />
                          </span>
                        </div>
                        <Form.Control
                          type="password"
                          placeholder="Password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          required
                        />
                      </div>
                    </Form.Group>
                    
                    <div className="mb-3 d-flex justify-content-between">
                      <Form.Check 
                        type="checkbox" 
                        label="Remember me" 
                        id="remember-me"
                      />
                      <a href="#forgot-password" className="forgot-password">
                        Forgot Password?
                      </a>
                    </div>
                    
                    <div className="d-grid gap-2">
                      <Button
                        variant="primary"
                        type="submit"
                        disabled={loading}
                        className="login-button"
                      >
                        {loading ? 'Processing...' : 'Login'}
                      </Button>
                    </div>
                  </Form>
                ) : (
                  <Form onSubmit={handleVerifyOTP}>
                    <Form.Group className="mb-4">
                      <p className="otp-info">
                        A One-Time Password (OTP) has been sent to your email address.
                        Please enter it below to complete the login.
                      </p>
                      <Form.Control
                        type="text"
                        placeholder="Enter 6-digit OTP"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value)}
                        required
                        maxLength={6}
                        className="otp-input text-center"
                      />
                    </Form.Group>
                    
                    <div className="d-grid gap-2">
                      <Button
                        variant="primary"
                        type="submit"
                        disabled={loading}
                        className="login-button"
                      >
                        {loading ? 'Verifying...' : 'Verify OTP'}
                      </Button>
                    </div>
                  </Form>
                )}
              </Card.Body>
            </Card>
            
            <div className="text-center mt-3">
              <p className="register-text">
                Don't have an account? <Link to="/register">Register</Link>
              </p>
            </div>
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default Login;