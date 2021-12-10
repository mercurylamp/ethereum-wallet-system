package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.constant.Constants;
import com.example.ethereumwalletsystem.entity.Currency;
import com.example.ethereumwalletsystem.payload.AddCurrencyRequest;
import com.example.ethereumwalletsystem.payload.AddCurrencyResponse;
import com.example.ethereumwalletsystem.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    @Transactional
    public AddCurrencyResponse addCurrency(AddCurrencyRequest request) {
        String symbol = request.getSymbol();
        String contract = request.getContract();
        int decimal = request.getDecimal();
        Currency currency = createCurrencyEntity(symbol, contract, decimal);
        return new AddCurrencyResponse(currency.getUuid(), currency.getSymbol(), currency.getContract(), currency.getDecimal());
    }

    @Transactional
    public Currency createCurrencyEntity(String symbol, String contract, int decimal) {
        Currency currency = new Currency(symbol, contract, decimal);
        return currencyRepository.save(currency);
    }

    @Transactional(readOnly = true)
    List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Transactional(readOnly = true)
    Currency getCurrencyBySymbol(String symbol) {
        return currencyRepository.findBySymbol(symbol).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Currency getDefaultCurrency() {
        return currencyRepository.findBySymbol(Constants.DEFAULT_CURRENCY).orElseThrow();
    }

    @PostConstruct
    void setDefaultCurrency() {
        log.info("Checking default currency (eth)");
        Optional<Currency> currencyOptional = currencyRepository.findBySymbol(Constants.DEFAULT_CURRENCY);
        if (currencyOptional.isEmpty()) {
            createCurrencyEntity(Constants.DEFAULT_CURRENCY, null, 18);
            log.info("Default currency set (eth)");
        }
    }
}
