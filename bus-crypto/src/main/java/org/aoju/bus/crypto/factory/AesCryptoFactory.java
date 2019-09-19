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
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 高级加密标准，是下一代的加密算法标准，速度快，安全级别高；
 * AES是一个使用128为分组块的分组加密算法，分组块和128、192或256位的密钥一起作为输入，
 * 对4×4的字节数组上进行操作。众所周之AES是种十分高效的算法，尤其在8位架构中，这源于它面向字节的设计。
 * AES 适用于8位的小型单片机或者普通的32位微处理器,并且适合用专门的硬件实现，硬件实现能够使其吞吐量（每秒可以到达的加密/解密bit数）
 * 达到十亿量级
 *
 * @author Kimi Liu
 * @version 3.5.1
 * @since JDK 1.8
 */
public class AesCryptoFactory implements CryptoFactory {

    private volatile Cipher encryptCipher = null;
    private volatile Cipher decryptCipher = null;

    /**
     * 加密
     *
     * @param key     密钥
     * @param content 需要加密的内容
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        try {
            return encryptCipher(key).doFinal(content);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }


    /**
     * 解密
     *
     * @param key     密钥
     * @param content 需要解密的内容
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        try {
            return decryptCipher(key).doFinal(content);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new InstrumentException(e);
        }
    }

    private Cipher encryptCipher(String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        if (encryptCipher == null) {
            synchronized (AesCryptoFactory.class) {
                if (encryptCipher == null) {
                    KeyGenerator generator = KeyGenerator.getInstance(ModeType.AES);
                    SecureRandom random = SecureRandom.getInstance(ModeType.SHAPRNG);
                    random.setSeed(key.getBytes(Charset.UTF_8));
                    generator.init(128, random);
                    SecretKey secretKey = generator.generateKey();
                    byte[] enCodeFormat = secretKey.getEncoded();
                    SecretKeySpec spec = new SecretKeySpec(enCodeFormat, ModeType.AES);
                    Cipher cipher = Cipher.getInstance(ModeType.AES);
                    cipher.init(Cipher.ENCRYPT_MODE, spec);
                    this.encryptCipher = cipher;
                }
            }
        }
        return encryptCipher;
    }

    private Cipher decryptCipher(String key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        if (decryptCipher == null) {
            synchronized (AesCryptoFactory.class) {
                if (decryptCipher == null) {
                    KeyGenerator generator = KeyGenerator.getInstance(ModeType.AES);
                    SecureRandom random = SecureRandom.getInstance(ModeType.SHAPRNG);
                    random.setSeed(key.getBytes(Charset.UTF_8));
                    generator.init(128, random);
                    SecretKey secretKey = generator.generateKey();
                    byte[] enCodeFormat = secretKey.getEncoded();
                    SecretKeySpec spec = new SecretKeySpec(enCodeFormat, ModeType.AES);
                    Cipher cipher = Cipher.getInstance(ModeType.AES);
                    cipher.init(Cipher.DECRYPT_MODE, spec);
                    this.decryptCipher = cipher;
                }
            }
        }
        return decryptCipher;
    }

}
