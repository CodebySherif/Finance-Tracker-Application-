const API_BASE = '/api';

// ---------- auth guard ----------

const qs = new URLSearchParams(window.location.search);
if (qs.get('token')) {
  localStorage.setItem('ledger_token', qs.get('token'));
  localStorage.setItem('ledger_username', qs.get('username') || 'Account');
  window.history.replaceState({}, document.title, window.location.pathname); // keep token out of the URL bar
}

const token = localStorage.getItem('ledger_token');
const username = localStorage.getItem('ledger_username');

if (!token) {
  window.location.href = 'login.html';
}

const state = {
  currentMonth: currentYearMonth(),
};

const els = {
  monthLabel: document.getElementById('currentMonthLabel'),
  prevMonth: document.getElementById('prevMonth'),
  nextMonth: document.getElementById('nextMonth'),
  userNameLabel: document.getElementById('userNameLabel'),
  logoutBtn: document.getElementById('logoutBtn'),
  totalIncome: document.getElementById('totalIncome'),
  totalExpense: document.getElementById('totalExpense'),
  totalBalance: document.getElementById('totalBalance'),
  budgetList: document.getElementById('budgetList'),
  receiptTape: document.getElementById('receiptTape'),
  txForm: document.getElementById('transactionForm'),
  txDescription: document.getElementById('txDescription'),
  txAmount: document.getElementById('txAmount'),
  txDate: document.getElementById('txDate'),
  txFormError: document.getElementById('txFormError'),
  budgetForm: document.getElementById('budgetForm'),
  budgetLimit: document.getElementById('budgetLimit'),
};

els.userNameLabel.textContent = username || 'Account';

els.logoutBtn.addEventListener('click', () => {
  localStorage.removeItem('ledger_token');
  localStorage.removeItem('ledger_username');
  window.location.href = 'login.html';
});

// ---------- helpers ----------

function currentYearMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function formatMonthLabel(yearMonth) {
  const [y, m] = yearMonth.split('-').map(Number);
  const date = new Date(y, m - 1, 1);
  return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
}

function formatCurrency(amount) {
  const n = Number(amount || 0);
  return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatShortDate(isoDate) {
  const [y, m, d] = isoDate.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

async function api(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('ledger_token')}`,
    },
    ...options,
  });

  if (res.status === 401) {
    // Token missing/expired/invalid - send back to login
    localStorage.removeItem('ledger_token');
    localStorage.removeItem('ledger_username');
    window.location.href = 'login.html';
    return new Promise(() => {}); // halt further handling
  }

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      message = body.message || message;
    } catch (_) { /* ignore parse errors */ }
    throw new Error(message);
  }
  if (res.status === 204) return null;
  return res.json();
}

// ---------- rendering ----------

function renderMonthLabel() {
  els.monthLabel.textContent = formatMonthLabel(state.currentMonth);
}

function renderSummary(summary) {
  els.totalIncome.textContent = formatCurrency(summary.totalIncome);
  els.totalExpense.textContent = formatCurrency(summary.totalExpense);
  els.totalBalance.textContent = formatCurrency(summary.balance);

  const b = summary.budgetStatus;
  if (!b) {
    els.budgetList.innerHTML = '<p class="empty-hint">No budget set for this month yet.</p>';
    return;
  }

  const pct = Math.min(100, b.percentUsed);
  els.budgetList.innerHTML = `
    <div class="budget-item ${b.overBudget ? 'over' : ''}">
      <div class="budget-head">
        <span class="budget-name">Monthly spending</span>
        <span class="budget-figures">${formatCurrency(b.spent)} / ${formatCurrency(b.monthlyLimit)}</span>
      </div>
      <div class="budget-track">
        <div class="budget-fill" style="width:${pct}%"></div>
      </div>
    </div>`;
}

function renderTransactions(transactions) {
  if (!transactions || transactions.length === 0) {
    els.receiptTape.innerHTML = '<p class="receipt-empty">No transactions logged this month yet.<br/>Add one on the left to start the tape.</p>';
    return;
  }

  els.receiptTape.innerHTML = transactions
    .map((t) => {
      const sign = t.type === 'INCOME' ? '+' : '−';
      return `
      <div class="receipt-row">
        <span class="receipt-date">${formatShortDate(t.date)}</span>
        <span class="receipt-desc" title="${escapeHtml(t.description)}">${escapeHtml(t.description)}</span>
        <span class="receipt-amount ${t.type.toLowerCase()}">${sign} ${formatCurrency(t.amount)}</span>
        <span class="receipt-delete"><button data-id="${t.id}">remove</button></span>
      </div>`;
    })
    .join('');

  els.receiptTape.querySelectorAll('.receipt-delete button').forEach((btn) => {
    btn.addEventListener('click', async () => {
      try {
        await api(`/transactions/${btn.dataset.id}`, { method: 'DELETE' });
        await refreshAll();
      } catch (err) {
        alert(err.message);
      }
    });
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}

// ---------- data loading ----------

async function loadSummary() {
  const summary = await api(`/summary?month=${state.currentMonth}`);
  renderSummary(summary);
}

async function loadTransactions() {
  const transactions = await api(`/transactions?month=${state.currentMonth}`);
  renderTransactions(transactions);
}

async function refreshAll() {
  renderMonthLabel();
  await Promise.all([loadSummary(), loadTransactions()]);
}

// ---------- event handlers ----------

els.prevMonth.addEventListener('click', async () => {
  state.currentMonth = shiftMonth(state.currentMonth, -1);
  await refreshAll();
});

els.nextMonth.addEventListener('click', async () => {
  state.currentMonth = shiftMonth(state.currentMonth, 1);
  await refreshAll();
});

function shiftMonth(yearMonth, delta) {
  const [y, m] = yearMonth.split('-').map(Number);
  const date = new Date(y, m - 1 + delta, 1);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

els.txForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  els.txFormError.textContent = '';
  const type = els.txForm.querySelector('input[name="type"]:checked').value;

  const payload = {
    description: els.txDescription.value.trim(),
    amount: parseFloat(els.txAmount.value),
    type,
    date: els.txDate.value,
  };

  try {
    await api('/transactions', { method: 'POST', body: JSON.stringify(payload) });
    els.txForm.reset();
    els.txDate.value = new Date().toISOString().slice(0, 10);
    await refreshAll();
  } catch (err) {
    els.txFormError.textContent = err.message;
  }
});

els.budgetForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const payload = {
    monthlyLimit: parseFloat(els.budgetLimit.value),
    month: state.currentMonth,
  };
  try {
    await api('/budgets', { method: 'POST', body: JSON.stringify(payload) });
    els.budgetForm.reset();
    await loadSummary();
  } catch (err) {
    alert(err.message);
  }
});

// ---------- init ----------

(async function init() {
  els.txDate.value = new Date().toISOString().slice(0, 10);
  renderMonthLabel();
  try {
    await refreshAll();
  } catch (err) {
    console.error(err);
    els.receiptTape.innerHTML = `<p class="receipt-empty">Could not reach the server.<br/>Is the backend running on port 8080?</p>`;
  }
})();
