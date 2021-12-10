package com.example.ethereumwalletsystem.controller;

import com.example.ethereumwalletsystem.payload.*;
import com.example.ethereumwalletsystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<CreateWalletResponse> create(
        @Valid @RequestBody CreateWalletRequest createWalletRequest
    ) {
        CreateWalletResponse response = walletService.createNewWallet(createWalletRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{walletId}/address")
    public ResponseEntity<CreateDepositResponse> createDepositAddress(
        @PathVariable UUID walletId,
        @Valid @RequestBody ShareRequest shareRequest
    ) {
        CreateDepositResponse response = walletService.createNewDeposit(walletId, shareRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<GetWalletResponse> getById(
        @PathVariable UUID walletId
    ) {
        GetWalletResponse response = walletService.getMasterWallet(walletId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{walletId}/address/{address}")
    public ResponseEntity<GetAddressResponse> getByAddress(
        @PathVariable UUID walletId,
        @PathVariable String address
    ) {
        GetAddressResponse response = walletService.getDepositAddress(walletId, address);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
