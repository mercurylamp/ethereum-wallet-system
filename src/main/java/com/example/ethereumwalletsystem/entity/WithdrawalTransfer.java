package com.example.ethereumwalletsystem.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Entity
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class WithdrawalTransfer extends Transfer {

    @NotNull
    private long gasPrice;

    @NotNull
    private long gasLimit = 21000;

    @NotNull
    private String address;

    public WithdrawalTransfer(BigInteger amount, long gasPrice, long gasLimit, String address, Wallet wallet, Transaction transaction, Currency currency) {
        super(amount, wallet, transaction, currency);
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.address = address;
    }
}
