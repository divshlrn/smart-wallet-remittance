
package org.example.wallet.walletservice.service;

import org.example.wallet.walletservice.dto.WalletResponse;
import org.example.wallet.walletservice.entity.IdempotencyRecord;
import org.example.wallet.walletservice.entity.TransactionType;
import org.example.wallet.walletservice.entity.Wallet;
import org.example.wallet.walletservice.entity.WalletStatus;
import org.example.wallet.walletservice.entity.WalletTransaction;
import org.example.wallet.walletservice.exception.InsufficientBalanceException;
import org.example.wallet.walletservice.exception.ResourceNotFoundException;
import org.example.wallet.walletservice.repository.IdempotencyRecordRepository;
import org.example.wallet.walletservice.repository.WalletRepository;
import org.example.wallet.walletservice.repository.WalletTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletOperationProcessor {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public WalletOperationProcessor(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdempotencyRecordRepository idempotencyRecordRepository) {

        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    @Transactional
    public WalletResponse creditWallet(
            UUID walletId, BigDecimal amount, String idempotencyKey) {

        validateRequest(amount, idempotencyKey, "Credit");

        IdempotencyRecord existing = findExistingRequest(
                idempotencyKey, walletId, amount, TransactionType.CREDIT);

        if (existing != null) {
            return responseFromExistingRequest(existing);
        }

        Wallet wallet = findWallet(walletId);
        validateActiveWallet(wallet);

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        // Claim the idempotency key before changing the wallet.
        saveIdempotencyRecord(
                idempotencyKey, walletId, TransactionType.CREDIT,
                amount, balanceAfter);

        wallet.setBalance(balanceAfter);
        Wallet savedWallet = walletRepository.save(wallet);

        saveLedgerEntry(
                savedWallet.getId(), TransactionType.CREDIT,
                amount, balanceBefore, balanceAfter);

        return mapToResponse(savedWallet);
    }

    @Transactional
    public WalletResponse debitWallet(
            UUID walletId, BigDecimal amount, String idempotencyKey) {

        validateRequest(amount, idempotencyKey, "Debit");

        IdempotencyRecord existing = findExistingRequest(
                idempotencyKey, walletId, amount, TransactionType.DEBIT);

        if (existing != null) {
            return responseFromExistingRequest(existing);
        }

        Wallet wallet = findWallet(walletId);
        validateActiveWallet(wallet);

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for this transaction");
        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // Claim the idempotency key before changing the wallet.
        saveIdempotencyRecord(
                idempotencyKey, walletId, TransactionType.DEBIT,
                amount, balanceAfter);

        wallet.setBalance(balanceAfter);
        Wallet savedWallet = walletRepository.save(wallet);

        saveLedgerEntry(
                savedWallet.getId(), TransactionType.DEBIT,
                amount, balanceBefore, balanceAfter);

        return mapToResponse(savedWallet);
    }

    private void validateRequest(
            BigDecimal amount, String idempotencyKey, String operation) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    operation + " amount must be greater than zero");
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must contain 1 to 100 characters");
        }
    }

    private Wallet findWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found with id: " + walletId));
    }

    private void validateActiveWallet(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Wallet is not active");
        }
    }

    private IdempotencyRecord findExistingRequest(
            String idempotencyKey,
            UUID walletId,
            BigDecimal amount,
            TransactionType operationType) {

        return idempotencyRecordRepository
                .findByIdempotencyKey(idempotencyKey)
                .map(record -> {
                    boolean sameRequest =
                            record.getWalletId().equals(walletId)
                                    && record.getOperationType() == operationType
                                    && record.getAmount().compareTo(amount) == 0;

                    if (!sameRequest) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Idempotency key was already used "
                                        + "for a different request");
                    }

                    return record;
                })
                .orElse(null);
    }

    private void saveIdempotencyRecord(
            String idempotencyKey,
            UUID walletId,
            TransactionType operationType,
            BigDecimal amount,
            BigDecimal balanceAfter) {

        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        record.setWalletId(walletId);
        record.setOperationType(operationType);
        record.setAmount(amount);
        record.setBalanceAfter(balanceAfter);

        // Force the INSERT now so a duplicate key is detected here.
        idempotencyRecordRepository.saveAndFlush(record);
    }

    private void saveLedgerEntry(
            UUID walletId,
            TransactionType transactionType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(walletId);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);

        walletTransactionRepository.save(transaction);
    }

    private WalletResponse responseFromExistingRequest(
            IdempotencyRecord record) {

        Wallet wallet = findWallet(record.getWalletId());
        WalletResponse response = mapToResponse(wallet);

        // Preserve the balance produced by the original operation.
        response.setBalance(record.getBalanceAfter());

        return response;
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
}
