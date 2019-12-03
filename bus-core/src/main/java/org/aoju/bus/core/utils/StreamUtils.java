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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.net.URL;

/**
 * @author Kimi Liu
 * @version 5.3.1
 * @since JDK 1.8+
 */
public class StreamUtils {

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
            throw new InstrumentException(e);
        } finally {
            safeClose(writer);
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
        // 啥都没写,强制触发一下写
        // 这是考虑到 walnut 的输出流实现,比如你写一个空文件
        // 那么输入流就是空的,但是 walnut 的包裹输出流并不知道你写过了
        // 它人你就是打开一个输出流,然后再关上,所以自然不会对内容做改动
        // 所以这里触发一个写,它就知道,喔你要写个空喔
        if (0 == bytesCount) {
            ops.write(buf, 0, 0);
        }
        ops.flush();
        return bytesCount;
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
            throw new InstrumentException(e);
        } finally {
            safeClose(ops);
            safeClose(ins);
        }
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
            throw new InstrumentException(e);
        } finally {
            safeClose(writer);
            safeClose(reader);
        }
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
            throw new InstrumentException(e);
        } finally {
            safeClose(ops);
        }
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
     * 从一个文本流中读取全部内容并返回
     *
     * @param reader 文本输入流
     * @return 文本内容
     */
    public static String readAndClose(Reader reader) {
        try {
            return read(reader).toString();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            safeClose(reader);
        }
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

            char[] data = new char[64];
            int len;
            while (true) {
                if ((len = reader.read(data)) == -1)
                    break;
                sb.append(data, 0, len);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            safeClose(reader);
        }
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
            throw new InstrumentException(e);
        } finally {
            safeClose(reader);
        }
    }

    /**
     * 读取一个输入流中所有的字节
     *
     * @param ins 输入流,必须支持 available()
     * @return 一个字节数组
     * @throws IOException 异常
     */
    public static byte[] readBytes(InputStream ins) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, ins);
        return out.toByteArray();
    }

    /**
     * 读取一个输入流中所有的字节,并关闭输入流
     *
     * @param ins 输入流,必须支持 available()
     * @return 一个字节数组
     */
    public static byte[] readBytesAndClose(InputStream ins) {
        byte[] bytes;
        try {
            bytes = readBytes(ins);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            StreamUtils.safeClose(ins);
        }
        return bytes;
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
        if (null != fa)
            try {
                fa.flush();
            } catch (IOException e) {
            }
    }

    /**
     * 为一个输入流包裹一个缓冲流 如果这个输入流本身就是缓冲流,则直接返回
     *
     * @param ins 输入流
     * @return 缓冲输入流
     */
    public static BufferedInputStream buff(InputStream ins) {
        if (ins == null)
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
        if (ops == null)
            throw new NullPointerException("ops is null!");
        if (ops instanceof BufferedOutputStream)
            return (BufferedOutputStream) ops;
        return new BufferedOutputStream(ops);
    }

    /**
     * 为一个文本输入流包裹一个缓冲流 如果这个输入流本身就是缓冲流,则直接返回
     *
     * @param reader 文本输入流
     * @return 缓冲文本输入流
     */
    public static BufferedReader buffr(Reader reader) {
        if (reader instanceof BufferedReader)
            return (BufferedReader) reader;
        return new BufferedReader(reader);
    }

    /**
     * 为一个文本输出流包裹一个缓冲流 如果这个文本输出流本身就是缓冲流,则直接返回
     *
     * @param ops 文本输出流
     * @return 缓冲文本输出流
     */
    public static BufferedWriter buffw(Writer ops) {
        if (ops instanceof BufferedWriter)
            return (BufferedWriter) ops;
        return new BufferedWriter(ops);
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
            throw new InstrumentException(e);
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
            throw new InstrumentException(e);
        }
    }


    public static Reader utf8r(InputStream is) {
        return new InputStreamReader(utf8filte(is), Charset.UTF_8);
    }

    public static Writer utf8w(OutputStream os) {
        return new OutputStreamWriter(os, Charset.UTF_8);
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
            throw new InstrumentException(e);
        } finally {
            safeClose(fw);
        }

    }

    public static String nextLineTrim(BufferedReader br) throws IOException {
        String line = null;
        while (br.ready()) {
            line = br.readLine();
            if (line == null)
                break;
            if (StringUtils.isBlank(line))
                continue;
            return line.trim();
        }
        return line;
    }

    public static long writeAndClose(OutputStream ops, InputStream ins, int buf) {
        try {
            return write(ops, ins, buf);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            safeClose(ops);
            safeClose(ins);
        }
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

    public static void copy(InputStream in, OutputStream out, byte[] buf)
            throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0)
            if (out != null)
                out.write(buf, 0, count);
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
                    ByteUtils.swapShorts(buf, 0, count);
                    break;
                case 4:
                    ByteUtils.swapInts(buf, 0, count);
                    break;
                case 8:
                    ByteUtils.swapLongs(buf, 0, count);
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
            URL url = ResourceUtils.getResource(name.substring(9), StreamUtils.class);
            if (url == null)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(':') < 2)
            return new FileInputStream(name);
        return new URL(name).openStream();
    }

}
