import axios from 'axios';
import authService from './authService';

// API URL - Connect to the API Gateway
const API_URL = 'http://localhost:8080/api/fund-transfer';

// Debug logging
const debug = true;

/**
 * Initiate a fund transfer
 * @param {Object} transferRequest 
 * @returns {Promise}
 */
const initiateTransfer = async (transferRequest) => {
  try {
    if (debug) console.log('[Fund Transfer Service] Initiating transfer:', transferRequest);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Fund Transfer Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/transfer`, transferRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[Fund Transfer Service] Transfer initiated:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Fund Transfer Service] Error initiating transfer:', error);
    throw error;
  }
};

/**
 * Get transfer history for a customer
 * @param {number} customerId 
 * @returns {Promise}
 */
const getTransferHistory = async (customerId) => {
  try {
    if (debug) console.log('[Fund Transfer Service] Fetching transfer history for customer:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Fund Transfer Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/history/${customerId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Fund Transfer Service] Transfer history received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[Fund Transfer Service] Error fetching transfer history:', error);
    throw error;
  }
};

/**
 * Get transfer by ID
 * @param {number} transferId 
 * @returns {Promise}
 */
const getTransferById = async (transferId) => {
  try {
    if (debug) console.log('[Fund Transfer Service] Fetching transfer:', transferId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[Fund Transfer Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/${transferId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[Fund Transfer Service] Transfer details received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[Fund Transfer Service] Error fetching transfer:', error);
    throw error;
  }
};

const fundTransferService = {
  initiateTransfer,
  getTransferHistory,
  getTransferById
};

export default fundTransferService;