package com.financetracker.controller;

import com.financetracker.model.Transaction;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<Transaction> getAll(@AuthenticationPrincipal UserPrincipal principal,
                                     @RequestParam(required = false) String month) {
        if (month != null && !month.isBlank()) {
            return transactionService.findForMonth(principal.getUserId(), YearMonth.parse(month));
        }
        return transactionService.findAll(principal.getUserId());
    }

    @GetMapping("/{id}")
    public Transaction getOne(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return transactionService.findById(principal.getUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody Transaction transaction) {
        return transactionService.create(transaction, principal.getUser());
    }

    @PutMapping("/{id}")
    public Transaction update(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long id, @Valid @RequestBody Transaction transaction) {
        return transactionService.update(principal.getUserId(), id, transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        transactionService.delete(principal.getUserId(), id);
    }
}
