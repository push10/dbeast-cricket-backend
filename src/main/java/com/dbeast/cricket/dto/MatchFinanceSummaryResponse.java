package com.dbeast.cricket.dto;

public class MatchFinanceSummaryResponse {

    private final Double totalExpenses;
    private final Double totalContributions;
    private final Double balanceDifference;
    private final int expenseCount;
    private final int contributorCount;

    public MatchFinanceSummaryResponse(
            Double totalExpenses,
            Double totalContributions,
            Double balanceDifference,
            int expenseCount,
            int contributorCount
    ) {
        this.totalExpenses = totalExpenses;
        this.totalContributions = totalContributions;
        this.balanceDifference = balanceDifference;
        this.expenseCount = expenseCount;
        this.contributorCount = contributorCount;
    }

    public Double getTotalExpenses() {
        return totalExpenses;
    }

    public Double getTotalContributions() {
        return totalContributions;
    }

    public Double getBalanceDifference() {
        return balanceDifference;
    }

    public int getExpenseCount() {
        return expenseCount;
    }

    public int getContributorCount() {
        return contributorCount;
    }
}
