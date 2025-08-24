package org.payetonkawa.auth.auth_service.security;

import io.jsonwebtoken.Claims;
import jakarta.annotation.PostConstruct;
import org.payetonkawa.auth.auth_service.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.payetonkawa.auth.auth_service.model.Role;
import org.slf4j.Logger;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    private static final Logger Logger = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    @Value("${auth.jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${auth.jwt.public-key}")
    private Resource publicKeyResource;

    @Value("${auth.jwt.expirationMs}")
    int jwtExpirationMs;

    @Value("${auth.jwt.refreshExpirationMs}")
    int refreshExpirationMs;

    PrivateKey privateKey;
    PublicKey publicKey;

    @PostConstruct
    public void loadKeys() throws Exception {
        // Lecture via InputStream (compatible avec un JAR)
        String privateKeyContent = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        String publicKeyContent = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] privateBytes = Base64.getDecoder().decode(privateKeyContent);
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyContent);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
        publicKey = kf.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();


        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


    public String generateRefreshToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log the exception if needed
            Logger.warn("JWT validation failed", e);
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getEncodedPublicKey() {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                "\n-----END PUBLIC KEY-----";
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
