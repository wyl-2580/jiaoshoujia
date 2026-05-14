package com.jiaoshoujia.framework.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@ConfigurationProperties(prefix = "jwt.rsa")
public class RsaKeyProperties {

    private Resource privateKeyLocation;

    private Resource publicKeyLocation;

    public Resource getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public void setPrivateKeyLocation(Resource privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public Resource getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(Resource publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public RSAPrivateKey privateKey() {
        try {
            String pem = readPem(privateKeyLocation);
            pem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

    public RSAPublicKey publicKey() {
        try {
            String pem = readPem(publicKeyLocation);
            pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(pem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    private String readPem(Resource resource) throws IOException {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
