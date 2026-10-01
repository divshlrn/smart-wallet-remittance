package org.example.wallet.walletservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Version;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallet_user_currency",
                        columnNames = {"user_id", "currency"}
                )
        }
)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }

        if (status == null) {
            status = WalletStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public Currency getCurrency() {
        return currency;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public Long getVersion() {
        return version;
    }
}
