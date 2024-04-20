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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.function.XConsumer;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 文件读取器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileReader extends FileWrapper {

    /**
     * 构造
     *
     * @param file    文件
     * @param charset 编码
     */
    public FileReader(File file, java.nio.charset.Charset charset) {
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
        this(file, Charset.charset(charset));
    }

    /**
     * 构造
     *
     * @param filePath 文件路径,相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码
     */
    public FileReader(String filePath, java.nio.charset.Charset charset) {
        this(FileKit.file(filePath), charset);
    }

    /**
     * 构造
     *
     * @param filePath 文件路径,相对路径会被转换为相对于ClassPath的路径
     * @param charset  编码
     */
    public FileReader(String filePath, String charset) {
        this(FileKit.file(filePath), Charset.charset(charset));
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
     * @param filePath 文件路径,相对路径会被转换为相对于ClassPath的路径
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
    public static FileReader create(File file, java.nio.charset.Charset charset) {
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
     * @throws InternalException 异常
     */
    public byte[] readBytes() throws InternalException {
        long len = file.length();
        if (len >= Integer.MAX_VALUE) {
            throw new InternalException("File is larger then max array size");
        }

        byte[] bytes = new byte[(int) len];
        FileInputStream in = null;
        int readLength;
        try {
            in = new FileInputStream(file);
            readLength = in.read(bytes);
            if (readLength < len) {
                throw new IOException(StringKit.format("File length is [{}] but read [{}]!", len, readLength));
            }
        } catch (Exception e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(in);
        }

        return bytes;
    }

    /**
     * 读取文件内容
     *
     * @return 内容
     * @throws InternalException 异常
     */
    public String readString() throws InternalException {
        return new String(readBytes(), this.charset);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public <T extends Collection<String>> T readLines(T collection) throws InternalException {
        BufferedReader reader = null;
        try {
            reader = FileKit.getReader(file, charset);
            String line;
            while (true) {
                line = reader.readLine();
                if (null == line) {
                    break;
                }
                collection.add(line);
            }
            return collection;
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(reader);
        }
    }

    /**
     * 按照行处理文件内容
     *
     * @param lineHandler 行处理器
     * @throws InternalException 异常
     */
    public void readLines(final XConsumer<String> lineHandler) throws InternalException {
        BufferedReader reader = null;
        try {
            reader = FileKit.getReader(file, charset);
            IoKit.readLines(reader, lineHandler);
        } finally {
            IoKit.close(reader);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
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
     * @throws InternalException 异常
     */
    public <T> T read(ReaderHandler<T> readerHandler) throws InternalException {
        BufferedReader reader = null;
        T result;
        try {
            reader = FileKit.getReader(this.file, charset);
            result = readerHandler.handle(reader);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(reader);
        }
        return result;
    }

    /**
     * 获得一个文件读取器
     *
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public BufferedReader getReader() throws InternalException {
        return IoKit.getReader(getInputStream(), this.charset);
    }

    /**
     * 获得输入流
     *
     * @return 输入流
     * @throws InternalException 异常
     */
    public BufferedInputStream getInputStream() throws InternalException {
        try {
            return new BufferedInputStream(new FileInputStream(this.file));
        } catch (IOException e) {
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
    public long writeToStream(OutputStream out) throws InternalException {
        return writeToStream(out, false);
    }

    /**
     * 将文件写入流中
     *
     * @param out     流
     * @param isClose 是否关闭输出流
     * @return 写出的流byte数
     * @throws InternalException IO异常
     */
    public long writeToStream(OutputStream out, boolean isClose) throws InternalException {
        try (FileInputStream in = new FileInputStream(this.file)) {
            return IoKit.copy(in, out);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (isClose) {
                IoKit.close(out);
            }
        }
    }

    /**
     * 检查文件
     *
     * @throws InternalException 异常
     */
    private void checkFile() throws InternalException {
        if (false == file.exists()) {
            throw new InternalException("File not exist : " + file);
        }
        if (false == file.isFile()) {
            throw new InternalException("Not a file :" + file);
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
