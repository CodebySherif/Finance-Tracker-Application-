package com.financetracker.service;

import com.financetracker.model.Budget;
import com.financetracker.model.User;
import com.financetracker.repository.BudgetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public List<Budget> findAll(Long userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Optional<Budget> findForMonth(Long userId, String month) {
        return budgetRepository.findByUserIdAndMonth(userId, month);
    }

    public Budget findById(Long userId, Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found with id " + id));
        if (!budget.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Budget not found with id " + id);
        }
        return budget;
    }

    /** Create, or update in place if this user already has a budget for that month. */
    public Budget create(Budget budget, User owner) {
        budget.setUser(owner);
        return budgetRepository.findByUserIdAndMonth(owner.getId(), budget.getMonth())
                .map(existing -> {
                    existing.setMonthlyLimit(budget.getMonthlyLimit());
                    return budgetRepository.save(existing);
                })
                .orElseGet(() -> budgetRepository.save(budget));
    }

    public Budget update(Long userId, Long id, Budget updated) {
        Budget existing = findById(userId, id);
        existing.setMonthlyLimit(updated.getMonthlyLimit());
        existing.setMonth(updated.getMonth());
        return budgetRepository.save(existing);
    }

    public void delete(Long userId, Long id) {
        findById(userId, id); // validates existence + ownership
        budgetRepository.deleteById(id);
    }
}
