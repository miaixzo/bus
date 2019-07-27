package org.aoju.bus.crypto.factory;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.crypto.CryptoFactory;
import org.aoju.bus.crypto.Mode;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * 数据加密标准，速度较快，适用于加密大量数据的场合。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DesCryptoFactory implements CryptoFactory {

    private volatile Cipher encryptCipher = null;
    private volatile Cipher decryptCipher = null;

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 需要加密的内容
     * @return 加密结果
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        try {
            return encryptCipher(key).doFinal(content);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 解密
     *
     * @param key     密钥
     * @param content 需要解密的内容
     * @return 解密结果
     */
    @Override
    public byte[] decrypt(String key, byte[] content) throws RuntimeException {
        try {
            return decryptCipher(key).doFinal(content);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
            throw new InstrumentException(e);
        }
    }


    private Cipher encryptCipher(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        if (encryptCipher == null) {
            synchronized (DesCryptoFactory.class) {
                if (encryptCipher == null) {
                    SecureRandom random = new SecureRandom();
                    DESKeySpec desKey = new DESKeySpec(key.getBytes(Charset.UTF_8));
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(Mode.DES.getValue());
                    Cipher cipher = Cipher.getInstance(Mode.DES.getValue());
                    cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(desKey), random);
                    this.encryptCipher = cipher;
                }
            }
        }
        return encryptCipher;
    }

    private Cipher decryptCipher(String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        if (decryptCipher == null) {
            synchronized (DesCryptoFactory.class) {
                if (decryptCipher == null) {
                    SecureRandom random = new SecureRandom();
                    DESKeySpec desKey = new DESKeySpec(key.getBytes(Charset.UTF_8));
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(Mode.DES.getValue());
                    Cipher cipher = Cipher.getInstance(Mode.DES.getValue());
                    cipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(desKey), random);
                    this.decryptCipher = cipher;
                }
            }
        }
        return decryptCipher;
    }

}
