/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.algorithm.SecureRandom;
import org.miaixz.bus.shade.safety.algorithm.SymmetricSecureKey;
import org.miaixz.bus.shade.safety.complex.AllComplex;
import org.miaixz.bus.shade.safety.complex.AnyComplex;
import org.miaixz.bus.shade.safety.complex.NotComplex;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;

/**
 * Jar 工具类,包含I/O,密钥,过滤器的工具方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Builder {

    public static final String WEB_INF_CLASSES = "WEB-INF/classes/";
    public static final String WEB_INF_LIB = "WEB-INF/lib/";
    public static final String META_INF_MANIFEST = Normal.META_INF + "/MANIFEST.MF";
    public static final String XJAR_SRC_DIR = Builder.class.getPackage().getName().replace(Symbol.C_DOT, Symbol.C_SLASH) + Symbol.SLASH;
    public static final String XJAR_INF_DIR = Normal.META_INF + Symbol.SLASH;
    public static final String XJAR_INF_IDX = "FOREST.MF";
    public static final String XJAR_ALGORITHM = "--xjar.algorithm=";
    public static final String XJAR_KEYSIZE = "--xjar.keysize=";
    public static final String XJAR_IVSIZE = "--xjar.ivsize=";
    public static final String XJAR_PASSWORD = "--xjar.password=";
    public static final String XJAR_KEYFILE = "--xjar.keyfile=";
    public static final String XJAR_ALGORITHM_KEY = "Jar-Algorithm";
    public static final String XJAR_KEYSIZE_KEY = "Jar-Keysize";
    public static final String XJAR_IVSIZE_KEY = "Jar-Ivsize";
    public static final String XJAR_PASSWORD_KEY = "Jar-Password";
    public static final String XJAR_KEY_ALGORITHM = "algorithm";
    public static final String XJAR_KEY_KEYSIZE = "keysize";
    public static final String XJAR_KEY_IVSIZE = "ivsize";
    public static final String XJAR_KEY_PASSWORD = "password";
    public static final String XJAR_KEY_HOLD = "hold";
    public static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
    public static final String BOOT_INF_LIB = "BOOT-INF/lib/";
    public static final String CRLF = System.getProperty("line.separator");
    public static final String ALGORITHM = "AES";
    public static int DEFAULT_KEYSIZE = Normal._128;
    public static int DEFAULT_IVSIZE = Normal._128;

    // 保留密钥在 META-INF/MANIFEST.MF 中,启动时无需输入密钥
    public static int FLAG_DANGER = 1;
    // 危险模式：保留密钥
    public static int MODE_DANGER = FLAG_DANGER;
    // 普通模式
    public static int MODE_NORMAL = 0;

    /**
     * 从输入流中读取一行字节码
     *
     * @param in 输入流
     * @return 最前面的一行字节码
     * @throws IOException I/O 异常
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
     * 往输出流中写入一行字节码
     *
     * @param out  输出流
     * @param line 一行字节码
     * @throws IOException I/O 异常
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
     * 关闭资源,等效于XKit.close(closeable, true);
     *
     * @param closeable 资源
     */
    public static void close(Closeable closeable) {
        try {
            close(closeable, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭资源
     *
     * @param closeable 资源
     * @param quietly   是否安静关闭,即捕获到关闭异常时是否忽略
     * @throws IOException 当quietly == false, 时捕获到的I/O异常将会往外抛
     */
    public static void close(Closeable closeable, boolean quietly) throws IOException {
        if (null == closeable) return;
        try {
            closeable.close();
        } catch (IOException e) {
            if (!quietly) throw e;
        }
    }

    /**
     * 输入流传输到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 传输长度
     * @throws IOException I/O 异常
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
     * reader传输到writer
     *
     * @param reader reader
     * @param writer writer
     * @return 传输长度
     * @throws IOException I/O 异常
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
     * 输入流传输到文件
     *
     * @param in   输入流
     * @param file 文件
     * @return 传输长度
     * @throws IOException I/O 异常
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
     * reader传输到文件
     *
     * @param reader reader
     * @param file   文件
     * @return 传输长度
     * @throws IOException I/O 异常
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
     * 删除文件,如果是目录将不递归删除子文件或目录,等效于delete(file, false);
     *
     * @param file 文件/目录
     * @return 是否删除成功
     */
    public static boolean delete(File file) {
        return delete(file, false);
    }

    /**
     * 删除文件,如果是目录将递归删除子文件或目录
     *
     * @param file        文件/目录
     * @param recursively 递归
     * @return 是否删除成功
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
     * 根据密码生成密钥
     *
     * @param password 密码
     * @return 密钥
     * @throws NoSuchAlgorithmException 没有该密钥算法
     */
    public static Key key(String password) throws NoSuchAlgorithmException {
        return key("AES", DEFAULT_KEYSIZE, DEFAULT_IVSIZE, password);
    }

    /**
     * 根据密码生成密钥
     *
     * @param algorithm 密钥算法
     * @param password  密码
     * @return 密钥
     * @throws NoSuchAlgorithmException 没有该密钥算法
     */
    public static Key key(String algorithm, String password) throws NoSuchAlgorithmException {
        return key(algorithm, DEFAULT_KEYSIZE, DEFAULT_IVSIZE, password);
    }

    /**
     * 根据密码生成密钥
     *
     * @param algorithm 密钥算法
     * @param keysize   密钥长度
     * @param password  密码
     * @return 密钥
     * @throws NoSuchAlgorithmException 没有该密钥算法
     */
    public static Key key(String algorithm, int keysize, String password) throws NoSuchAlgorithmException {
        return key(algorithm, keysize, DEFAULT_IVSIZE, password);
    }

    /**
     * 根据密码生成密钥
     *
     * @param algorithm 密钥算法
     * @param keysize   密钥长度
     * @param ivsize    向量长度
     * @param password  密码
     * @return 密钥
     * @throws NoSuchAlgorithmException 没有该密钥算法
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

    public static void retainKey(Key key, Attributes attributes) {
        attributes.putValue(XJAR_ALGORITHM_KEY, key.getAlgorithm());
        attributes.putValue(XJAR_KEYSIZE_KEY, String.valueOf(key.getKeysize()));
        attributes.putValue(XJAR_IVSIZE_KEY, String.valueOf(key.getIvsize()));
        attributes.putValue(XJAR_PASSWORD_KEY, key.getPassword());
    }

    public static void removeKey(Attributes attributes) {
        attributes.remove(new Attributes.Name(XJAR_ALGORITHM_KEY));
        attributes.remove(new Attributes.Name(XJAR_KEYSIZE_KEY));
        attributes.remove(new Attributes.Name(XJAR_IVSIZE_KEY));
        attributes.remove(new Attributes.Name(XJAR_PASSWORD_KEY));
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param <E> 对象
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static <E> AllComplex<E> all() {
        return new AllComplex<>();
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param <E>     对象
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static <E> AllComplex<E> all(Collection<? extends Complex<E>> filters) {
        return new AllComplex<>(filters);
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param <E> 对象
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static <E> AllComplex<E> and() {
        return new AllComplex<>();
    }

    /**
     * 创建多个子过滤器AND连接的混合过滤器
     *
     * @param <E>     对象
     * @param filters 子过滤器
     * @return 多个子过滤器AND连接的混合过滤器
     */
    public static <E> AllComplex<E> and(Collection<? extends Complex<E>> filters) {
        return new AllComplex<>(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param <E> 对象
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static <E> AnyComplex<E> any() {
        return new AnyComplex<>();
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param <E>     对象
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static <E> AnyComplex<E> any(Collection<? extends Complex<E>> filters) {
        return new AnyComplex<>(filters);
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param <E> 对象
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static <E> AnyComplex<E> or() {
        return new AnyComplex<>();
    }

    /**
     * 创建多个子过滤器OR连接的混合过滤器
     *
     * @param <E>     对象
     * @param filters 子过滤器
     * @return 多个子过滤器OR连接的混合过滤器
     */
    public static <E> AnyComplex<E> or(Collection<? extends Complex<E>> filters) {
        return new AnyComplex<>(filters);
    }

    /**
     * 创建非门逻辑运算过滤器,实际上就是将委派过滤器的过滤结果取反
     *
     * @param <E>    对象
     * @param filter 委派过滤器
     * @param <E>    记录类型
     * @return 非门逻辑过滤器
     */
    public static <E> Complex<E> not(Complex<E> filter) {
        return new NotComplex<>(filter);
    }

    public static boolean isRelative(String path) {
        return !isAbsolute(path);
    }

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

    public static String absolutize(String path) {
        return normalize(isAbsolute(path) ? path : System.getProperty("user.dir") + File.separator + path);
    }

    public static String normalize(String path) {
        return path.replaceAll("[/\\\\]+", Symbol.SLASH);
    }

}
