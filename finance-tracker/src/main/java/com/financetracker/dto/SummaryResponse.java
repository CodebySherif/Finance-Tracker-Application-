package com.financetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private String month;              // "2026-07"
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private List<BudgetStatus> budgetStatuses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetStatus {
        private Long categoryId;
        private String categoryName;
        private BigDecimal monthlyLimit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentUsed;
        private boolean overBudget;
    }
}
