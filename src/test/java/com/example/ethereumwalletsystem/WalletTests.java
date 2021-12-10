package com.example.ethereumwalletsystem;

import com.example.ethereumwalletsystem.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestConstructor;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WalletTests {
    private final WalletService walletService;

    public WalletTests(WalletService walletService) {
        this.walletService = walletService;
    }


}
