package com.financetracker.repository;

import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);

    List<Transaction> findByTypeAndDateBetween(TransactionType type, LocalDate start, LocalDate end);

    List<Transaction> findByCategoryIdAndDateBetween(Long categoryId, LocalDate start, LocalDate end);

    List<Transaction> findAllByOrderByDateDesc();
}
