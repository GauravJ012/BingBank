import axios from 'axios';
import authService from './authService';

// API URL - Connect to the API Gateway
const API_URL = 'http://localhost:8080/api/accounts';

// Debug logging
const debug = true;

/**
 * Get account details by customer ID
 * @param {number} customerId 
 * @returns {Promise}
 */
const getAccountsByCustomerId = async (customerId) => {
  try {
    if (debug) console.log('[Account Service] Fetching accounts for customer ID:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Account Service] No token available');
      throw new Error('Authentication required');
    }
    
    // Don't add Bearer prefix if it's already there
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    if (debug) console.log('[Account Service] Using Authorization header:', authHeader.substring(0, 20) + '...');
    
    const response = await axios.get(`${API_URL}/customer/${customerId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Account Service] Response received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Account Service] Error fetching accounts:', error);
    console.error('[Account Service] Error details:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    });
    throw error;
  }
};

/**
 * Get account details by account number
 * @param {string} accountNumber 
 * @returns {Promise}
 */
const getAccountByAccountNumber = async (accountNumber) => {
  try {
    if (debug) console.log('[Account Service] Fetching account for number:', accountNumber);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Account Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/${accountNumber}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Account Service] Account details response:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Account Service] Error fetching account details:', error);
    throw error;
  }
};

/**
 * Check if an account number exists
 * @param {string} accountNumber 
 * @returns {Promise<boolean>}
 */
const checkAccountExists = async (accountNumber) => {
  try {
    if (debug) console.log('[Account Service] Checking if account exists:', accountNumber);
    
    // This endpoint doesn't require authentication (for registration)
    const response = await axios.get(`${API_URL}/exists/${accountNumber}`);
    
    if (debug) console.log('[Account Service] Account exists response:', response.data);
    return response.data.exists;
  } catch (error) {
    console.error('[Account Service] Error checking account existence:', error);
    return false;
  }
};

const accountService = {
  getAccountsByCustomerId,
  getAccountByAccountNumber,
  checkAccountExists
};

export default accountService;