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
package org.miaixz.bus.core.io.file;

import java.io.*;
import java.nio.file.OpenOption;
import java.util.Map;
import java.util.Map.Entry;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 文件写入器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileWriter extends FileWrapper {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码，使用 {@link Charset}
     */
    public FileWriter(final File file, final java.nio.charset.Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码，使用 {@link Charset}
     */
    public FileWriter(final String filePath, final java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码，使用 {@link Charset#charset(String)}
     */
    public FileWriter(final String filePath, final String charset) {
        this(FileKit.file(filePath), Charset.charset(charset));
    }

    /**
     * 构造 编码使用 {@link Charset#UTF_8}
     *
     * @param file 文件
     */
    public FileWriter(final File file) {
        this(file, Charset.UTF_8);
    }

    /**
     * 构造 编码使用 {@link Charset#UTF_8}
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     */
    public FileWriter(final String filePath) {
        this(filePath, Charset.UTF_8);
    }

    /**
     * 创建 FileWriter
     *
     * @param file    文件
     * @param charset 编码，使用 {@link Charset}
     * @return FileWriter
     */
    public static FileWriter of(final File file, final java.nio.charset.Charset charset) {
        return new FileWriter(file, charset);
    }

    /**
     * 创建 FileWriter, 编码：{@link Charset#UTF_8}
     *
     * @param file 文件
     * @return FileWriter
     */
    public static FileWriter of(final File file) {
        return new FileWriter(file);
    }

    /**
     * 将String写入文件
     *
     * @param content  写入的内容
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File write(final String content, final boolean isAppend) throws InternalException {
        BufferedWriter writer = null;
        try {
            writer = getWriter(isAppend);
            writer.write(content);
            writer.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(writer);
        }
        return file;
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File write(final String content) throws InternalException {
        return write(content, false);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public File append(final String content) throws InternalException {
        return write(content, true);
    }

    /**
     * 将列表写入文件，覆盖模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public <T> File writeLines(final Iterable<T> list) throws InternalException {
        return writeLines(list, false);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public <T> File appendLines(final Iterable<T> list) throws InternalException {
        return writeLines(list, true);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public <T> File writeLines(final Iterable<T> list, final boolean isAppend) throws InternalException {
        return writeLines(list, null, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>           集合元素类型
     * @param list          列表
     * @param lineSeparator 换行符枚举（Windows、Mac或Linux换行符）
     * @param isAppend      是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public <T> File writeLines(final Iterable<T> list, final LineSeparator lineSeparator, final boolean isAppend)
            throws InternalException {
        try (final PrintWriter writer = getPrintWriter(isAppend)) {
            boolean isFirst = true;
            for (final T t : list) {
                if (null != t) {
                    if (isFirst) {
                        isFirst = false;
                        if (isAppend && FileKit.isNotEmpty(this.file)) {
                            // 追加模式下且文件非空，补充换行符
                            printNewLine(writer, lineSeparator);
                        }
                    } else {
                        printNewLine(writer, lineSeparator);
                    }
                    writer.print(t);

                    writer.flush();
                }
            }
        }
        return this.file;
    }

    /**
     * 将Map写入文件，每个键值对为一行，一行中键与值之间使用kvSeparator分隔
     *
     * @param map         Map
     * @param kvSeparator 键和值之间的分隔符，如果传入null使用默认分隔符" = "
     * @param isAppend    是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File writeMap(final Map<?, ?> map, final String kvSeparator, final boolean isAppend)
            throws InternalException {
        return writeMap(map, null, kvSeparator, isAppend);
    }

    /**
     * 将Map写入文件，每个键值对为一行，一行中键与值之间使用kvSeparator分隔
     *
     * @param map           Map
     * @param lineSeparator 换行符枚举（Windows、Mac或Linux换行符）
     * @param kvSeparator   键和值之间的分隔符，如果传入null使用默认分隔符" = "
     * @param isAppend      是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File writeMap(final Map<?, ?> map, final LineSeparator lineSeparator, String kvSeparator,
            final boolean isAppend) throws InternalException {
        if (null == kvSeparator) {
            kvSeparator = " = ";
        }
        try (final PrintWriter writer = getPrintWriter(isAppend)) {
            for (final Entry<?, ?> entry : map.entrySet()) {
                if (null != entry) {
                    writer.print(StringKit.format("{}{}{}", entry.getKey(), kvSeparator, entry.getValue()));
                    printNewLine(writer, lineSeparator);
                    writer.flush();
                }
            }
        }
        return this.file;
    }

    /**
     * 写入数据到文件
     *
     * @param data 数据
     * @param off  数据开始位置
     * @param len  数据长度
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File write(final byte[] data, final int off, final int len) throws InternalException {
        return write(data, off, len, false);
    }

    /**
     * 追加数据到文件
     *
     * @param data 数据
     * @param off  数据开始位置
     * @param len  数据长度
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File append(final byte[] data, final int off, final int len) throws InternalException {
        return write(data, off, len, true);
    }

    /**
     * 写入数据到文件
     *
     * @param data     数据
     * @param off      数据开始位置
     * @param len      数据长度
     * @param isAppend 是否追加模式
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public File write(final byte[] data, final int off, final int len, final boolean isAppend)
            throws InternalException {
        try (final FileOutputStream out = new FileOutputStream(FileKit.touch(file), isAppend)) {
            out.write(data, off, len);
            out.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return file;
    }

    /**
     * 将流的内容写入文件 此方法会自动关闭输入流
     *
     * @param in 输入流，不关闭
     * @return dest
     * @throws InternalException IO异常
     */
    public File writeFromStream(final InputStream in) throws InternalException {
        return writeFromStream(in, true);
    }

    /**
     * 将流的内容写入文件
     *
     * @param in      输入流，不关闭
     * @param options 选项，如追加模式传{@link java.nio.file.StandardOpenOption#APPEND}
     * @return file
     */
    public File writeFromStream(final InputStream in, final boolean isCloseIn, final OpenOption... options) {
        OutputStream out = null;
        try {
            out = FileKit.getOutputStream(file, options);
            IoKit.copy(in, out);
        } finally {
            IoKit.closeQuietly(out);
            if (isCloseIn) {
                IoKit.closeQuietly(in);
            }
        }
        return file;
    }

    /**
     * 获得一个输出流对象
     *
     * @param options 选项，如追加模式传{@link java.nio.file.StandardOpenOption#APPEND}
     * @return 输出流对象
     */
    public BufferedOutputStream getOutputStream(final OpenOption... options) {
        return FileKit.getOutputStream(file, options);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public BufferedWriter getWriter(final boolean isAppend) throws InternalException {
        try {
            return new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(FileKit.touch(file), isAppend), charset));
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException IO异常
     */
    public PrintWriter getPrintWriter(final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(isAppend));
    }

    /**
     * 检查文件
     *
     * @throws InternalException IO异常
     */
    private void checkFile() throws InternalException {
        Assert.notNull(file, "File to write content is null !");
        if (this.file.exists() && !file.isFile()) {
            throw new InternalException("File [{}] is not a file !", this.file.getAbsoluteFile());
        }
    }

    /**
     * 打印新行
     *
     * @param writer        Writer
     * @param lineSeparator 换行符枚举
     */
    private void printNewLine(final PrintWriter writer, final LineSeparator lineSeparator) {
        if (null == lineSeparator) {
            // 默认换行符
            writer.println();
        } else {
            // 自定义换行符
            writer.print(lineSeparator.getValue());
        }
    }

}
