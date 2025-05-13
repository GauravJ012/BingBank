import axios from 'axios';
import authService from './authService';

const API_URL = 'http://localhost:8083/api/transactions';

// Set up interceptor for auth token
axios.interceptors.request.use(
  config => {
    const token = authService.getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

/**
 * Get recent transactions for the current customer
 * @param {number} limit - Number of transactions to retrieve
 * @returns {Promise<Array>} List of transactions
 */
const getRecentTransactions = async (limit = 5) => {
  try {
    const accountId = authService.getUser().accountId;
    const response = await axios.get(`${API_URL}/account/${accountId}/recent?limit=${limit}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching recent transactions:', error);
    throw error;
  }
};

/**
 * Get all transactions for the current customer with pagination
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated transactions
 */
const getAllTransactions = async (page = 0, size = 10) => {
  try {
    const accountId = authService.getUser().accountId;
    const response = await axios.get(`${API_URL}/account/${accountId}?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching all transactions:', error);
    throw error;
  }
};

/**
 * Get transaction details by ID
 * @param {number} transactionId - Transaction ID
 * @returns {Promise<Object>} Transaction details
 */
const getTransactionById = async (transactionId) => {
  try {
    const response = await axios.get(`${API_URL}/${transactionId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching transaction details:', error);
    throw error;
  }
};

/**
 * Transfer money to another account
 * @param {Object} transferData - Transfer data
 * @returns {Promise<Object>} Transaction result
 */
const transferMoney = async (transferData) => {
  try {
    const response = await axios.post(`${API_URL}/transfer`, transferData);
    return response.data;
  } catch (error) {
    console.error('Error transferring money:', error);
    throw error;
  }
};

const transactionService = {
  getRecentTransactions,
  getAllTransactions,
  getTransactionById,
  transferMoney
};

export default transactionService;