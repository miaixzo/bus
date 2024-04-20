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
package org.miaixz.bus.shade.safety.boot;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.JdkDecryptorProvider;
import org.miaixz.bus.shade.safety.provider.JdkEncryptorProvider;

import java.io.*;
import java.util.zip.Deflater;

/**
 * Spring-Boot JAR包加解密工具类,在不提供过滤器的情况下会加密BOOT-INF/下的所有资源,及包括项目本身的资源和依赖jar资源
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Boot {

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src  原文包
     * @param dest 加密包
     * @param key  密钥
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, Key key) throws Exception {
        encrypt(new File(src), new File(dest), key);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src  原文包
     * @param dest 加密包
     * @param key  密钥
     * @param mode 加密模式
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, Key key, int mode) throws Exception {
        encrypt(new File(src), new File(dest), key, mode);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src  原文包
     * @param dest 加密包
     * @param key  密钥
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, Key key) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, key);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src  原文包
     * @param dest 加密包
     * @param key  密钥
     * @param mode 加密模式
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, Key key, int mode) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, key, mode);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in  原文包输入流
     * @param out 加密包输出流
     * @param key 密钥
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, Key key) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()));
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in   原文包输入流
     * @param out  加密包输出流
     * @param key  密钥
     * @param mode 加密模式
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, int mode) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()), Deflater.DEFLATED, mode);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src    原文包
     * @param dest   加密包
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(new File(src), new File(dest), key, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src    原文包
     * @param dest   加密包
     * @param key    密钥
     * @param mode   加密模式
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, Key key, int mode, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(new File(src), new File(dest), key, mode, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src    原文包
     * @param dest   加密包
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, key, filter);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src    原文包
     * @param dest   加密包
     * @param key    密钥
     * @param mode   加密模式
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, Key key, int mode, Complex<JarArchiveEntry> filter) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, key, mode, filter);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in     原文包输入流
     * @param out    加密包输出流
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()), filter);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in     原文包输入流
     * @param out    加密包输出流
     * @param key    密钥
     * @param mode   加密模式
     * @param filter 过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, int mode, Complex<JarArchiveEntry> filter) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()), Deflater.DEFLATED, mode, filter);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src      原文包
     * @param dest     加密包
     * @param password 密码
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize) throws Exception {
        encrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src      原文包
     * @param dest     加密包
     * @param password 密码
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, password, algorithm, keysize, ivsize);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in       原文包输入流
     * @param out      加密包输出流
     * @param password 密码
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password) throws Exception {
        encrypt(in, out, password, Builder.ALGORITHM);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm) throws Exception {
        encrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize) throws Exception {
        encrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, int ivsize) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(algorithm));
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src      原文包
     * @param dest     加密包
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src      原文包
     * @param dest     加密包
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param src       原文包
     * @param dest      加密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            encrypt(in, out, password, algorithm, keysize, ivsize, filter);
        }
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in       原文包输入流
     * @param out      加密包输出流
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(in, out, password, Builder.ALGORITHM, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 加密 Spring-Boot JAR 包
     *
     * @param in        原文包输入流
     * @param out       加密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 加密异常
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(algorithm), filter);
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src  加密包
     * @param dest 解密包
     * @param key  密钥
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, Key key) throws Exception {
        decrypt(new File(src), new File(dest), key);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src  加密包
     * @param dest 解密包
     * @param key  密钥
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, Key key) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            decrypt(in, out, key);
        }
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in  加密包输入流
     * @param out 解密包输出流
     * @param key 密钥
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, Key key) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(key.getAlgorithm()));
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src    加密包
     * @param dest   解密包
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(new File(src), new File(dest), key, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src    加密包
     * @param dest   解密包
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            decrypt(in, out, key, filter);
        }
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in     加密包输入流
     * @param out    解密包输出流
     * @param key    密钥
     * @param filter 过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(key.getAlgorithm()), filter);
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src      加密包
     * @param dest     解密包
     * @param password 密码
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize) throws Exception {
        decrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src      加密包
     * @param dest     解密包
     * @param password 密码
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            decrypt(in, out, password, algorithm, keysize, ivsize);
        }
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in       加密包输入流
     * @param out      解密包输出流
     * @param password 密码
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password) throws Exception {
        decrypt(in, out, password, Builder.ALGORITHM);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm) throws Exception {
        decrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize) throws Exception {
        decrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, int ivsize) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(algorithm));
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src      加密包
     * @param dest     解密包
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src      加密包
     * @param dest     解密包
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param src       加密包
     * @param dest      解密包
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            decrypt(in, out, password, algorithm, keysize, ivsize, filter);
        }
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in       加密包输入流
     * @param out      解密包输出流
     * @param password 密码
     * @param filter   过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(in, out, password, Builder.ALGORITHM, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * 解密 Spring-Boot JAR 包
     *
     * @param in        加密包输入流
     * @param out       解密包输出流
     * @param password  密码
     * @param algorithm 加密算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param filter    过滤器
     * @throws Exception 解密异常
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize, int ivsize, Complex<JarArchiveEntry> filter) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(algorithm), filter);
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootDecryptor.decrypt(key, in, out);
    }

}
