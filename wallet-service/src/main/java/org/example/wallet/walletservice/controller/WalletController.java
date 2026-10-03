package org.example.wallet.walletservice.controller;

import jakarta.validation.Valid;
import org.example.wallet.walletservice.dto.*;
import org.example.wallet.walletservice.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {

        WalletResponse response = walletService.createWallet(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable UUID walletId) {

        return ResponseEntity.ok(
                walletService.getWallet(walletId)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletResponse>> getWalletsByUser(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                walletService.getWalletsByUser(userId)
        );
    }

    @PostMapping("/{walletId}/credit")
    public ResponseEntity<WalletResponse> creditWallet(
            @PathVariable UUID walletId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreditWalletRequest request) {

        WalletResponse response = walletService.creditWallet(
                walletId,
                request.getAmount(),
                idempotencyKey
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{walletId}/debit")
    public ResponseEntity<WalletResponse> debitWallet(
            @PathVariable UUID walletId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DebitWalletRequest request) {

        WalletResponse response = walletService.debitWallet(
                walletId,
                request.getAmount(),
                idempotencyKey
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getWalletTransactions(
            @PathVariable UUID walletId) {

        return ResponseEntity.ok(
                walletService.getWalletTransactions(walletId));
    }

}