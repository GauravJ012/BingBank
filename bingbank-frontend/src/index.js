import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

// Initialize sidebar collapsed state from localStorage
const isSidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
if (isSidebarCollapsed) {
  document.body.classList.add('sidebar-collapsed');
} else {
  document.body.classList.remove('sidebar-collapsed');
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);