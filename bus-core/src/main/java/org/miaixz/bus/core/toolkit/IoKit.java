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

import org.miaixz.bus.core.collection.LineIterator;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.Progress;
import org.miaixz.bus.core.io.Segment;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.copier.ChannelCopier;
import org.miaixz.bus.core.io.copier.FileChannelCopier;
import org.miaixz.bus.core.io.copier.ReaderWriterCopier;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.RealSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.RealSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.stream.*;
import org.miaixz.bus.core.io.timout.AsyncTimeout;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.function.XConsumer;

import java.io.ObjectInputStream;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;

/**
 * IO工具类
 * IO工具类只是辅助流的读写,并不负责关闭流
 * 原因是流可能被多次读写,读写关闭后容易造成问题
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IoKit {

    /**
     * 默认缓存大小 8192
     */
    public static final int DEFAULT_BUFFER_SIZE = 2 << 12;
    /**
     * 默认大缓存大小 32768
     */
    public static final int DEFAULT_LARGE_BUFFER_SIZE = 2 << 14;

    /**
     * 数据流末尾
     */
    public static final int EOF = -1;


    private IoKit() {
    }

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }

    public static short reverseBytesShort(short s) {
        int i = s & 0xffff;
        int reversed = (i & 0xff00) >>> 8
                | (i & 0x00ff) << 8;
        return (short) reversed;
    }

    public static int reverseBytesInt(int i) {
        return (i & 0xff000000) >>> 24
                | (i & 0x00ff0000) >>> 8
                | (i & 0x0000ff00) << 8
                | (i & 0x000000ff) << 24;
    }

    public static long reverseBytesLong(long v) {
        return (v & 0xff00000000000000L) >>> 56
                | (v & 0x00ff000000000000L) >>> 40
                | (v & 0x0000ff0000000000L) >>> 24
                | (v & 0x000000ff00000000L) >>> 8
                | (v & 0x00000000ff000000L) << 8
                | (v & 0x0000000000ff0000L) << 24
                | (v & 0x000000000000ff00L) << 40
                | (v & 0x00000000000000ffL) << 56;
    }

    /**
     * 即使被声明也不允许直接抛出
     * 这是一种很糟糕的做饭,很容易遭到攻击
     * 清理后捕获并重新抛出异常 参见Java Puzzlers #43
     *
     * @param t 异常
     */
    public static void sneakyRethrow(Throwable t) {
        IoKit.<Error>sneakyThrow2(t);
    }

    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
        throw (T) t;
    }

    public static boolean arrayRangeEquals(
            byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            if (a[i + aOffset] != b[i + bOffset]) return false;
        }
        return true;
    }

    /**
     * 将Reader中的内容复制到Writer中 使用默认缓存大小，拷贝后不关闭Reader
     *
     * @param reader Reader
     * @param writer Writer
     * @return 拷贝的字节数
     */
    public static long copy(final Reader reader, final Writer writer) {
        return copy(reader, writer, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader     Reader
     * @param writer     Writer
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize) {
        return copy(reader, writer, bufferSize, null);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader     Reader
     * @param writer     Writer
     * @param bufferSize 缓存大小
     * @param progress   进度处理器
     * @return 传输的byte数
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize, final Progress progress) {
        return copy(reader, writer, bufferSize, -1, progress);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader     Reader，非空
     * @param writer     Writer，非空
     * @param bufferSize 缓存大小，-1表示默认
     * @param count      最大长度，-1表示无限制
     * @param progress   进度处理器，{@code null}表示无
     * @return 传输的byte数
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize, final long count, final Progress progress) {
        Assert.notNull(reader, "Reader is null !");
        Assert.notNull(writer, "Writer is null !");
        return new ReaderWriterCopier(bufferSize, count, progress).copy(reader, writer);
    }

    /**
     * 拷贝流，使用默认Buffer大小，拷贝后不关闭流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 传输的byte数
     */
    public static long copy(final InputStream in, final OutputStream out) {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize) {
        return copy(in, out, bufferSize, null);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @param progress   进度条
     * @return 传输的byte数
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize, final Progress progress) {
        return copy(in, out, bufferSize, -1, progress);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @param count      总拷贝长度，-1表示无限制
     * @param progress   进度条
     * @return 传输的byte数
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize, final long count, final Progress progress) {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");
        final long copySize = copy(Channels.newChannel(in), Channels.newChannel(out), bufferSize, count, progress);
        flush(out);
        return copySize;
    }

    /**
     * 拷贝文件Channel，使用NIO，拷贝后不会关闭channel
     *
     * @param inChannel  {@link FileChannel}
     * @param outChannel {@link FileChannel}
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(FileChannel inChannel, FileChannel outChannel) throws InternalException {
        Assert.notNull(inChannel, "In channel is null!");
        Assert.notNull(outChannel, "Out channel is null!");

        try {
            return copySafely(inChannel, outChannel);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 文件拷贝实现
     *
     * <pre>
     * FileChannel#transferTo 或 FileChannel#transferFrom 的实现是平台相关的，需要确保低版本平台的兼容性
     * 例如 android 7以下平台在使用 ZipInputStream 解压文件的过程中，
     * 通过 FileChannel#transferFrom 传输到文件时，其返回值可能小于 totalBytes，不处理将导致文件内容缺失
     *
     * // 错误写法，dstChannel.transferFrom 返回值小于 zipEntry.getSize()，导致解压后文件内容缺失
     * try (InputStream srcStream = zipFile.getInputStream(zipEntry);
     * 		ReadableByteChannel srcChannel = Channels.newChannel(srcStream);
     * 		FileOutputStream fos = new FileOutputStream(saveFile);
     * 		FileChannel dstChannel = fos.getChannel()) {
     * 		dstChannel.transferFrom(srcChannel, 0, zipEntry.getSize());
     *  }
     * </pre>
     *
     * @param inChannel  输入通道
     * @param outChannel 输出通道
     * @return 输入通道的字节数
     * @throws IOException 发生IO错误
     */
    private static long copySafely(FileChannel inChannel, FileChannel outChannel) throws IOException {
        final long totalBytes = inChannel.size();
        // 确保文件内容不会缺失
        for (long pos = 0, remaining = totalBytes; remaining > 0; ) {
            // 实际传输的字节数
            final long writeBytes = inChannel.transferTo(pos, remaining, outChannel);
            pos += writeBytes;
            remaining -= writeBytes;
        }
        return totalBytes;
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in  {@link ReadableByteChannel}
     * @param out {@link WritableByteChannel}
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out) throws InternalException {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in         {@link ReadableByteChannel}
     * @param out        {@link WritableByteChannel}
     * @param bufferSize 缓冲大小，如果小于等于0，使用默认
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize) throws InternalException {
        return copy(in, out, bufferSize, null);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in         {@link ReadableByteChannel}
     * @param out        {@link WritableByteChannel}
     * @param bufferSize 缓冲大小，如果小于等于0，使用默认
     * @param progress   {@link Progress}进度处理器
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize, Progress progress) throws InternalException {
        return copy(in, out, bufferSize, -1, progress);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in         {@link ReadableByteChannel}
     * @param out        {@link WritableByteChannel}
     * @param bufferSize 缓冲大小，如果小于等于0，使用默认
     * @param count      读取总长度
     * @param progress   {@link Progress}进度处理器
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize, long count, Progress progress) throws InternalException {
        return new ChannelCopier(bufferSize, count, progress).copy(in, out);
    }

    /**
     * 文件拷贝实现
     *
     * @param in  输入
     * @param out 输出
     * @return 拷贝的字节数
     */
    public static long copy(final FileInputStream in, final FileOutputStream out) {
        Assert.notNull(in, "FileInputStream is null!");
        Assert.notNull(out, "FileOutputStream is null!");

        return new FileChannelCopier(-1).copy(in, out);
    }

    /**
     * 获得一个文件读取器
     *
     * @param in          输入流
     * @param charsetName 字符集名称
     * @return BufferedReader对象
     */
    public static BufferedReader getReader(InputStream in, String charsetName) {
        return getReader(in, java.nio.charset.Charset.forName(charsetName));
    }

    /**
     * 获得一个Reader
     *
     * @param in      输入流
     * @param charset 字符集
     * @return BufferedReader对象
     */
    public static BufferedReader getReader(InputStream in, java.nio.charset.Charset charset) {
        if (null == in) {
            return null;
        }

        final InputStreamReader reader;
        if (null == charset) {
            reader = new InputStreamReader(in);
        } else {
            reader = new InputStreamReader(in, charset);
        }

        return new BufferedReader(reader);
    }

    /**
     * 从{@link BOMInputStream}中获取Reader
     *
     * @param in {@link BOMInputStream}
     * @return {@link BufferedReader}
     */
    public static BufferedReader getReader(BOMInputStream in) {
        return getReader(in, in.getCharset());
    }

    /**
     * 从{@link InputStream}中获取{@link BOMReader}
     *
     * @param in {@link InputStream}
     * @return {@link BOMReader}
     */
    public static BOMReader getReader(InputStream in) {
        return new BOMReader(in);
    }

    /**
     * 获得{@link BufferedReader}
     * 如果是{@link BufferedReader}强转返回,否则新建 如果提供的Reader为null返回null
     *
     * @param reader 普通Reader,如果为null返回null
     * @return {@link BufferedReader} or null
     */
    public static BufferedReader getReader(Reader reader) {
        if (null == reader) {
            return null;
        }

        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * 获得{@link PushbackReader}
     * 如果是{@link PushbackReader}强转返回,否则新建
     *
     * @param reader       普通Reader
     * @param pushBackSize 推后的byte数
     * @return {@link PushbackReader}
     */
    public static PushbackReader getPushBackReader(Reader reader, int pushBackSize) {
        return (reader instanceof PushbackReader) ? (PushbackReader) reader : new PushbackReader(reader, pushBackSize);
    }

    /**
     * 获得一个Writer
     *
     * @param out         输入流
     * @param charsetName 字符集
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter getWriter(OutputStream out, String charsetName) {
        return getWriter(out, java.nio.charset.Charset.forName(charsetName));
    }

    /**
     * 获得一个Writer
     *
     * @param out     输入流
     * @param charset 字符集
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter getWriter(OutputStream out, java.nio.charset.Charset charset) {
        if (null == out) {
            return null;
        }

        if (null == charset) {
            return new OutputStreamWriter(out);
        } else {
            return new OutputStreamWriter(out, charset);
        }
    }

    /**
     * 从流中读取内容
     *
     * @param in          输入流
     * @param charsetName 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String read(InputStream in, String charsetName) throws InternalException {
        FastByteOutputStream out = read(in);
        return StringKit.isBlank(charsetName) ? out.toString() : out.toString(Charset.charset(charsetName));
    }

    /**
     * 从流中读取内容，读取完毕后关闭流
     *
     * @param in      输入流,读取完毕后关闭流
     * @param charset 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String read(InputStream in, java.nio.charset.Charset charset) throws InternalException {
        FastByteOutputStream out = read(in);
        return null == charset ? out.toString() : out.toString(charset);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后关闭流
     *
     * @param in 输入流
     * @return 输出流
     * @throws InternalException 异常
     */
    public static FastByteOutputStream read(InputStream in) throws InternalException {
        return read(in, true);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后可选是否关闭流
     *
     * @param in      输入流
     * @param isClose 读取完毕后是否关闭流
     * @return 输出流
     * @throws InternalException IO异常
     */
    public static FastByteOutputStream read(InputStream in, boolean isClose) throws InternalException {
        return StreamReader.of(in, isClose).read();
    }

    /**
     * 从Reader中读取String,读取完毕后并关闭Reader
     *
     * @param reader Reader
     * @return String
     * @throws InternalException 异常
     */
    public static String read(Reader reader) throws InternalException {
        return read(reader, true);
    }

    /**
     * 从{@link Reader}中读取String
     *
     * @param reader  {@link Reader}
     * @param isClose 是否关闭{@link Reader}
     * @return String
     * @throws InternalException IO异常
     */
    public static String read(Reader reader, boolean isClose) throws InternalException {
        final StringBuilder builder = StringKit.builder();
        final CharBuffer buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip());
            }
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (isClose) {
                IoKit.close(reader);
            }
        }
        return builder.toString();
    }

    /**
     * 从FileChannel中读取UTF-8编码内容
     *
     * @param fileChannel 文件管道
     * @return 内容
     * @throws InternalException 异常
     */
    public static String read(FileChannel fileChannel) throws InternalException {
        return read(fileChannel, Charset.UTF_8);
    }

    /**
     * 从FileChannel中读取内容,读取完毕后并不关闭Channel
     *
     * @param fileChannel 文件管道
     * @param charsetName 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String read(FileChannel fileChannel, String charsetName) throws InternalException {
        return read(fileChannel, Charset.charset(charsetName));
    }

    /**
     * 从FileChannel中读取内容
     *
     * @param fileChannel 文件管道
     * @param charset     字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String read(FileChannel fileChannel, java.nio.charset.Charset charset) throws InternalException {
        MappedByteBuffer buffer;
        try {
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return StringKit.toString(buffer, charset);
    }

    /**
     * 从流中读取内容，读取完毕后并不关闭流
     *
     * @param channel 可读通道，读取完毕后并不关闭通道
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String read(ReadableByteChannel channel, java.nio.charset.Charset charset) throws InternalException {
        FastByteOutputStream out = read(channel);
        return null == charset ? out.toString() : out.toString(charset);
    }

    /**
     * 从流中读取内容，读到输出流中
     *
     * @param channel 可读通道，读取完毕后并不关闭通道
     * @return 输出流
     * @throws InternalException IO异常
     */
    public static FastByteOutputStream read(ReadableByteChannel channel) throws InternalException {
        final FastByteOutputStream out = new FastByteOutputStream();
        copy(channel, Channels.newChannel(out));
        return out;
    }

    /**
     * 从流中读取bytes
     *
     * @param in {@link InputStream}
     * @return bytes
     * @throws InternalException 异常
     */
    public static byte[] readBytes(InputStream in) throws InternalException {
        return readBytes(in, true);
    }

    /**
     * 从流中读取bytes
     *
     * @param in      {@link InputStream}
     * @param isClose 是否关闭输入流
     * @return bytes
     * @throws InternalException IO异常
     */
    public static byte[] readBytes(InputStream in, boolean isClose) throws InternalException {
        if (in instanceof FileInputStream) {
            // 文件流的长度是可预见的，此时直接读取效率更高
            final byte[] result;
            try {
                final int available = in.available();
                result = new byte[available];
                final int readLength = in.read(result);
                if (readLength != available) {
                    throw new IOException(StringKit.format("File length is [{}] but read [{}]!", available, readLength));
                }
            } catch (IOException e) {
                throw new InternalException(e);
            } finally {
                if (isClose) {
                    close(in);
                }
            }
            return result;
        }

        // 未知bytes总量的流
        return read(in, isClose).toByteArray();
    }

    /**
     * 读取指定长度的byte数组,不关闭流
     *
     * @param in     {@link InputStream},为null返回null
     * @param length 长度,小于等于0返回空byte数组
     * @return bytes
     * @throws InternalException 异常
     */
    public static byte[] readBytes(InputStream in, int length) throws InternalException {
        if (null == in) {
            return null;
        }
        if (length <= 0) {
            return Normal.EMPTY_BYTE_ARRAY;
        }

        final FastByteOutputStream out = new FastByteOutputStream(length);
        copy(in, out, DEFAULT_BUFFER_SIZE, length, null);
        return out.toByteArray();
    }

    /**
     * 读取16进制字符串
     *
     * @param in          {@link InputStream}
     * @param length      长度
     * @param toLowerCase true 传换成小写格式 , false 传换成大写格式
     * @return 16进制字符串
     * @throws InternalException 异常
     */
    public static String readHex(InputStream in, int length, boolean toLowerCase) throws InternalException {
        return HexKit.encodeHexString(readBytes(in, length), toLowerCase);
    }

    /**
     * 从流中读取前28个byte并转换为16进制,字母部分使用大写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws InternalException 异常
     */
    public static String readHex28Upper(InputStream in) throws InternalException {
        return readHex(in, 28, false);
    }

    /**
     * 从流中读取前28个byte并转换为16进制,字母部分使用小写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws InternalException 异常
     */
    public static String readHex28Lower(InputStream in) throws InternalException {
        return readHex(in, 28, true);
    }

    /**
     * 从流中读取内容,读到输出流中
     *
     * @param <T> 读取对象的类型
     * @param in  输入流
     * @return 输出流
     * @throws InternalException 异常
     */
    public static <T> T readObject(InputStream in) throws InternalException {
        if (null == in) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(in);
            final T object = (T) ois.readObject();
            return object;
        } catch (IOException e) {
            throw new InternalException(e);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 从流中读取内容,使用UTF-8编码
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(InputStream in, T collection) throws InternalException {
        return readLines(in, Charset.UTF_8, collection);
    }

    /**
     * 从流中读取内容
     *
     * @param <T>         集合类型
     * @param in          输入流
     * @param charsetName 字符集
     * @param collection  返回集合
     * @return 内容
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(InputStream in, String charsetName, T collection) throws InternalException {
        return readLines(in, Charset.charset(charsetName), collection);
    }

    /**
     * 从流中读取内容
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param charset    字符集
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(InputStream in, java.nio.charset.Charset charset, T collection) throws InternalException {
        return readLines(getReader(in, charset), collection);
    }

    /**
     * 从Reader中读取内容
     *
     * @param <T>        集合类型
     * @param reader     {@link Reader}
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(Reader reader, T collection) throws InternalException {
        readLines(reader, (XConsumer<String>) collection::add);
        return collection;
    }

    /**
     * 按行读取UTF-8编码数据,针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param lineHandler 行处理接口,实现accept方法用于编辑一行的数据后入到指定地方
     * @throws InternalException 异常
     */
    public static void readLines(InputStream in, XConsumer<String> lineHandler) throws InternalException {
        readLines(in, Charset.UTF_8, lineHandler);
    }

    /**
     * 按行读取数据,针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param charset     {@link java.nio.charset.Charset}编码
     * @param lineHandler 行处理接口,实现accept方法用于编辑一行的数据后入到指定地方
     * @throws InternalException 异常
     */
    public static void readLines(InputStream in, java.nio.charset.Charset charset, XConsumer<String> lineHandler) throws InternalException {
        readLines(getReader(in, charset), lineHandler);
    }

    /**
     * 按行读取数据,针对每行的数据做处理
     * {@link Reader}自带编码定义,因此读取数据的编码跟随其编码
     *
     * @param reader      {@link Reader}
     * @param lineHandler 行处理接口,实现accept方法用于编辑一行的数据后入到指定地方
     * @throws InternalException 异常
     */
    public static void readLines(Reader reader, XConsumer<String> lineHandler) throws InternalException {
        Assert.notNull(reader);
        Assert.notNull(lineHandler);

        for (String line : new LineIterator(reader)) {
            lineHandler.accept(line);
        }
    }

    /**
     * String 转为流
     *
     * @param content     内容
     * @param charsetName 编码
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(String content, String charsetName) {
        return toStream(content, Charset.charset(charsetName));
    }

    /**
     * String 转为流
     *
     * @param content 内容
     * @param charset 编码
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(String content, java.nio.charset.Charset charset) {
        if (null == content) {
            return null;
        }
        return toStream(StringKit.bytes(content, charset));
    }

    /**
     * String 转为UTF-8编码的字节流流
     *
     * @param content 内容
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(String content) {
        return toStream(content, Charset.UTF_8);
    }

    /**
     * 文件转为{@link InputStream}
     *
     * @param file 文件
     * @return {@link InputStream}
     */
    public static InputStream toStream(File file) {
        Assert.notNull(file);
        return toStream(file.toPath());
    }

    /**
     * 文件转为{@link InputStream}
     *
     * @param path {@link Path}，非空
     * @return {@link InputStream}
     */
    public static InputStream toStream(final Path path) {
        Assert.notNull(path);
        try {
            return Files.newInputStream(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * String 转为流
     *
     * @param content 内容bytes
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(byte[] content) {
        if (null == content) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * {@link FastByteOutputStream}转为{@link ByteArrayInputStream}
     *
     * @param out {@link java.io.ByteArrayOutputStream}
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(FastByteOutputStream out) {
        if (out == null) {
            return null;
        }
        return new ByteArrayInputStream(out.toByteArray());
    }


    /**
     * 将{@link InputStream}转换为支持mark标记的流
     * 若原流支持mark标记，则返回原流，否则使用{@link BufferedInputStream} 包装之
     *
     * @param in 流
     * @return {@link InputStream}
     */
    public static InputStream toMarkSupportStream(final InputStream in) {
        if (null == in) {
            return null;
        }
        if (false == in.markSupported()) {
            return new BufferedInputStream(in);
        }
        return in;
    }

    /**
     * 转换为{@link PushbackInputStream}
     * 如果传入的输入流已经是{@link PushbackInputStream}，强转返回，否则新建一个
     *
     * @param in           {@link InputStream}
     * @param pushBackSize 推后的byte数
     * @return {@link PushbackInputStream}
     */
    public static PushbackInputStream toPushbackStream(final InputStream in, final int pushBackSize) {
        return (in instanceof PushbackInputStream) ? (PushbackInputStream) in : new PushbackInputStream(in, pushBackSize);
    }

    /**
     * 将指定{@link InputStream} 转换为{@link InputStream#available()}方法可用的流。
     * 在Socket通信流中，服务端未返回数据情况下{@link InputStream#available()}方法始终为{@code 0}
     * 因此，在读取前需要调用{@link InputStream#read()}读取一个字节（未返回会阻塞），一旦读取到了，{@link InputStream#available()}方法就正常了。
     * 需要注意的是，在网络流中，是按照块来传输的，所以 {@link InputStream#available()} 读取到的并非最终长度，而是此次块的长度。
     * 此方法返回对象的规则为：
     *
     * <ul>
     *     <li>FileInputStream 返回原对象，因为文件流的available方法本身可用</li>
     *     <li>其它InputStream 返回PushbackInputStream</li>
     * </ul>
     *
     * @param in 被转换的流
     * @return 转换后的流，可能为{@link PushbackInputStream}
     */
    public static InputStream toAvailableStream(final InputStream in) {
        if (in instanceof FileInputStream) {
            // FileInputStream本身支持available方法。
            return in;
        }

        final PushbackInputStream pushbackInputStream = toPushbackStream(in, 1);
        try {
            final int available = pushbackInputStream.available();
            if (available <= 0) {
                //此操作会阻塞，直到有数据被读到
                final int b = pushbackInputStream.read();
                pushbackInputStream.unread(b);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return pushbackInputStream;
    }

    /**
     * 转换为{@link BufferedInputStream}
     *
     * @param in {@link InputStream}
     * @return {@link BufferedInputStream}
     */
    public static BufferedInputStream toBuffered(final InputStream in) {
        Assert.notNull(in, "InputStream must be not null!");
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
    }

    /**
     * 转换为{@link BufferedInputStream}
     *
     * @param in         {@link InputStream}
     * @param bufferSize buffer size
     * @return {@link BufferedInputStream}
     */
    public static BufferedInputStream toBuffered(final InputStream in, final int bufferSize) {
        Assert.notNull(in, "InputStream must be not null!");
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in, bufferSize);
    }

    /**
     * 转换为{@link BufferedOutputStream}
     *
     * @param out {@link OutputStream}
     * @return {@link BufferedOutputStream}
     */
    public static BufferedOutputStream toBuffered(final OutputStream out) {
        Assert.notNull(out, "OutputStream must be not null!");
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream(out);
    }

    /**
     * 转换为{@link BufferedOutputStream}
     *
     * @param out        {@link OutputStream}
     * @param bufferSize buffer size
     * @return {@link BufferedOutputStream}
     */
    public static BufferedOutputStream toBuffered(final OutputStream out, final int bufferSize) {
        Assert.notNull(out, "OutputStream must be not null!");
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream(out, bufferSize);
    }

    /**
     * 转换为{@link BufferedReader}
     *
     * @param reader {@link Reader}
     * @return {@link BufferedReader}
     */
    public static BufferedReader toBuffered(final Reader reader) {
        Assert.notNull(reader, "Reader must be not null!");
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * 转换为{@link BufferedReader}
     *
     * @param reader     {@link Reader}
     * @param bufferSize buffer size
     * @return {@link BufferedReader}
     */
    public static BufferedReader toBuffered(final Reader reader, final int bufferSize) {
        Assert.notNull(reader, "Reader must be not null!");
        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader, bufferSize);
    }

    /**
     * 转换为{@link BufferedWriter}
     *
     * @param writer {@link Writer}
     * @return {@link BufferedWriter}
     */
    public static BufferedWriter toBuffered(final Writer writer) {
        Assert.notNull(writer, "Writer must be not null!");
        return (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    /**
     * 转换为{@link BufferedWriter}
     *
     * @param writer     {@link Writer}
     * @param bufferSize buffer size
     * @return {@link BufferedWriter}
     */
    public static BufferedWriter toBuffered(final Writer writer, final int bufferSize) {
        Assert.notNull(writer, "Writer must be not null!");
        return (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer, bufferSize);
    }

    /**
     * 获得{@link PushbackReader}
     * 如果是{@link PushbackReader}强转返回，否则新建
     *
     * @param reader       普通Reader
     * @param pushBackSize 推后的byte数
     * @return {@link PushbackReader}
     */
    public static PushbackReader toPushBackReader(final Reader reader, final int pushBackSize) {
        return (reader instanceof PushbackReader) ? (PushbackReader) reader : new PushbackReader(reader, pushBackSize);
    }

    /**
     * 将byte[]写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param content    写入的内容
     * @throws InternalException 异常
     */
    public static void write(OutputStream out, boolean isCloseOut, byte[] content) throws InternalException {
        StreamWriter.of(out, isCloseOut).write(content);
    }

    /**
     * 将多部分内容写到流中,自动转换为UTF-8字符串
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容,调用toString()方法,不包括不会自动换行
     * @throws InternalException 异常
     */
    public static void writeUtf8(OutputStream out, boolean isCloseOut, Object... contents) throws InternalException {
        StreamWriter.of(out, isCloseOut).writeObject(contents);
    }

    /**
     * 将多部分内容写到流中,自动转换为字符串
     *
     * @param out         输出流
     * @param charsetName 写出的内容的字符集
     * @param isCloseOut  写入完毕是否关闭输出流
     * @param contents    写入的内容,调用toString()方法,不包括不会自动换行
     * @throws InternalException 异常
     */
    public static void write(OutputStream out, String charsetName, boolean isCloseOut, Object... contents) throws InternalException {
        StreamWriter.of(out, isCloseOut).writeString(Charset.charset(charsetName), contents);
    }

    /**
     * 将多部分内容写到流中,自动转换为字符串
     *
     * @param out        输出流
     * @param charset    写出的内容的字符集
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容,调用toString()方法,不包括不会自动换行
     * @throws InternalException 异常
     */
    public static void write(OutputStream out, java.nio.charset.Charset charset, boolean isCloseOut, Object... contents) throws InternalException {
        OutputStreamWriter osw = null;
        try {
            osw = getWriter(out, charset);
            for (Object content : contents) {
                if (null != content) {
                    osw.write(Convert.toString(content, Normal.EMPTY));
                }
            }
            osw.flush();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                close(osw);
            }
        }
    }

    /**
     * 将多部分内容写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param object     写入的对象内容
     */
    public static void write(OutputStream out, boolean isCloseOut, Serializable object) throws InternalException {
        writeObjects(out, isCloseOut, object);
    }

    /**
     * 将多部分内容写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容
     * @throws InternalException 异常
     */
    public static void writeObjects(OutputStream out, boolean isCloseOut, Serializable... contents) throws InternalException {
        ObjectOutputStream osw = null;
        try {
            osw = out instanceof ObjectOutputStream ? (ObjectOutputStream) out : new ObjectOutputStream(out);
            for (Object content : contents) {
                if (null != content) {
                    osw.writeObject(content);
                }
            }
            osw.flush();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                close(osw);
            }
        }
    }

    /**
     * 关闭
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 关闭
     * 关闭失败不会抛出异常
     *
     * @param autoCloseable 被关闭的对象
     */
    public static void close(AutoCloseable autoCloseable) {
        if (null != autoCloseable) {
            try {
                autoCloseable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 关闭
     * 关闭失败不会抛出异常
     *
     * @param socket 被关闭的对象
     */
    public static void close(Socket socket) {
        if (null != socket) {
            try {
                socket.close();
            } catch (AssertionError e) {
                if (!isAndroidGetsocknameError(e)) throw e;
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @param serverSocket 被关闭的对象
     *                     关闭{@code serverSocket}，忽略任何已检查的异常。
     *                     如果{@code serverSocket}为空，则不执行任何操作
     */
    public static void close(ServerSocket serverSocket) {
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @param channel 需要被关闭的通道
     */
    public static void close(AsynchronousSocketChannel channel) {
        boolean connected = true;
        try {
            channel.shutdownInput();
        } catch (IOException ignored) {
        } catch (NotYetConnectedException e) {
            connected = false;
        }
        try {
            if (connected) {
                channel.shutdownOutput();
            }
        } catch (IOException | NotYetConnectedException ignored) {
        }
        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 从缓存中刷出数据
     *
     * @param flushable {@link Flushable}
     */
    public static void flush(Flushable flushable) {
        if (null != flushable) {
            try {
                flushable.flush();
            } catch (Exception e) {
                // 静默刷出
            }
        }
    }

    /**
     * 对比两个流内容是否相同
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true, 否则false
     * @throws InternalException 异常
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws InternalException {
        if (false == (input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (false == (input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        try {
            int ch = input1.read();
            while (EOF != ch) {
                int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            int ch2 = input2.read();
            return ch2 == EOF;
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 对比两个Reader的内容是否一致
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个reader
     * @param input2 第二个reader
     * @return 两个流的内容一致返回true, 否则false
     * @throws InternalException 异常
     */
    public static boolean contentEquals(Reader input1, Reader input2) throws InternalException {
        input1 = getReader(input1);
        input2 = getReader(input2);

        try {
            int ch = input1.read();
            while (EOF != ch) {
                int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            int ch2 = input2.read();
            return ch2 == EOF;
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 对比两个流内容是否相同,忽略EOL字符
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true, 否则false
     * @throws InternalException 异常
     */
    public static boolean contentEqualsIgnoreEOL(Reader input1, Reader input2) throws InternalException {
        final BufferedReader br1 = getReader(input1);
        final BufferedReader br2 = getReader(input2);

        try {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            while (null != line1 && null != line2 && line1.equals(line2)) {
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
            return null == line1 ? null == line2 : line1.equals(line2);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回缓冲区从{@code source}读取的字节流
     * 返回的源将对其内存缓冲区执行批量读取
     *
     * @param source 字节流
     * @return 返回缓冲区
     */
    public static BufferSource buffer(Source source) {
        return new RealSource(source);
    }

    /**
     * 返回一个新接收器，该接收器缓冲写{@code sink}
     * 返回的接收器将批量写入{@code sink}
     *
     * @param sink 接收一个字节流
     * @return 接收缓冲区
     */
    public static BufferSink buffer(Sink sink) {
        return new RealSink(sink);
    }

    /**
     * 返回一个向{@code out}写入的接收器
     *
     * @param out 输出流
     * @return 接收缓冲区
     */
    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }

    /**
     * 返回一个向{@code socket}写入的接收器。优先选择这个方法，
     * 而不是{@link #sink(OutputStream)}，因为这个方法支持超时
     * 当套接字写超时时，套接字将由看门狗线程异步关闭
     *
     * @param out     数据输出流
     * @param timeout 超时信息
     * @return 接收器
     */
    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (null == out) {
            throw new IllegalArgumentException("out == null");
        }
        if (null == timeout) {
            throw new IllegalArgumentException("timeout == null");
        }

        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                checkOffsetAndCount(source.size, 0, byteCount);
                while (byteCount > 0) {
                    timeout.throwIfReached();
                    Segment head = source.head;
                    int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
                    out.write(head.data, head.pos, toCopy);

                    head.pos += toCopy;
                    byteCount -= toCopy;
                    source.size -= toCopy;

                    if (head.pos == head.limit) {
                        source.head = head.pop();
                        LifeCycle.recycle(head);
                    }
                }
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                out.close();
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }

            @Override
            public String toString() {
                return "sink(" + out + ")";
            }
        };
    }

    /**
     * 返回一个向{@code socket}写入的接收器。优先选择这个方法，
     * 而不是{@link #sink(OutputStream)}，因为这个方法支持超时
     * 当套接字写超时时，套接字将由任务线程异步关闭
     *
     * @param socket 套接字
     * @return 接收器
     * @throws IOException IO异常
     */
    public static Sink sink(Socket socket) throws IOException {
        if (null == socket) {
            throw new IllegalArgumentException("socket == null");
        }
        if (null == socket.getOutputStream()) {
            throw new IOException("socket's output stream == null");
        }
        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    /**
     * 返回从{@code in}中读取的缓冲数据
     *
     * @param in 数据输入流
     * @return 缓冲数据
     */
    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    /**
     * 返回从{@code in}中读取的缓冲数据
     *
     * @param in      数据输入流
     * @param timeout 超时信息
     * @return 缓冲数据
     */
    private static Source source(final InputStream in, final Timeout timeout) {
        if (null == in) {
            throw new IllegalArgumentException("in == null");
        }
        if (null == timeout) {
            throw new IllegalArgumentException("timeout == null");
        }

        return new Source() {
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                if (byteCount == 0) return 0;
                try {
                    timeout.throwIfReached();
                    Segment tail = sink.writableSegment(1);
                    int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
                    int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
                    if (bytesRead == -1) {
                        if (tail.pos == tail.limit) {
                            // We allocated a tail segment, but didn't end up needing it. Recycle!
                            sink.head = tail.pop();
                            LifeCycle.recycle(tail);
                        }
                        return -1;
                    }
                    tail.limit += bytesRead;
                    sink.size += bytesRead;
                    return bytesRead;
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e)) throw new IOException(e);
                    throw e;
                }
            }

            @Override
            public void close() throws IOException {
                in.close();
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }

            @Override
            public String toString() {
                return "source(" + in + ")";
            }
        };
    }

    /**
     * 返回从{@code file}读取的缓冲数据
     *
     * @param file 文件
     * @return 缓冲数据
     * @throws FileNotFoundException 文件未找到
     */
    public static Source source(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return source(new FileInputStream(file));
    }

    /**
     * 返回从{@code path}读取的缓冲数据
     *
     * @param path    路径
     * @param options 选项
     * @return 缓冲数据
     * @throws IOException IO异常
     */
    public static Source source(Path path, OpenOption... options) throws IOException {
        if (null == path) {
            throw new IllegalArgumentException("path == null");
        }
        return source(Files.newInputStream(path, options));
    }

    /**
     * 返回一个向{@code file}写入的接收器
     *
     * @param file 文件
     * @return 接收器
     * @throws FileNotFoundException 文件未找到
     */
    public static Sink sink(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return sink(new FileOutputStream(file));
    }

    /**
     * 返回一个附加到{@code file}的接收器
     *
     * @param file 文件
     * @return 接收器
     * @throws FileNotFoundException 文件未找到
     */
    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (null == file) {
            throw new IllegalArgumentException("file == null");
        }
        return sink(new FileOutputStream(file, true));
    }

    /**
     * 返回一个向{@code path}写入的接收器.
     *
     * @param path    路径
     * @param options 属性
     * @return 写入的数据的接收器
     * @throws IOException IO异常
     */
    public static Sink sink(Path path, OpenOption... options) throws IOException {
        if (null == path) {
            throw new IllegalArgumentException("path == null");
        }
        return sink(Files.newOutputStream(path, options));
    }

    /**
     * 返回一个都不写的接收器
     *
     * @return 接收器
     */
    public static Sink blackhole() {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                source.skip(byteCount);
            }

            @Override
            public void flush() {
            }

            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }

            @Override
            public void close() {
            }
        };
    }

    /**
     * 返回从{@code socket}读取的缓存信息。与{@link #source(InputStream)}相比，
     * 更喜欢这个方法， 因为这个方法支持超时。当套接字读取超时时，套接字将由任务线程异步关闭
     *
     * @param socket 套接字
     * @return 取的缓存信息
     * @throws IOException IO异常
     */
    public static Source source(Socket socket) throws IOException {
        if (null == socket) {
            throw new IllegalArgumentException("socket == null");
        }
        if (null == socket.getInputStream()) {
            throw new IOException("socket's input stream == null");
        }
        AsyncTimeout timeout = timeout(socket);
        Source source = source(socket.getInputStream(), timeout);
        return timeout.source(source);
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            @Override
            protected IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (null != cause) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            @Override
            protected void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
                    Console.log("Failed to close timed out socket " + socket, e);
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e)) {
                        Console.log("Failed to close timed out socket " + socket, e);
                    } else {
                        throw e;
                    }
                }
            }
        };
    }

    /**
     * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
     * https://code.google.com/p/android/issues/detail?id=54072
     */
    static boolean isAndroidGetsocknameError(AssertionError e) {
        return null != e.getCause() && null != e.getMessage()
                && e.getMessage().contains("getsockname failed");
    }

    /**
     * 将指定的字符串转换为输入流，使用平台的默认字符编码编码为字节
     *
     * @param input 要转换的字符串
     * @return 一个输入流
     */
    public static InputStream toInputStream(String input) {
        byte[] bytes = input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 将指定的字符串转换为输入流，使用指定的字符编码编码为字节
     *
     * @param input    要转换的字符串
     * @param encoding 要使用的编码，null表示平台默认值
     * @return 一个输入流
     * @throws IOException 如果编码无效
     */
    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = null != encoding ? input.getBytes(encoding) : input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 使用指定的字符编码将InputStream的内容作为字符串获取
     * 这个方法在内部缓冲输入，所以不需要使用BufferedInputStream
     *
     * @param input    要从中读取的InputStream
     * @param encoding 要使用的编码，null表示平台默认值
     * @return 所请求的字符串
     * @throws NullPointerException 如果输入为空
     * @throws IOException          如果发生I/O错误
     */
    public static String toString(InputStream input, String encoding)
            throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    /**
     * 使用平台的默认字符编码将字节从InputStream复制到写入器上的字符
     * 该方法在内部缓冲输入，因此不需要使用 BufferedInputStream
     * 方法使用 {@link InputStreamReader}
     *
     * @param input  要从中读取的InputStream
     * @param output 要写入的输出者
     */
    public static void copy(InputStream input, Writer output) {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    /**
     * 使用指定的字符编码将字节从InputStream复制到写入器上的字符.
     * 该方法在内部缓冲输入，因此不需要使用 BufferedInputStream
     * 方法使用 {@link InputStreamReader}
     *
     * @param input    要从中读取的InputStream
     * @param output   要写入的输出者
     * @param encoding 要使用的编码，null表示平台默认值
     * @throws NullPointerException 如果输入为空
     * @throws IOException          如果发生I/O错误
     */
    public static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (null == encoding) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    /**
     * 以字符串的形式获取阅读器的内容
     *
     * @param input 读取信息
     * @return 所请求的字符串
     */
    public static String toString(Reader input) {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    /**
     * 使用平台的默认字符编码将byte[]的内容作为字符串获取
     *
     * @param input 要从中读取的字节数组
     * @return 所请求的字符串
     */
    public static String toString(byte[] input) {
        return new String(input);
    }

    /**
     * 使用指定的字符编码将字节[]的内容作为字符串获取
     *
     * @param input    要从中读取的字节数组
     * @param encoding 要使用的编码，null表示平台默认值
     * @return 所请求的字符串
     * @throws NullPointerException 如果输入为空
     * @throws IOException          如果发生I/O错误(从未发生)
     */
    public static String toString(byte[] input, String encoding)
            throws IOException {
        if (null == encoding) {
            return new String(input);
        } else {
            return new String(input, encoding);
        }
    }

}
