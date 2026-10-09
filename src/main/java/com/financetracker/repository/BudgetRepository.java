package com.financetracker.repository;

import com.financetracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserIdAndMonth(Long userId, String month);

    List<Budget> findByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
