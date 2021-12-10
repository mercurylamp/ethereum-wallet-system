package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CreateWalletResponse {
    private final UUID id;
    private final String label;
    private final String address;
    private final List<String> shares;
}
