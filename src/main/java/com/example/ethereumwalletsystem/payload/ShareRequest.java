package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@NoArgsConstructor
public class ShareRequest {
    @NotEmpty(message = "share must not be null or empty")
    private String share;
}
