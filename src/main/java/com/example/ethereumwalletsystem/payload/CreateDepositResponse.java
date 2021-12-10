package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateDepositResponse {
    private final String address;
}
