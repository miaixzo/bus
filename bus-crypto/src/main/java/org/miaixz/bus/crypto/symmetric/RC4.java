/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.crypto.symmetric;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.exception.CryptoException;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.HexKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.crypto.Builder;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * RC4加密解密算法实现
 * 注意：由于安全问题，已经基本不在HTTPS中使用了
 * 来自：https://github.com/xSAVIKx/RC4-cipher/blob/master/src/main/java/com/github/xsavikx/rc4/RC4.java
 *
 * @author Iurii Sergiichuk
 * @author Kimi Liu
 * @since Java 17+
 */
public class RC4 implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int SBOX_LENGTH = Normal._256;
    /**
     * 密钥最小长度
     */
    private static final int KEY_MIN_LENGTH = 5;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    /**
     * Sbox
     */
    private int[] sbox;

    /**
     * 构造
     *
     * @param key 密钥
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public RC4(String key) throws CryptoException {
        setKey(key);
    }

    /**
     * 加密
     *
     * @param message 消息
     * @param charset 编码
     * @return 密文
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public byte[] encrypt(String message, Charset charset) throws CryptoException {
        return crypt(StringKit.bytes(message, charset));
    }

    /**
     * 加密，使用默认编码：UTF-8
     *
     * @param message 消息
     * @return 密文
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public byte[] encrypt(String message) throws CryptoException {
        return encrypt(message, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Hex
     */
    public String encryptHex(byte[] data) {
        return HexKit.encodeHexString(crypt(data));
    }

    /**
     * 加密
     *
     * @param data 数据
     * @return 加密后的Base64
     */
    public String encryptBase64(byte[] data) {
        return Base64.encode(crypt(data));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Hex
     */
    public String encryptHex(String data, Charset charset) {
        return HexKit.encodeHexString(encrypt(data, charset));
    }

    /**
     * 加密，使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Hex
     */
    public String encryptHex(String data) {
        return HexKit.encodeHexString(encrypt(data));
    }

    /**
     * 加密
     *
     * @param data    被加密的字符串
     * @param charset 编码
     * @return 加密后的Base64
     */
    public String encryptBase64(String data, Charset charset) {
        return Base64.encode(encrypt(data, charset));
    }


    /**
     * 加密，使用UTF-8编码
     *
     * @param data 被加密的字符串
     * @return 加密后的Base64
     */
    public String encryptBase64(String data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * 解密
     *
     * @param message 消息
     * @param charset 编码
     * @return 明文
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public String decrypt(byte[] message, Charset charset) throws CryptoException {
        return StringKit.toString(crypt(message), charset);
    }

    /**
     * 解密，使用默认编码UTF-8
     *
     * @param message 消息
     * @return 明文
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public String decrypt(byte[] message) throws CryptoException {
        return decrypt(message, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串，使用默认编码UTF-8
     *
     * @param message 消息
     * @return 明文
     */
    public String decrypt(String message) {
        return decrypt(Builder.decode(message));
    }

    /**
     * 解密Hex（16进制）或Base64表示的字符串
     *
     * @param message 明文
     * @param charset 解密后的charset
     * @return 明文
     */
    public String decrypt(String message, Charset charset) {
        return StringKit.toString(decrypt(message), charset);
    }


    /**
     * 加密或解密指定值，调用此方法前需初始化密钥
     *
     * @param msg 要加密或解密的消息
     * @return 加密或解密后的值
     */
    public byte[] crypt(final byte[] msg) {
        final ReadLock readLock = this.lock.readLock();
        byte[] code;
        readLock.lock();
        try {
            final int[] sbox = this.sbox.clone();
            code = new byte[msg.length];
            int i = 0;
            int j = 0;
            for (int n = 0; n < msg.length; n++) {
                i = (i + 1) % SBOX_LENGTH;
                j = (j + sbox[i]) % SBOX_LENGTH;
                swap(i, j, sbox);
                int rand = sbox[(sbox[i] + sbox[j]) % SBOX_LENGTH];
                code[n] = (byte) (rand ^ msg[n]);
            }
        } finally {
            readLock.unlock();
        }
        return code;
    }

    /**
     * 设置密钥
     *
     * @param key 密钥
     * @throws CryptoException key长度小于5或者大于255抛出此异常
     */
    public void setKey(String key) throws CryptoException {
        final int length = key.length();
        if (length < KEY_MIN_LENGTH || length >= SBOX_LENGTH) {
            throw new CryptoException("Key length has to be between {} and {}", KEY_MIN_LENGTH, (SBOX_LENGTH - 1));
        }

        final WriteLock writeLock = this.lock.writeLock();
        writeLock.lock();
        try {
            this.sbox = initSBox(StringKit.bytes(key));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 初始化Sbox
     *
     * @param key 密钥
     * @return sbox
     */
    private int[] initSBox(byte[] key) {
        int[] sbox = new int[SBOX_LENGTH];
        int j = 0;

        for (int i = 0; i < SBOX_LENGTH; i++) {
            sbox[i] = i;
        }

        for (int i = 0; i < SBOX_LENGTH; i++) {
            j = (j + sbox[i] + (key[i % key.length]) & 0xFF) % SBOX_LENGTH;
            swap(i, j, sbox);
        }
        return sbox;
    }

    /**
     * 交换指定两个位置的值
     *
     * @param i    位置1
     * @param j    位置2
     * @param sbox 数组
     */
    private void swap(int i, int j, int[] sbox) {
        int temp = sbox[i];
        sbox[i] = sbox[j];
        sbox[j] = temp;
    }

}