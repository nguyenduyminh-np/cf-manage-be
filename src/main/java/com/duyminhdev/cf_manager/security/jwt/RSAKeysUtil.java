package com.duyminhdev.cf_manager.security.jwt;

import com.duyminhdev.cf_manager.dto.records.JwtProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class RSAKeysUtil {

    private final JwtProperties props;
    private final ResourceLoader resourceLoader;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAKeysUtil(JwtProperties props, ResourceLoader resourceLoader) {
        this.props = props;
        this.resourceLoader = resourceLoader;
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey() {
        return publicKey;
    }

    @PostConstruct
    void init() {
        try {
            Resource privRes = resourceLoader.getResource(props.privateKey());
            Resource pubRes = resourceLoader.getResource(props.publicKey());
            this.privateKey = loadPrivateKey(privRes);
            this.publicKey = loadPublicKey(pubRes);
            log.info("RSA key pair loaded successfully.");
        } catch (Exception e) {
            log.error("RSAKeysUtil error: {}", e.getMessage());
            throw new IllegalStateException("Cannot load RSA keys for JWT. Check PEM format & paths.", e);
        }
    }

    private static PrivateKey loadPrivateKey(Resource pem) throws Exception {
        String content = new String(pem.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String base64 = content
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static PublicKey loadPublicKey(Resource pem) throws Exception {
        String content = new String(pem.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String base64 = content
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
    }
}
