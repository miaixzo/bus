/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.safety.boot;

import java.io.*;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.JdkDecryptorProvider;
import org.miaixz.bus.shade.safety.provider.JdkEncryptorProvider;

/**
 * Utility class for encrypting and decrypting Spring Boot JAR packages. By default, it encrypts all resources under
 * BOOT-INF/, including the project's own resources and dependency JARs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Boot {

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src  The source JAR file path.
     * @param dest The destination encrypted JAR file path.
     * @param key  The encryption key.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, Key key) throws Exception {
        encrypt(new File(src), new File(dest), key);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src  The source JAR file path.
     * @param dest The destination encrypted JAR file path.
     * @param key  The encryption key.
     * @param mode The encryption mode.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, Key key, int mode) throws Exception {
        encrypt(new File(src), new File(dest), key, mode);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src  The source JAR file.
     * @param dest The destination encrypted JAR file.
     * @param key  The encryption key.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, Key key) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, key);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src  The source JAR file.
     * @param dest The destination encrypted JAR file.
     * @param key  The encryption key.
     * @param mode The encryption mode.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, Key key, int mode) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, key, mode);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in  The input stream of the source JAR.
     * @param out The output stream for the encrypted JAR.
     * @param key The encryption key.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, Key key) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()));
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in   The input stream of the source JAR.
     * @param out  The output stream for the encrypted JAR.
     * @param key  The encryption key.
     * @param mode The encryption mode.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, int mode) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()),
                Deflater.DEFLATED, mode);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src    The source JAR file path.
     * @param dest   The destination encrypted JAR file path.
     * @param key    The encryption key.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(new File(src), new File(dest), key, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src    The source JAR file path.
     * @param dest   The destination encrypted JAR file path.
     * @param key    The encryption key.
     * @param mode   The encryption mode.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, Key key, int mode, Complex<JarArchiveEntry> filter)
            throws Exception {
        encrypt(new File(src), new File(dest), key, mode, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src    The source JAR file.
     * @param dest   The destination encrypted JAR file.
     * @param key    The encryption key.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, key, filter);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src    The source JAR file.
     * @param dest   The destination encrypted JAR file.
     * @param key    The encryption key.
     * @param mode   The encryption mode.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, Key key, int mode, Complex<JarArchiveEntry> filter)
            throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, key, mode, filter);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in     The input stream of the source JAR.
     * @param out    The output stream for the encrypted JAR.
     * @param key    The encryption key.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, Complex<JarArchiveEntry> filter)
            throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()),
                filter);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in     The input stream of the source JAR.
     * @param out    The output stream for the encrypted JAR.
     * @param key    The encryption key.
     * @param mode   The encryption mode.
     * @param filter The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, Key key, int mode, Complex<JarArchiveEntry> filter)
            throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(key.getAlgorithm()),
                Deflater.DEFLATED, mode, filter);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src      The source JAR file path.
     * @param dest     The destination encrypted JAR file path.
     * @param password The password to use for encryption.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, String password) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, String password, String algorithm) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize)
            throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize)
            throws Exception {
        encrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src      The source JAR file.
     * @param dest     The destination encrypted JAR file.
     * @param password The password to use for encryption.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password, String algorithm) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize)
            throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, password, algorithm, keysize, ivsize);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in       The input stream of the source JAR.
     * @param out      The output stream for the encrypted JAR.
     * @param password The password to use for encryption.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, String password) throws Exception {
        encrypt(in, out, password, Builder.ALGORITHM);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm) throws Exception {
        encrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize)
            throws Exception {
        encrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            int ivsize) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(algorithm));
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src      The source JAR file path.
     * @param dest     The destination encrypted JAR file path.
     * @param password The password to use for encryption.
     * @param filter   The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(String src, String dest, String password, Complex<JarArchiveEntry> filter)
            throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file path.
     * @param dest      The destination encrypted JAR file path.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src      The source JAR file.
     * @param dest     The destination encrypted JAR file.
     * @param password The password to use for encryption.
     * @param filter   The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(File src, File dest, String password, String algorithm, Complex<JarArchiveEntry> filter)
            throws Exception {
        encrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            File src,
            File dest,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param src       The source JAR file.
     * @param dest      The destination encrypted JAR file.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            File src,
            File dest,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            encrypt(in, out, password, algorithm, keysize, ivsize, filter);
        }
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in       The input stream of the source JAR.
     * @param out      The output stream for the encrypted JAR.
     * @param password The password to use for encryption.
     * @param filter   The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(InputStream in, OutputStream out, String password, Complex<JarArchiveEntry> filter)
            throws Exception {
        encrypt(in, out, password, Builder.ALGORITHM, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        encrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Encrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the source JAR.
     * @param out       The output stream for the encrypted JAR.
     * @param password  The password to use for encryption.
     * @param algorithm The encryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If an encryption error occurs.
     */
    public static void encrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        BootEncryptorProvider xBootEncryptor = new BootEncryptorProvider(new JdkEncryptorProvider(algorithm), filter);
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootEncryptor.encrypt(key, in, out);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src  The source encrypted JAR file path.
     * @param dest The destination decrypted JAR file path.
     * @param key  The decryption key.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, Key key) throws Exception {
        decrypt(new File(src), new File(dest), key);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src  The source encrypted JAR file.
     * @param dest The destination decrypted JAR file.
     * @param key  The decryption key.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, Key key) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            decrypt(in, out, key);
        }
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in  The input stream of the encrypted JAR.
     * @param out The output stream for the decrypted JAR.
     * @param key The decryption key.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, Key key) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(key.getAlgorithm()));
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src    The source encrypted JAR file path.
     * @param dest   The destination decrypted JAR file path.
     * @param key    The decryption key.
     * @param filter The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(new File(src), new File(dest), key, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src    The source encrypted JAR file.
     * @param dest   The destination decrypted JAR file.
     * @param key    The decryption key.
     * @param filter The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, Key key, Complex<JarArchiveEntry> filter) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            decrypt(in, out, key, filter);
        }
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in     The input stream of the encrypted JAR.
     * @param out    The output stream for the decrypted JAR.
     * @param key    The decryption key.
     * @param filter The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, Key key, Complex<JarArchiveEntry> filter)
            throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(key.getAlgorithm()),
                filter);
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src      The source encrypted JAR file path.
     * @param dest     The destination decrypted JAR file path.
     * @param password The password to use for decryption.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, String password) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, String password, String algorithm) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize)
            throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, String password, String algorithm, int keysize, int ivsize)
            throws Exception {
        decrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src      The source encrypted JAR file.
     * @param dest     The destination decrypted JAR file.
     * @param password The password to use for decryption.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password, String algorithm) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password, String algorithm, int keysize, int ivsize)
            throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            decrypt(in, out, password, algorithm, keysize, ivsize);
        }
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in       The input stream of the encrypted JAR.
     * @param out      The output stream for the decrypted JAR.
     * @param password The password to use for decryption.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, String password) throws Exception {
        decrypt(in, out, password, Builder.ALGORITHM);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm) throws Exception {
        decrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, String password, String algorithm, int keysize)
            throws Exception {
        decrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            int ivsize) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(algorithm));
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootDecryptor.decrypt(key, in, out);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src      The source encrypted JAR file path.
     * @param dest     The destination decrypted JAR file path.
     * @param password The password to use for decryption.
     * @param filter   The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(String src, String dest, String password, Complex<JarArchiveEntry> filter)
            throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file path.
     * @param dest      The destination decrypted JAR file path.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            String src,
            String dest,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(new File(src), new File(dest), password, algorithm, keysize, ivsize, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src      The source encrypted JAR file.
     * @param dest     The destination decrypted JAR file.
     * @param password The password to use for decryption.
     * @param filter   The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password, Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, Builder.ALGORITHM, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(File src, File dest, String password, String algorithm, Complex<JarArchiveEntry> filter)
            throws Exception {
        decrypt(src, dest, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            File src,
            File dest,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(src, dest, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param src       The source encrypted JAR file.
     * @param dest      The destination decrypted JAR file.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            File src,
            File dest,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            decrypt(in, out, password, algorithm, keysize, ivsize, filter);
        }
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in       The input stream of the encrypted JAR.
     * @param out      The output stream for the decrypted JAR.
     * @param password The password to use for decryption.
     * @param filter   The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(InputStream in, OutputStream out, String password, Complex<JarArchiveEntry> filter)
            throws Exception {
        decrypt(in, out, password, Builder.ALGORITHM, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(in, out, password, algorithm, Builder.DEFAULT_KEYSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            Complex<JarArchiveEntry> filter) throws Exception {
        decrypt(in, out, password, algorithm, keysize, Builder.DEFAULT_IVSIZE, filter);
    }

    /**
     * Decrypts a Spring Boot JAR package.
     *
     * @param in        The input stream of the encrypted JAR.
     * @param out       The output stream for the decrypted JAR.
     * @param password  The password to use for decryption.
     * @param algorithm The decryption algorithm.
     * @param keysize   The key size.
     * @param ivsize    The IV size.
     * @param filter    The filter to apply.
     * @throws Exception If a decryption error occurs.
     */
    public static void decrypt(
            InputStream in,
            OutputStream out,
            String password,
            String algorithm,
            int keysize,
            int ivsize,
            Complex<JarArchiveEntry> filter) throws Exception {
        BootDecryptorProvider xBootDecryptor = new BootDecryptorProvider(new JdkDecryptorProvider(algorithm), filter);
        Key key = Builder.key(algorithm, keysize, ivsize, password);
        xBootDecryptor.decrypt(key, in, out);
    }

}
