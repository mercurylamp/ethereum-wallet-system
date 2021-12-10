package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class GetWalletResponse {
    private final UUID id;
    private final String label;
    private final String address;
    private final BigInteger balance;
    private final BigInteger confirmedBalance;
    private final BigInteger spendableBalance;
}
