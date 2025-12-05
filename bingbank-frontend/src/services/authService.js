import axios from 'axios';

// API URL - Connect to your actual backend
const API_URL = 'http://localhost:8080/api/auth';

// Debug logging 
const debug = true;

/**
 * Login user with email and password
 * @param {string} email 
 * @param {string} password 
 * @returns {Promise}
 */
const login = async (email, password) => {
  try {
    if (debug) console.log('Attempting login for:', email);
    const response = await axios.post(`${API_URL}/login`, { email, password });
    if (debug) console.log('Login response:', response);
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error.response?.data || error.message || 'Login failed';
  }
};

/**
 * Verify OTP code (for login)
 * @param {string} email 
 * @param {string} otp 
 * @returns {Promise}
 */
const verifyOTP = async (email, otp) => {
  try {
    if (debug) console.log('Verifying OTP for:', email);
    const response = await axios.post(`${API_URL}/verify-otp`, { email, otp });
    if (debug) console.log('OTP verification response:', response);
    
    // If successful, store token and user data
    if (response.data.accessToken) {
      // Store token WITHOUT Bearer prefix (we'll add it in the interceptor)
      const token = response.data.accessToken;
      if (debug) console.log('Storing token:', token);
      localStorage.setItem('token', token);
      
      // Store user data
      const userData = {
        id: response.data.customerId,
        email: response.data.email,
        firstName: response.data.firstName,
        lastName: response.data.lastName
      };
      if (debug) console.log('Storing user data:', userData);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Wait a moment to ensure storage is complete
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // Verify data was stored correctly
      const storedToken = localStorage.getItem('token');
      const storedUser = localStorage.getItem('user');
      if (debug) {
        console.log('Stored token:', storedToken);
        console.log('Stored user:', storedUser);
      }
      
      if (!storedToken || !storedUser) {
        console.error('Failed to store authentication data in localStorage');
        throw new Error('Authentication storage failed');
      }
    }
    
    return response;
  } catch (error) {
    console.error('OTP verification error:', error);
    throw error.response?.data || error.message || 'OTP verification failed';
  }
};

/**
 * Register new user
 * @param {Object} userData 
 * @returns {Promise}
 */
const register = async (userData) => {
  try {
    if (debug) console.log('Registering new user:', userData);
    const response = await axios.post(`${API_URL}/register`, userData);
    if (debug) console.log('Registration response:', response);
    return response;
  } catch (error) {
    console.error('Registration error:', error);
    throw error.response?.data || error.message || 'Registration failed';
  }
};

/**
 * Verify registration OTP
 * @param {string} email 
 * @param {string} otp 
 * @returns {Promise}
 */
const verifyRegistrationOTP = async (email, otp) => {
  try {
    if (debug) console.log('Verifying registration OTP for:', email);
    const response = await axios.post(`${API_URL}/verify-registration-otp`, { 
      email: email, 
      otp: otp 
    });
    if (debug) console.log('Registration OTP verification response:', response);
    
    return response;
  } catch (error) {
    console.error('Registration OTP verification error:', error);
    throw error.response?.data || error.message || 'Registration OTP verification failed';
  }
};

/**
 * Request password reset OTP
 * @param {string} email 
 * @returns {Promise}
 */
const requestPasswordReset = async (email) => {
  try {
    if (debug) console.log('Requesting password reset for:', email);
    const response = await axios.post(`${API_URL}/forgot-password`, { email });
    if (debug) console.log('Password reset request response:', response);
    return response;
  } catch (error) {
    console.error('Password reset request error:', error);
    throw error.response?.data || error.message || 'Failed to request password reset';
  }
};

/**
 * Verify password reset OTP
 * @param {string} email 
 * @param {string} otp 
 * @returns {Promise}
 */
const verifyPasswordResetOTP = async (email, otp) => {
  try {
    if (debug) console.log('Verifying password reset OTP for:', email);
    const response = await axios.post(`${API_URL}/verify-reset-otp`, { email, otp });
    if (debug) console.log('OTP verification response:', response);
    return response;
  } catch (error) {
    console.error('OTP verification error:', error);
    throw error.response?.data || error.message || 'Failed to verify OTP';
  }
};

/**
 * Reset password
 * @param {string} email 
 * @param {string} otp 
 * @param {string} newPassword 
 * @returns {Promise}
 */
