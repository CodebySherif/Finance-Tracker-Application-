package com.financetracker.controller;

import com.financetracker.model.Budget;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<Budget> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        return budgetService.findAll(principal.getUserId());
    }

    @GetMapping("/{id}")
    public Budget getOne(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return budgetService.findById(principal.getUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Budget create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody Budget budget) {
        return budgetService.create(budget, principal.getUser());
    }

    @PutMapping("/{id}")
    public Budget update(@AuthenticationPrincipal UserPrincipal principal,
                          @PathVariable Long id, @Valid @RequestBody Budget budget) {
        return budgetService.update(principal.getUserId(), id, budget);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        budgetService.delete(principal.getUserId(), id);
    }
}
