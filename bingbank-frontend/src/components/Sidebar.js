import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { 
  FaHome, FaExchangeAlt, FaHistory, FaCreditCard, 
  FaPiggyBank, FaSignOutAlt, FaBars, FaChevronLeft
} from 'react-icons/fa';
import '../styles/Sidebar.css';
import authService from '../services/authService';
import logo from '../assets/logo.png';

const Sidebar = () => {
  const location = useLocation();
  const { pathname } = location;
  const [collapsed, setCollapsed] = useState(false);
  
  // Check if sidebar was collapsed in previous session
  useEffect(() => {
    const savedCollapsedState = localStorage.getItem('sidebarCollapsed');
    if (savedCollapsedState) {
      setCollapsed(JSON.parse(savedCollapsedState));
    }
  }, []);
  
  const toggleSidebar = () => {
    const newCollapsedState = !collapsed;
    setCollapsed(newCollapsedState);
    localStorage.setItem('sidebarCollapsed', JSON.stringify(newCollapsedState));
    
    // Add/remove collapsed class from body for responsive design
    if (newCollapsedState) {
      document.body.classList.add('sidebar-collapsed');
    } else {
      document.body.classList.remove('sidebar-collapsed');
    }
  };

  const menuItems = [
    { path: '/dashboard', name: 'Dashboard', icon: <FaHome size={20} /> },
    { path: '/transactions', name: 'Transactions', icon: <FaHistory size={20} /> },
    { path: '/fund-transfer', name: 'Fund Transfer', icon: <FaExchangeAlt size={20} /> },
    { path: '/cards', name: 'Cards', icon: <FaCreditCard size={20} /> },
    { path: '/fixed-deposits', name: 'Fixed Deposits', icon: <FaPiggyBank size={20} /> },
  ];

  const handleLogout = () => {
    authService.logout();
  };

  return (
    <div className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-toggle" onClick={toggleSidebar}>
        {collapsed ? <FaBars size={18} /> : <FaChevronLeft size={18} />}
      </div>
      
      <div className="logo-container">
        <img src={logo} alt="BingBank" className="bank-logo-img" />
      </div>
      
      <div className="menu">
        {menuItems.map((item) => (
          <Link
            to={item.path}
            key={item.path}
            className={`menu-item ${pathname === item.path ? 'active' : ''}`}
            title={collapsed ? item.name : ''}
          >
            <div className="icon">{item.icon}</div>
            <div className="menu-text">{item.name}</div>
          </Link>
        ))}
      </div>
      
      <div className="menu-footer">
        <div className="menu-item logout-item" onClick={handleLogout} title={collapsed ? 'Logout' : ''}>
          <div className="icon">
            <FaSignOutAlt size={20} />
          </div>
          <div className="menu-text">Logout</div>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;