const resetPassword = async (email, otp, newPassword) => {
  try {
    if (debug) console.log('Resetting password for:', email);
    const response = await axios.post(`${API_URL}/reset-password`, { 
      email, 
      otp, 
      newPassword 
    });
    if (debug) console.log('Password reset response:', response);
    return response;
  } catch (error) {
    console.error('Password reset error:', error);
    throw error.response?.data || error.message || 'Failed to reset password';
  }
};

/**
 * Get customer details by ID
 * @param {number} customerId 
 * @returns {Promise}
 */
const getCustomerDetails = async (customerId) => {
  try {
    if (debug) console.log('Fetching customer details for ID:', customerId);
    
    // Make sure we have a valid token before making this request
    const token = getToken();
    if (!token) {
      console.error('No token available for customer details request');
      throw new Error('Authentication required');
    }
    
    // Use our auth-enabled axios instance
    const response = await axios.get(`${API_URL}/customer/${customerId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (debug) console.log('Customer details response:', response);
    return response;
  } catch (error) {
    console.error('Error fetching customer details:', error);
    // Don't throw here - let the component handle the fallback
    return { data: null };
  }
};

/**
 * Get customer by ID (alias for getCustomerDetails)
 * @param {number} customerId 
 * @returns {Promise}
 */
const getCustomerById = async (customerId) => {
  return getCustomerDetails(customerId);
};

/**
 * Enable or disable two-factor authentication
 * @param {number} customerId 
 * @param {boolean} enable 
 * @returns {Promise}
 */
const enable2FA = async (customerId, enable) => {
  try {
    const response = await axios.post(`${API_URL}/enable-2fa/${customerId}`, enable);
    return response;
  } catch (error) {
    throw error.response?.data || error.message || 'Failed to update 2FA settings';
  }
};

/**
 * Check if user is authenticated
 * @returns {boolean}
 */
const isAuthenticated = () => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  const result = !!token && !!user;
  if (debug) console.log('isAuthenticated check:', { token: !!token, user: !!user, result });
  return result;
};

/**
 * Get current user data
 * @returns {Object|null}
 */
const getUser = () => {
  try {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
      if (debug) console.log('No user data found in localStorage');
      return null;
    }
    const userData = JSON.parse(userStr);
    if (debug) console.log('Retrieved user data:', userData);
    return userData;
  } catch (error) {
    console.error('Error parsing user data:', error);
    // If there's an error parsing, clear the malformed data
    localStorage.removeItem('user');
    return null;
  }
};

/**
 * Get auth token
 * @returns {string|null}
 */
const getToken = () => {
  const token = localStorage.getItem('token');
  if (debug && !token) console.log('No token found in localStorage');
  return token;
};

/**
 * Logout user
 */
const logout = () => {
  if (debug) console.log('Logging out user');
  localStorage.removeItem('user');
  localStorage.removeItem('token');
  // Force reload to clear any state
  window.location.href = '/login';
};

// Create a separate axios instance for authenticated requests
const authAxios = axios.create();

// Set up axios interceptor to include auth token in all requests
authAxios.interceptors.request.use(
  config => {
    const token = getToken();
    if (token) {
      // Add proper Bearer prefix if not already present
      const tokenWithBearer = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers['Authorization'] = tokenWithBearer;
      if (debug) console.log('Adding Authorization header:', tokenWithBearer);
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Handle 401 responses (token expired) without auto-logout during dashboard load
let isInitialPageLoad = true;
setTimeout(() => { isInitialPageLoad = false; }, 5000); // Give 5 seconds for initial page load

authAxios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // Skip auto-logout during initial page load to prevent logout loops
      if (!isInitialPageLoad) {
        console.error('401 Unauthorized response - logging out');
        logout();
      } else {
        console.warn('401 Unauthorized during initial page load - ignoring');
      }
    }
    return Promise.reject(error);
  }
);

// Update the regular axios instance to use our auth interceptor
axios.interceptors.request.use(
  config => {
    const token = getToken();
    if (token) {
      // Add proper Bearer prefix if not already present
      const tokenWithBearer = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers['Authorization'] = tokenWithBearer;
      if (debug) console.log('Adding Authorization header to regular axios:', tokenWithBearer);
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

const authService = {
  login,
  verifyOTP,
  register,
  verifyRegistrationOTP,
  requestPasswordReset,      // ADDED
  verifyPasswordResetOTP,    // ADDED
  resetPassword,             // ADDED
  enable2FA,
  getCustomerDetails,
  getCustomerById,
  isAuthenticated,
  getUser,
  getToken,
  logout,
  authAxios
};

export default authService;