package com.example.ethereumwalletsystem.entity;

import com.example.ethereumwalletsystem.constant.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@MappedSuperclass
@NoArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private UUID uuid = UUID.randomUUID();

    @NotNull
    private BigInteger amount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status = Status.pending;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @ToString.Exclude
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @ToString.Exclude
    private Currency currency;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    public Transfer(BigInteger amount, Wallet wallet, Transaction transaction, Currency currency) {
        this.amount = amount;
        this.wallet = wallet;
        this.transaction = transaction;
        this.currency = currency;
    }
}
