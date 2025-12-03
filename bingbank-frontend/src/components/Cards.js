import React, { useState, useEffect } from 'react';
import { Container, Tabs, Tab, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import DebitCard from './DebitCard';
import CreditCard from './CreditCard';
import authService from '../services/authService';
import '../styles/Cards.css';

const Cards = () => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('debit');

  useEffect(() => {
    initializeCards();
  }, [navigate]);

  const initializeCards = async () => {
    const isAuth = authService.isAuthenticated();
    if (!isAuth) {
      navigate('/login', { replace: true });
      return;
    }

    const userData = authService.getUser();
    if (!userData || !userData.id) {
      setLoading(false);
      return;
    }

    setCustomer(userData);
    setLoading(false);
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Spinner animation="border" role="status">
          <span className="visually-hidden">Loading...</span>
        </Spinner>
        <div className="ms-3">Loading cards...</div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="main-content">
        <Container fluid>
          <div className="bank-header">
            <h1>My Cards</h1>
          </div>

          <Tabs
            id="cards-tabs"
            activeKey={activeTab}
            onSelect={(k) => setActiveTab(k)}
            className="mb-4 custom-tabs"
          >
            <Tab eventKey="debit" title="Debit Card">
              {customer && <DebitCard customerId={customer.id} />}
            </Tab>
            <Tab eventKey="credit" title="Credit Card">
              {customer && <CreditCard customerId={customer.id} />}
            </Tab>
          </Tabs>
        </Container>
      </div>
    </div>
  );
};

export default Cards;