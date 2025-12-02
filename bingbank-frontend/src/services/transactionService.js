import axios from 'axios';
import authService from './authService';

// API URL - Connect to the API Gateway
const API_URL = 'http://localhost:8080/api/transactions';

// Debug logging
const debug = true;

/**
 * Get latest 5 transactions for dashboard
 * @param {string} accountNumber 
 * @returns {Promise}
 */
const getLatestTransactions = async (accountNumber) => {
  try {
    if (debug) console.log('[Transaction Service] Fetching latest transactions for:', accountNumber);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Transaction Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/latest/${accountNumber}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Transaction Service] Latest transactions received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Transaction Service] Error fetching latest transactions:', error);
    throw error;
  }
};

/**
 * Get all transactions for an account
 * @param {string} accountNumber 
 * @returns {Promise}
 */
const getAllTransactions = async (accountNumber) => {
  try {
    if (debug) console.log('[Transaction Service] Fetching all transactions for:', accountNumber);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Transaction Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/account/${accountNumber}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Transaction Service] All transactions received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[Transaction Service] Error fetching all transactions:', error);
    throw error;
  }
};

/**
 * Get filtered transactions
 * @param {Object} filterRequest 
 * @returns {Promise}
 */
const getFilteredTransactions = async (filterRequest) => {
  try {
    if (debug) console.log('[Transaction Service] Filtering transactions:', filterRequest);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Transaction Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/filter`, filterRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Transaction Service] Filtered transactions received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[Transaction Service] Error filtering transactions:', error);
    throw error;
  }
};

/**
 * Download bank statement PDF
 * @param {Object} statementRequest 
 */
const downloadStatement = async (statementRequest) => {
  try {
    if (debug) console.log('[Transaction Service] Generating statement:', statementRequest);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Transaction Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/statement/pdf`, statementRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      },
      responseType: 'blob' // Important for PDF download
    });
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `bank_statement_${statementRequest.accountNumber}_${new Date().toISOString().split('T')[0]}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    
    if (debug) console.log('[Transaction Service] Statement downloaded successfully');
  } catch (error) {
    console.error('[Transaction Service] Error downloading statement:', error);
    throw error;
  }
};

const transactionService = {
  getLatestTransactions,
  getAllTransactions,
  getFilteredTransactions,
  downloadStatement
};

export default transactionService;