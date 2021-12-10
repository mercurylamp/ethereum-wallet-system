package com.example.ethereumwalletsystem.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawalRequest {
    @NotEmpty(message = "currency must not be null or empty")
    private String currency;
    @NotEmpty(message = "address must not be null or empty")
    private String address;
    @NotNull(message = "amount must not be null")
    private BigInteger amount;
    @NotEmpty(message = "share must not be null or empty")
    private String share;
    private Long gasPrice;
    private Long gasLimit = 21000L;
}
