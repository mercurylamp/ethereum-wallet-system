package com.example.ethereumwalletsystem.controller;

import com.example.ethereumwalletsystem.payload.AddCurrencyRequest;
import com.example.ethereumwalletsystem.payload.AddCurrencyResponse;
import com.example.ethereumwalletsystem.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @PostMapping
    public ResponseEntity<AddCurrencyResponse> add(
        @Valid @RequestBody AddCurrencyRequest addCurrencyRequest
    ) {
        AddCurrencyResponse response = currencyService.addCurrency(addCurrencyRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
