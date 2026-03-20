package com.dbeast.cricket.dto;

import java.util.List;

public class MatchFinanceOverviewResponse {

    private final MatchFinanceSummaryResponse summary;
    private final List<MatchExpenseResponse> expenses;
    private final List<MatchContributionResponse> contributions;
    private final List<MatchFinancePlayerResponse> players;

    public MatchFinanceOverviewResponse(
            MatchFinanceSummaryResponse summary,
            List<MatchExpenseResponse> expenses,
            List<MatchContributionResponse> contributions,
            List<MatchFinancePlayerResponse> players
    ) {
        this.summary = summary;
        this.expenses = expenses;
        this.contributions = contributions;
        this.players = players;
    }

    public MatchFinanceSummaryResponse getSummary() {
        return summary;
    }

    public List<MatchExpenseResponse> getExpenses() {
        return expenses;
    }

    public List<MatchContributionResponse> getContributions() {
        return contributions;
    }

    public List<MatchFinancePlayerResponse> getPlayers() {
        return players;
    }
}
