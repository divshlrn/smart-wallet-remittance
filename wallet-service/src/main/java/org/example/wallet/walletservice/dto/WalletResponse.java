package org.example.wallet.walletservice.dto;

import org.example.wallet.walletservice.entity.Currency;
import org.example.wallet.walletservice.entity.Wallet;
import org.example.wallet.walletservice.entity.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WalletResponse {

    private UUID id;
    private UUID userId;
    private Currency currency;
    private BigDecimal balance;
    private WalletStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public WalletResponse(){

    }

    public WalletResponse(
            UUID id,
            UUID userId,
            Currency currency,
            BigDecimal balance,
            WalletStatus status,
            Instant createdAt,
            Instant updatedAt) {

        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getStatus(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}