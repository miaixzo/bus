package org.aoju.bus.pay.provider.jdpay.util;

import org.aoju.bus.core.codec.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;


public class RsaUtil {
    public static final String KEY_MAC = "HmacMD5";
    private static final String KEY_SHA = "SHA";
    private static final String KEY_MD5 = "MD5";
    private static final String hexString = "0123456789ABCDEF";

    public static byte[] decryptBASE64(String key) {
        return Base64.decode(key);
    }


    public static String encryptBASE64(byte[] key) {
        return Base64.encode(key);
    }


    public static byte[] encryptMD5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }


    public static byte[] encryptSHA(byte[] data) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA");
        sha.update(data);
        return sha.digest();
    }


    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");
        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }


    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), "HmacMD5");
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return mac.doFinal(data);
    }


    public static String bytesToString(byte[] src) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String bytes = stringBuilder.toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);

        for (int i = 0; i < bytes.length(); i += 2)
            baos.write(hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1)));
        return baos.toString();
    }
}
