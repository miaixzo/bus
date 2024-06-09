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
package org.miaixz.bus.core.xyz;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.iterator.LineIterator;
import org.miaixz.bus.core.io.BomReader;
import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.copier.ChannelCopier;
import org.miaixz.bus.core.io.copier.FileChannelCopier;
import org.miaixz.bus.core.io.copier.ReaderWriterCopier;
import org.miaixz.bus.core.io.copier.StreamCopier;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.RealSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.RealSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.io.stream.StreamReader;
import org.miaixz.bus.core.io.stream.StreamWriter;
import org.miaixz.bus.core.io.timout.AsyncTimeout;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.InternalException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * IO工具类
 * IO工具类只是辅助流的读写，并不负责关闭流。原因是流可能被多次读写，读写关闭后容易造成问题。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class IoKit {

    /**
     * 将Reader中的内容复制到Writer中 使用默认缓存大小，拷贝后不关闭Reader
     *
     * @param reader Reader
     * @param writer Writer
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(final Reader reader, final Writer writer) throws InternalException {
        return copy(reader, writer, Normal.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader     Reader
     * @param writer     Writer
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize) throws InternalException {
        return copy(reader, writer, bufferSize, null);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader         Reader
     * @param writer         Writer
     * @param bufferSize     缓存大小
     * @param streamProgress 进度处理器
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize, final StreamProgress streamProgress) throws InternalException {
        return copy(reader, writer, bufferSize, -1, streamProgress);
    }

    /**
     * 将Reader中的内容复制到Writer中，拷贝后不关闭Reader
     *
     * @param reader         Reader，非空
     * @param writer         Writer，非空
     * @param bufferSize     缓存大小，-1表示默认
     * @param count          最大长度，-1表示无限制
     * @param streamProgress 进度处理器，{@code null}表示无
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final Reader reader, final Writer writer, final int bufferSize, final long count, final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(reader, "Reader is null !");
        Assert.notNull(writer, "Writer is null !");
        return new ReaderWriterCopier(bufferSize, count, streamProgress).copy(reader, writer);
    }

    /**
     * 拷贝流，使用默认Buffer大小，拷贝后不关闭流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final InputStream in, final OutputStream out) throws InternalException {
        return copy(in, out, Normal.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize) throws InternalException {
        return copy(in, out, bufferSize, (StreamProgress) null);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize, final StreamProgress streamProgress) throws InternalException {
        return copy(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * 拷贝流，拷贝后不关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param count          总拷贝长度，-1表示无限制
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copy(final InputStream in, final OutputStream out, final int bufferSize, final long count, final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");
        return new StreamCopier(bufferSize, count, streamProgress).copy(in, out);
    }

    /**
     * 拷贝文件流，使用NIO
     *
     * @param in  输入
     * @param out 输出
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copy(final FileInputStream in, final FileOutputStream out) throws InternalException {
        Assert.notNull(in, "FileInputStream is null!");
        Assert.notNull(out, "FileOutputStream is null!");

        return FileChannelCopier.of().copy(in, out);
    }

    /**
     * 获得一个文件读取器，默认使用 UTF-8 编码
     *
     * @param in 输入流
     * @return BufferedReader对象
     */
    public static BufferedReader toUtf8Reader(final InputStream in) {
        return toReader(in, Charset.UTF_8);
    }

    /**
     * 从{@link InputStream}中获取{@link BomReader}
     *
     * @param in {@link InputStream}
     * @return {@link BomReader}
     */
    public static BomReader toBomReader(final InputStream in) {
        return new BomReader(in);
    }

    /**
     * 获得一个Reader
     *
     * @param in      输入流
     * @param charset 字符集
     * @return BufferedReader对象
     */
    public static BufferedReader toReader(final InputStream in, final java.nio.charset.Charset charset) {
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
     * 获得一个Writer，默认编码UTF-8
     *
     * @param out 输入流
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter toUtf8Writer(final OutputStream out) {
        return toWriter(out, Charset.UTF_8);
    }

    /**
     * 获得一个Writer
     *
     * @param out     输入流
     * @param charset 字符集
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter toWriter(final OutputStream out, final java.nio.charset.Charset charset) {
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
     * 从流中读取UTF8编码的内容
     *
     * @param in 输入流
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readUtf8(final InputStream in) throws InternalException {
        return read(in, Charset.UTF_8);
    }

    /**
     * 从流中读取内容，读取完毕后关闭流
     *
     * @param in      输入流，读取完毕后关闭流
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String read(final InputStream in, final java.nio.charset.Charset charset) throws InternalException {
        return StringKit.toString(readBytes(in), charset);
    }

    /**
     * 从流中读取内容，读到输出流中，读取完毕后关闭流
     *
     * @param in 输入流
     * @return 输出流
     * @throws InternalException IO异常
     */
    public static FastByteArrayOutputStream read(final InputStream in) throws InternalException {
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
    public static FastByteArrayOutputStream read(final InputStream in, final boolean isClose) throws InternalException {
        return StreamReader.of(in, isClose).read();
    }

    /**
     * 从Reader中读取String，读取完毕后关闭Reader
     *
     * @param reader Reader
     * @return String
     * @throws InternalException IO异常
     */
    public static String read(final Reader reader) throws InternalException {
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
    public static String read(final Reader reader, final boolean isClose) throws InternalException {
        final StringBuilder builder = StringKit.builder();
        final CharBuffer buffer = CharBuffer.allocate(Normal.DEFAULT_BUFFER_SIZE);
        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip());
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isClose) {
                IoKit.closeQuietly(reader);
            }
        }
        return builder.toString();
    }

    /**
     * 从流中读取bytes，读取完毕后关闭流
     *
     * @param in {@link InputStream}
     * @return bytes
     * @throws InternalException IO异常
     */
    public static byte[] readBytes(final InputStream in) throws InternalException {
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
    public static byte[] readBytes(final InputStream in, final boolean isClose) throws InternalException {
        return StreamReader.of(in, isClose).readBytes();
    }

    /**
     * 读取指定长度的byte数组，不关闭流
     *
     * @param in     {@link InputStream}，为{@code null}返回{@code null}
     * @param length 长度，小于等于0返回空byte数组
     * @return bytes
     * @throws InternalException IO异常
     */
    public static byte[] readBytes(final InputStream in, final int length) throws InternalException {
        return StreamReader.of(in, false).readBytes(length);
    }

    /**
     * 读取16进制字符串
     *
     * @param in          {@link InputStream}
     * @param length      长度
     * @param toLowerCase true 传换成小写格式 ， false 传换成大写格式
     * @return 16进制字符串
     * @throws InternalException IO异常
     */
    public static String readHex(final InputStream in, final int length, final boolean toLowerCase) throws InternalException {
        return HexKit.encodeString(readBytes(in, length), toLowerCase);
    }

    /**
     * 从流中读取对象，即对象的反序列化，读取后不关闭流
     *
     * <p>
     * 注意！！！ 此方法不会检查反序列化安全，可能存在反序列化漏洞风险！！！
     * </p>
     *
     * @param <T>           读取对象的类型
     * @param in            输入流
     * @param acceptClasses 读取对象类型
     * @return 输出流
     * @throws InternalException IO异常
     * @throws InternalException ClassNotFoundException包装
     */
    public static <T> T readObject(final InputStream in, final Class<?>... acceptClasses) throws InternalException, InternalException {
        return StreamReader.of(in, false).readObject(acceptClasses);
    }

    /**
     * 从流中读取内容，使用UTF-8编码
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(final InputStream in, final T collection) throws InternalException {
        return readLines(in, Charset.UTF_8, collection);
    }

    /**
     * 从流中读取内容
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param charset    字符集
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readLines(final InputStream in, final java.nio.charset.Charset charset, final T collection) throws InternalException {
        return readLines(toReader(in, charset), collection);
    }

    /**
     * 从Reader中读取内容
     *
     * @param <T>        集合类型
     * @param reader     {@link Reader}
     * @param collection 返回集合
     * @return 内容
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readLines(final Reader reader, final T collection) throws InternalException {
        readLines(reader, (ConsumerX<String>) collection::add);
        return collection;
    }

    /**
     * 按行读取UTF-8编码数据，针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws InternalException IO异常
     */
    public static void readUtf8Lines(final InputStream in, final ConsumerX<String> lineHandler) throws InternalException {
        readLines(in, Charset.UTF_8, lineHandler);
    }

    /**
     * 按行读取数据，针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param charset     {@link java.nio.charset.Charset}编码
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws InternalException IO异常
     */
    public static void readLines(final InputStream in, final java.nio.charset.Charset charset, final ConsumerX<String> lineHandler) throws InternalException {
        readLines(toReader(in, charset), lineHandler);
    }

    /**
     * 按行读取数据，针对每行的数据做处理
     * {@link Reader}自带编码定义，因此读取数据的编码跟随其编码。
     * 此方法不会关闭流，除非抛出异常
     *
     * @param reader      {@link Reader}
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws InternalException IO异常
     */
    public static void readLines(final Reader reader, final ConsumerX<String> lineHandler) throws InternalException {
        Assert.notNull(reader);
        Assert.notNull(lineHandler);

        for (final String line : lineIter(reader)) {
            lineHandler.accept(line);
        }
    }

    /**
     * String 转为UTF-8编码的字节流流
     *
     * @param content 内容
     * @return 字节流
     */
    public static ByteArrayInputStream toUtf8Stream(final String content) {
        return toStream(content, Charset.UTF_8);
    }

    /**
     * String 转为流
     *
     * @param content 内容
     * @param charset 编码
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(final String content, final java.nio.charset.Charset charset) {
        if (content == null) {
            return null;
        }
        return toStream(ByteKit.toBytes(content, charset));
    }

    /**
     * 文件转为{@link InputStream}
     *
     * @param file 文件，非空
     * @return {@link InputStream}
     */
    public static InputStream toStream(final File file) {
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
     * byte[] 转为{@link ByteArrayInputStream}
     *
     * @param content 内容bytes
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(final byte[] content) {
        if (content == null) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * {@link ByteArrayOutputStream}转为{@link ByteArrayInputStream}
     *
     * @param out {@link ByteArrayOutputStream}
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(final ByteArrayOutputStream out) {
        if (out == null) {
            return null;
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     * {@link FastByteArrayOutputStream}转为{@link ByteArrayInputStream}
     *
     * @param out {@link FastByteArrayOutputStream}
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(final FastByteArrayOutputStream out) {
        if (out == null) {
            return null;
        }
        return new ByteArrayInputStream(out.toByteArray());
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
     * 将{@link InputStream}转换为支持mark标记的流
     * 若原流支持mark标记，则返回原流，否则使用{@link BufferedInputStream} 包装之
     *
     * @param in 流
     * @return {@link InputStream}
     */
    public static InputStream toMarkSupport(final InputStream in) {
        if (null == in) {
            return null;
        }
        if (!in.markSupported()) {
            return new BufferedInputStream(in);
        }
        return in;
    }

    /**
     * 将{@link Reader}转换为支持mark标记的Reader
     * 若原Reader支持mark标记，则返回原Reader，否则使用{@link BufferedReader} 包装之
     *
     * @param reader {@link Reader}
     * @return {@link Reader}
     */
    public static Reader toMarkSupport(final Reader reader) {
        if (null == reader) {
            return null;
        }
        if (!reader.markSupported()) {
            return new BufferedReader(reader);
        }
        return reader;
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
     * 将byte[]写到流中，并关闭目标流
     *
     * @param out     输出流
     * @param content 写入的内容
     * @throws InternalException IO异常
     */
    public static void writeClose(final OutputStream out, final byte[] content) throws InternalException {
        write(out, true, content);
    }

    /**
     * 将byte[]写到流中，并关闭目标流
     *
     * @param out     输出流
     * @param content 写入的内容
     * @throws InternalException IO异常
     */
    public static void write(final OutputStream out, final byte[] content) throws InternalException {
        write(out, false, content);
    }

    /**
     * 将byte[]写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param content    写入的内容
     * @throws InternalException IO异常
     */
    public static void write(final OutputStream out, final boolean isCloseOut, final byte[] content) throws InternalException {
        StreamWriter.of(out, isCloseOut).write(content);
    }

    /**
     * 将多部分内容写到流中，自动转换为UTF-8字符串
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容，调用toString()方法，不包括不会自动换行
     * @throws InternalException IO异常
     */
    public static void writeUtf8(final OutputStream out, final boolean isCloseOut, final Object... contents) throws InternalException {
        write(out, Charset.UTF_8, isCloseOut, contents);
    }

    /**
     * 将多部分内容写到流中，自动转换为字符串
     *
     * @param out        输出流
     * @param charset    写出的内容的字符集
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容，调用toString()方法，不包括不会自动换行
     * @throws InternalException IO异常
     */
    public static void write(final OutputStream out, final java.nio.charset.Charset charset, final boolean isCloseOut, final Object... contents) throws InternalException {
        StreamWriter.of(out, isCloseOut).writeString(charset, contents);
    }

    /**
     * 将多部分内容写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容
     * @throws InternalException IO异常
     */
    public static void writeObjects(final OutputStream out, final boolean isCloseOut, final Object... contents) throws InternalException {
        StreamWriter.of(out, isCloseOut).writeObject(contents);
    }

    /**
     * 从缓存中刷出数据
     *
     * @param flushable {@link Flushable}
     */
    public static void flush(final Flushable flushable) {
        if (null != flushable) {
            try {
                flushable.flush();
            } catch (final Exception e) {
                // 静默刷出
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
     * 按照给定顺序连续关闭一系列对象
     * 这些对象必须按照顺序关闭，否则会出错。
     *
     * @param closeables 需要关闭的对象
     */
    public static void closeQuietly(final AutoCloseable... closeables) {
        for (final AutoCloseable closeable : closeables) {
            close(closeable);
        }
    }

    /**
     * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
     * https://code.google.com/p/android/issues/detail?id=54072
     */
    public static boolean isAndroidGetsocknameError(AssertionError e) {
        return null != e.getCause() && null != e.getMessage()
                && e.getMessage().contains("getsockname failed");
    }

    /**
     * 关闭
     * 关闭失败抛出{@link IOException}异常
     *
     * @param closeable 被关闭的对象
     * @throws IOException IO异常
     */
    public static void nullSafeClose(final Closeable closeable) throws IOException {
        if (null != closeable) {
            closeable.close();
        }
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
                    SectionBuffer head = source.head;
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
                return "sink(" + out + Symbol.PARENTHESE_RIGHT;
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
                    SectionBuffer tail = sink.writableSegment(1);
                    int maxToCopy = (int) Math.min(byteCount, SectionBuffer.SIZE - tail.limit);
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
                return "source(" + in + Symbol.PARENTHESE_RIGHT;
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
     * 对比两个流内容是否相同
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true，否则false
     * @throws InternalException IO异常
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws InternalException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        try {
            int ch = input1.read();
            while (Normal.__1 != ch) {
                final int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            final int ch2 = input2.read();
            return ch2 == Normal.__1;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 对比两个Reader的内容是否一致
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个reader
     * @param input2 第二个reader
     * @return 两个流的内容一致返回true，否则false
     * @throws InternalException IO异常
     */
    public static boolean contentEquals(Reader input1, Reader input2) throws InternalException {
        input1 = toBuffered(input1);
        input2 = toBuffered(input2);

        try {
            int ch = input1.read();
            while (Normal.__1 != ch) {
                final int ch2 = input2.read();
                if (ch != ch2) {
                    return false;
                }
                ch = input1.read();
            }

            final int ch2 = input2.read();
            return ch2 == Normal.__1;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 对比两个流内容是否相同，忽略EOL字符
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true，否则false
     * @throws InternalException IO异常
     */
    public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2) throws InternalException {
        final BufferedReader br1 = toBuffered(input1);
        final BufferedReader br2 = toBuffered(input2);

        try {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            while (line1 != null && line1.equals(line2)) {
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
            return Objects.equals(line1, line2);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 返回行遍历器
     * <pre>
     * LineIterator it = null;
     * try {
     * 	it = IoKit.lineIter(reader);
     * 	while (it.hasNext()) {
     * 		String line = it.nextLine();
     * 		// do something with line
     *    }
     * } finally {
     * 		it.close();
     * }
     * </pre>
     *
     * @param reader {@link Reader}
     * @return {@link LineIterator}
     */
    public static LineIterator lineIter(final Reader reader) {
        return new LineIterator(reader);
    }

    /**
     * 返回行遍历器
     * <pre>
     * LineIterator it = null;
     * try {
     * 	it = IoKit.lineIter(in, Charset.CHARSET_UTF_8);
     * 	while (it.hasNext()) {
     * 		String line = it.nextLine();
     * 		// do something with line
     *    }
     * } finally {
     * 		it.close();
     * }
     * </pre>
     *
     * @param in      {@link InputStream}
     * @param charset 编码
     * @return {@link LineIterator}
     */
    public static LineIterator lineIter(final InputStream in, final java.nio.charset.Charset charset) {
        return new LineIterator(in, charset);
    }

    /**
     * {@link ByteArrayOutputStream} 转换为String
     *
     * @param out     {@link ByteArrayOutputStream}
     * @param charset 编码
     * @return 字符串
     */
    public static String toString(final ByteArrayOutputStream out, final java.nio.charset.Charset charset) {
        try {
            return out.toString(charset.name());
        } catch (final UnsupportedEncodingException e) {
            throw new InternalException(e);
        }
    }

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }

    /**
     * 即使被声明也不允许直接抛出
     * 这是一种很糟糕的做饭,很容易遭到攻击
     * 清理后捕获并重新抛出异常
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

    public static void copy(InputStream in, OutputStream out, byte[] buf)
            throws IOException {
        int count;
        while ((count = in.read(buf, 0, buf.length)) > 0)
            if (null != out) {
                out.write(buf, 0, count);
            }
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

    public static void copy(InputStream in, OutputStream out, int len,
                            int swapBytes) throws IOException {
        copy(in, out, len, swapBytes, new byte[Math.min(len, 2048)]);
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

    public static InputStream openFileOrURL(String name) throws IOException {
        if (name.startsWith("resource:")) {
            URL url = ResourceKit.getResourceUrl(name.substring(9), StreamKit.class);
            if (null == url)
                throw new FileNotFoundException(name);
            return url.openStream();
        }
        if (name.indexOf(Symbol.C_COLON) < 2)
            return new FileInputStream(name);
        return new URL(name).openStream();
    }

    /**
     * 拷贝流 thanks to: https://github.com/venusdrogon/feilong-io/blob/master/src/main/java/com/feilong/io/IOWriteUtil.java
     * 本方法不会关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copyNio(final InputStream in, final OutputStream out, final int bufferSize, final StreamProgress streamProgress) throws InternalException {
        return copyNio(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * 拷贝流
     * 本方法不会关闭流
     *
     * @param in             输入流， 非空
     * @param out            输出流， 非空
     * @param bufferSize     缓存大小，-1表示默认
     * @param count          最大长度，-1表示无限制
     * @param streamProgress 进度条，{@code null}表示无进度条
     * @return 传输的byte数
     * @throws InternalException IO异常
     */
    public static long copyNio(final InputStream in, final OutputStream out, final int bufferSize, final long count, final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(in, "InputStream channel is null!");
        Assert.notNull(out, "OutputStream channel is null!");
        final long copySize = copyNio(Channels.newChannel(in), Channels.newChannel(out), bufferSize, count, streamProgress);
        flush(out);
        return copySize;
    }

    /**
     * 拷贝文件Channel，使用NIO，拷贝后不会关闭channel
     *
     * @param in  {@link FileChannel}，非空
     * @param out {@link FileChannel}，非空
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copyNio(final FileChannel in, final FileChannel out) throws InternalException {
        Assert.notNull(in, "In channel is null!");
        Assert.notNull(out, "Out channel is null!");

        return FileChannelCopier.of().copy(in, out);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in  {@link ReadableByteChannel}
     * @param out {@link WritableByteChannel}
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copyNio(final ReadableByteChannel in, final WritableByteChannel out) throws InternalException {
        return copyNio(in, out, Normal.DEFAULT_BUFFER_SIZE);
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
    public static long copyNio(final ReadableByteChannel in, final WritableByteChannel out, final int bufferSize) throws InternalException {
        return copyNio(in, out, bufferSize, null);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in             {@link ReadableByteChannel}
     * @param out            {@link WritableByteChannel}
     * @param bufferSize     缓冲大小，如果小于等于0，使用默认
     * @param streamProgress {@link StreamProgress}进度处理器
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copyNio(final ReadableByteChannel in, final WritableByteChannel out, final int bufferSize, final StreamProgress streamProgress) throws InternalException {
        return copyNio(in, out, bufferSize, -1, streamProgress);
    }

    /**
     * 拷贝流，使用NIO，不会关闭channel
     *
     * @param in             {@link ReadableByteChannel}
     * @param out            {@link WritableByteChannel}
     * @param bufferSize     缓冲大小，如果小于等于0，使用默认
     * @param count          读取总长度
     * @param streamProgress {@link StreamProgress}进度处理器
     * @return 拷贝的字节数
     * @throws InternalException IO异常
     */
    public static long copyNio(final ReadableByteChannel in, final WritableByteChannel out, final int bufferSize, final long count, final StreamProgress streamProgress) throws InternalException {
        Assert.notNull(in, "In channel is null!");
        Assert.notNull(out, "Out channel is null!");
        return new ChannelCopier(bufferSize, count, streamProgress).copy(in, out);
    }

    /**
     * 从流中读取内容，读取完毕后并不关闭流
     *
     * @param channel 可读通道，读取完毕后并不关闭通道
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readNio(final ReadableByteChannel channel, final java.nio.charset.Charset charset) throws InternalException {
        final FastByteArrayOutputStream out = readNio(channel);
        return null == charset ? out.toString() : out.toString(charset);
    }

    /**
     * 从流中读取内容，读到输出流中
     *
     * @param channel 可读通道，读取完毕后并不关闭通道
     * @return 输出流
     * @throws InternalException IO异常
     */
    public static FastByteArrayOutputStream readNio(final ReadableByteChannel channel) throws InternalException {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        copyNio(channel, Channels.newChannel(out));
        return out;
    }

    /**
     * 从FileChannel中读取UTF-8编码内容
     *
     * @param fileChannel 文件管道
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readNio(final FileChannel fileChannel) throws InternalException {
        return readNio(fileChannel, Charset.UTF_8);
    }

    /**
     * 从FileChannel中读取内容
     *
     * @param fileChannel 文件管道
     * @param charset     字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readNio(final FileChannel fileChannel, final java.nio.charset.Charset charset) throws InternalException {
        final MappedByteBuffer buffer;
        try {
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return StringKit.toString(buffer, charset);
    }

}
