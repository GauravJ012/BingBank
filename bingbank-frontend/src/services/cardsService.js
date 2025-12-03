import axios from 'axios';
import authService from './authService';

// API URL - Connect to the API Gateway
const API_URL = 'http://localhost:8080/api/cards';

// Debug logging
const debug = true;

/**
 * Get debit card by customer ID
 */
const getDebitCard = async (customerId) => {
  try {
    if (debug) console.log('[Cards Service] Fetching debit card for customer:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/debit/customer/${customerId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Cards Service] Debit card received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error fetching debit card:', error);
    throw error;
  }
};

/**
 * Get credit card by customer ID
 */
const getCreditCard = async (customerId) => {
  try {
    if (debug) console.log('[Cards Service] Fetching credit card for customer:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/credit/customer/${customerId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Cards Service] Credit card received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error fetching credit card:', error);
    throw error;
  }
};

/**
 * Get credit card transactions
 */
const getCreditCardTransactions = async (cardId) => {
  try {
    if (debug) console.log('[Cards Service] Fetching transactions for card:', cardId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/credit/${cardId}/transactions`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Cards Service] Transactions received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error fetching transactions:', error);
    throw error;
  }
};

/**
 * Get filtered credit card transactions
 */
const getFilteredTransactions = async (cardId, filterRequest) => {
  try {
    if (debug) console.log('[Cards Service] Fetching filtered transactions');
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/credit/${cardId}/transactions/filter`, filterRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Cards Service] Filtered transactions received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error fetching filtered transactions:', error);
    throw error;
  }
};

/**
 * Download credit card statement
 */
const downloadStatement = async (cardId, year, month) => {
  try {
    if (debug) console.log('[Cards Service] Downloading statement for:', year, month);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/credit/${cardId}/statement/${year}/${month}`, {
      headers: {
        'Authorization': authHeader
      },
      responseType: 'blob'
    });
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `credit_card_statement_${year}_${String(month).padStart(2, '0')}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    
    if (debug) console.log('[Cards Service] Statement downloaded successfully');
  } catch (error) {
    console.error('[Cards Service] Error downloading statement:', error);
    throw error;
  }
};

/**
 * Change ATM PIN
 */
const changeAtmPin = async (changePinRequest) => {
  try {
    if (debug) console.log('[Cards Service] Changing ATM PIN');
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/debit/change-pin`, changePinRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Cards Service] PIN changed successfully');
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error changing PIN:', error);
    throw error;
  }
};

/**
 * Deactivate card
 */
const deactivateCard = async (deactivateRequest) => {
  try {
    if (debug) console.log('[Cards Service] Deactivating card');
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const endpoint = deactivateRequest.cardType === 'DEBIT' ? 
      `${API_URL}/debit/deactivate` : `${API_URL}/credit/deactivate`;
    
    const response = await axios.post(endpoint, deactivateRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Cards Service] Card deactivated successfully');
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error deactivating card:', error);
    throw error;
  }
};

/**
 * Pay credit card bill
 */
const payCreditCardBill = async (payBillRequest) => {
  try {
    if (debug) console.log('[Cards Service] Paying credit card bill');
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Cards Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/credit/pay-bill`, payBillRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Cards Service] Bill paid successfully');
    return response.data;
  } catch (error) {
    console.error('[Cards Service] Error paying bill:', error);
    throw error;
  }
};

const cardsService = {
  getDebitCard,
  getCreditCard,
  getCreditCardTransactions,
  getFilteredTransactions,
  downloadStatement,
  changeAtmPin,
  deactivateCard,
  payCreditCardBill
};

export default cardsService;