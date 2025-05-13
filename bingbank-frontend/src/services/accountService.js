import axios from 'axios';
import authService from './authService';

const API_URL = 'http://localhost:8082/api/accounts';

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
 * Get customer information
 * @returns {Promise<Object>} Customer information
 */
const getCustomerInfo = async () => {
  try {
    const customerId = authService.getUser().id;
    const response = await axios.get(`${API_URL}/customer/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching customer info:', error);
    throw error;
  }
};

/**
 * Get account details
 * @returns {Promise<Object>} Account details
 */
const getAccountDetails = async () => {
  try {
    const customerId = authService.getUser().id;
    const response = await axios.get(`${API_URL}/customer/${customerId}/primary`);
    return response.data;
  } catch (error) {
    console.error('Error fetching account details:', error);
    throw error;
  }
};

/**
 * Get all accounts for the current customer
 * @returns {Promise<Array>} List of accounts
 */
const getAllAccounts = async () => {
  try {
    const customerId = authService.getUser().id;
    const response = await axios.get(`${API_URL}/customer/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching all accounts:', error);
    throw error;
  }
};

const accountService = {
  getCustomerInfo,
  getAccountDetails,
  getAllAccounts
};

export default accountService;