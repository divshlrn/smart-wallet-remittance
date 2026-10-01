package org.example.wallet.walletservice.service;

import org.example.wallet.walletservice.dto.CreateWalletRequest;
import org.example.wallet.walletservice.dto.WalletResponse;
import org.example.wallet.walletservice.entity.Wallet;
import org.example.wallet.walletservice.entity.WalletStatus;
import org.example.wallet.walletservice.exception.InsufficientBalanceException;
import org.example.wallet.walletservice.exception.ResourceNotFoundException;
import org.example.wallet.walletservice.exception.WalletAlreadyExistsException;
import org.example.wallet.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.wallet.walletservice.entity.TransactionType;
import org.example.wallet.walletservice.entity.WalletTransaction;
import org.example.wallet.walletservice.repository.WalletTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    public WalletResponse createWallet(CreateWalletRequest request) {

        boolean alreadyExists =
                walletRepository.existsByUserIdAndCurrency(
                        request.getUserId(),
                        request.getCurrency()
                );

        if (alreadyExists) {
            throw new WalletAlreadyExistsException(
                    "Wallet already exists for this user and currency"
            );
        }

        Wallet wallet = new Wallet();

        wallet.setUserId(request.getUserId());
        wallet.setCurrency(request.getCurrency());

        Wallet savedWallet = walletRepository.save(wallet);

        return WalletResponse.from(savedWallet);
    }

    @Override
    public WalletResponse getWallet(UUID walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wallet not found: " + walletId
                        )
                );

        return WalletResponse.from(wallet);
    }

    @Override
    public List<WalletResponse> getWalletsByUser(UUID userId) {

        return walletRepository.findByUserId(userId)
                .stream()
                .map(WalletResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public WalletResponse creditWallet(UUID walletId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Credit amount must be greater than zero"
            );
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found with id: " + walletId
                        )
                );

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot credit an inactive wallet"
            );
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        wallet.setBalance(balanceAfter);

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(savedWallet.getId());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);

        walletTransactionRepository.save(transaction);

        return mapToResponse(savedWallet);
    }

    private WalletResponse mapToResponse(Wallet wallet) {

        WalletResponse response = new WalletResponse();

        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setCurrency(wallet.getCurrency());
        response.setBalance(wallet.getBalance());
        response.setStatus(wallet.getStatus());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());

        return response;
    }

    @Override
    @Transactional
    public WalletResponse debitWallet(UUID walletId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Debit amount must be greater than zero"
            );
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found with id: " + walletId
                        )
                );

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Wallet is not active"
            );
        }

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for this transaction"
            );
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(savedWallet.getId());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);

        walletTransactionRepository.save(transaction);

        return mapToResponse(savedWallet);
    }


}