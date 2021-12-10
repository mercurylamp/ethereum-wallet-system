package com.example.ethereumwalletsystem;

import com.example.ethereumwalletsystem.entity.Currency;
import com.example.ethereumwalletsystem.service.CurrencyService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.util.List;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class EthereumWalletSystemApplicationTests {

	private final CurrencyService currencyService;

	EthereumWalletSystemApplicationTests(CurrencyService currencyService) {
		this.currencyService = currencyService;
	}

//	@Test
//	@DisplayName("Check Default Currency is Present")
//	void checkDefaultCurrencyExists() {
//		Currency currency = currencyService.getDefaultCurrency();
//		Assertions.assertThat(currency).isNotNull();
//	}
}
