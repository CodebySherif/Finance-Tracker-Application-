package com.financetracker.controller;

import com.financetracker.dto.SummaryResponse;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    public SummaryResponse getSummary(@AuthenticationPrincipal UserPrincipal principal,
                                       @RequestParam(required = false) String month) {
        YearMonth ym = (month != null && !month.isBlank()) ? YearMonth.parse(month) : YearMonth.now();
        return summaryService.getSummary(principal.getUserId(), ym);
    }
}
