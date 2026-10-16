/**
 * Campus Mess Management System - Core Client Library
 */

const API_BASE = '/api';

// Authentication & Session
function getCurrentUser() {
  const data = localStorage.getItem('mess_user');
  try {
    return data ? JSON.parse(data) : null;
  } catch (e) {
    return null;
  }
}

function setCurrentUser(user) {
  if (user) {
    localStorage.setItem('mess_user', JSON.stringify(user));
  } else {
    localStorage.removeItem('mess_user');
  }
}

function logout() {
  localStorage.removeItem('mess_user');
  showToast('Logged out successfully.', 'info');
  setTimeout(() => {
    window.location.href = 'login.html';
  }, 400);
}

function requireAuth(allowedRoles = []) {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = 'login.html';
    return null;
  }
  if (allowedRoles.length > 0 && !allowedRoles.map(r => r.toLowerCase()).includes(user.role.toLowerCase())) {
    showToast(`Access restricted. Redirecting to your dashboard.`, 'error');
    const redirectMap = {
      student: 'student.html',
      manager: 'manager.html',
      cook: 'cook.html',
      admin: 'admin.html'
    };
    window.location.href = redirectMap[user.role.toLowerCase()] || 'login.html';
    return null;
  }
  return user;
}

// API Fetch Helper
async function apiCall(endpoint, method = 'GET', body = null) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json'
    }
  };
  if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    options.body = JSON.stringify(body);
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, options);
    const contentType = res.headers.get('content-type');
    let data;
    if (contentType && contentType.includes('application/json')) {
      data = await res.json();
    } else {
      data = await res.text();
    }

    if (!res.ok) {
      const errorMsg = (data && data.message) ? data.message : `Request failed (${res.status})`;
      throw new Error(errorMsg);
    }
    return data;
  } catch (err) {
    console.error('API Error:', err);
    throw err;
  }
}

// Global Toast System
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'error') icon = '⚠️';

  toast.innerHTML = `
    <div style="display: flex; align-items: center; gap: 0.6rem;">
      <span>${icon}</span>
      <span>${message}</span>
    </div>
    <span style="cursor: pointer; opacity: 0.7; font-weight: bold;" onclick="this.parentElement.remove()">&times;</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    if (toast.parentElement) toast.remove();
  }, 4000);
}

// Modal Helpers
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('open');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('open');
}

// 1-Click Role Launcher Bar
function renderTopBar(activeRole = '') {
  const user = getCurrentUser();
  const bar = document.createElement('div');
  bar.className = 'demo-role-bar';
  bar.innerHTML = `
    <div style="display: flex; align-items: center; gap: 0.75rem;">
      <span style="font-weight: 700; color: #38bdf8;">⚡ DEMO ROLE SWITCHER:</span>
      <div class="demo-pills">
        <button class="demo-pill ${activeRole === 'student' ? 'active' : ''}" onclick="quickSwitchRole('student')">👨‍🎓 Student</button>
        <button class="demo-pill ${activeRole === 'manager' ? 'active' : ''}" onclick="quickSwitchRole('manager')">📋 Manager</button>
        <button class="demo-pill ${activeRole === 'cook' ? 'active' : ''}" onclick="quickSwitchRole('cook')">👨‍🍳 Cook</button>
        <button class="demo-pill ${activeRole === 'admin' ? 'active' : ''}" onclick="quickSwitchRole('admin')">🛡️ Admin</button>
      </div>
    </div>
    <div style="display: flex; align-items: center; gap: 1rem;">
      ${user ? `<span style="color: #94a3b8;">Signed in: <strong style="color: #ffffff;">${user.name}</strong> (${user.role.toUpperCase()})</span>
      <button class="demo-pill" style="border-color: #ef4444; color: #fca5a5;" onclick="logout()">Sign Out</button>` : `<a href="login.html" style="color: #38bdf8; font-weight: 600;">Sign In</a>`}
    </div>
  `;
  document.body.prepend(bar);
}

async function quickSwitchRole(role) {
  const demoAccounts = {
    student: { email: 'student@mess.com', pass: 'Student@123', target: 'student.html' },
    manager: { email: 'manager@mess.com', pass: 'Manager@123', target: 'manager.html' },
    cook: { email: 'cook@mess.com', pass: 'Cook@123', target: 'cook.html' },
    admin: { email: 'admin@mess.com', pass: 'Admin@123', target: 'admin.html' }
  };

  const account = demoAccounts[role];
  if (!account) return;

  try {
    const res = await apiCall('/auth/login', 'POST', { email: account.email, password: account.pass });
    if (res.success && res.user) {
      setCurrentUser(res.user);
      window.location.href = account.target;
    }
  } catch (err) {
    showToast('Failed to switch demo role: ' + err.message, 'error');
  }
}

// Common Formatting
function formatCurrency(amount) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}
