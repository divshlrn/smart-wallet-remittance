package org.example.wallet.walletservice.service;

import org.example.wallet.walletservice.dto.CreateWalletRequest;
import org.example.wallet.walletservice.dto.WalletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest request);

    WalletResponse getWallet(UUID walletId);

    List<WalletResponse> getWalletsByUser(UUID userId);

    WalletResponse creditWallet(UUID walletId, BigDecimal amount);

    WalletResponse debitWallet(UUID walletId, BigDecimal amount);
}