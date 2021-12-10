package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
public class AddCurrencyRequest {
    @NotEmpty(message = "symbol must not be null or empty")
    private String symbol;
    @NotEmpty(message = "contract must not be null or empty")
    private String contract;
    private int decimal = 18;
}
