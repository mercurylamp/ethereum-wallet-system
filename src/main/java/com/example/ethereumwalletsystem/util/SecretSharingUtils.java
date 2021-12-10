package com.example.ethereumwalletsystem.util;

import com.codahale.shamir.Scheme;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT;

public final class SecretSharingUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Scheme SCHEME = new Scheme(secureRandom, 3, 2);

    public static List<String> split(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        Map<Integer, byte[]> shares = SCHEME.split(secretBytes);

        List<String> result = new ArrayList<>();

        for (Integer key : shares.keySet()) {
            result.add(String.format("%d-%s", key, Base64.getEncoder().encodeToString(shares.get(key))));
        }

        return result;
    }

    public static String recover(List<String> shares) {
        Map<Integer, byte[]> splits = new HashMap<>();

        for (String share : shares) {
            String[] keyAndShare = share.split("-");
            splits.put(Integer.parseInt(keyAndShare[0]), Base64.getDecoder().decode(keyAndShare[1]));
        }

        byte[] recovered = SCHEME.join(splits);

        return new String(recovered, StandardCharsets.UTF_8);
    }

    public static String getAddressFromMnemonic(String mnemonic, int path) {
        Credentials credentials = getCredentialsFromMnemonic(mnemonic, path);
        return credentials.getAddress();
    }

    public static Credentials getCredentialsFromMnemonic(String mnemonic, int path) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        Bip32ECKeyPair masterWalletKeyPair = Bip32ECKeyPair.deriveKeyPair(master, new int[]{44 | HARDENED_BIT, 60 | HARDENED_BIT, HARDENED_BIT, 0, path});
        return Credentials.create(masterWalletKeyPair);
    }

    public static String generateMnemonic() {
        byte[] initialEntropy = secureRandom.generateSeed(32);
        return MnemonicUtils.generateMnemonic(initialEntropy);
    }
}
