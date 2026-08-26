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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionService transactionService;
    private final BudgetService budgetService;

    public SummaryResponse getSummary(YearMonth month) {
        String monthKey = month.toString(); // "2026-07"
        List<Transaction> transactions = transactionService.findForMonth(month);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        List<Budget> budgets = budgetService.findForMonth(monthKey);
        List<SummaryResponse.BudgetStatus> statuses = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spent = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getCategory() != null
                            && t.getCategory().getId().equals(budget.getCategory().getId()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
            double percentUsed = budget.getMonthlyLimit().compareTo(BigDecimal.ZERO) == 0
                    ? 0.0
                    : spent.divide(budget.getMonthlyLimit(), 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();

            statuses.add(new SummaryResponse.BudgetStatus(
                    budget.getCategory().getId(),
                    budget.getCategory().getName(),
                    budget.getMonthlyLimit(),
                    spent,
                    remaining,
                    percentUsed,
                    spent.compareTo(budget.getMonthlyLimit()) > 0
            ));
        }

        return new SummaryResponse(monthKey, totalIncome, totalExpense, balance, statuses);
    }
}
