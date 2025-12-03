import axios from 'axios';
import authService from './authService';

// API URL - Connect to the API Gateway
const API_URL = 'http://localhost:8080/api/fixed-deposits';

// Debug logging
const debug = true;

/**
 * Get all active FDs for a customer
 * @param {number} customerId 
 * @returns {Promise}
 */
const getActiveFDs = async (customerId) => {
  try {
    if (debug) console.log('[FD Service] Fetching active FDs for customer:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[FD Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/customer/${customerId}/active`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[FD Service] Active FDs received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[FD Service] Error fetching active FDs:', error);
    throw error;
  }
};

/**
 * Get all FDs (active and closed) for a customer
 * @param {number} customerId 
 * @returns {Promise}
 */
const getAllFDs = async (customerId) => {
  try {
    if (debug) console.log('[FD Service] Fetching all FDs for customer:', customerId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[FD Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/customer/${customerId}`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[FD Service] All FDs received:', response.data.length);
    return response.data;
  } catch (error) {
    console.error('[FD Service] Error fetching all FDs:', error);
    throw error;
  }
};

/**
 * Create a new FD
 * @param {Object} fdRequest 
 * @returns {Promise}
 */
const createFD = async (fdRequest) => {
  try {
    if (debug) console.log('[FD Service] Creating FD:', fdRequest);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[FD Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/create`, fdRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[FD Service] FD created successfully:', response.data);
    return response.data;
  } catch (error) {
    console.error('[FD Service] Error creating FD:', error);
    throw error;
  }
};

/**
 * Close an existing FD
 * @param {Object} closeRequest 
 * @returns {Promise}
 */
const closeFD = async (closeRequest) => {
  try {
    if (debug) console.log('[FD Service] Closing FD:', closeRequest);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[FD Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.post(`${API_URL}/close`, closeRequest, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      }
    });
    
    if (debug) console.log('[FD Service] FD closed successfully:', response.data);
    return response.data;
  } catch (error) {
    console.error('[FD Service] Error closing FD:', error);
    throw error;
  }
};

/**
 * Get growth data for FD tracking
 * @param {number} fdId 
 * @returns {Promise}
 */
const getGrowthData = async (fdId) => {
  try {
    if (debug) console.log('[FD Service] Fetching growth data for FD:', fdId);
    
    const token = authService.getToken();
    if (!token) {
      console.error('[FD Service] No token available');
      throw new Error('Authentication required');
    }
    
    const authHeader = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    
    const response = await axios.get(`${API_URL}/${fdId}/growth`, {
      headers: {
        'Authorization': authHeader
      }
    });
    
    if (debug) console.log('[FD Service] Growth data received:', response.data);
    return response.data;
  } catch (error) {
    console.error('[FD Service] Error fetching growth data:', error);
    throw error;
  }
};

const fixedDepositService = {
  getActiveFDs,
  getAllFDs,
  createFD,
  closeFD,
  getGrowthData
};

export default fixedDepositService;