package org.example.wallet.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import org.example.wallet.walletservice.entity.Currency;

import java.util.UUID;

public class CreateWalletRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private Currency currency;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}