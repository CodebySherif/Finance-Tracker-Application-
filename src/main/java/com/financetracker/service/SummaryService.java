package com.financetracker.service;

import com.financetracker.dto.SummaryResponse;
import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionService transactionService;
    private final BudgetService budgetService;

    public SummaryResponse getSummary(Long userId, YearMonth month) {
        String monthKey = month.toString(); // "2026-07"
        List<Transaction> transactions = transactionService.findForMonth(userId, month);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        Optional<Budget> budgetOpt = budgetService.findForMonth(userId, monthKey);
        SummaryResponse.BudgetStatus status = null;

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            BigDecimal remaining = budget.getMonthlyLimit().subtract(totalExpense);
            double percentUsed = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) == 0
                    ? 0.0
                    : totalExpense.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();

            status = new SummaryResponse.BudgetStatus(
                    budget.getMonthlyLimit(),
                    totalExpense,
                    remaining,
                    percentUsed,
                    totalExpense.compareTo(budget.getMonthlyLimit()) > 0
            );
        }

        return new SummaryResponse(monthKey, totalIncome, totalExpense, balance, status);
    }
}
