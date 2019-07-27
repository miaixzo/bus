package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.core.convert.Convert;
import org.aoju.bus.core.io.*;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * IO工具类<br>
 * IO工具类只是辅助流的读写，并不负责关闭流。原因是流可能被多次读写，读写关闭后容易造成问题。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class IoUtils {

    /**
     * 默认缓存大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    /**
     * 默认中等缓存大小
     */
    public static final int DEFAULT_MIDDLE_BUFFER_SIZE = 4096;
    /**
     * 默认大缓存大小
     */
    public static final int DEFAULT_LARGE_BUFFER_SIZE = 8192;

    /**
     * 数据流末尾
     */
    public static final int EOF = -1;


    private IoUtils() {
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
     * Throws {@code t}, even if the declared throws clause doesn't permit it.
     * This is a terrible – but terribly convenient – hack that makes it easy to
     * catch and rethrow exceptions after cleanup. See Java Puzzlers #43.
     */
    public static void sneakyRethrow(Throwable t) {
        IoUtils.<Error>sneakyThrow2(t);
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
     * 将Reader中的内容复制到Writer中 使用默认缓存大小
     *
     * @param reader Reader
     * @param writer Writer
     * @return 拷贝的字节数
     * @throws CommonException IO异常
     */
    public static long copy(Reader reader, Writer writer) throws CommonException {
        return copy(reader, writer, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将Reader中的内容复制到Writer中
     *
     * @param reader     Reader
     * @param writer     Writer
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copy(Reader reader, Writer writer, int bufferSize) throws CommonException {
        return copy(reader, writer, bufferSize, null);
    }

    /**
     * 将Reader中的内容复制到Writer中
     *
     * @param reader         Reader
     * @param writer         Writer
     * @param bufferSize     缓存大小
     * @param streamProgress 进度处理器
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copy(Reader reader, Writer writer, int bufferSize, StreamProgress streamProgress) throws CommonException {
        char[] buffer = new char[bufferSize];
        long size = 0;
        int readSize;
        if (null != streamProgress) {
            streamProgress.start();
        }
        try {
            while ((readSize = reader.read(buffer, 0, bufferSize)) != EOF) {
                writer.write(buffer, 0, readSize);
                size += readSize;
                writer.flush();
                if (null != streamProgress) {
                    streamProgress.progress(size);
                }
            }
        } catch (Exception e) {
            throw new CommonException(e);
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }
        return size;
    }

    /**
     * 拷贝流，使用默认Buffer大小
     *
     * @param in  输入流
     * @param out 输出流
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copy(InputStream in, OutputStream out) throws CommonException {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 拷贝流
     *
     * @param in         输入流
     * @param out        输出流
     * @param bufferSize 缓存大小
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize) throws CommonException {
        return copy(in, out, bufferSize, null);
    }

    /**
     * 拷贝流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize, StreamProgress streamProgress) throws CommonException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");
        if (bufferSize <= 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        byte[] buffer = new byte[bufferSize];
        if (null != streamProgress) {
            streamProgress.start();
        }
        long size = 0;
        try {
            for (int readSize = -1; (readSize = in.read(buffer)) != EOF; ) {
                out.write(buffer, 0, readSize);
                size += readSize;
                out.flush();
                if (null != streamProgress) {
                    streamProgress.progress(size);
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }
        return size;
    }

    /**
     * 拷贝流 thanks to: https://github.com/venusdrogon/feilong-io/blob/master/src/main/java/com/feilong/io/IOWriteUtil.java<br>
     * 本方法不会关闭流
     *
     * @param in             输入流
     * @param out            输出流
     * @param bufferSize     缓存大小
     * @param streamProgress 进度条
     * @return 传输的byte数
     * @throws CommonException IO异常
     */
    public static long copyByNIO(InputStream in, OutputStream out, int bufferSize, StreamProgress streamProgress) throws CommonException {
        return copy(Channels.newChannel(in), Channels.newChannel(out), bufferSize, streamProgress);
    }

    /**
     * 拷贝文件流，使用NIO
     *
     * @param in  输入
     * @param out 输出
     * @return 拷贝的字节数
     * @throws CommonException IO异常
     */
    public static long copy(FileInputStream in, FileOutputStream out) throws CommonException {
        Assert.notNull(in, "FileInputStream is null!");
        Assert.notNull(out, "FileOutputStream is null!");

        final FileChannel inChannel = in.getChannel();
        final FileChannel outChannel = out.getChannel();

        try {
            return inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 拷贝流，使用NIO，不会关闭流
     *
     * @param in             {@link ReadableByteChannel}
     * @param out            {@link WritableByteChannel}
     * @param bufferSize     缓冲大小，如果小于等于0，使用默认
     * @param streamProgress {@link StreamProgress}进度处理器
     * @return 拷贝的字节数
     * @throws CommonException IO异常
     */
    public static long copy(ReadableByteChannel in, WritableByteChannel out, int bufferSize, StreamProgress streamProgress) throws CommonException {
        Assert.notNull(in, "InputStream is null !");
        Assert.notNull(out, "OutputStream is null !");

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize <= 0 ? DEFAULT_BUFFER_SIZE : bufferSize);
        long size = 0;
        if (null != streamProgress) {
            streamProgress.start();
        }
        try {
            while (in.read(byteBuffer) != EOF) {
                byteBuffer.flip();// 写转读
                size += out.write(byteBuffer);
                byteBuffer.clear();
                if (null != streamProgress) {
                    streamProgress.progress(size);
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        if (null != streamProgress) {
            streamProgress.finish();
        }

        return size;
    }

    /**
     * 获得一个文件读取器
     *
     * @param in          输入流
     * @param charsetName 字符集名称
     * @return BufferedReader对象
     */
    public static BufferedReader getReader(InputStream in, String charsetName) {
        return getReader(in, Charset.forName(charsetName));
    }

    /**
     * 获得一个Reader
     *
     * @param in      输入流
     * @param charset 字符集
     * @return BufferedReader对象
     */
    public static BufferedReader getReader(InputStream in, Charset charset) {
        if (null == in) {
            return null;
        }

        InputStreamReader reader = null;
        if (null == charset) {
            reader = new InputStreamReader(in);
        } else {
            reader = new InputStreamReader(in, charset);
        }

        return new BufferedReader(reader);
    }

    /**
     * 获得{@link BufferedReader}<br>
     * 如果是{@link BufferedReader}强转返回，否则新建。如果提供的Reader为null返回null
     *
     * @param reader 普通Reader，如果为null返回null
     * @return {@link BufferedReader} or null
     * @since 3.0.9
     */
    public static BufferedReader getReader(Reader reader) {
        if (null == reader) {
            return null;
        }

        return (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * 获得{@link PushbackReader}<br>
     * 如果是{@link PushbackReader}强转返回，否则新建
     *
     * @param reader       普通Reader
     * @param pushBackSize 推后的byte数
     * @return {@link PushbackReader}
     * @since 3.1.0
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
        return getWriter(out, Charset.forName(charsetName));
    }

    /**
     * 获得一个Writer
     *
     * @param out     输入流
     * @param charset 字符集
     * @return OutputStreamWriter对象
     */
    public static OutputStreamWriter getWriter(OutputStream out, Charset charset) {
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
     * @throws CommonException IO异常
     */
    public static String read(InputStream in, String charsetName) throws CommonException {
        FastByteArrayOutputStream out = read(in);
        return StringUtils.isBlank(charsetName) ? out.toString() : out.toString(charsetName);
    }

    /**
     * 从流中读取内容，读取完毕后并不关闭流
     *
     * @param in      输入流，读取完毕后并不关闭流
     * @param charset 字符集
     * @return 内容
     * @throws CommonException IO异常
     */
    public static String read(InputStream in, Charset charset) throws CommonException {
        FastByteArrayOutputStream out = read(in);
        return null == charset ? out.toString() : out.toString(charset);
    }

    /**
     * 从流中读取内容，读到输出流中
     *
     * @param in 输入流
     * @return 输出流
     * @throws CommonException IO异常
     */
    public static FastByteArrayOutputStream read(InputStream in) throws CommonException {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        copy(in, out);
        return out;
    }

    /**
     * 从Reader中读取String，读取完毕后并不关闭Reader
     *
     * @param reader Reader
     * @return String
     * @throws CommonException IO异常
     */
    public static String read(Reader reader) throws CommonException {
        final StringBuilder builder = new StringBuilder();
        final CharBuffer buffer = CharBuffer.allocate(DEFAULT_BUFFER_SIZE);
        try {
            while (-1 != reader.read(buffer)) {
                builder.append(buffer.flip().toString());
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return builder.toString();
    }

    /**
     * 从FileChannel中读取UTF-8编码内容
     *
     * @param fileChannel 文件管道
     * @return 内容
     * @throws CommonException IO异常
     */
    public static String readUtf8(FileChannel fileChannel) throws CommonException {
        return read(fileChannel, org.aoju.bus.core.consts.Charset.UTF_8);
    }

    /**
     * 从FileChannel中读取内容，读取完毕后并不关闭Channel
     *
     * @param fileChannel 文件管道
     * @param charsetName 字符集
     * @return 内容
     * @throws CommonException IO异常
     */
    public static String read(FileChannel fileChannel, String charsetName) throws CommonException {
        return read(fileChannel, CharsetUtils.charset(charsetName));
    }

    /**
     * 从FileChannel中读取内容
     *
     * @param fileChannel 文件管道
     * @param charset     字符集
     * @return 内容
     * @throws CommonException IO异常
     */
    public static String read(FileChannel fileChannel, Charset charset) throws CommonException {
        MappedByteBuffer buffer;
        try {
            buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).load();
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return StringUtils.str(buffer, charset);
    }

    /**
     * 从流中读取bytes
     *
     * @param in {@link InputStream}
     * @return bytes
     * @throws CommonException IO异常
     */
    public static byte[] readBytes(InputStream in) throws CommonException {
        final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * 读取指定长度的byte数组，不关闭流
     *
     * @param in     {@link InputStream}，为null返回null
     * @param length 长度，小于等于0返回空byte数组
     * @return bytes
     * @throws CommonException IO异常
     */
    public static byte[] readBytes(InputStream in, int length) throws CommonException {
        if (null == in) {
            return null;
        }
        if (length <= 0) {
            return new byte[0];
        }

        byte[] b = new byte[length];
        int readLength;
        try {
            readLength = in.read(b);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        if (readLength > 0 && readLength < length) {
            byte[] b2 = new byte[length];
            System.arraycopy(b, 0, b2, 0, readLength);
            return b2;
        } else {
            return b;
        }
    }

    /**
     * 读取16进制字符串
     *
     * @param in          {@link InputStream}
     * @param length      长度
     * @param toLowerCase true 传换成小写格式 ， false 传换成大写格式
     * @return 16进制字符串
     * @throws CommonException IO异常
     */
    public static String readHex(InputStream in, int length, boolean toLowerCase) throws CommonException {
        return HexUtils.encodeHexStr(readBytes(in, length), toLowerCase);
    }

    /**
     * 从流中读取前28个byte并转换为16进制，字母部分使用大写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws CommonException IO异常
     */
    public static String readHex28Upper(InputStream in) throws CommonException {
        return readHex(in, 28, false);
    }

    /**
     * 从流中读取前28个byte并转换为16进制，字母部分使用小写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws CommonException IO异常
     */
    public static String readHex28Lower(InputStream in) throws CommonException {
        return readHex(in, 28, true);
    }

    /**
     * 从流中读取内容，读到输出流中
     *
     * @param <T> 读取对象的类型
     * @param in  输入流
     * @return 输出流
     * @throws CommonException IO异常
     */
    public static <T> T readObj(InputStream in) throws CommonException {
        if (in == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(in);
            final T obj = (T) ois.readObject();
            return obj;
        } catch (IOException e) {
            throw new CommonException(e);
        } catch (ClassNotFoundException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 从流中读取内容，使用UTF-8编码
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param collection 返回集合
     * @return 内容
     * @throws CommonException IO异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(InputStream in, T collection) throws CommonException {
        return readLines(in, org.aoju.bus.core.consts.Charset.UTF_8, collection);
    }

    /**
     * 从流中读取内容
     *
     * @param <T>         集合类型
     * @param in          输入流
     * @param charsetName 字符集
     * @param collection  返回集合
     * @return 内容
     * @throws CommonException IO异常
     */
    public static <T extends Collection<String>> T readLines(InputStream in, String charsetName, T collection) throws CommonException {
        return readLines(in, CharsetUtils.charset(charsetName), collection);
    }

    /**
     * 从流中读取内容
     *
     * @param <T>        集合类型
     * @param in         输入流
     * @param charset    字符集
     * @param collection 返回集合
     * @return 内容
     * @throws CommonException IO异常
     */
    public static <T extends Collection<String>> T readLines(InputStream in, Charset charset, T collection) throws CommonException {
        return readLines(getReader(in, charset), collection);
    }

    /**
     * 从Reader中读取内容
     *
     * @param <T>        集合类型
     * @param reader     {@link Reader}
     * @param collection 返回集合
     * @return 内容
     * @throws CommonException IO异常
     */
    public static <T extends Collection<String>> T readLines(Reader reader, final T collection) throws CommonException {
        readLines(reader, new LineHandler() {
            @Override
            public void handle(String line) {
                collection.add(line);
            }
        });
        return collection;
    }

    /**
     * 按行读取UTF-8编码数据，针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws CommonException IO异常
     * @since 3.1.1
     */
    public static void readUtf8Lines(InputStream in, LineHandler lineHandler) throws CommonException {
        readLines(in, org.aoju.bus.core.consts.Charset.UTF_8, lineHandler);
    }

    /**
     * 按行读取数据，针对每行的数据做处理
     *
     * @param in          {@link InputStream}
     * @param charset     {@link Charset}编码
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws CommonException IO异常
     * @since 3.0.9
     */
    public static void readLines(InputStream in, Charset charset, LineHandler lineHandler) throws CommonException {
        readLines(getReader(in, charset), lineHandler);
    }

    /**
     * 按行读取数据，针对每行的数据做处理<br>
     * {@link Reader}自带编码定义，因此读取数据的编码跟随其编码。
     *
     * @param reader      {@link Reader}
     * @param lineHandler 行处理接口，实现handle方法用于编辑一行的数据后入到指定地方
     * @throws CommonException IO异常
     */
    public static void readLines(Reader reader, LineHandler lineHandler) throws CommonException {
        Assert.notNull(reader);
        Assert.notNull(lineHandler);

        // 从返回的内容中读取所需内容
        final BufferedReader bReader = getReader(reader);
        String line = null;
        try {
            while ((line = bReader.readLine()) != null) {
                lineHandler.handle(line);
            }
        } catch (IOException e) {
            throw new CommonException(e);
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
        return toStream(content, CharsetUtils.charset(charsetName));
    }

    /**
     * String 转为流
     *
     * @param content 内容
     * @param charset 编码
     * @return 字节流
     */
    public static ByteArrayInputStream toStream(String content, Charset charset) {
        if (content == null) {
            return null;
        }
        return toStream(StringUtils.bytes(content, charset));
    }

    /**
     * String 转为流
     *
     * @param content 内容bytes
     * @return 字节流
     * @since 4.1.8
     */
    public static ByteArrayInputStream toStream(byte[] content) {
        if (content == null) {
            return null;
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * 文件转为流
     *
     * @param file 文件
     * @return {@link FileInputStream}
     */
    public static FileInputStream toStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 转换为{@link BufferedInputStream}
     *
     * @param in {@link InputStream}
     * @return {@link BufferedInputStream}
     * @since 4.0.10
     */
    public static BufferedInputStream toBuffered(InputStream in) {
        return (in instanceof BufferedInputStream) ? (BufferedInputStream) in : new BufferedInputStream(in);
    }

    /**
     * 转换为{@link BufferedOutputStream}
     *
     * @param out {@link OutputStream}
     * @return {@link BufferedOutputStream}
     * @since 4.0.10
     */
    public static BufferedOutputStream toBuffered(OutputStream out) {
        return (out instanceof BufferedOutputStream) ? (BufferedOutputStream) out : new BufferedOutputStream(out);
    }

    /**
     * 将{@link InputStream}转换为支持mark标记的流<br>
     * 若原流支持mark标记，则返回原流，否则使用{@link BufferedInputStream} 包装之
     *
     * @param in 流
     * @return {@link InputStream}
     * @since 4.0.9
     */
    public static InputStream toMarkSupportStream(InputStream in) {
        if (null == in) {
            return null;
        }
        if (false == in.markSupported()) {
            return new BufferedInputStream(in);
        }
        return in;
    }

    /**
     * 转换为{@link PushbackInputStream}<br>
     * 如果传入的输入流已经是{@link PushbackInputStream}，强转返回，否则新建一个
     *
     * @param in           {@link InputStream}
     * @param pushBackSize 推后的byte数
     * @return {@link PushbackInputStream}
     * @since 3.1.0
     */
    public static PushbackInputStream toPushbackStream(InputStream in, int pushBackSize) {
        return (in instanceof PushbackInputStream) ? (PushbackInputStream) in : new PushbackInputStream(in, pushBackSize);
    }

    /**
     * 将byte[]写到流中
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param content    写入的内容
     * @throws CommonException IO异常
     */
    public static void write(OutputStream out, boolean isCloseOut, byte[] content) throws CommonException {
        try {
            out.write(content);
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            if (isCloseOut) {
                close(out);
            }
        }
    }

    /**
     * 将多部分内容写到流中，自动转换为UTF-8字符串
     *
     * @param out        输出流
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容，调用toString()方法，不包括不会自动换行
     * @throws CommonException IO异常
     * @since 3.1.1
     */
    public static void writeUtf8(OutputStream out, boolean isCloseOut, Object... contents) throws CommonException {
        write(out, org.aoju.bus.core.consts.Charset.UTF_8, isCloseOut, contents);
    }

    /**
     * 将多部分内容写到流中，自动转换为字符串
     *
     * @param out         输出流
     * @param charsetName 写出的内容的字符集
     * @param isCloseOut  写入完毕是否关闭输出流
     * @param contents    写入的内容，调用toString()方法，不包括不会自动换行
     * @throws CommonException IO异常
     */
    public static void write(OutputStream out, String charsetName, boolean isCloseOut, Object... contents) throws CommonException {
        write(out, CharsetUtils.charset(charsetName), isCloseOut, contents);
    }

    /**
     * 将多部分内容写到流中，自动转换为字符串
     *
     * @param out        输出流
     * @param charset    写出的内容的字符集
     * @param isCloseOut 写入完毕是否关闭输出流
     * @param contents   写入的内容，调用toString()方法，不包括不会自动换行
     * @throws CommonException IO异常
     * @since 3.0.9
     */
    public static void write(OutputStream out, Charset charset, boolean isCloseOut, Object... contents) throws CommonException {
        OutputStreamWriter osw = null;
        try {
            osw = getWriter(out, charset);
            for (Object content : contents) {
                if (content != null) {
                    osw.write(Convert.toStr(content, Normal.EMPTY));
                    osw.flush();
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
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
     * @param contents   写入的内容
     * @throws CommonException IO异常
     */
    public static void writeObjects(OutputStream out, boolean isCloseOut, Serializable... contents) throws CommonException {
        ObjectOutputStream osw = null;
        try {
            osw = out instanceof ObjectOutputStream ? (ObjectOutputStream) out : new ObjectOutputStream(out);
            for (Object content : contents) {
                if (content != null) {
                    osw.writeObject(content);
                    osw.flush();
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        } finally {
            if (isCloseOut) {
                close(osw);
            }
        }
    }

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param closeable 被关闭的对象
     */
    public static void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                // 静默关闭
            }
        }
    }

    /**
     * 关闭<br>
     * 关闭失败不会抛出异常
     *
     * @param sock 被关闭的对象
     */
    public static void close(Socket sock) {
        if (sock != null)
            try {
                sock.close();
            } catch (IOException ignore) {
            }
    }

    /**
     * 对比两个流内容是否相同<br>
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true，否则false
     * @throws CommonException IO异常
     * @since 4.0.6
     */
    public static boolean contentEquals(InputStream input1, InputStream input2) throws CommonException {
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
            throw new CommonException(e);
        }
    }

    /**
     * 对比两个Reader的内容是否一致<br>
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个reader
     * @param input2 第二个reader
     * @return 两个流的内容一致返回true，否则false
     * @throws CommonException IO异常
     * @since 4.0.6
     */
    public static boolean contentEquals(Reader input1, Reader input2) throws CommonException {
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
            throw new CommonException(e);
        }
    }

    /**
     * 对比两个流内容是否相同，忽略EOL字符<br>
     * 内部会转换流为 {@link BufferedInputStream}
     *
     * @param input1 第一个流
     * @param input2 第二个流
     * @return 两个流的内容一致返回true，否则false
     * @throws CommonException IO异常
     * @since 4.0.6
     */
    public static boolean contentEqualsIgnoreEOL(Reader input1, Reader input2) throws CommonException {
        final BufferedReader br1 = getReader(input1);
        final BufferedReader br2 = getReader(input2);

        try {
            String line1 = br1.readLine();
            String line2 = br2.readLine();
            while (line1 != null && line2 != null && line1.equals(line2)) {
                line1 = br1.readLine();
                line2 = br2.readLine();
            }
            return line1 == null ? line2 == null ? true : false : line1.equals(line2);
        } catch (IOException e) {
            throw new CommonException(e);
        }
    }

    /**
     * 计算流CRC32校验码，计算后关闭流
     *
     * @param in 文件，不能为目录
     * @return CRC32值
     * @throws CommonException IO异常
     * @since 4.0.6
     */
    public static long checksumCRC32(InputStream in) throws CommonException {
        return checksum(in, new CRC32()).getValue();
    }

    /**
     * 计算流的校验码，计算后关闭流
     *
     * @param in       流
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws CommonException IO异常
     * @since 4.0.10
     */
    public static Checksum checksum(InputStream in, Checksum checksum) throws CommonException {
        Assert.notNull(in, "InputStream is null !");
        if (null == checksum) {
            checksum = new CRC32();
        }
        try {
            in = new CheckedInputStream(in, checksum);
            IoUtils.copy(in, new NullOutputStream());
        } finally {
            IoUtils.close(in);
        }
        return checksum;
    }

    /**
     * Returns a new source that buffers reads from {@code source}. The returned
     * source will perform bulk reads into its in-memory buffer. Use this wherever
     * you read a source to get an ergonomic and efficient access to data.
     */
    public static BufferedSource buffer(Source source) {
        return new RealBufferedSource(source);
    }

    /**
     * Returns a new sink that buffers writes to {@code sink}. The returned sink
     * will batch writes to {@code sink}. Use this wherever you write to a sink to
     * get an ergonomic and efficient access to data.
     */
    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }

    /**
     * Returns a sink that writes to {@code out}.
     */
    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }

    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) throw new IllegalArgumentException("out == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                IoUtils.checkOffsetAndCount(source.size, 0, byteCount);
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
                        SegmentPool.recycle(head);
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
     * Returns a sink that writes to {@code socket}. Prefer this over {@link
     * #sink(OutputStream)} because this method honors timeouts. When the socket
     * write times out, the socket is asynchronously closed by a watchdog thread.
     */
    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("socket == null");
        if (socket.getOutputStream() == null) throw new IOException("socket's output stream == null");
        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    /**
     * Returns a source that reads from {@code in}.
     */
    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) throw new IllegalArgumentException("in == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

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
                    if (bytesRead == -1) return -1;
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
     * Returns a source that reads from {@code file}.
     */
    public static Source source(File file) throws FileNotFoundException {
        if (file == null) throw new IllegalArgumentException("file == null");
        return source(new FileInputStream(file));
    }

    /**
     * Returns a source that reads from {@code path}.
     */
    public static Source source(Path path, OpenOption... options) throws IOException {
        if (path == null) throw new IllegalArgumentException("path == null");
        return source(Files.newInputStream(path, options));
    }

    /**
     * Returns a sink that writes to {@code file}.
     */
    public static Sink sink(File file) throws FileNotFoundException {
        if (file == null) throw new IllegalArgumentException("file == null");
        return sink(new FileOutputStream(file));
    }

    /**
     * Returns a sink that appends to {@code file}.
     */
    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (file == null) throw new IllegalArgumentException("file == null");
        return sink(new FileOutputStream(file, true));
    }

    /**
     * Returns a sink that writes to {@code path}.
     */
    public static Sink sink(Path path, OpenOption... options) throws IOException {
        if (path == null) throw new IllegalArgumentException("path == null");
        return sink(Files.newOutputStream(path, options));
    }

    /**
     * Returns a sink that writes nowhere.
     */
    public static Sink blackhole() {
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                source.skip(byteCount);
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

    /**
     * Returns a source that reads from {@code socket}. Prefer this over {@link
     * #source(InputStream)} because this method honors timeouts. When the socket
     * read times out, the socket is asynchronously closed by a watchdog thread.
     */
    public static Source source(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("socket == null");
        if (socket.getInputStream() == null) throw new IOException("socket's input stream == null");
        AsyncTimeout timeout = timeout(socket);
        Source source = source(socket.getInputStream(), timeout);
        return timeout.source(source);
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            @Override
            protected IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (cause != null) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            @Override
            protected void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
                    throw new InstrumentException(e);
                } catch (AssertionError e) {
                    if (isAndroidGetsocknameError(e)) {
                        throw new InstrumentException(e);
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
        return e.getCause() != null && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the string to convert
     * @return an input stream
     * @since Commons IO 1.1
     */
    public static InputStream toInputStream(String input) {
        byte[] bytes = input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     *
     * @param input    the string to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @throws IOException if the encoding is invalid
     * @since Commons IO 1.1
     */
    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Get the contents of an <internal>InputStream</internal> as a String
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <internal>BufferedInputStream</internal>.
     *
     * @param input    the <internal>InputStream</internal> to read from
     * @param encoding the encoding to use, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static String toString(InputStream input, String encoding)
            throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    /**
     * Copy bytes from an <internal>InputStream</internal> to chars on a
     * <internal>Writer</internal> using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <internal>BufferedInputStream</internal>.
     * <p>
     * This method uses {@link InputStreamReader}.
     *
     * @param input  the <internal>InputStream</internal> to read from
     * @param output the <internal>Writer</internal> to write to
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void copy(InputStream input, Writer output) {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    /**
     * Copy bytes from an <internal>InputStream</internal> to chars on a
     * <internal>Writer</internal> using the specified character encoding.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <internal>BufferedInputStream</internal>.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method uses {@link InputStreamReader}.
     *
     * @param input    the <internal>InputStream</internal> to read from
     * @param output   the <internal>Writer</internal> to write to
     * @param encoding the encoding to use, null means platform default
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    /**
     * Get the contents of a <internal>Reader</internal> as a String.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <internal>BufferedReader</internal>.
     *
     * @param input the <internal>Reader</internal> to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static String toString(Reader input) {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    /**
     * Get the contents of a <internal>byte[]</internal> as a String
     * using the default character encoding of the platform.
     *
     * @param input the byte array to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs (never occurs)
     */
    public static String toString(byte[] input) {
        return new String(input);
    }

    /**
     * Get the contents of a <internal>byte[]</internal> as a String
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input    the byte array to read from
     * @param encoding the encoding to use, null means platform default
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs (never occurs)
     */
    public static String toString(byte[] input, String encoding)
            throws IOException {
        if (encoding == null) {
            return new String(input);
        } else {
            return new String(input, encoding);
        }
    }

}
