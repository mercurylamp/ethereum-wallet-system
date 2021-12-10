package com.example.ethereumwalletsystem;

import com.codahale.shamir.Scheme;
import com.example.ethereumwalletsystem.util.SecretSharingUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecretSharingUtilsTests {

    private final String secret = "science cycle again glance prosper brass attract profit obscure doll wild ahead empty group liquid useful scrub boat proud penalty capable wolf promote tissue";

    @Test
    @DisplayName("Shamir Secret Sharing Library Test Case")
    void shamirLibTest() {
        Scheme scheme = new Scheme(new SecureRandom(), 3, 2);

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        Map<Integer, byte[]> shares = scheme.split(secretBytes);

        Map<Integer, byte[]> onetwo = new HashMap<>();
        Map<Integer, byte[]> onethree = new HashMap<>();
        Map<Integer, byte[]> twothree = new HashMap<>();

        onetwo.put(1, shares.get(1));
        onetwo.put(2, shares.get(2));

        onethree.put(1, shares.get(1));
        onethree.put(3, shares.get(3));

        twothree.put(2, shares.get(2));
        twothree.put(3, shares.get(3));

        byte[] recovered1 = scheme.join(onetwo);
        byte[] recovered2 = scheme.join(onethree);
        byte[] recovered3 = scheme.join(twothree);

        Assertions.assertThat(recovered1).isEqualTo(secretBytes);
        Assertions.assertThat(recovered2).isEqualTo(secretBytes);
        Assertions.assertThat(recovered3).isEqualTo(secretBytes);
    }

    @Test
    @DisplayName("Secret Sharing Service Test Case")
    void secretSharingServiceTest() {
        List<String> shares = SecretSharingUtils.split(secret);

        for (String share : shares) {
            System.out.println(share);
        }

        String recovered1 = SecretSharingUtils.recover(shares.subList(0, 2));
        String recovered2 = SecretSharingUtils.recover(shares.subList(1, 3));
        String recovered3 = SecretSharingUtils.recover(shares);

        Assertions.assertThat(recovered1).isEqualTo(secret);
        Assertions.assertThat(recovered2).isEqualTo(secret);
        Assertions.assertThat(recovered3).isEqualTo(secret);

        String errorRecovered1 = SecretSharingUtils.recover(shares.subList(0, 1));
        String errorRecovered2 = SecretSharingUtils.recover(shares.subList(1, 2));

        Assertions.assertThat(errorRecovered1).isNotEqualTo(secret);
        Assertions.assertThat(errorRecovered2).isNotEqualTo(secret);
    }
}
