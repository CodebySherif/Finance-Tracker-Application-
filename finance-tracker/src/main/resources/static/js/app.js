const API_BASE = '/api';

const state = {
  currentMonth: currentYearMonth(),
  categories: [],
};

const els = {
  monthLabel: document.getElementById('currentMonthLabel'),
  prevMonth: document.getElementById('prevMonth'),
  nextMonth: document.getElementById('nextMonth'),
  totalIncome: document.getElementById('totalIncome'),
  totalExpense: document.getElementById('totalExpense'),
  totalBalance: document.getElementById('totalBalance'),
  budgetList: document.getElementById('budgetList'),
  receiptTape: document.getElementById('receiptTape'),
  txForm: document.getElementById('transactionForm'),
  txDescription: document.getElementById('txDescription'),
  txAmount: document.getElementById('txAmount'),
  txDate: document.getElementById('txDate'),
  txCategory: document.getElementById('txCategory'),
  txFormError: document.getElementById('txFormError'),
  budgetForm: document.getElementById('budgetForm'),
  budgetCategory: document.getElementById('budgetCategory'),
  budgetLimit: document.getElementById('budgetLimit'),
  categoryForm: document.getElementById('categoryForm'),
  categoryName: document.getElementById('categoryName'),
  categoryType: document.getElementById('categoryType'),
  categoryChips: document.getElementById('categoryChips'),
};

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

async function loadUser() {
  try {
    const res = await fetch('/api/me');
    const user = await res.json();

    if (user.authenticated) {
      const banner = document.getElementById('welcome-banner');
      if (banner) {
        banner.innerHTML = `
          <img src="${user.picture}" alt="profile" style="width:40px;height:40px;border-radius:50%;">
          <span>Welcome back, ${user.name} 👋</span>
        `;
      }
    }
  } catch (err) {
    console.error('Failed to load user info', err);
  }
}
loadUser();

function formatShortDate(isoDate) {
  const [y, m, d] = isoDate.split('-').map(Number);
  const date = new Date(y, m - 1, d);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

async function api(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
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

function renderCategoryOptions() {
  const buildOptions = (type) =>
      state.categories
          .filter((c) => c.type === type)
          .map((c) => `<option value="${c.id}">${escapeHtml(c.name)}</option>`)
          .join('');

  const expenseOptions = buildOptions('EXPENSE');
  const incomeOptions = buildOptions('INCOME');

  els.txCategory.innerHTML = `
    <optgroup label="Expense">${expenseOptions}</optgroup>
    <optgroup label="Income">${incomeOptions}</optgroup>
  `;

  els.budgetCategory.innerHTML = expenseOptions || '<option disabled>No expense categories yet</option>';
}

function renderCategoryChips() {
  if (state.categories.length === 0) {
    els.categoryChips.innerHTML = '<p class="empty-hint">No categories yet.</p>';
    return;
  }
  els.categoryChips.innerHTML = state.categories
      .map(
          (c) => `
      <span class="chip ${c.type === 'INCOME' ? 'income' : ''}" data-id="${c.id}">
        ${escapeHtml(c.name)}
        <span class="chip-remove" data-id="${c.id}" title="Delete category">&times;</span>
      </span>`
      )
      .join('');

  els.categoryChips.querySelectorAll('.chip-remove').forEach((el) => {
    el.addEventListener('click', async () => {
      if (!confirm('Delete this category? Transactions using it will keep their history but lose the label.')) return;
      try {
        await api(`/categories/${el.dataset.id}`, { method: 'DELETE' });
        await loadCategories();
        await loadSummary();
        await loadTransactions();
      } catch (err) {
        alert(err.message);
      }
    });
  });
}

function renderSummary(summary) {
  els.totalIncome.textContent = formatCurrency(summary.totalIncome);
  els.totalExpense.textContent = formatCurrency(summary.totalExpense);
  els.totalBalance.textContent = formatCurrency(summary.balance);

  if (!summary.budgetStatuses || summary.budgetStatuses.length === 0) {
    els.budgetList.innerHTML = '<p class="empty-hint">No budgets set for this month yet.</p>';
    return;
  }

  els.budgetList.innerHTML = summary.budgetStatuses
      .map((b) => {
        const pct = Math.min(100, b.percentUsed);
        return `
      <div class="budget-item ${b.overBudget ? 'over' : ''}">
        <div class="budget-head">
          <span class="budget-name">${escapeHtml(b.categoryName)}</span>
          <span class="budget-figures">${formatCurrency(b.spent)} / ${formatCurrency(b.monthlyLimit)}</span>
        </div>
        <div class="budget-track">
          <div class="budget-fill" style="width:${pct}%"></div>
        </div>
      </div>`;
      })
      .join('');
}

function renderTransactions(transactions) {
  if (!transactions || transactions.length === 0) {
    els.receiptTape.innerHTML = '<p class="receipt-empty">No transactions logged this month yet.<br/>Add one on the left to start the tape.</p>';
    return;
  }

  els.receiptTape.innerHTML = transactions
      .map((t) => {
        const sign = t.type === 'INCOME' ? '+' : '−';
        const categoryName = t.category ? escapeHtml(t.category.name) : 'Uncategorized';
        return `
      <div class="receipt-row">
        <span class="receipt-date">${formatShortDate(t.date)}</span>
        <span class="receipt-desc" title="${escapeHtml(t.description)}">${escapeHtml(t.description)}</span>
        <span class="receipt-category">${categoryName}</span>
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

async function loadCategories() {
  state.categories = await api('/categories');
  renderCategoryOptions();
  renderCategoryChips();
}

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
    category: { id: parseInt(els.txCategory.value, 10) },
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
    category: { id: parseInt(els.budgetCategory.value, 10) },
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

els.categoryForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const payload = {
    name: els.categoryName.value.trim(),
    type: els.categoryType.value,
    color: els.categoryType.value === 'INCOME' ? '#2F9E44' : '#E8590C',
  };
  try {
    await api('/categories', { method: 'POST', body: JSON.stringify(payload) });
    els.categoryForm.reset();
    await loadCategories();
  } catch (err) {
    alert(err.message);
  }
});

// ---------- init ----------

(async function init() {
  els.txDate.value = new Date().toISOString().slice(0, 10);
  renderMonthLabel();
  try {
    await loadCategories();
    await refreshAll();
  } catch (err) {
    console.error(err);
    els.receiptTape.innerHTML = `<p class="receipt-empty">Could not load your data.<br/>Try signing in again, or refresh the page.</p>`;
  }
})();