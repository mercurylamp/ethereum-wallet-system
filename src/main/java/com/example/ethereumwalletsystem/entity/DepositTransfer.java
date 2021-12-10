package com.example.ethereumwalletsystem.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import java.math.BigInteger;

@Entity
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DepositTransfer extends Transfer {

    public DepositTransfer(BigInteger amount, Wallet wallet, Transaction transaction, Currency currency) {
        super(amount, wallet, transaction, currency);
    }
}
