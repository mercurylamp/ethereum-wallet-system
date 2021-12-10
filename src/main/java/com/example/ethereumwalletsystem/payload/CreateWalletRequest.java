package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
public class CreateWalletRequest {
    @NotEmpty(message = "label must not be null or empty")
    private String label;
}
