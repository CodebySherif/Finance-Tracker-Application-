const API_BASE = '/api';

// If already logged in, skip straight to the dashboard.
if (localStorage.getItem('ledger_token')) {
  window.location.href = 'index.html';
}

const els = {
  loginView: document.getElementById('loginView'),
  registerView: document.getElementById('registerView'),
  showRegister: document.getElementById('showRegister'),
  showLogin: document.getElementById('showLogin'),
  loginForm: document.getElementById('loginForm'),
  loginUsername: document.getElementById('loginUsername'),
  loginPassword: document.getElementById('loginPassword'),
  loginError: document.getElementById('loginError'),
  registerForm: document.getElementById('registerForm'),
  registerUsername: document.getElementById('registerUsername'),
  registerEmail: document.getElementById('registerEmail'),
  registerPassword: document.getElementById('registerPassword'),
  registerError: document.getElementById('registerError'),
};

els.showRegister.addEventListener('click', () => {
  els.loginView.style.display = 'none';
  els.registerView.style.display = 'block';
});

els.showLogin.addEventListener('click', () => {
  els.registerView.style.display = 'none';
  els.loginView.style.display = 'block';
});

async function api(path, body) {
  const res = await fetch(API_BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || 'Something went wrong');
  }
  return data;
}

function storeSessionAndRedirect(auth) {
  localStorage.setItem('ledger_token', auth.token);
  localStorage.setItem('ledger_username', auth.username);
  window.location.href = 'index.html';
}

els.loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  els.loginError.textContent = '';
  try {
    const auth = await api('/auth/login', {
      username: els.loginUsername.value.trim(),
      password: els.loginPassword.value,
    });
    storeSessionAndRedirect(auth);
  } catch (err) {
    els.loginError.textContent = err.message;
  }
});

els.registerForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  els.registerError.textContent = '';
  try {
    const auth = await api('/auth/register', {
      username: els.registerUsername.value.trim(),
      email: els.registerEmail.value.trim(),
      password: els.registerPassword.value,
    });
    storeSessionAndRedirect(auth);
  } catch (err) {
    els.registerError.textContent = err.message;
  }
});

fetch(API_BASE + '/auth/providers')
  .then((r) => r.json())
  .then((p) => { if (p.google) document.getElementById('googleBlock').style.display = 'block'; })
  .catch(() => {});
