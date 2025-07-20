package org.payetonkawa.auth.auth_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        String privateKeyContent = "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAQCmsnEcLkpaecn5"
                + "pZne0WQEVcI35SJUtQzWxJhjzD+qxE8qiXcsrNOOFiUjySXbpEfFgADhfWcV4/gm"
                + "Kwm3xvMZM53FIcNtnT7U6WLo8LoM4lrSg9/WGOMR253rftmakLIv31f1nH43WkSq"
                + "aP2iII89RawmzfEhK4YeBEM+sFbCdXdUp3p0OA/A/R1tfrD5tyr5nrszDbpEHJEf"
                + "2eqrG3uytXxW4MI1Bp0dz4exd8c2HxdwzIRRL6pOanB25fkYkUzzj0u27X2gIx7l"
                + "YuoO0QwIeavFDdgVaJinc9/VUj8evXSbDcJEFkwcevCg1al9jcCGb+WIlyRek5Fd"
                + "Bv0SWWL3AgMBAAECgf9UDL/7YVu9yYIu63Jf3/4pCPmn5GB/OnmxSZdxx9SIJC/h"
                + "cee5OdbPxtPzIwZLONQE1UkVrEbbXbGfciFSjyzN0kz3H1kgNd7Ce6dqRYqyD1Sg"
                + "+N07RV6W3x5OfYV96Z+4rAX3yoscrpY0Esl/F2Tr5ROrb7IQ2FOzLSI0BxPtpvWp"
                + "ijMTj5/WnpMrCJfO+L3YAdFhOqnWI1aGkODXFgt3/LrA4MiLs9UT2bmfA2Mo3lHA"
                + "12aqAoA90WF1aL0p+iuMnR2uTyPBA4/Kb/UgfjD2Y+VSnx6whg0GrGqKb2bGrZEi"
                + "fKPfKjDpMmTFdmamYiYH605A/obLF21s0wAiCrECgYEA0mB/5WT6IgwDnvrVbIza"
                + "W2q8MDv9QW3WKLqHsNtUXNvgbdoKbrf0oAYjK2LEvVGEN2YrH2wqWJ2I0YElJ3QG"
                + "wP9KxuL8NS+9+A1S4RcfzUSsxNWSgMey+X9mt2pNqrIfyONzZvWsAG51++8Y4GBk"
                + "cUl9+9NduLQiHd6Y6GePJzECgYEAytj2vnpYJeh5U6eTxLZgcXUUK2rw3M93YgRU"
                + "tnmTXZ8Ui8rOlLkDq0ZV2lqzVb/7KIDJYUhPQq7xtvEOI/XllbaxUuwmxRqnH0xY"
                + "+enNofcZiiJIM9/tQDnE4fOwgl1SngyRkLfCbVmI3csFxep+T9NU07SGa9N+HQgV"
                + "CGHzcqcCgYEAr7E/nogyDXUTIRZCXyFSyr3c5Dt1Scs86068O4swbUyDpbpOtCCU"
                + "/3+gp38Y5zLIxKtioB9pThBiTvUX3lcbvNp3zJmMH677a8lUyf7R5E/SKaBjdYFR"
                + "iuRHGC6DJmcT/GVX30iUmNenWPtZPynBOIsz17exsateQqu/73dtKkECgYAdJBQ2"
                + "gw78yyv6550j6mtqgT5XinJ11lw5OZQN7uNL1Nm3x3fsB7ROO7mDt/oBfKYRSIcl"
                + "saSpWMai2HiGlAHagzdwSTq8oDqYFXjG1l7TQNNrc8dZpHbJ/kKZj0+xOsTurbGP"
                + "z8ZfvTgbXNpEd46T2zFZhJq3DNgWRZ336VJ3CwKBgBt7wTi7NpGatyaH71PLZrDm"
                + "lNQLec7JU2Doz3LI2hOLqSUr4Nz4D6d2vGBX717K/McVNFH97X7bf4b2ITGZB3QQ"
                + "DQhnN41BCIqYOHgV5lmDvAtYIMaJ0IWMs6/QLxkobTP4nNMMAceO8DjjTXsoGBPq"
                + "lovNh3yAl4V2CB5VuEiE";

        String publicKeyContent = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAprJxHC5KWnnJ+aWZ3tFk"
                + "BFXCN+UiVLUM1sSYY8w/qsRPKol3LKzTjhYlI8kl26RHxYAA4X1nFeP4JisJt8bz"
                + "GTOdxSHDbZ0+1Oli6PC6DOJa0oPf1hjjEdud637ZmpCyL99X9Zx+N1pEqmj9oiCP"
                + "PUWsJs3xISuGHgRDPrBWwnV3VKd6dDgPwP0dbX6w+bcq+Z67Mw26RByRH9nqqxt7"
                + "srV8VuDCNQadHc+HsXfHNh8XcMyEUS+qTmpwduX5GJFM849Ltu19oCMe5WLqDtEM"
                + "CHmrxQ3YFWiYp3Pf1VI/Hr10mw3CRBZMHHrwoNWpfY3Ahm/liJckXpORXQb9Elli"
                + "9wIDAQAB";

        byte[] privateBytes = Base64.getDecoder().decode(privateKeyContent);
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyContent);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        PublicKey publicKey = kf.generatePublic(new X509EncodedKeySpec(publicBytes));

        jwtService.privateKey = privateKey;
        jwtService.publicKey = publicKey;
        jwtService.jwtExpirationMs = 3600000;
        jwtService.refreshExpirationMs = 7200000;

        System.out.println("JWT Service initialized with test keys and expiration times.");
    }

    @Test
    void generateAndValidateAccessToken_shouldWork() {
        String token = jwtService.generateAccessToken("user@example.com");
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("user@example.com", jwtService.getEmailFromToken(token));

        System.out.println("Access token generated and validated successfully.");
    }

    @Test
    void generateAndValidateRefreshToken_shouldWork() {
        String token = jwtService.generateRefreshToken("refresh@example.com");
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
        assertEquals("refresh@example.com", jwtService.getEmailFromToken(token));

        System.out.println("Refresh token generated and validated successfully.");
    }

    @Test
    void getEncodedPublicKey_shouldReturnPemFormat() {
        String pem = jwtService.getEncodedPublicKey();
        assertTrue(pem.contains("BEGIN PUBLIC KEY"));
        assertTrue(pem.contains("END PUBLIC KEY"));
        assertTrue(pem.contains("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A"));

        System.out.println("Public key in PEM format retrieved successfully.");
    }

    @Test
    void validateToken_invalidToken_shouldReturnFalse() {
        assertFalse(jwtService.validateToken("invalid.token.here"));

        System.out.println("Invalid token validation returned false as expected.");
    }
}
