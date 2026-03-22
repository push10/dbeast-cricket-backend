package com.dbeast.cricket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "wallet_recharge_requests")
public class WalletRechargeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "requested_by_id", nullable = false)
    private Player requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private Player approvedBy;

    @Column(nullable = false)
    private Double amount;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletRechargeStatus status = WalletRechargeStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletRechargeType requestType = WalletRechargeType.SELF_REQUEST;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column
    private LocalDate approvedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Player requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Player getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Player approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WalletRechargeStatus getStatus() {
        return status;
    }

    public void setStatus(WalletRechargeStatus status) {
        this.status = status;
    }

    public WalletRechargeType getRequestType() {
        return requestType;
    }

    public void setRequestType(WalletRechargeType requestType) {
        this.requestType = requestType;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }
}
