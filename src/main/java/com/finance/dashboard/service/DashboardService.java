package com.finance.dashboard.service;

import com.finance.dashboard.dto.DashboardSummary;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public DashboardService(FinancialRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {

        BigDecimal totalIncome  = recordRepository.sumIncome();
        BigDecimal totalExpense = recordRepository.sumExpense();

        // Handle nulls (important!)
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        Map<String, Map<String, BigDecimal>> categoryWise = buildCategoryWise();
        Map<String, Map<String, BigDecimal>> monthly      = buildMonthlyTrends();

        List<RecordResponse> recent = recordRepository
                .findRecentActivity(PageRequest.of(0, 10))
                .stream()
                .map(RecordResponse::from)
                .toList();

        // ❌ removed builder → ✅ using setters
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpense);
        summary.setNetBalance(netBalance);
        summary.setCategoryWiseTotals(categoryWise);
        summary.setMonthlyTrends(monthly);
        summary.setRecentActivity(recent);

        return summary;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Map<String, Map<String, BigDecimal>> buildCategoryWise() {
        List<Object[]> rows = recordRepository.categoryWiseTotals();
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String category  = (String) row[0];
            String type      = row[1].toString();
            BigDecimal total = (BigDecimal) row[2];

            result.computeIfAbsent(category, k -> new LinkedHashMap<>())
                  .put(type, total);
        }
        return result;
    }

    private Map<String, Map<String, BigDecimal>> buildMonthlyTrends() {
        List<Object[]> rows = recordRepository.monthlyTrends();
        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            int year         = ((Number) row[0]).intValue();
            int month        = ((Number) row[1]).intValue();
            String type      = row[2].toString();
            BigDecimal total = (BigDecimal) row[3];

            String key = String.format("%d-%02d", year, month);

            result.computeIfAbsent(key, k -> new LinkedHashMap<>())
                  .put(type, total);
        }
        return result;
    }
}