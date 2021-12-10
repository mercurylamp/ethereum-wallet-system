package com.example.ethereumwalletsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EthereumWalletSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EthereumWalletSystemApplication.class, args);
	}

}
