package com.example.ethereumwalletsystem.controller;

import com.example.ethereumwalletsystem.payload.ShareRequest;
import com.example.ethereumwalletsystem.payload.WithdrawalRequest;
import com.example.ethereumwalletsystem.payload.WithdrawalResponse;
import com.example.ethereumwalletsystem.service.WithdrawalTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class TransferController {
    private final WithdrawalTransferService withdrawalTransferService;

    @PostMapping("/{walletId}/send")
    public ResponseEntity<WithdrawalResponse> send(
        @PathVariable UUID walletId,
        @Valid @RequestBody WithdrawalRequest withdrawalRequest
    ) {
        WithdrawalResponse response = withdrawalTransferService.createNewWithdrawalTransfer(walletId, withdrawalRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/flush")
    public ResponseEntity<List<WithdrawalResponse>> flush(
        @PathVariable UUID walletId,
        @Valid @RequestBody ShareRequest shareRequest
    ) {
        List<WithdrawalResponse> response = withdrawalTransferService.flush(walletId, shareRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
