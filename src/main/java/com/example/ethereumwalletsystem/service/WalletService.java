package com.example.ethereumwalletsystem.service;

import com.example.ethereumwalletsystem.entity.Wallet;
import com.example.ethereumwalletsystem.payload.*;
import com.example.ethereumwalletsystem.repository.WalletRepository;
import com.example.ethereumwalletsystem.util.SecretSharingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CurrencyService currencyService;

    @Transactional(readOnly = true)
    public Wallet findMasterWalletByUuid(UUID uuid) {
        return walletRepository.findMasterWalletByUuid(uuid).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Wallet findFirstByUuidOrderByPathDesc(UUID uuid) {
        return walletRepository.findFirstByUuidOrderByPathDesc(uuid).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Wallet> findDepositWalletByUuid(UUID uuid) {
        return walletRepository.findDepositWalletByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public Set<String> getWalletAddresses() {
        return walletRepository.getWalletAddresses();
    }

    @Transactional
    public Wallet findByAddressForUpdate(String address) {
        return walletRepository.findByAddressForUpdate(address).orElseThrow();
    }

    @Transactional
    public Wallet findByUuidAndPathForUpdate(UUID uuid, int path) {
        return walletRepository.findByUuidAndPathForUpdate(uuid, path).orElseThrow();
    }

    @Transactional(readOnly = true)
    public GetWalletResponse getMasterWallet(UUID uuid) {
        Wallet wallet = findMasterWalletByUuid(uuid);
        return new GetWalletResponse(wallet.getUuid(), wallet.getLabel(), wallet.getAddress(), wallet.getBalance(), wallet.getConfirmedBalance(), wallet.getSpendableBalance());
    }

    @Transactional(readOnly = true)
    public GetAddressResponse getDepositAddress(UUID walletUuid, String address) {
        Wallet wallet = walletRepository.findByUuidAndAddress(walletUuid, address).orElseThrow();
        return new GetAddressResponse(wallet.getAddress(), wallet.getBalance(), wallet.getConfirmedBalance(), wallet.getSpendableBalance());
    }

    @Transactional
    public CreateDepositResponse createNewDeposit(UUID uuid, ShareRequest shareRequest) {
        Wallet wallet = findFirstByUuidOrderByPathDesc(uuid);

        String mnemonic = SecretSharingUtils.recover(List.of(wallet.getShare(), shareRequest.getShare()));
        int path = wallet.getPath() + 1;
        String address = SecretSharingUtils.getAddressFromMnemonic(mnemonic, path);

        Wallet newWallet = new Wallet();
        newWallet.setUuid(wallet.getUuid());
        newWallet.setAddress(address);
        newWallet.setLabel(wallet.getLabel());
        newWallet.setShare(wallet.getShare());
        newWallet.setPath(path);
        newWallet.setCurrency(wallet.getCurrency());
        walletRepository.save(newWallet);
        return new CreateDepositResponse(address);
    }

    @Transactional
    public CreateWalletResponse createNewWallet(CreateWalletRequest request) {
        String label = request.getLabel();
        String mnemonic = SecretSharingUtils.generateMnemonic();
        String address = SecretSharingUtils.getAddressFromMnemonic(mnemonic, 0);
        List<String> shares = SecretSharingUtils.split(mnemonic);
        Wallet wallet = createWalletEntity(label, address, shares.get(0));
        return new CreateWalletResponse(wallet.getUuid(), wallet.getLabel(), wallet.getAddress(), shares.subList(1, 3));
    }

    @Transactional
    public Wallet createWalletEntity(String label, String address, String share) {
        Wallet wallet = new Wallet(label, address, share);
        wallet.setCurrency(currencyService.getDefaultCurrency());
        return walletRepository.save(wallet);
    }
}
