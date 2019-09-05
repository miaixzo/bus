/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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

import org.aoju.bus.core.io.LineHandler;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.CharsetUtils;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 文件读取器
 *
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
public class FileReader extends FileWrapper {

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileReader(File file, Charset charset) {
        super(file, charset);
        checkFile();
    }

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileReader(File file, String charset) {
        this(file, CharsetUtils.charset(charset));
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码
     */
    public FileReader(String filePath, Charset charset) {
        this(FileUtils.file(filePath), charset);
    }

    /**
     * 构造
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码
     */
    public FileReader(String filePath, String charset) {
        this(FileUtils.file(filePath), CharsetUtils.charset(charset));
    }

    /**
     * 构造
     * 编码使用 {@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param file 文件
     */
    public FileReader(File file) {
        this(file, DEFAULT_CHARSET);
    }

    /**
     * 构造
     * 编码使用 {@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param filePath 文件路径，相对路径会被转换为相对于ClassPath的路径
     */
    public FileReader(String filePath) {
        this(filePath, DEFAULT_CHARSET);
    }

    /**
     * 创建 FileReader
     *
     * @param file    文件
     * @param charset 编码
     * @return {@link FileReader}
     */
    public static FileReader create(File file, Charset charset) {
        return new FileReader(file, charset);
    }

    /**
     * 创建 FileReader, 编码：{@link FileWrapper#DEFAULT_CHARSET}
     *
     * @param file 文件
     * @return {@link FileReader}
     */
    public static FileReader create(File file) {
        return new FileReader(file);
    }

    /**
     * 读取文件所有数据
     * 文件的长度不能超过 {@link Integer#MAX_VALUE}
     *
     * @return 字节码
     * @throws InstrumentException 异常
     */
    public byte[] readBytes() throws InstrumentException {
        long len = file.length();
        if (len >= Integer.MAX_VALUE) {
            throw new InstrumentException("File is larger then max array size");
        }

        byte[] bytes = new byte[(int) len];
        FileInputStream in = null;
        int readLength;
        try {
            in = new FileInputStream(file);
            readLength = in.read(bytes);
            if (readLength < len) {
                throw new IOException(StringUtils.format("File length is [{}] but read [{}]!", len, readLength));
            }
        } catch (Exception e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
        }

        return bytes;
    }

    /**
     * 读取文件内容
     *
     * @return 内容
     * @throws InstrumentException 异常
     */
    public String readString() throws InstrumentException {
        return new String(readBytes(), this.charset);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public <T extends Collection<String>> T readLines(T collection) throws InstrumentException {
        BufferedReader reader = null;
        try {
            reader = FileUtils.getReader(file, charset);
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                collection.add(line);
            }
            return collection;
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(reader);
        }
    }

    /**
     * 按照行处理文件内容
     *
     * @param lineHandler 行处理器
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public void readLines(LineHandler lineHandler) throws InstrumentException {
        BufferedReader reader = null;
        try {
            reader = FileUtils.getReader(file, charset);
            IoUtils.readLines(reader, lineHandler);
        } finally {
            IoUtils.close(reader);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public List<String> readLines() throws InstrumentException {
        return readLines(new ArrayList<String>());
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           读取的结果对象类型
     * @param readerHandler Reader处理类
     * @return 从文件中read出的数据
     * @throws InstrumentException 异常
     */
    public <T> T read(ReaderHandler<T> readerHandler) throws InstrumentException {
        BufferedReader reader = null;
        T result = null;
        try {
            reader = FileUtils.getReader(this.file, charset);
            result = readerHandler.handle(reader);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(reader);
        }
        return result;
    }

    /**
     * 获得一个文件读取器
     *
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public BufferedReader getReader() throws InstrumentException {
        return IoUtils.getReader(getInputStream(), this.charset);
    }

    /**
     * 获得输入流
     *
     * @return 输入流
     * @throws InstrumentException 异常
     */
    public BufferedInputStream getInputStream() throws InstrumentException {
        try {
            return new BufferedInputStream(new FileInputStream(this.file));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将文件写入流中
     *
     * @param out 流
     * @return File
     * @throws InstrumentException 异常
     */
    public File writeToStream(OutputStream out) throws InstrumentException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            IoUtils.copy(in, out);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
        }
        return this.file;
    }

    /**
     * 检查文件
     *
     * @throws InstrumentException 异常
     */
    private void checkFile() throws InstrumentException {
        if (false == file.exists()) {
            throw new InstrumentException("File not exist: " + file);
        }
        if (false == file.isFile()) {
            throw new InstrumentException("Not a file:" + file);
        }
    }

    /**
     * Reader处理接口
     *
     * @param <T> Reader处理返回结果类型
     */
    public interface ReaderHandler<T> {
        T handle(BufferedReader reader) throws IOException;
    }

}
