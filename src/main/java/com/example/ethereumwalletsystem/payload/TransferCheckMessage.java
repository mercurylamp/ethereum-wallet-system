package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransferCheckMessage {
    private Long transferId;
    private Long transactionId;
    private String transactionHash;
}
