package com.dbeast.cricket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateMatchExpenseRequest {

    @NotBlank(message = "Expense title is required")
    private String title;

    @NotBlank(message = "Expense category is required")
    private String category;

    private boolean mandatoryForAvailablePlayers;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private Double totalAmount;

    private List<Long> participantPlayerIds;

    private List<MatchExpenseDiscountRequest> discounts;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isMandatoryForAvailablePlayers() {
        return mandatoryForAvailablePlayers;
    }

    public void setMandatoryForAvailablePlayers(boolean mandatoryForAvailablePlayers) {
        this.mandatoryForAvailablePlayers = mandatoryForAvailablePlayers;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<Long> getParticipantPlayerIds() {
        return participantPlayerIds;
    }

    public void setParticipantPlayerIds(List<Long> participantPlayerIds) {
        this.participantPlayerIds = participantPlayerIds;
    }

    public List<MatchExpenseDiscountRequest> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<MatchExpenseDiscountRequest> discounts) {
        this.discounts = discounts;
    }
}
