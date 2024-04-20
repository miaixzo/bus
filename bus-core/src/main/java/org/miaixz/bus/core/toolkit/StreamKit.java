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
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合的stream操作封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamKit {

    private static final int COPY_BUFFER_SIZE = 2048;

    private static final int BUF_SIZE = 8192;
    private static final byte[] UTF_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    /**
     * 判断两个输入流是否严格相等
     *
     * @param ina 输入流
     * @param inb 输入流
     * @return the boolean
     * @throws IOException 异常
     */
    public static boolean equals(InputStream ina, InputStream inb) throws IOException {
        int dA;
        while ((dA = ina.read()) != -1) {
            int dB = inb.read();
            if (dA != dB)
                return false;
        }
        return inb.read() == -1;
    }

    /**
     * 从一个文本流中读取全部内容并返回
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输出流
     *
     * @param reader 文本输出流
     * @return 文本内容
     * @throws IOException 异常
     */
    public static StringBuilder read(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        read(reader, sb);
        return sb;
    }

    /**
     * 读取一个输入流中所有的字节
     *
     * @param ins 输入流,必须支持 available()
     * @return 一个字节数组
     * @throws IOException 异常
     */
    public static byte[] read(InputStream ins) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, ins);
        return out.toByteArray();
    }

    /**
     * 从一个文本流中读取全部内容并写入缓冲
     *
     * @param reader 文本输出流
     * @param sb     输出的文本缓冲
     * @return 读取的字符数量
     * @throws IOException 异常
     */
    public static int read(Reader reader, StringBuilder sb) throws IOException {
        char[] cbuf = new char[BUF_SIZE];
        int count = 0;
        int len;
        while (-1 != (len = reader.read(cbuf))) {
            sb.append(cbuf, 0, len);
            count += len;
        }
        return count;
    }

    /**
     * 从一个文本输入流读取所有内容,并将该流关闭
     *
     * @param reader 文本输入流
     * @return 输入流所有内容
     */
    public static String readAll(Reader reader) {
        if (!(reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        try {
            StringBuilder sb = new StringBuilder();

            char[] data = new char[Normal._64];
            int len;
            while (true) {
                if ((len = reader.read(data)) == -1)
                    break;
                sb.append(data, 0, len);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(reader);
        }
    }

    public static String readText(InputStream in) throws IOException {
        return readText(in, null, -1);
    }

    public static String readText(InputStream in, String encoding) throws IOException {
        return readText(in, encoding, -1);
    }

    public static String readText(InputStream in, String encoding, int bufferSize)
            throws IOException {
        Reader reader = null == encoding ? new InputStreamReader(in) : new InputStreamReader(in,
                encoding);

        return readText(reader, bufferSize);
    }

    public static String readText(Reader reader) throws IOException {
        return readText(reader, -1);
    }

    public static String readText(Reader reader, int bufferSize) throws IOException {
        StringWriter writer = new StringWriter();

        io(reader, writer, bufferSize);
        return writer.toString();
    }

    /**
     * 将一段文本全部写入一个writer
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输出流
     *
     * @param writer 操作器
     * @param cs     文本
     * @throws IOException 异常
     */
    public static void write(Writer writer, CharSequence cs) throws IOException {
        if (null != cs && null != writer) {
            writer.write(cs.toString());
            writer.flush();
        }
    }

    /**
     * 将输入流写入一个输出流 块大小为 8192
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输入/出流
     *
     * @param ops 输出流
     * @param ins 输入流
     * @return 写入的字节数
     * @throws IOException 异常
     */
    public static long write(OutputStream ops, InputStream ins) throws IOException {
        return write(ops, ins, BUF_SIZE);
    }

    /**
     * 将输入流写入一个输出流
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输入/出流
     *
     * @param ops        输出流
     * @param ins        输入流
     * @param bufferSize 缓冲块大小
     * @return 写入的字节数
     * @throws IOException 异常
     */
    public static long write(OutputStream ops, InputStream ins, int bufferSize) throws IOException {
        if (null == ops || null == ins)
            return 0;

        byte[] buf = new byte[bufferSize];
        int len;
        long bytesCount = 0;
        while (-1 != (len = ins.read(buf))) {
            bytesCount += len;
            ops.write(buf, 0, len);
        }
        if (0 == bytesCount) {
            ops.write(buf, 0, 0);
        }
        ops.flush();
        return bytesCount;
    }

    /**
     * 将文本输入流写入一个文本输出流 块大小为 8192
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输入/出流
     *
     * @param writer 输出流
     * @param reader 输入流
     * @return the long
     * @throws IOException 异常
     */
    public static long write(Writer writer, Reader reader) throws IOException {
        if (null == writer || null == reader)
            return 0;

        char[] cbuf = new char[BUF_SIZE];
        int len, count = 0;
        while (true) {
            len = reader.read(cbuf);
            if (len == -1)
                break;
            writer.write(cbuf, 0, len);
            count += len;
        }
        return count;
    }

    /**
     * 将一个字节数组写入一个输出流
     * <p>
     * <b style=color:red>注意</b>,它并不会关闭输出流
     *
     * @param ops   输出流
     * @param bytes 字节数组
     * @throws IOException 异常
     */
    public static void write(OutputStream ops, byte[] bytes) throws IOException {
        if (null == ops || null == bytes || bytes.length == 0)
            return;
        ops.write(bytes);
    }


    /**
     * 从一个文本流中读取全部内容并返回
     *
     * @param reader 文本输入流
     * @return 文本内容
     */
    public static String readAndClose(Reader reader) {
        try {
            return read(reader).toString();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(reader);
        }
    }

    /**
     * 读取一个输入流中所有的字节,并关闭输入流
     *
     * @param ins 输入流,必须支持 available()
     * @return 一个字节数组
     */
    public static byte[] readAndClose(InputStream ins) {
        byte[] bytes;
        try {
            bytes = read(ins);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            StreamKit.safeClose(ins);
        }
        return bytes;
    }

    /**
     * 从一个文本流中读取全部内容并写入缓冲
     * <p>
     * <b style=color:red>注意</b>,它会关闭输出流
     *
     * @param reader 文本输出流
     * @param sb     输出的文本缓冲
     * @return 读取的字符数量
     */
    public static int readAndClose(InputStreamReader reader, StringBuilder sb) {
        try {
            return read(reader, sb);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(reader);
        }
    }

    /**
     * 将一段文本全部写入一个writer
     * <p>
     * <b style=color:red>注意</b>,它会关闭输出流
     *
     * @param writer 输出流
     * @param cs     文本
     */
    public static void writeAndClose(Writer writer, CharSequence cs) {
        try {
            write(writer, cs);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(writer);
        }
    }

    /**
     * 将输入流写入一个输出流 块大小为 8192
     * <p>
     * <b style=color:red>注意</b>,它会关闭输入/出流
     *
     * @param ops 输出流
     * @param ins 输入流
     * @return 写入的字节数
     */
    public static long writeAndClose(OutputStream ops, InputStream ins) {
        try {
            return write(ops, ins);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(ops);
            safeClose(ins);
        }
    }

    /**
     * 将文本输入流写入一个文本输出流 块大小为 8192
     * <p>
     * <b style=color:red>注意</b>,它会关闭输入/出流
     *
     * @param writer 输出流
     * @param reader 输入流
     * @return the long
     */
    public static long writeAndClose(Writer writer, Reader reader) {
        try {
            return write(writer, reader);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(writer);
            safeClose(reader);
        }
    }

    /**
     * 将一个字节数组写入一个输出流
     * <p>
     * <b style=color:red>注意</b>,它会关闭输出流
     *
     * @param ops   输出流
     * @param bytes 字节数组
     */
    public static void writeAndClose(OutputStream ops, byte[] bytes) {
        try {
            write(ops, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(ops);
        }
    }

    public static long writeAndClose(OutputStream ops, InputStream ins, int buf) {
        try {
            return write(ops, ins, buf);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(ops);
            safeClose(ins);
        }
    }

    /**
     * 为一个文本输入流包裹一个缓冲流 如果这个输入流本身就是缓冲流,则直接返回
     *
     * @param reader 文本输入流
     * @return 缓冲文本输入流
     */
    public static BufferedReader buff(Reader reader) {
        if (reader instanceof BufferedReader)
            return (BufferedReader) reader;
        return new BufferedReader(reader);
    }

    /**
     * 为一个文本输出流包裹一个缓冲流 如果这个文本输出流本身就是缓冲流,则直接返回
     *
     * @param writer 文本输出流
     * @return 缓冲文本输出流
     */
    public static BufferedWriter buff(Writer writer) {
        if (writer instanceof BufferedWriter)
            return (BufferedWriter) writer;
        return new BufferedWriter(writer);
    }

    /**
     * 为一个输入流包裹一个缓冲流 如果这个输入流本身就是缓冲流,则直接返回
     *
     * @param ins 输入流
     * @return 缓冲输入流
     */
    public static BufferedInputStream buff(InputStream ins) {
        if (null == ins)
            throw new NullPointerException("ins is null!");
        if (ins instanceof BufferedInputStream)
            return (BufferedInputStream) ins;
        // BufferedInputStream的构造方法,竟然是允许null参数的!! 我&$#^$&%
        return new BufferedInputStream(ins);
    }

    /**
     * 为一个输出流包裹一个缓冲流 如果这个输出流本身就是缓冲流,则直接返回
     *
     * @param ops 输出流
     * @return 缓冲输出流
     */
    public static BufferedOutputStream buff(OutputStream ops) {
        if (null == ops)
            throw new NullPointerException("ops is null!");
        if (ops instanceof BufferedOutputStream)
            return (BufferedOutputStream) ops;
        return new BufferedOutputStream(ops);
    }

    public static void readFully(InputStream in, byte[] b, int off, int len)
            throws IOException {
        if (readAvailable(in, b, off, len) < len)
            throw new EOFException();
    }

    public static void skipFully(InputStream in, long n) throws IOException {
        while (n > 0) {
            long count = in.skip(n);
            if (count == 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                count = 1;
            }
            n -= count;
        }
    }

    /**
     * 关闭一个可关闭对象,可以接受 null 如果成功关闭,返回 true,发生异常 返回 false
     *
     * @param cb 可关闭对象
     * @return 是否成功关闭
     */
    public static boolean safeClose(Closeable cb) {
        if (null != cb)
            try {
                cb.close();
            } catch (IOException e) {
                return false;
            }
        return true;
    }

    /**
     * 安全刷新一个可刷新的对象,可接受 null
     *
     * @param fa 可刷新对象
     */
    public static void safeFlush(Flushable fa) {
        if (null != fa) {
            try {
                fa.flush();
            } catch (IOException e) {
            }
        }
    }

    public static Reader utf8r(InputStream is) {
        return new InputStreamReader(utf8filte(is), Charset.UTF_8);
    }

    public static Writer utf8w(OutputStream os) {
        return new OutputStreamWriter(os, Charset.UTF_8);
    }

    /**
     * 判断并移除UTF-8的BOM头
     *
     * @param in 输入流
     * @return 输入流
     */
    public static InputStream utf8filte(InputStream in) {
        try {
            if (in.available() == -1)
                return in;
            PushbackInputStream pis = new PushbackInputStream(in, 3);
            byte[] header = new byte[3];
            int len = pis.read(header, 0, 3);
            if (len < 1)
                return in;
            if (header[0] != UTF_BOM[0] || header[1] != UTF_BOM[1] || header[2] != UTF_BOM[2]) {
                pis.unread(header, 0, len);
            }
            return pis;
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 根据一个文件建立一个输出流
     *
     * @param file 文件
     * @return 输出流
     */
    public static OutputStream fileOut(File file) {
        try {
            return buff(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new InternalException(e);
        }
    }

    public static InputStream wrap(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static void appendWriteAndClose(File f, String text) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(f, true);
            fw.write(text);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            safeClose(fw);
        }
    }

    /**
     * 获取输出流
     *
     * @param path  文件路径
     * @param klass 参考的类, -- 会用这个类的 ClassLoader
     * @param enc   文件路径编码
     * @return 输出流
     */
    public static InputStream findFileAsStream(String path, Class<?> klass, String enc) {
        File f = new File(path);
        if (f.exists())
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e1) {
                return null;
            }
        if (null != klass) {
            InputStream ins = klass.getClassLoader().getResourceAsStream(path);
            if (null == ins)
                ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (null != ins)
                return ins;
        }
        return ClassLoader.getSystemResourceAsStream(path);
    }

    public static int readAvailable(InputStream in, byte[] b, int off, int len)
            throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0)
                break;
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }


    public static String nextLineTrim(BufferedReader br) throws IOException {
        String line = null;
        while (br.ready()) {
            line = br.readLine();
            if (null == line)
                break;
            if (StringKit.isBlank(line))
                continue;
            return line.trim();
        }
        return line;
    }

    public static void copy(InputStream in, OutputStream out, byte[] buf)
            throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0)
            if (null != out) {
                out.write(buf, 0, count);
            }
    }

    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        copy(in, out, new byte[COPY_BUFFER_SIZE]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            byte[] buf) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            int count = in.read(buf, 0, Math.min(len, buf.length));
            if (count < 0)
                throw new EOFException();
            out.write(buf, 0, count);
            len -= count;
        }
    }

    public static void copy(InputStream in, OutputStream out, int len)
            throws IOException {
        copy(in, out, len, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            int swapBytes, byte[] buf) throws IOException {
        if (swapBytes == 1) {
            copy(in, out, len, buf);
            return;
        }
        if (!(swapBytes == 2 || swapBytes == 4))
            throw new IllegalArgumentException("swapBytes: " + swapBytes);
        if (len < 0 || (len % swapBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
        int off = 0;
        while (len > 0) {
            int count = in.read(buf, off, Math.min(len, buf.length - off));
            if (count < 0)
                throw new EOFException();
            len -= count;
            count += off;
            off = count % swapBytes;
            count -= off;
            switch (swapBytes) {
                case 2:
                    ByteKit.swapShorts(buf, 0, count);
                    break;
                case 4:
                    ByteKit.swapInts(buf, 0, count);
                    break;
                case 8:
                    ByteKit.swapLongs(buf, 0, count);
                    break;
            }
            out.write(buf, 0, count);
            if (off > 0)
                System.arraycopy(buf, count, buf, 0, off);
        }
    }

    public static void copy(InputStream in, OutputStream out, int len,
                            int swapBytes) throws IOException {
        copy(in, out, len, swapBytes, new byte[Math.min(len, COPY_BUFFER_SIZE)]);
    }

    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = FileKit.getUrl(name.substring(9), StreamKit.class);
            if (null == url)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(Symbol.C_COLON) < 2)
            return new FileInputStream(name);
        return new URL(name).openStream();
    }

    public static void io(Reader in, Writer out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = 8192 >> 1;
        }

        char[] buffer = new char[bufferSize];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    public static <T> Stream<T> of(T... array) {
        Assert.notNull(array, "Array must be not null!");
        return Stream.of(array);
    }

    /**
     * {@link Iterable}转换为{@link Stream}，默认非并行
     *
     * @param iterable 集合
     * @param <T>      集合元素类型
     * @return {@link Stream}
     */
    public static <T> Stream<T> of(Iterable<T> iterable) {
        return of(iterable, false);
    }

    /**
     * {@link Iterable}转换为{@link Stream}
     *
     * @param iterable 集合
     * @param parallel 是否并行
     * @param <T>      集合元素类型
     * @return {@link Stream}
     */
    public static <T> Stream<T> of(Iterable<T> iterable, boolean parallel) {
        Assert.notNull(iterable, "Iterable must be not null!");
        return iterable instanceof Collection ?
                ((Collection<T>) iterable).stream() :
                StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * {@link Iterator} 转换为 {@link Stream}
     *
     * @param iterator 迭代器
     * @param <T>      集合元素类型
     * @return {@link Stream}
     * @throws IllegalArgumentException 如果iterator为null，抛出该异常
     */
    public static <T> Stream<T> of(Iterator<T> iterator) {
        return of(iterator, false);
    }

    /**
     * {@link Iterator} 转换为 {@link Stream}
     *
     * @param iterator 迭代器
     * @param parallel 是否并行
     * @param <T>      集合元素类型
     * @return {@link Stream}
     * @throws IllegalArgumentException 如果iterator为null，抛出该异常
     */
    public static <T> Stream<T> of(Iterator<T> iterator, boolean parallel) {
        Assert.notNull(iterator, "iterator must not be null!");
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param file 文件
     * @return {@link Stream}
     */
    public static Stream<String> of(File file) {
        return of(file, Charset.UTF_8);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param path 路径
     * @return {@link Stream}
     */
    public static Stream<String> of(Path path) {
        return of(path, Charset.UTF_8);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param file    文件
     * @param charset 编码
     * @return {@link Stream}
     */
    public static Stream<String> of(File file, java.nio.charset.Charset charset) {
        Assert.notNull(file, "File must be not null!");
        return of(file.toPath(), charset);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param path    路径
     * @param charset 编码
     * @return {@link Stream}
     */
    public static Stream<String> of(Path path, java.nio.charset.Charset charset) {
        try {
            return Files.lines(path, charset);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

}
