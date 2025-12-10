package vn.com.viettel.core.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RSACrypto {
    final static Logger LOGGER = LoggerFactory.getLogger(RSACrypto.class);
    final static String ALGORITHM = "SHA512withRSA";
    static final String ENCRYPT_ALGO = "RSA/ECB/PKCS1Padding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Gen RSA couple key
     *
     * @param keySize
     * @return [] key: [0] = public key, [1] = private key
     * @throws NoSuchAlgorithmException
     */
    public static String[] genRSAKey(int keySize) throws NoSuchAlgorithmException {
        String[] array = new String[2];
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        kpg.initialize(keySize, new SecureRandom());
        KeyPair keys = kpg.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey) keys.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();
        array[0] = genKey(publicKey);
        array[1] = genKey(privateKey);
        return array;
    }

    static String genKey(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null.");
        }
        byte[] bKeyEncoded = key.getEncoded();
        byte[] b = DERtoString(bKeyEncoded);
        return new String(b);
    }

    private static byte[] DERtoString(byte[] bytes) {
        ByteArrayOutputStream pemStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(pemStream);

        byte[] stringBytes = encodeBase64(bytes).getBytes(StandardCharsets.UTF_8);
        String encoded = new String(stringBytes);
        encoded = encoded.replace("\r", "");
        encoded = encoded.replace("\n", "");

        int i = 0;
        while ((i + 1) * 64 <= encoded.length()) {
            writer.print(encoded.substring(i * 64, (i + 1) * 64));
            i++;
        }
        if (encoded.length() % 64 != 0) {
            writer.print(encoded.substring(i * 64));
        }
        writer.flush();
        return pemStream.toByteArray();
    }

    /**
     * Encrypt data
     *
     * @param dataToEncrypt
     * @param pubCer
     * @param isFile
     * @return
     * @throws Exception
     */
    public static String encrypt(String dataToEncrypt, String pubCer, Boolean isFile) throws Exception {
        RSAPublicKey publicKey = loadPublicKey(pubCer, isFile);
        if (publicKey != null) {
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(1, publicKey);

            int keySize = publicKey.getModulus().bitLength() / 8;
            int maxLength = keySize - 42;

            byte[] bytes = dataToEncrypt.getBytes(StandardCharsets.UTF_8);

            int dataLength = bytes.length;
            int iterations = dataLength / maxLength;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i <= iterations; i++) {
                byte[] tempBytes = new byte[Math.min(dataLength - maxLength * i, maxLength)];
                System.arraycopy(bytes, maxLength * i, tempBytes, 0,
                        tempBytes.length);
                byte[] encryptedBytes = cipher.doFinal(tempBytes);

                reverse(encryptedBytes);
                sb.append(encodeBase64(encryptedBytes));
            }

            String sEncrypted = sb.toString();
            sEncrypted = sEncrypted.replace("\r", "");
            sEncrypted = sEncrypted.replace("\n", "");
            return sEncrypted;
        }
        return null;
    }

    /**
     * verify data
     *
     * @param dataToVerify
     * @param signedData
     * @param pubCer
     * @param isFile
     * @return
     * @throws Exception
     */
    public static boolean verify(String dataToVerify, String signedData, String pubCer, Boolean isFile) throws Exception {
        RSAPublicKey publicKey = loadPublicKey(pubCer, isFile);
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(dataToVerify.getBytes(StandardCharsets.UTF_8), 0,
                dataToVerify.getBytes(StandardCharsets.UTF_8).length);
        byte[] bSign = decodeBase64(signedData);

        return signature.verify(bSign);
    }

    /**
     * Sign data
     *
     * @param dataToSign
     * @param privateKey
     * @param isFile
     * @return
     * @throws Exception
     */
    public static String sign(String dataToSign, String privateKey, Boolean isFile) throws Exception {
        RSAPrivateKey rsaPrivateKey = loadPrivateKey(privateKey, isFile);
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(rsaPrivateKey);
        signature.update(dataToSign.getBytes(StandardCharsets.UTF_8));

        byte[] bSigned = signature.sign();

        return encodeBase64(bSigned);
    }

    /**
     * decrypt data
     *
     * @param dataEncrypted
     * @param privateKey
     * @param isFile
     * @return
     * @throws Exception
     */
    public static String decrypt(String dataEncrypted, String privateKey, Boolean isFile) throws Exception {
        RSAPrivateKey rsaPrivateKey = loadPrivateKey(privateKey, isFile);
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(2, rsaPrivateKey);

        dataEncrypted = dataEncrypted.replace("\r", "");
        dataEncrypted = dataEncrypted.replace("\n", "");
        int dwKeySize = rsaPrivateKey.getModulus().bitLength();
        int base64BlockSize = dwKeySize / 8 % 3 != 0 ? dwKeySize / 8 / 3 * 4 + 4
                : dwKeySize / 8 / 3 * 4;
        int iterations = dataEncrypted.length() / base64BlockSize;
        ByteBuffer bb = ByteBuffer.allocate(100000);
        for (int i = 0; i < iterations; i++) {
            String sTemp = dataEncrypted.substring(base64BlockSize * i,
                    base64BlockSize * i + base64BlockSize);
            byte[] bTemp = decodeBase64(sTemp);

            reverse(bTemp);
            byte[] encryptedBytes = cipher.doFinal(bTemp);
            bb.put(encryptedBytes);
        }
        byte[] bDecrypted = bb.array();
        return new String(bDecrypted).trim();
    }

    /**
     * Encrypt data
     *
     * @param dataToEncrypt
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String encrypt(String dataToEncrypt, RSAPublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(1, publicKey);

        int keySize = publicKey.getModulus().bitLength() / 8;
        int maxLength = keySize - 42;

        byte[] bytes = dataToEncrypt.getBytes(StandardCharsets.UTF_8);

        int dataLength = bytes.length;
        int iterations = dataLength / maxLength;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i <= iterations; i++) {
            byte[] tempBytes = new byte[Math.min(dataLength - maxLength * i, maxLength)];
            System.arraycopy(bytes, maxLength * i, tempBytes, 0,
                    tempBytes.length);
            byte[] encryptedBytes = cipher.doFinal(tempBytes);

            reverse(encryptedBytes);
            sb.append(encodeBase64(encryptedBytes));
        }

        String sEncrypted = sb.toString();
        sEncrypted = sEncrypted.replace("\r", "");
        sEncrypted = sEncrypted.replace("\n", "");
        return sEncrypted;
    }

    /**
     * verify data
     *
     * @param dataToVerify
     * @param signedData
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static boolean verify(String dataToVerify, String signedData, RSAPublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(dataToVerify.getBytes(StandardCharsets.UTF_8), 0,
                dataToVerify.getBytes(StandardCharsets.UTF_8).length);
        byte[] bSign = decodeBase64(signedData);

        return signature.verify(bSign);
    }

    /**
     * Sign data
     *
     * @param dataToSign
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String sign(String dataToSign, RSAPrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(privateKey);
        signature.update(dataToSign.getBytes(StandardCharsets.UTF_8));

        byte[] bSigned = signature.sign();

        return encodeBase64(bSigned);
    }

    /**
     * decrypt data
     *
     * @param dataEncrypted
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decrypt(String dataEncrypted, RSAPrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
        cipher.init(2, privateKey);

        dataEncrypted = dataEncrypted.replace("\r", "");
        dataEncrypted = dataEncrypted.replace("\n", "");
        int dwKeySize = privateKey.getModulus().bitLength();
        int base64BlockSize = dwKeySize / 8 % 3 != 0 ? dwKeySize / 8 / 3 * 4 + 4
                : dwKeySize / 8 / 3 * 4;
        int iterations = dataEncrypted.length() / base64BlockSize;
        ByteBuffer bb = ByteBuffer.allocate(100000);
        for (int i = 0; i < iterations; i++) {
            String sTemp = dataEncrypted.substring(base64BlockSize * i,
                    base64BlockSize * i + base64BlockSize);
            byte[] bTemp = decodeBase64(sTemp);

            reverse(bTemp);
            byte[] encryptedBytes = cipher.doFinal(bTemp);
            bb.put(encryptedBytes);
        }
        byte[] bDecrypted = bb.array();
        return new String(bDecrypted).trim();
    }

    /**
     * load private key from key string if load key from file: isFile = true,
     * else isFile = false
     *
     * @param key
     * @param isFile
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static RSAPrivateKey loadPrivateKey(String key, Boolean isFile) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String sReadFile;
        if (isFile) {
            File file = new File(key);
            sReadFile = fullyReadFile(file);
        } else {
            sReadFile = key.trim();
        }

        if ((sReadFile.startsWith("-----BEGIN PRIVATE KEY-----"))
                && (sReadFile.endsWith("-----END PRIVATE KEY-----"))) {
            sReadFile = sReadFile.replace("-----BEGIN PRIVATE KEY-----", "");
            sReadFile = sReadFile.replace("-----END PRIVATE KEY-----", "");
            sReadFile = sReadFile.replace("\n", "");
            sReadFile = sReadFile.replace("\r", "");
            sReadFile = sReadFile.replace(" ", "");
        }
        if ((sReadFile.startsWith("-----BEGIN RSA PRIVATE KEY-----"))
                && (sReadFile.endsWith("-----END RSA PRIVATE KEY-----"))) {
            sReadFile = sReadFile.replace("-----BEGIN RSA PRIVATE KEY-----", "");
            sReadFile = sReadFile.replace("-----END RSA PRIVATE KEY-----", "");
            sReadFile = sReadFile.replace("\n", "");
            sReadFile = sReadFile.replace("\r", "");
            sReadFile = sReadFile.replace(" ", "");
        }
        byte[] b = decodeBase64(sReadFile);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    /**
     * load public key from key string
     *
     * @param pubCer
     * @param isFile
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws CertificateException
     */
    private static RSAPublicKey loadPublicKey(String pubCer, Boolean isFile) throws IOException {
        String sReadFile;
        if (isFile) {
            File file = new File(pubCer);
            sReadFile = fullyReadFile(file);
        } else {
            sReadFile = pubCer.trim();
        }
        boolean isCert = false;
        if ((sReadFile.startsWith("-----BEGIN PUBLIC KEY-----"))
                && (sReadFile.endsWith("-----END PUBLIC KEY-----"))) {
            sReadFile = sReadFile.replace("-----BEGIN PUBLIC KEY-----", "");
            sReadFile = sReadFile.replace("-----END PUBLIC KEY-----", "");
            sReadFile = sReadFile.replace("\n", "");
            sReadFile = sReadFile.replace("\r", "");
            sReadFile = sReadFile.replace(" ", "");
        }
        if ((sReadFile.startsWith("-----BEGIN CERTIFICATE-----"))
                && (sReadFile.endsWith("-----END CERTIFICATE-----"))) {
            sReadFile = sReadFile.replace("-----BEGIN CERTIFICATE-----", "");
            sReadFile = sReadFile.replace("-----END CERTIFICATE-----", "");
            sReadFile = sReadFile.replace("\n", "");
            sReadFile = sReadFile.replace("\r", "");
            sReadFile = sReadFile.replace(" ", "");
            isCert = true;
        }

        RSAPublicKey publicKey = null;
        try {
            if (isCert) {
                FileInputStream is = new FileInputStream(pubCer);
                X509Certificate cert = X509Certificate.getInstance(is);
                publicKey = (RSAPublicKey) cert.getPublicKey();
            } else {
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(sReadFile));
                publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            }
        } catch (Exception e) {
            LOGGER.error("RSAPublicKey", e);
        }
        return publicKey;
    }

    private static String fullyReadFile(File file) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            byte[] bytesOfFile = new byte[(int) file.length()];
            dis.readFully(bytesOfFile);
            String sRead = new String(bytesOfFile);
            return sRead.trim();
        }
    }

    private static String encodeBase64(byte[] dataToEncode) {
        return new String(Base64.encode(dataToEncode));
    }

    private static byte[] decodeBase64(String dataToDecode) {
        return Base64.decode(dataToDecode);
    }

    private static byte[] reverse(byte[] b) {
        int left = 0;
        int right = b.length - 1;

        while (left < right) {
            byte temp = b[left];
            b[left] = b[right];
            b[right] = temp;

            left++;
            right--;
        }
        return b;
    }
}
