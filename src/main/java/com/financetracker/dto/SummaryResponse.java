package com.financetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private String month;              // "2026-07"
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BudgetStatus budgetStatus; // null if no budget set for this month

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetStatus {
        private BigDecimal monthlyLimit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentUsed;
        private boolean overBudget;
    }
}
