package org.example.wallet.walletservice.exception;

public class WalletAlreadyExistsException extends RuntimeException {

    public WalletAlreadyExistsException(String message) {
        super(message);
    }
}