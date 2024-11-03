package com.example.commonmodel.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 128;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    // 암호화 키 - 환경 변수 또는 안전한 저장소에서 관리해야 함
    private static final String SECRET_KEY = "EncryptionKey123";  // 16, 24, 32 bytes

    // IV 생성 - 요청마다 새로운 IV를 사용하는 것이 좋음
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH_BYTE];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    // AES 암호화
    public static String encrypt(String data, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        byte[] encryptedIvAndData = new byte[IV_LENGTH_BYTE + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedIvAndData, 0, IV_LENGTH_BYTE);
        System.arraycopy(encryptedBytes, 0, encryptedIvAndData, IV_LENGTH_BYTE, encryptedBytes.length);
        return Base64.getEncoder().encodeToString(encryptedIvAndData);
    }

    // AES 복호화
    public static String decrypt(String encryptedData) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[IV_LENGTH_BYTE];
        System.arraycopy(decoded, 0, iv, 0, iv.length);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] originalData = cipher.doFinal(decoded, IV_LENGTH_BYTE, decoded.length - IV_LENGTH_BYTE);
        return new String(originalData);
    }
}
