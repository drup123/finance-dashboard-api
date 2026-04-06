package com.finance.dashboard.dto;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


public class DashboardSummary {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;

    // category -> { INCOME: amount, EXPENSE: amount }
    private Map<String, Map<String, BigDecimal>> categoryWiseTotals;

    public BigDecimal getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(BigDecimal totalIncome) {
		this.totalIncome = totalIncome;
	}

	public BigDecimal getTotalExpenses() {
		return totalExpenses;
	}

	public void setTotalExpenses(BigDecimal totalExpenses) {
		this.totalExpenses = totalExpenses;
	}

	public BigDecimal getNetBalance() {
		return netBalance;
	}

	public void setNetBalance(BigDecimal netBalance) {
		this.netBalance = netBalance;
	}

	public Map<String, Map<String, BigDecimal>> getCategoryWiseTotals() {
		return categoryWiseTotals;
	}

	public void setCategoryWiseTotals(Map<String, Map<String, BigDecimal>> categoryWiseTotals) {
		this.categoryWiseTotals = categoryWiseTotals;
	}

	public Map<String, Map<String, BigDecimal>> getMonthlyTrends() {
		return monthlyTrends;
	}

	public void setMonthlyTrends(Map<String, Map<String, BigDecimal>> monthlyTrends) {
		this.monthlyTrends = monthlyTrends;
	}

	public List<RecordResponse> getRecentActivity() {
		return recentActivity;
	}

	public void setRecentActivity(List<RecordResponse> recentActivity) {
		this.recentActivity = recentActivity;
	}

	// "YYYY-MM" -> { INCOME: amount, EXPENSE: amount }
    private Map<String, Map<String, BigDecimal>> monthlyTrends;

    private List<RecordResponse> recentActivity;
}
