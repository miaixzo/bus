/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.crypto.factory;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.crypto.CryptoFactory;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * 数据加密标准，速度较快，适用于加密大量数据的场合。
 *
 * @author Kimi Liu
 * @version 3.5.3
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
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ModeType.DES);
                    Cipher cipher = Cipher.getInstance(ModeType.DES);
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
                    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ModeType.DES);
                    Cipher cipher = Cipher.getInstance(ModeType.DES);
                    cipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(desKey), random);
                    this.decryptCipher = cipher;
                }
            }
        }
        return decryptCipher;
    }

}
