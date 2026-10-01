package org.example.wallet.walletservice.repository;

import org.example.wallet.walletservice.entity.Currency;
import org.example.wallet.walletservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository
        extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserIdAndCurrency(UUID userId, Currency currency);

    List<Wallet> findByUserId(UUID userId);

    boolean existsByUserIdAndCurrency(UUID userId, Currency currency);
}
