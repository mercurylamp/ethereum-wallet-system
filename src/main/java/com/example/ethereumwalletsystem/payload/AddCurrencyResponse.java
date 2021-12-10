package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class AddCurrencyResponse {
    private final UUID id;
    private final String symbol;
    private final String contract;
    private final int decimal;
}
