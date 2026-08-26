package com.financetracker.controller;

import com.financetracker.model.Budget;
import com.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public List<Budget> getAll(@RequestParam(required = false) String month) {
        if (month != null && !month.isBlank()) {
            return budgetService.findForMonth(month);
        }
        return budgetService.findAll();
    }

    @GetMapping("/{id}")
    public Budget getOne(@PathVariable Long id) {
        return budgetService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Budget create(@Valid @RequestBody Budget budget) {
        return budgetService.create(budget);
    }

    @PutMapping("/{id}")
    public Budget update(@PathVariable Long id, @Valid @RequestBody Budget budget) {
        return budgetService.update(id, budget);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }
}
