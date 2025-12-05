import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { Container, Row, Col, Card, Alert } from 'react-bootstrap';
import authService from '../services/authService';
import '../styles/Register.css';

const Register = () => {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showOtpField, setShowOtpField] = useState(false);
  const [registrationEmail, setRegistrationEmail] = useState('');
  const [otpValue, setOtpValue] = useState(''); // Separate state for OTP

  // Check if user is already logged in
  useEffect(() => {
    if (authService.isAuthenticated()) {
      navigate('/dashboard');
    }
  }, [navigate]);

  // FIXED: All fields now have empty string '' instead of undefined
  const initialValues = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    age: '',
    gender: '',
    address: '',
    mobile: '',
    twoFactorEnabled: true,
    accountNumber: ''
  };

  const validationSchema = Yup.object({
    firstName: Yup.string()
      .required('First name is required')
      .min(2, 'First name must be at least 2 characters'),
    lastName: Yup.string()
      .required('Last name is required')
      .min(2, 'Last name must be at least 2 characters'),
    email: Yup.string()
      .email('Invalid email format')
      .required('Email is required'),
    password: Yup.string()
      .required('Password is required')
      .min(6, 'Password must be at least 6 characters'),
    mobile: Yup.string()
      .matches(/^\+?[1-9]\d{1,14}$/, 'Invalid phone number'),
    age: Yup.number()
      .min(18, 'Must be at least 18 years old')
      .max(120, 'Invalid age'),
    accountNumber: Yup.string()
      .required('Account number is required')
      .matches(/^5\d{8}$/, 'Account number must start with 5 followed by 8 digits')
  });

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      console.log('Registering user with values:', values);
      const response = await authService.register(values);
      
      console.log('Registration response:', response);
      
      if (response.data && response.data.email) {
        setRegistrationEmail(response.data.email);
        setShowOtpField(true);
        setOtpValue(''); // Clear OTP field
        setSuccess(response.data.message || 'OTP has been sent to your email for verification.');
        setError('');
      } else {
        setSuccess('Registration initiated! Redirecting to login...');
        setError('');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      }
    } catch (err) {
      console.error('Registration error:', err);
      const errorMessage = err.response?.data || err.message || 'Registration failed. Please try again.';
      setError(errorMessage);
      setSuccess('');
    } finally {
      setSubmitting(false);
    }
  };

  const handleVerifyOTP = async (e) => {
    e.preventDefault();
    
    if (!otpValue || otpValue.length !== 6) {
      setError('Please enter a valid 6-digit OTP');
      return;
    }

    try {
      console.log('Verifying OTP for email:', registrationEmail);
      setError('');
      await authService.verifyRegistrationOTP(registrationEmail, otpValue);
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      console.error('OTP verification error:', err);
      const errorMessage = err.response?.data || err.message || 'OTP verification failed. Please try again.';
      setError(errorMessage);
      setSuccess('');
    }
  };

  // If already authenticated, don't show register form
  if (authService.isAuthenticated()) {
    return null;
  }

  return (
    <Container className="register-container">
      <Row className="justify-content-center">
        <Col md={8} lg={7}>
          <div className="text-center mb-4">
            <h1 className="brand-name">BingBank</h1>
            <p className="brand-tagline">Open your account today</p>
          </div>
          
          <Card className="register-card">
            <Card.Body>
              <h2 className="text-center mb-4">
                {showOtpField ? 'Verify OTP' : 'Register for NetBanking'}
              </h2>
              
              {error && <Alert variant="danger">{error}</Alert>}
              {success && <Alert variant="success">{success}</Alert>}
              
              {!showOtpField ? (
                <Formik
                  initialValues={initialValues}
                  validationSchema={validationSchema}
                  onSubmit={handleSubmit}
                >
                  {({ isSubmitting, values }) => (
                    <Form>
                      <Row>
                        <Col md={6}>
                          <div className="mb-3">
                            <label htmlFor="firstName" className="form-label">First Name *</label>
                            <Field
                              type="text"
                              id="firstName"
                              name="firstName"
                              className="form-control"
                              placeholder="Enter first name"
                              value={values.firstName}
                            />
                            <ErrorMessage name="firstName" component="div" className="text-danger" />
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="mb-3">
                            <label htmlFor="lastName" className="form-label">Last Name *</label>
                            <Field
                              type="text"
                              id="lastName"
                              name="lastName"
                              className="form-control"
                              placeholder="Enter last name"
                              value={values.lastName}
                            />
                            <ErrorMessage name="lastName" component="div" className="text-danger" />
                          </div>
                        </Col>
                      </Row>

                      <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email *</label>
                        <Field
                          type="email"
                          id="email"
                          name="email"
                          className="form-control"
                          placeholder="Enter email"
                          value={values.email}
                        />
                        <ErrorMessage name="email" component="div" className="text-danger" />
                      </div>

                      <div className="mb-3">
                        <label htmlFor="password" className="form-label">Password *</label>
                        <Field
                          type="password"
                          id="password"
                          name="password"
                          className="form-control"
                          placeholder="Enter password (min 6 characters)"
                          value={values.password}
                        />
                        <ErrorMessage name="password" component="div" className="text-danger" />
                      </div>

                      <div className="mb-3">
                        <label htmlFor="accountNumber" className="form-label">Account Number *</label>
                        <Field
                          type="text"
                          id="accountNumber"
                          name="accountNumber"
                          className="form-control"
                          placeholder="Enter your 9-digit account number (starts with 5)"
                          value={values.accountNumber}
                        />
                        <ErrorMessage name="accountNumber" component="div" className="text-danger" />
                        <small className="form-text text-muted">
                          Your account number must start with 5 and be 9 digits long
                        </small>
                      </div>

                      <Row>
                        <Col md={6}>
                          <div className="mb-3">
                            <label htmlFor="age" className="form-label">Age</label>
                            <Field
                              type="number"
                              id="age"
                              name="age"
                              className="form-control"
                              placeholder="Enter age"
                              value={values.age}
                            />
                            <ErrorMessage name="age" component="div" className="text-danger" />
                          </div>
                        </Col>
                        <Col md={6}>
                          <div className="mb-3">
                            <label htmlFor="gender" className="form-label">Gender</label>
                            <Field 
                              as="select" 
                              id="gender" 
                              name="gender" 
                              className="form-control"
                              value={values.gender}
                            >
                              <option value="">Select gender</option>
                              <option value="male">Male</option>
                              <option value="female">Female</option>
                              <option value="other">Other</option>
                            </Field>
                            <ErrorMessage name="gender" component="div" className="text-danger" />
                          </div>
                        </Col>
                      </Row>

                      <div className="mb-3">
                        <label htmlFor="mobile" className="form-label">Mobile Number</label>
                        <Field
                          type="text"
                          id="mobile"
                          name="mobile"
                          className="form-control"
                          placeholder="Enter mobile number"
                          value={values.mobile}
                        />
                        <ErrorMessage name="mobile" component="div" className="text-danger" />
                      </div>

                      <div className="mb-3">
                        <label htmlFor="address" className="form-label">Address</label>
                        <Field
                          as="textarea"
                          id="address"
                          name="address"
                          className="form-control"
                          placeholder="Enter address"
                          rows="3"
                          value={values.address}
                        />
                        <ErrorMessage name="address" component="div" className="text-danger" />
                      </div>

                      <div className="mb-3 form-check">
                        <Field
                          type="checkbox"
                          id="twoFactorEnabled"
                          name="twoFactorEnabled"
                          className="form-check-input"
                          checked={values.twoFactorEnabled}
                        />
                        <label className="form-check-label" htmlFor="twoFactorEnabled">
                          Enable Two-Factor Authentication (Recommended)
                        </label>
                      </div>

                      <div className="d-grid">
                        <button
                          type="submit"
                          className="btn btn-primary register-button"
                          disabled={isSubmitting}
                        >
                          {isSubmitting ? 'Registering...' : 'Register'}
                        </button>
                      </div>
                    </Form>
                  )}
                </Formik>
              ) : (
                <div>
                  <div className="mb-4 text-center">
                    <p className="otp-info">
                      A verification code has been sent to <strong>{registrationEmail}</strong>.
                      Please enter the 6-digit code below to complete your registration.
                    </p>
                  </div>
                  
                  <form onSubmit={handleVerifyOTP}>
                    <div className="mb-3">
                      <label htmlFor="otp" className="form-label">Verification Code</label>
                      <input
                        type="text"
                        id="otp"
                        name="otp"
                        className="form-control otp-input text-center"
                        placeholder="Enter 6-digit code"
                        maxLength="6"
                        autoComplete="off"
                        value={otpValue}
                        onChange={(e) => {
                          const value = e.target.value.replace(/\D/g, ''); // Only numbers
                          setOtpValue(value);
                        }}
                        required
                      />
                      <small className="form-text text-muted text-center d-block mt-2">
                        Please check your email for the verification code
                      </small>
                    </div>
                    
                    <div className="d-grid">
                      <button
                        type="submit"
                        className="btn btn-primary register-button"
                      >
                        Verify Code
                      </button>
                    </div>

                    <div className="text-center mt-3">
                      <button
                        type="button"
                        className="btn btn-link"
                        onClick={() => {
                          setShowOtpField(false);
                          setOtpValue('');
                          setError('');
                          setSuccess('');
                        }}
                      >
                        Back to Registration
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </Card.Body>
            <Card.Footer className="text-center">
              <p className="mb-0">
                Already have an account?{' '}
                <Link to="/login" className="text-decoration-none">
                  Login here
                </Link>
              </p>
            </Card.Footer>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Register;