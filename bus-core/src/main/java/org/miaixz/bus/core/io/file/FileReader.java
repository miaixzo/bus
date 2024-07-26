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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ExceptionKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 文件读取器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileReader extends FileWrapper {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码，使用 {@link Charset}
     */
    public FileReader(final File file, final java.nio.charset.Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * 创建 FileReader
     *
     * @param file    文件
     * @param charset 编码，使用 {@link Charset}
     * @return FileReader
     */
    public static FileReader of(final File file, final java.nio.charset.Charset charset) {
        return new FileReader(file, charset);
    }

    /**
     * 创建 FileReader, 编码：{@link Charset#UTF_8}
     *
     * @param file 文件
     * @return FileReader
     */
    public static FileReader of(final File file) {
        return new FileReader(file, Charset.UTF_8);
    }

    /**
     * 读取文件所有数据 文件的长度不能超过 {@link Integer#MAX_VALUE}
     *
     * @return 字节码
     * @throws InternalException IO异常
     */
    public byte[] readBytes() throws InternalException {
        try {
            return Files.readAllBytes(this.file.toPath());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取文件内容
     *
     * @return 内容
     * @throws InternalException IO异常
     */
    public String readString() throws InternalException {
        // JDK11+不再推荐使用这种方式，推荐使用Files.readString
        return new String(readBytes(), this.charset);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public <T extends Collection<String>> T readLines(final T collection) throws InternalException {
        return readLines(collection, null);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param collection 集合
     * @param predicate  断言，断言为真的加入到提供的集合中
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public <T extends Collection<String>> T readLines(final T collection, final Predicate<String> predicate)
            throws InternalException {
        readLines((ConsumerX<String>) s -> {
            if (null == predicate || predicate.test(s)) {
                collection.add(s);
            }
        });
        return collection;
    }

    /**
     * 按照行处理文件内容
     *
     * @param lineHandler 行处理器
     * @throws InternalException IO异常
     */
    public void readLines(final ConsumerX<String> lineHandler) throws InternalException {
        BufferedReader reader = null;
        try {
            reader = FileKit.getReader(file, charset);
            IoKit.readLines(reader, lineHandler);
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public List<String> readLines() throws InternalException {
        return readLines(new ArrayList<>());
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           读取的结果对象类型
     * @param readerHandler Reader处理类
     * @return 从文件中read出的数据
     * @throws InternalException IO异常
     */
    public <T> T read(final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        BufferedReader reader = null;
        T result;
        try {
            reader = FileKit.getReader(this.file, charset);
            result = readerHandler.applying(reader);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        } finally {
            IoKit.closeQuietly(reader);
        }
        return result;
    }

    /**
     * 获得一个文件读取器
     *
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public BufferedReader getReader() throws InternalException {
        return IoKit.toReader(getInputStream(), this.charset);
    }

    /**
     * 获得输入流
     *
     * @return 输入流
     * @throws InternalException IO异常
     */
    public BufferedInputStream getInputStream() throws InternalException {
        try {
            return new BufferedInputStream(Files.newInputStream(this.file.toPath()));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 将文件写入流中，此方法不会关闭比输出流
     *
     * @param out 流
     * @return 写出的流byte数
     * @throws InternalException IO异常
     */
    public long writeToStream(final OutputStream out) throws InternalException {
        return writeToStream(out, false);
    }

    /**
     * 将文件写入流中
     *
     * @param out        流
     * @param isCloseOut 是否关闭输出流
     * @return 写出的流byte数
     * @throws InternalException IO异常
     */
    public long writeToStream(final OutputStream out, final boolean isCloseOut) throws InternalException {
        try (final FileInputStream in = new FileInputStream(this.file)) {
            return IoKit.copy(in, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isCloseOut) {
                IoKit.closeQuietly(out);
            }
        }
    }

    /**
     * 检查文件
     *
     * @throws InternalException IO异常
     */
    private void checkFile() throws InternalException {
        if (!file.exists()) {
            throw new InternalException("File not exist: " + file);
        }
        if (!file.isFile()) {
            throw new InternalException("Not a file:" + file);
        }
    }

}
