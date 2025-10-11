/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.safety;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.algorithm.SecureRandom;
import org.miaixz.bus.shade.safety.algorithm.SymmetricSecureKey;
import org.miaixz.bus.shade.safety.complex.AllComplex;
import org.miaixz.bus.shade.safety.complex.AnyComplex;
import org.miaixz.bus.shade.safety.complex.NotComplex;

/**
 * Utility class providing methods for I/O operations, key generation, and filter management within the context of JAR
 * safety and shading.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Builder {

    /**
     * Path to WEB-INF/classes/ directory.
     */
    public static final String WEB_INF_CLASSES = "WEB-INF/classes/";
    /**
     * Path to WEB-INF/lib/ directory.
     */
    public static final String WEB_INF_LIB = "WEB-INF/lib/";
    /**
     * Path to META-INF/MANIFEST.MF file.
     */
    public static final String META_INF_MANIFEST = Normal.META_INF + "/MANIFEST.MF";
    /**
     * Source directory for xjar classes.
     */
    public static final String XJAR_SRC_DIR = Builder.class.getPackage().getName().replace(Symbol.C_DOT, Symbol.C_SLASH)
            + Symbol.SLASH;
    /**
     * Info directory for xjar.
     */
    public static final String XJAR_INF_DIR = Normal.META_INF + Symbol.SLASH;
    /**
     * Index file for xjar.
     */
    public static final String XJAR_INF_IDX = "FOREST.MF";
    /**
     * Command line argument prefix for algorithm.
     */
    public static final String XJAR_ALGORITHM = "--xjar.algorithm=";
    /**
     * Command line argument prefix for key size.
     */
    public static final String XJAR_KEYSIZE = "--xjar.keysize=";
    /**
     * Command line argument prefix for IV size.
     */
    public static final String XJAR_IVSIZE = "--xjar.ivsize=";
    /**
     * Command line argument prefix for password.
     */
    public static final String XJAR_PASSWORD = "--xjar.password=";
    /**
     * Command line argument prefix for key file.
     */
    public static final String XJAR_KEYFILE = "--xjar.keyfile=";
    /**
     * Manifest attribute key for algorithm.
     */
    public static final String XJAR_ALGORITHM_KEY = "Jar-Algorithm";
    /**
     * Manifest attribute key for key size.
     */
    public static final String XJAR_KEYSIZE_KEY = "Jar-Keysize";
    /**
     * Manifest attribute key for IV size.
     */
    public static final String XJAR_IVSIZE_KEY = "Jar-Ivsize";
    /**
     * Manifest attribute key for password.
     */
    public static final String XJAR_PASSWORD_KEY = "Jar-Password";
    /**
     * Key for algorithm in key properties.
     */
    public static final String XJAR_KEY_ALGORITHM = "algorithm";
    /**
     * Key for key size in key properties.
     */
    public static final String XJAR_KEY_KEYSIZE = "keysize";
    /**
     * Key for IV size in key properties.
     */
    public static final String XJAR_KEY_IVSIZE = "ivsize";
    /**
     * Key for password in key properties.
     */
    public static final String XJAR_KEY_PASSWORD = "password";
    /**
     * Key for hold in key properties.
     */
    public static final String XJAR_KEY_HOLD = "hold";
    /**
     * Path to BOOT-INF/classes/ directory.
     */
    public static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    /**
     * Path to BOOT-INF/lib/ directory.
     */
    public static final String BOOT_INF_LIB = "BOOT-INF/lib/";
    /**
     * System-dependent line separator.
     */
    public static final String CRLF = System.getProperty(Keys.LINE_SEPARATOR);
    /**
     * Default encryption algorithm.
     */
    public static final String ALGORITHM = "AES";
    /**
     * Default key size for encryption.
     */
    public static int DEFAULT_KEYSIZE = Normal._128;
    /**
     * Default IV size for encryption.
     */
    public static int DEFAULT_IVSIZE = Normal._128;

    /**
     * Danger flag: retain key in META-INF/MANIFEST.MF, no password required at startup.
     */
    public static int FLAG_DANGER = 1;
    /**
     * Danger mode: key is retained.
     */
    public static int MODE_DANGER = FLAG_DANGER;
    /**
     * Normal mode.
     */
    public static int MODE_NORMAL = 0;

    /**
     * Reads a line of bytes from the input stream.
     *
     * @param in The input stream to read from.
     * @return The first line of bytes, or {@code null} if the end of the stream is reached.
     * @throws IOException If an I/O error occurs.
     */
    public static byte[] readln(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (b != -1) {
            switch (b) {
                case Symbol.C_CR:
                    break;

                case Symbol.C_LF:
                    return bos.toByteArray();

                default:
                    bos.write(b);
                    break;
            }
            b = in.read();
        }
        return bos.toByteArray();
    }

    /**
     * Writes a line of bytes to the output stream, followed by a carriage return and line feed.
     *
     * @param out  The output stream to write to.
     * @param line The line of bytes to write. If {@code null}, nothing is written.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeln(OutputStream out, byte[] line) throws IOException {
        if (null == line) {
            return;
        }
        out.write(line);
        out.write(Symbol.C_CR);
        out.write(Symbol.C_LF);
    }

    /**
     * Closes a {@link Closeable} resource, suppressing any {@link IOException} that may occur. This is equivalent to
     * calling {@code close(closeable, true);}.
     *
     * @param closeable The resource to close.
     * @throws RuntimeException If an {@link IOException} occurs and cannot be quietly handled.
     */
    public static void close(Closeable closeable) {
        try {
            close(closeable, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes a {@link Closeable} resource.
     *
     * @param closeable The resource to close.
     * @param quietly   If {@code true}, any {@link IOException} thrown during closing will be suppressed. If
     *                  {@code false}, the {@link IOException} will be rethrown.
     * @throws IOException If an I/O error occurs and {@code quietly} is {@code false}.
     */
    public static void close(Closeable closeable, boolean quietly) throws IOException {
        if (null == closeable)
            return;
        try {
            closeable.close();
        } catch (IOException e) {
            if (!quietly)
                throw e;
        }
    }

    /**
     * Transfers all bytes from the given input stream to the given output stream.
     *
     * @param in  The input stream to read from.
     * @param out The output stream to write to.
     * @return The total number of bytes transferred.
     * @throws IOException If an I/O error occurs.
     */
    public static long transfer(InputStream in, OutputStream out) throws IOException {
        long total = 0;
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
            total += length;
        }
        out.flush();
        return total;
    }

    /**
     * Transfers all characters from the given reader to the given writer.
     *
     * @param reader The reader to read from.
     * @param writer The writer to write to.
     * @return The total number of characters transferred.
     * @throws IOException If an I/O error occurs.
     */
    public static long transfer(Reader reader, Writer writer) throws IOException {
        long total = 0;
        char[] buffer = new char[4096];
        int length;
        while ((length = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, length);
            total += length;
        }
        writer.flush();
        return total;
    }

    /**
     * Transfers all bytes from the given input stream to the specified file.
     *
     * @param in   The input stream to read from.
     * @param file The file to write to.
     * @return The total number of bytes transferred.
     * @throws IOException If an I/O error occurs.
     */
    public static long transfer(InputStream in, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            return transfer(in, out);
        } finally {
            close(out);
        }
    }

    /**
     * Transfers all characters from the given reader to the specified file.
     *
     * @param reader The reader to read from.
     * @param file   The file to write to.
     * @return The total number of characters transferred.
     * @throws IOException If an I/O error occurs.
     */
    public static long transfer(Reader reader, File file) throws IOException {
        OutputStream out = null;
        Writer writer = null;
        try {
            out = new FileOutputStream(file);
            writer = new OutputStreamWriter(out);
            return transfer(reader, writer);
        } finally {
            close(writer);
            close(out);
        }
    }

    /**
     * Deletes the specified file or empty directory. If the file is a directory, it will not recursively delete its
     * contents. This is equivalent to calling {@code delete(file, false);}.
     *
     * @param file The file or directory to delete.
     * @return {@code true} if the file or directory was successfully deleted; {@code false} otherwise.
     */
    public static boolean delete(File file) {
        return delete(file, false);
    }

    /**
     * Deletes the specified file or directory. If the file is a directory and {@code recursively} is {@code true}, its
     * contents will be deleted first.
     *
     * @param file        The file or directory to delete.
     * @param recursively If {@code true}, recursively deletes directory contents. If {@code false}, only deletes empty
     *                    directories or files.
     * @return {@code true} if the file or directory was successfully deleted; {@code false} otherwise.
     */
    public static boolean delete(File file, boolean recursively) {
        if (file.isDirectory() && recursively) {
            boolean deleted = true;
            File[] files = file.listFiles();
            for (int i = 0; null != files && i < files.length; i++) {
                deleted &= delete(files[i], true);
            }
            return deleted && file.delete();
        } else {
            return file.delete();
        }
    }

    /**
     * Generates a symmetric encryption key using the default algorithm (AES), default key size, and default IV size.
     *
     * @param password The password to use as a seed for key generation.
     * @return A {@link Key} object containing the generated key information.
     * @throws NoSuchAlgorithmException If the default algorithm (AES) is not available.
     */
    public static Key key(String password) throws NoSuchAlgorithmException {
        return key("AES", DEFAULT_KEYSIZE, DEFAULT_IVSIZE, password);
    }

    /**
     * Generates a symmetric encryption key using the specified algorithm, default key size, and default IV size.
     *
     * @param algorithm The encryption algorithm (e.g., "AES").
     * @param password  The password to use as a seed for key generation.
     * @return A {@link Key} object containing the generated key information.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     */
    public static Key key(String algorithm, String password) throws NoSuchAlgorithmException {
        return key(algorithm, DEFAULT_KEYSIZE, DEFAULT_IVSIZE, password);
    }

    /**
     * Generates a symmetric encryption key using the specified algorithm, key size, and default IV size.
     *
     * @param algorithm The encryption algorithm (e.g., "AES").
     * @param keysize   The desired key size in bits.
     * @param password  The password to use as a seed for key generation.
     * @return A {@link Key} object containing the generated key information.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     */
    public static Key key(String algorithm, int keysize, String password) throws NoSuchAlgorithmException {
        return key(algorithm, keysize, DEFAULT_IVSIZE, password);
    }

    /**
     * Generates a symmetric encryption key using the specified algorithm, key size, and IV size. The password is used
     * to seed a secure random number generator for key and IV generation.
     *
     * @param algorithm The encryption algorithm (e.g., "AES").
     * @param keysize   The desired key size in bits.
     * @param ivsize    The desired IV size in bits.
     * @param password  The password to use as a seed for key generation.
     * @return A {@link Key} object containing the generated key information.
     * @throws NoSuchAlgorithmException If the specified algorithm is not available.
     */
    public static Key key(String algorithm, int keysize, int ivsize, String password) throws NoSuchAlgorithmException {
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        byte[] seed = sha512.digest(password.getBytes());
        KeyGenerator generator = KeyGenerator.getInstance(algorithm.split("[/]")[0]);
        SecureRandom random = new SecureRandom(seed);
        generator.init(keysize, random);
        SecretKey key = generator.generateKey();
        generator.init(ivsize, random);
        SecretKey iv = generator.generateKey();
        return new SymmetricSecureKey(algorithm, keysize, ivsize, password, key.getEncoded(), iv.getEncoded());
    }

    /**
     * Retains key-related attributes in the provided {@link Attributes} map. This typically stores the algorithm, key
     * size, IV size, and password for later retrieval.
     *
     * @param key        The {@link Key} object containing the key information.
     * @param attributes The {@link Attributes} map where the key information will be stored.
     */
    public static void retainKey(Key key, Attributes attributes) {
        attributes.putValue(XJAR_ALGORITHM_KEY, key.getAlgorithm());
        attributes.putValue(XJAR_KEYSIZE_KEY, String.valueOf(key.getKeysize()));
        attributes.putValue(XJAR_IVSIZE_KEY, String.valueOf(key.getIvsize()));
        attributes.putValue(XJAR_PASSWORD_KEY, key.getPassword());
    }

    /**
     * Removes key-related attributes from the provided {@link Attributes} map.
     *
     * @param attributes The {@link Attributes} map from which the key information will be removed.
     */
    public static void removeKey(Attributes attributes) {
        attributes.remove(new Attributes.Name(XJAR_ALGORITHM_KEY));
        attributes.remove(new Attributes.Name(XJAR_KEYSIZE_KEY));
        attributes.remove(new Attributes.Name(XJAR_IVSIZE_KEY));
        attributes.remove(new Attributes.Name(XJAR_PASSWORD_KEY));
    }

    /**
     * Creates a new {@link AllComplex} filter that performs a logical AND operation on its sub-filters.
     *
     * @param <E> The type of element being filtered.
     * @return A new {@link AllComplex} instance.
     */
    public static <E> AllComplex<E> all() {
        return new AllComplex<>();
    }

    /**
     * Creates a new {@link AllComplex} filter that performs a logical AND operation on the given collection of
     * sub-filters.
     *
     * @param <E>     The type of element being filtered.
     * @param filters A collection of {@link Complex} filters to be combined with AND logic.
     * @return A new {@link AllComplex} instance initialized with the given filters.
     */
    public static <E> AllComplex<E> all(Collection<? extends Complex<E>> filters) {
        return new AllComplex<>(filters);
    }

    /**
     * Creates a new {@link AllComplex} filter that performs a logical AND operation on its sub-filters. This is an
     * alias for {@link #all()}.
     *
     * @param <E> The type of element being filtered.
     * @return A new {@link AllComplex} instance.
     */
    public static <E> AllComplex<E> and() {
        return new AllComplex<>();
    }

    /**
     * Creates a new {@link AllComplex} filter that performs a logical AND operation on the given collection of
     * sub-filters. This is an alias for {@link #all(Collection)}.
     *
     * @param <E>     The type of element being filtered.
     * @param filters A collection of {@link Complex} filters to be combined with AND logic.
     * @return A new {@link AllComplex} instance initialized with the given filters.
     */
    public static <E> AllComplex<E> and(Collection<? extends Complex<E>> filters) {
        return new AllComplex<>(filters);
    }

    /**
     * Creates a new {@link AnyComplex} filter that performs a logical OR operation on its sub-filters.
     *
     * @param <E> The type of element being filtered.
     * @return A new {@link AnyComplex} instance.
     */
    public static <E> AnyComplex<E> any() {
        return new AnyComplex<>();
    }

    /**
     * Creates a new {@link AnyComplex} filter that performs a logical OR operation on the given collection of
     * sub-filters.
     *
     * @param <E>     The type of element being filtered.
     * @param filters A collection of {@link Complex} filters to be combined with OR logic.
     * @return A new {@link AnyComplex} instance initialized with the given filters.
     */
    public static <E> AnyComplex<E> any(Collection<? extends Complex<E>> filters) {
        return new AnyComplex<>(filters);
    }

    /**
     * Creates a new {@link AnyComplex} filter that performs a logical OR operation on its sub-filters. This is an alias
     * for {@link #any()}.
     *
     * @param <E> The type of element being filtered.
     * @return A new {@link AnyComplex} instance.
     */
    public static <E> AnyComplex<E> or() {
        return new AnyComplex<>();
    }

    /**
     * Creates a new {@link AnyComplex} filter that performs a logical OR operation on the given collection of
     * sub-filters. This is an alias for {@link #any(Collection)}.
     *
     * @param <E>     The type of element being filtered.
     * @param filters A collection of {@link Complex} filters to be combined with OR logic.
     * @return A new {@link AnyComplex} instance initialized with the given filters.
     */
    public static <E> AnyComplex<E> or(Collection<? extends Complex<E>> filters) {
        return new AnyComplex<>(filters);
    }

    /**
     * Creates a new {@link NotComplex} filter that negates the result of a delegated filter.
     *
     * @param <E>    The type of element being filtered.
     * @param filter The {@link Complex} filter whose result will be negated.
     * @return A new {@link NotComplex} instance that negates the given filter.
     */
    public static <E> Complex<E> not(Complex<E> filter) {
        return new NotComplex<>(filter);
    }

    /**
     * Checks if the given path is relative.
     *
     * @param path The path string to check.
     * @return {@code true} if the path is relative; {@code false} if it is absolute.
     */
    public static boolean isRelative(String path) {
        return !isAbsolute(path);
    }

    /**
     * Checks if the given path is absolute.
     *
     * @param path The path string to check.
     * @return {@code true} if the path is absolute; {@code false} if it is relative.
     */
    public static boolean isAbsolute(String path) {
        if (path.startsWith(Symbol.SLASH)) {
            return true;
        }
        Set<File> roots = new HashSet<>();
        Collections.addAll(roots, File.listRoots());
        File root = new File(path);
        while (null != root.getParentFile()) {
            root = root.getParentFile();
        }
        return roots.contains(root);
    }

    /**
     * Converts a given path to an absolute path. If the path is already absolute, it is normalized. If it is relative,
     * it is resolved against the current working directory.
     *
     * @param path The path string to absolutize.
     * @return The absolute and normalized path string.
     */
    public static String absolutize(String path) {
        return normalize(isAbsolute(path) ? path : System.getProperty("user.dir") + File.separator + path);
    }

    /**
     * Normalizes a path string by replacing multiple consecutive path separators with a single forward slash.
     *
     * @param path The path string to normalize.
     * @return The normalized path string.
     */
    public static String normalize(String path) {
        return path.replaceAll("[/\\\\]+", Symbol.SLASH);
    }

}
