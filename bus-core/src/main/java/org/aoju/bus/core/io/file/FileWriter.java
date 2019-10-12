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
package org.aoju.bus.core.io.file;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CharsetUtils;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 文件写入器
 *
 * @author Kimi Liu
 * @version 5.0.0
 * @since JDK 1.8+
 */
public class FileWriter extends FileWrapper {

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileWriter(File file, Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileWriter(File file, String charset) {
        this(file, CharsetUtils.charset(charset));
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码
     */
    public FileWriter(String filePath, Charset charset) {
        this(FileUtils.file(filePath), charset);
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码，使用 {@link CharsetUtils#charset(String)}
     */
    public FileWriter(String filePath, String charset) {
        this(FileUtils.file(filePath), CharsetUtils.charset(charset));
    }

    /**
     * 构造
     * 编码使用 {@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param file 文件
     */
    public FileWriter(File file) {
        this(file, DEFAULT_CHARSET);
    }

    /**
     * 构造
     * 编码使用 {@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     */
    public FileWriter(String filePath) {
        this(filePath, DEFAULT_CHARSET);
    }

    /**
     * 创建 FileWriter
     *
     * @param file    文件
     * @param charset 编码
     * @return {@link FileWriter}
     */
    public static FileWriter create(File file, Charset charset) {
        return new FileWriter(file, charset);
    }

    /**
     * 创建 FileWriter, 编码：{@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param file 文件
     * @return {@link FileWriter}
     */
    public static FileWriter create(File file) {
        return new FileWriter(file);
    }

    /**
     * 将String写入文件
     *
     * @param content  写入的内容
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public File write(String content, boolean isAppend) throws InstrumentException {
        BufferedWriter writer = null;
        try {
            writer = getWriter(isAppend);
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(writer);
        }
        return file;
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public File write(String content) throws InstrumentException {
        return write(content, false);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @return 写入的文件
     * @throws InstrumentException IO异常
     */
    public File append(String content) throws InstrumentException {
        return write(content, true);
    }

    /**
     * 将列表写入文件，覆盖模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public <T> File writeLines(Collection<T> list) throws InstrumentException {
        return writeLines(list, false);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public <T> File appendLines(Collection<T> list) throws InstrumentException {
        return writeLines(list, true);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public <T> File writeLines(Collection<T> list, boolean isAppend) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     * @since 3.1.9
     */
    public <T> File writeLines(Collection<T> list, LineSeparator lineSeparator, boolean isAppend) throws InstrumentException {
        try (PrintWriter writer = getPrintWriter(isAppend)) {
            for (T t : list) {
                if (null != t) {
                    writer.print(t.toString());
                    printNewLine(writer, lineSeparator);
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
     * @throws InstrumentException IO异常
     */
    public File writeMap(Map<?, ?> map, String kvSeparator, boolean isAppend) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public File writeMap(Map<?, ?> map, LineSeparator lineSeparator, String kvSeparator, boolean isAppend) throws InstrumentException {
        if (null == kvSeparator) {
            kvSeparator = " = ";
        }
        try (PrintWriter writer = getPrintWriter(isAppend)) {
            for (Entry<?, ?> entry : map.entrySet()) {
                if (null != entry) {
                    writer.print(StringUtils.format("{}{}{}", entry.getKey(), kvSeparator, entry.getValue()));
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
     * @throws InstrumentException IO异常
     */
    public File write(byte[] data, int off, int len) throws InstrumentException {
        return write(data, off, len, false);
    }

    /**
     * 追加数据到文件
     *
     * @param data 数据
     * @param off  数据开始位置
     * @param len  数据长度
     * @return 目标文件
     * @throws InstrumentException IO异常
     */
    public File append(byte[] data, int off, int len) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public File write(byte[] data, int off, int len, boolean isAppend) throws InstrumentException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(FileUtils.touch(file), isAppend);
            out.write(data, off, len);
            out.flush();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(out);
        }
        return file;
    }

    /**
     * 将流的内容写入文件
     * 此方法不会关闭输入流
     *
     * @param in 输入流，不关闭
     * @return dest
     * @throws InstrumentException IO异常
     */
    public File writeFromStream(InputStream in) throws InstrumentException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(FileUtils.touch(file));
            IoUtils.copy(in, out);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(out);
        }
        return file;
    }

    /**
     * 获得一个输出流对象
     *
     * @return 输出流对象
     * @throws InstrumentException IO异常
     */
    public BufferedOutputStream getOutputStream() throws InstrumentException {
        try {
            return new BufferedOutputStream(new FileOutputStream(FileUtils.touch(file)));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InstrumentException IO异常
     */
    public BufferedWriter getWriter(boolean isAppend) throws InstrumentException {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FileUtils.touch(file), isAppend), charset));
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InstrumentException IO异常
     */
    public PrintWriter getPrintWriter(boolean isAppend) throws InstrumentException {
        return new PrintWriter(getWriter(isAppend));
    }

    /**
     * 检查文件
     *
     * @throws InstrumentException IO异常
     */
    private void checkFile() throws InstrumentException {
        Assert.notNull(file, "File to write content is null !");
        if (this.file.exists() && false == file.isFile()) {
            throw new InstrumentException("File [{}] is not a file !", this.file.getAbsoluteFile());
        }
    }

    /**
     * 打印新行
     *
     * @param writer        Writer
     * @param lineSeparator 换行符枚举
     */
    private void printNewLine(PrintWriter writer, LineSeparator lineSeparator) {
        if (null == lineSeparator) {
            //默认换行符
            writer.println();
        } else {
            //自定义换行符
            writer.print(lineSeparator.getValue());
        }
    }

}
