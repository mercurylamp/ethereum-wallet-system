package com.example.ethereumwalletsystem.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Entity
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private UUID uuid = UUID.randomUUID();

    private String label;

    @NotNull
    private String address;

    @NotNull
    @ToString.Exclude
    private String share;

    @NotNull
    private BigInteger balance = BigInteger.ZERO;

    @NotNull
    private BigInteger confirmedBalance = BigInteger.ZERO;

    @NotNull
    private BigInteger spendableBalance = BigInteger.ZERO;

    private long nonce = 0;

    private int path = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @ToString.Exclude
    private Currency currency;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    public Wallet(String label, String address, String share) {
        this.label = label;
        this.address = address;
        this.share = share;
    }
}
