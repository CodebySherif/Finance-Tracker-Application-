package com.financetracker.service;

import com.financetracker.model.Budget;
import com.financetracker.repository.BudgetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public List<Budget> findForMonth(String month) {
        return budgetRepository.findByMonth(month);
    }

    public Budget findById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found with id " + id));
    }

    public Budget create(Budget budget) {
        budgetRepository.findByCategoryIdAndMonth(budget.getCategory().getId(), budget.getMonth())
                .ifPresent(b -> {
                    throw new IllegalArgumentException(
                            "A budget for this category already exists for " + budget.getMonth());
                });
        return budgetRepository.save(budget);
    }

    public Budget update(Long id, Budget updated) {
        Budget existing = findById(id);
        existing.setCategory(updated.getCategory());
        existing.setMonthlyLimit(updated.getMonthlyLimit());
        existing.setMonth(updated.getMonth());
        return budgetRepository.save(existing);
    }

    public void delete(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new EntityNotFoundException("Budget not found with id " + id);
        }
        budgetRepository.deleteById(id);
    }
}
