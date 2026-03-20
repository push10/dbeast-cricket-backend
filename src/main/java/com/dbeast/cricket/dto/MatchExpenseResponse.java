package com.dbeast.cricket.dto;

import java.time.LocalDate;
import java.util.List;

public class MatchExpenseResponse {

    private final Long id;
    private final String title;
    private final String category;
    private final boolean mandatoryForAvailablePlayers;
    private final Double totalAmount;
    private final Integer splitCount;
    private final Double perPlayerAmount;
    private final LocalDate expenseDate;
    private final String createdByName;
    private final List<MatchExpenseParticipantResponse> participants;
    private final List<MatchExpenseDiscountResponse> discounts;

    public MatchExpenseResponse(
            Long id,
            String title,
            String category,
            boolean mandatoryForAvailablePlayers,
            Double totalAmount,
            Integer splitCount,
            Double perPlayerAmount,
            LocalDate expenseDate,
            String createdByName,
            List<MatchExpenseParticipantResponse> participants,
            List<MatchExpenseDiscountResponse> discounts
    ) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.mandatoryForAvailablePlayers = mandatoryForAvailablePlayers;
        this.totalAmount = totalAmount;
        this.splitCount = splitCount;
        this.perPlayerAmount = perPlayerAmount;
        this.expenseDate = expenseDate;
        this.createdByName = createdByName;
        this.participants = participants;
        this.discounts = discounts;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public boolean isMandatoryForAvailablePlayers() {
        return mandatoryForAvailablePlayers;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Integer getSplitCount() {
        return splitCount;
    }

    public Double getPerPlayerAmount() {
        return perPlayerAmount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public List<MatchExpenseParticipantResponse> getParticipants() {
        return participants;
    }

    public List<MatchExpenseDiscountResponse> getDiscounts() {
        return discounts;
    }
}
