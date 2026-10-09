package com.financetracker.service;

import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public List<Transaction> findAll(Long userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    public List<Transaction> findForMonth(Long userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
    }

    public Transaction findById(Long userId, Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id " + id));
        assertOwnership(tx.getUser().getId(), userId, "Transaction", id);
        return tx;
    }

    public Transaction create(Transaction transaction, User owner) {
        transaction.setUser(owner);
        return transactionRepository.save(transaction);
    }

    public Transaction update(Long userId, Long id, Transaction updated) {
        Transaction existing = findById(userId, id);
        existing.setDescription(updated.getDescription());
        existing.setAmount(updated.getAmount());
        existing.setType(updated.getType());
        existing.setDate(updated.getDate());
        return transactionRepository.save(existing);
    }

    public void delete(Long userId, Long id) {
        findById(userId, id); // validates existence + ownership
        transactionRepository.deleteById(id);
    }

    private void assertOwnership(Long ownerId, Long requesterId, String entity, Long id) {
        if (!ownerId.equals(requesterId)) {
            // 404, not 403 - don't reveal that another user's record exists
            throw new EntityNotFoundException(entity + " not found with id " + id);
        }
    }
}
