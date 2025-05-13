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

  // Check if user is already logged in
  useEffect(() => {
    if (authService.isAuthenticated()) {
      navigate('/dashboard');
    }
  }, [navigate]);

  const initialValues = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    age: '',
    gender: '',
    address: '',
    mobile: '',
    twoFactorEnabled: true
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
      .max(120, 'Invalid age')
  });

  const handleSubmit = async (values, { setSubmitting }) => {
    try {
      await authService.register(values);
      setSuccess('Registration successful! Redirecting to login...');
      setError('');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(typeof err === 'string' ? err : 'Registration failed. Please try again.');
      setSuccess('');
    } finally {
      setSubmitting(false);
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
              <h2 className="text-center mb-4">Register for NetBanking</h2>
              
              {error && <Alert variant="danger">{error}</Alert>}
              {success && <Alert variant="success">{success}</Alert>}
              
              <Formik
                initialValues={initialValues}
                validationSchema={validationSchema}
                onSubmit={handleSubmit}
              >
                {({ isSubmitting }) => (
                  <Form>
                    <Row>
                      <Col md={6}>
                        <div className="mb-3">
                          <label htmlFor="firstName" className="form-label">First Name</label>
                          <Field
                            type="text"
                            id="firstName"
                            name="firstName"
                            className="form-control"
                            placeholder="Enter first name"
                          />
                          <ErrorMessage name="firstName" component="div" className="text-danger" />
                        </div>
                      </Col>
                      <Col md={6}>
                        <div className="mb-3">
                          <label htmlFor="lastName" className="form-label">Last Name</label>
                          <Field
                            type="text"
                            id="lastName"
                            name="lastName"
                            className="form-control"
                            placeholder="Enter last name"
                          />
                          <ErrorMessage name="lastName" component="div" className="text-danger" />
                        </div>
                      </Col>
                    </Row>

                    <div className="mb-3">
                      <label htmlFor="email" className="form-label">Email</label>
                      <Field
                        type="email"
                        id="email"
                        name="email"
                        className="form-control"
                        placeholder="Enter email"
                      />
                      <ErrorMessage name="email" component="div" className="text-danger" />
                    </div>

                    <div className="mb-3">
                      <label htmlFor="password" className="form-label">Password</label>
                      <Field
                        type="password"
                        id="password"
                        name="password"
                        className="form-control"
                        placeholder="Enter password"
                      />
                      <ErrorMessage name="password" component="div" className="text-danger" />
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
                          />
                          <ErrorMessage name="age" component="div" className="text-danger" />
                        </div>
                      </Col>
                      <Col md={6}>
                        <div className="mb-3">
                          <label htmlFor="gender" className="form-label">Gender</label>
                          <Field as="select" id="gender" name="gender" className="form-control">
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
                      />
                      <ErrorMessage name="address" component="div" className="text-danger" />
                    </div>

                    <div className="mb-3 form-check">
                      <Field
                        type="checkbox"
                        id="twoFactorEnabled"
                        name="twoFactorEnabled"
                        className="form-check-input"
                      />
                      <label className="form-check-label" htmlFor="twoFactorEnabled">
                        Enable Two-Factor Authentication
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