package org.example.wallet.walletservice.service;

import org.example.wallet.walletservice.dto.CreateWalletRequest;
import org.example.wallet.walletservice.dto.WalletResponse;
import org.example.wallet.walletservice.entity.Wallet;
import org.example.wallet.walletservice.exception.ResourceNotFoundException;
import org.example.wallet.walletservice.exception.WalletAlreadyExistsException;
import org.example.wallet.walletservice.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.example.wallet.walletservice.entity.TransactionType;
import org.example.wallet.walletservice.repository.WalletTransactionRepository;
import org.example.wallet.walletservice.entity.IdempotencyRecord;
import org.example.wallet.walletservice.repository.IdempotencyRecordRepository;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.example.wallet.walletservice.dto.WalletTransactionResponse;
import org.example.wallet.walletservice.entity.WalletTransaction;


@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final WalletOperationProcessor walletOperationProcessor;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            WalletOperationProcessor walletOperationProcessor) {

        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.walletOperationProcessor = walletOperationProcessor;
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
    public WalletResponse creditWallet(
            UUID walletId, BigDecimal amount, String idempotencyKey) {

        try {
            return walletOperationProcessor.creditWallet(
                    walletId, amount, idempotencyKey);

        } catch (DataIntegrityViolationException ex) {
            return recoverIdempotentRequest(
                    walletId, amount, idempotencyKey,
                    TransactionType.CREDIT, ex);

        } catch (ObjectOptimisticLockingFailureException ex) {
            return recoverIdempotentRequest(
                    walletId, amount, idempotencyKey,
                    TransactionType.CREDIT, ex);
        }
    }


    @Override
    public WalletResponse debitWallet(
            UUID walletId, BigDecimal amount, String idempotencyKey) {

        try {
            return walletOperationProcessor.debitWallet(
                    walletId, amount, idempotencyKey);

        } catch (DataIntegrityViolationException ex) {
            return recoverIdempotentRequest(
                    walletId, amount, idempotencyKey,
                    TransactionType.DEBIT, ex);

        } catch (ObjectOptimisticLockingFailureException ex) {
            return recoverIdempotentRequest(
                    walletId, amount, idempotencyKey,
                    TransactionType.DEBIT, ex);
        }
    }



    private WalletResponse recoverIdempotentRequest(
            UUID walletId,
            BigDecimal amount,
            String idempotencyKey,
            TransactionType operationType,
            RuntimeException originalException) {

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {

            IdempotencyRecord record = idempotencyRecordRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElse(null);

            if (record != null) {
                boolean sameRequest =
                        record.getWalletId().equals(walletId)
                                && record.getOperationType() == operationType
                                && amount != null
                                && record.getAmount().compareTo(amount) == 0;

                if (sameRequest) {
                    Wallet wallet = walletRepository.findById(walletId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Wallet not found with id: " + walletId));

                    WalletResponse response = WalletResponse.from(wallet);
                    response.setBalance(record.getBalanceAfter());
                    return response;
                }

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key was already used for a different request");
            }
        }

        if (originalException instanceof ObjectOptimisticLockingFailureException) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Wallet was modified by another request. Please retry.",
                    originalException);
        }

        throw originalException;
    }


    @Override
    public List<WalletTransactionResponse> getWalletTransactions(UUID walletId) {

        // Verify that the wallet exists.
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException(
                    "Wallet not found with id: " + walletId);
        }

        return walletTransactionRepository
                .findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(WalletTransactionResponse::from)
                .toList();
    }


}