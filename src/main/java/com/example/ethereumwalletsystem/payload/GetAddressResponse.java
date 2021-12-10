package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Getter
@RequiredArgsConstructor
public class GetAddressResponse {
    private final String address;
    private final BigInteger balance;
    private final BigInteger confirmedBalance;
    private final BigInteger spendableBalance;
}
