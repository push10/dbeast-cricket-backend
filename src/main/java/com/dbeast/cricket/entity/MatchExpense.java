package com.dbeast.cricket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_expenses")
public class MatchExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "created_by_player_id", nullable = false)
    private Player createdBy;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean mandatoryForAvailablePlayers;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private Integer splitCount;

    @Column(nullable = false)
    private Double perPlayerAmount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @OneToMany(mappedBy = "matchExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchExpenseParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "matchExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchExpenseDiscount> discounts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Player getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Player createdBy) {
        this.createdBy = createdBy;
    }

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

    public Integer getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(Integer splitCount) {
        this.splitCount = splitCount;
    }

    public Double getPerPlayerAmount() {
        return perPlayerAmount;
    }

    public void setPerPlayerAmount(Double perPlayerAmount) {
        this.perPlayerAmount = perPlayerAmount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public List<MatchExpenseParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MatchExpenseParticipant> participants) {
        this.participants = participants;
    }

    public List<MatchExpenseDiscount> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<MatchExpenseDiscount> discounts) {
        this.discounts = discounts;
    }
}
