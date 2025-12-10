package vn.com.viettel.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.codec.binary.Base64;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.com.viettel.core.utils.AESCrypto;

import javax.crypto.SecretKey;

@Configuration
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JasyptConfig {

    @Value("${app.jasypt.secret}")
    String secret;
    @Value("${app.jasypt.salt}")
    String salt;

    @Bean(name = {"jasyptStringEncryptor"})
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        String key = System.getProperty("viettel-jasypt-pwd");
        try {
            SecretKey secretKey = AESCrypto.getKeyFromPassword(secret, salt);
            String decryptKey = AESCrypto.decryptWithPrefixIV(Base64.decodeBase64(key),secretKey);
            config.setPassword(decryptKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        System.clearProperty("viettel-jasypt-pwd");
        return encryptor;
    }
}
