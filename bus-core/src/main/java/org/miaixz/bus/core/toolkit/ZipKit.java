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

import org.miaixz.bus.core.collection.EnumerationIterator;
import org.miaixz.bus.core.compress.*;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipKit {

    private static final int DEFAULT_BYTE_ARRAY_LENGTH = Normal._32;

    /**
     * 默认编码,使用平台相关编码
     */
    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * 打包到当前目录,使用默认编码UTF-8
     *
     * @param srcPath 源文件路径
     * @return 打包好的压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(String srcPath) throws InternalException {
        return zip(srcPath, DEFAULT_CHARSET);
    }

    /**
     * 打包到当前目录
     *
     * @param srcPath 源文件路径
     * @param charset 编码
     * @return 打包好的压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(String srcPath, java.nio.charset.Charset charset) throws InternalException {
        return zip(FileKit.file(srcPath), charset);
    }

    /**
     * 打包到当前目录,使用默认编码UTF-8
     *
     * @param srcFile 源文件或目录
     * @return 打包好的压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File srcFile) throws InternalException {
        return zip(srcFile, DEFAULT_CHARSET);
    }

    /**
     * 打包到当前目录
     *
     * @param srcFile 源文件或目录
     * @param charset 编码
     * @return 打包好的压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File srcFile, java.nio.charset.Charset charset) throws InternalException {
        final File zipFile = FileKit.file(srcFile.getParentFile(), FileKit.getPrefix(srcFile) + ".zip");
        zip(zipFile, charset, false, srcFile);
        return zipFile;
    }

    /**
     * 对文件或文件目录进行压缩
     * 不包含被打包目录
     *
     * @param srcPath 要压缩的源文件路径 如果压缩一个文件,则为该文件的全路径；如果压缩一个目录,则为该目录的顶层目录路径
     * @param zipPath 压缩文件保存的路径,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @return 压缩好的Zip文件
     * @throws InternalException IO异常
     */
    public static File zip(String srcPath, String zipPath) throws InternalException {
        return zip(srcPath, zipPath, false);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param srcPath    要压缩的源文件路径 如果压缩一个文件,则为该文件的全路径；如果压缩一个目录,则为该目录的顶层目录路径
     * @param zipPath    压缩文件保存的路径,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param withSrcDir 是否包含被打包目录
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(String srcPath, String zipPath, boolean withSrcDir) throws InternalException {
        return zip(srcPath, zipPath, DEFAULT_CHARSET, withSrcDir);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param srcPath    要压缩的源文件路径 如果压缩一个文件,则为该文件的全路径；如果压缩一个目录,则为该目录的顶层目录路径
     * @param zipPath    压缩文件保存的路径,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset    编码
     * @param withSrcDir 是否包含被打包目录
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(String srcPath, String zipPath, java.nio.charset.Charset charset, boolean withSrcDir) throws InternalException {
        final File srcFile = FileKit.file(srcPath);
        final File zipFile = FileKit.file(zipPath);
        zip(zipFile, charset, withSrcDir, srcFile);
        return zipFile;
    }

    /**
     * 对文件或文件目录进行压缩
     * 使用默认UTF-8编码
     *
     * @param zipFile    生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param withSrcDir 是否包含被打包目录,只针对压缩目录有效 若为false,则只压缩目录下的文件或目录,为true则将本目录也压缩
     * @param srcFiles   要压缩的源文件或目录
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, boolean withSrcDir, File... srcFiles) throws InternalException {
        return zip(zipFile, DEFAULT_CHARSET, withSrcDir, srcFiles);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param zipFile    生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset    编码
     * @param withSrcDir 是否包含被打包目录,只针对压缩目录有效 若为false,则只压缩目录下的文件或目录,为true则将本目录也压缩
     * @param srcFiles   要压缩的源文件或目录 如果压缩一个文件,则为该文件的全路径；如果压缩一个目录,则为该目录的顶层目录路径
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, java.nio.charset.Charset charset, boolean withSrcDir, File... srcFiles) throws InternalException {
        return zip(zipFile, charset, withSrcDir, null, srcFiles);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param zipFile    生成的Zip文件，包括文件名。注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset    编码
     * @param withSrcDir 是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩）
     * @param srcFiles   要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, java.nio.charset.Charset charset, boolean withSrcDir, FileFilter filter, File... srcFiles) throws InternalException {
        validateFiles(zipFile, srcFiles);

        ZipWriter.of(zipFile, charset).add(withSrcDir, filter, srcFiles).close();
        return zipFile;
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param out        生成的Zip到的目标流，包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset    编码
     * @param withSrcDir 是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件(过滤掉哪些文件或文件夹不加入压缩)
     * @param srcFiles   要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     */
    public static void zip(OutputStream out, java.nio.charset.Charset charset, boolean withSrcDir, FileFilter filter, File... srcFiles) {
        ZipWriter.of(out, charset).add(withSrcDir, filter, srcFiles).close();
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param zipOutputStream 生成的Zip到的目标流，自动关闭此流
     * @param withSrcDir      是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter          文件过滤器，通过实现此接口，自定义要过滤的文件(过滤掉哪些文件或文件夹不加入压缩)
     * @param srcFiles        要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     */
    public static void zip(ZipOutputStream zipOutputStream, boolean withSrcDir, FileFilter filter, File... srcFiles) {
        try (final ZipWriter zipWriter = new ZipWriter(zipOutputStream)) {
            zipWriter.add(withSrcDir, filter, srcFiles);
        }
    }

    /**
     * 对流中的数据加入到压缩文件,使用默认UTF-8编码
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param data    要压缩的数据
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String path, String data) throws InternalException {
        return zip(zipFile, path, data, DEFAULT_CHARSET);
    }

    /**
     * 对流中的数据加入到压缩文件
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param data    要压缩的数据
     * @param charset 编码
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String path, String data, java.nio.charset.Charset charset) throws InternalException {
        return zip(zipFile, path, IoKit.toStream(data, charset), charset);
    }

    /**
     * 对流中的数据加入到压缩文件
     * 使用默认编码UTF-8
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param in      要压缩的源
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String path, InputStream in) throws InternalException {
        return zip(zipFile, path, in, DEFAULT_CHARSET);
    }

    /**
     * 对流中的数据加入到压缩文件
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param in      要压缩的源
     * @param charset 编码
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String path, InputStream in, java.nio.charset.Charset charset) throws InternalException {
        return zip(zipFile, new String[]{path}, new InputStream[]{in}, charset);
    }

    /**
     * 对流中的数据加入到压缩文件
     * 路径列表和流列表长度必须一致
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param paths   流数据在压缩文件中的路径或文件名
     * @param ins     要压缩的源，添加完成后自动关闭流
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String[] paths, InputStream[] ins) throws InternalException {
        return zip(zipFile, paths, ins, DEFAULT_CHARSET);
    }

    /**
     * 对流中的数据加入到压缩文件
     * 路径列表和流列表长度必须一致
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param paths   流数据在压缩文件中的路径或文件名
     * @param ins     要压缩的源
     * @param charset 编码
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, String[] paths, InputStream[] ins, java.nio.charset.Charset charset) throws InternalException {
        if (ArrayKit.isEmpty(paths) || ArrayKit.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        try (final ZipWriter zipWriter = ZipWriter.of(zipFile, charset)) {
            for (int i = 0; i < paths.length; i++) {
                zipWriter.add(paths[i], ins[i]);
            }
        }
        return zipFile;
    }

    /**
     * 将文件流压缩到目标流中
     *
     * @param out   目标流，压缩完成自动关闭
     * @param paths 流数据在压缩文件中的路径或文件名
     * @param ins   要压缩的源，添加完成后自动关闭流
     */
    public static void zip(OutputStream out, String[] paths, InputStream[] ins) {
        if (ArrayKit.isEmpty(paths) || ArrayKit.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        try (final ZipWriter zipWriter = ZipWriter.of(out, DEFAULT_CHARSET)) {
            for (int i = 0; i < paths.length; i++) {
                zipWriter.add(paths[i], ins[i]);
            }
        }
    }

    /**
     * 将文件流压缩到目标流中
     *
     * @param zipOutputStream 目标流，压缩完成不关闭
     * @param paths           流数据在压缩文件中的路径或文件名
     * @param ins             要压缩的源，添加完成后自动关闭流
     * @throws InternalException IO异常
     */
    public static void zip(ZipOutputStream zipOutputStream, String[] paths, InputStream[] ins) throws InternalException {
        if (ArrayKit.isEmpty(paths) || ArrayKit.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        try (final ZipWriter zipWriter = new ZipWriter(zipOutputStream)) {
            for (int i = 0; i < paths.length; i++) {
                zipWriter.add(paths[i], ins[i]);
            }
        }
    }

    /**
     * 对流中的数据加入到压缩文件
     * 路径列表和流列表长度必须一致
     *
     * @param zipFile   生成的Zip文件，包括文件名。注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset   编码
     * @param resources 需要压缩的资源，资源的路径为{@link Resource#getName()}
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public static File zip(File zipFile, Charset charset, Resource... resources) throws InternalException {
        ZipWriter.of(zipFile, charset).add(resources).close();
        return zipFile;
    }

    /**
     * 解压到文件名相同的目录中,默认编码UTF-8
     *
     * @param zipFilePath 压缩文件路径
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(String zipFilePath) throws InternalException {
        return unzip(zipFilePath, DEFAULT_CHARSET);
    }

    /**
     * 解压到文件名相同的目录中
     *
     * @param zipFilePath 压缩文件路径
     * @param charset     编码
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(String zipFilePath, java.nio.charset.Charset charset) throws InternalException {
        return unzip(FileKit.file(zipFilePath), charset);
    }

    /**
     * 解压到文件名相同的目录中,使用UTF-8编码
     *
     * @param zipFile 压缩文件
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(File zipFile) throws InternalException {
        return unzip(zipFile, DEFAULT_CHARSET);
    }

    /**
     * 解压到文件名相同的目录中
     *
     * @param zipFile 压缩文件
     * @param charset 编码
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(File zipFile, java.nio.charset.Charset charset) throws InternalException {
        return unzip(zipFile, FileKit.file(zipFile.getParentFile(), FileKit.getPrefix(zipFile)), charset);
    }

    /**
     * 解压,默认UTF-8编码
     *
     * @param zipFilePath 压缩文件的路径
     * @param outFileDir  解压到的目录
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(String zipFilePath, String outFileDir) throws InternalException {
        return unzip(zipFilePath, outFileDir, DEFAULT_CHARSET);
    }

    /**
     * 解压
     *
     * @param zipFilePath 压缩文件的路径
     * @param outFileDir  解压到的目录
     * @param charset     编码
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(String zipFilePath, String outFileDir, java.nio.charset.Charset charset) throws InternalException {
        return unzip(FileKit.file(zipFilePath), FileKit.mkdir(outFileDir), charset);
    }

    /**
     * 解压,默认使用UTF-8编码
     *
     * @param zipFile zip文件
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(File zipFile, File outFile) throws InternalException {
        return unzip(zipFile, outFile, DEFAULT_CHARSET);
    }

    /**
     * 解压
     *
     * @param zipFile zip文件
     * @param outFile 解压到的目录
     * @param charset 编码
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(File zipFile, File outFile, java.nio.charset.Charset charset) throws InternalException {
        return unzip(zipFile(zipFile, charset), outFile);
    }

    /**
     * 解压
     *
     * @param zipFile zip文件,附带编码信息,使用完毕自动关闭
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InternalException IO异常
     */
    public static File unzip(ZipFile zipFile, File outFile) {
        return unzip(zipFile, outFile, -1);
    }

    /**
     * 限制解压后文件大小
     *
     * @param zipFile zip文件，附带编码信息，使用完毕自动关闭
     * @param outFile 解压到的目录
     * @param limit   限制解压文件大小(单位B)
     * @return 解压的目录
     */
    public static File unzip(ZipFile zipFile, File outFile, long limit) {
        if (outFile.exists() && outFile.isFile()) {
            throw new IllegalArgumentException(
                    StringKit.format("Target path [{}] exist!", outFile.getAbsolutePath()));
        }

        if (limit > 0) {
            final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            long zipFileSize = 0L;
            ZipEntry zipEntry;
            while (zipEntries.hasMoreElements()) {
                zipEntry = zipEntries.nextElement();
                zipFileSize += zipEntry.getSize();
                if (zipFileSize > limit) {
                    throw new IllegalArgumentException("The file size exceeds the limit");
                }
            }
        }

        try (final ZipReader reader = new ZipReader(zipFile)) {
            reader.readTo(outFile);
        }
        return outFile;
    }

    /**
     * 解压
     * ZIP条目不使用高速缓冲
     *
     * @param in      zip文件流,使用完毕自动关闭
     * @param outFile 解压到的目录
     * @param charset 编码
     * @return 解压的目录
     */
    public static File unzip(InputStream in, File outFile, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = DEFAULT_CHARSET;
        }
        return unzip(new ZipInputStream(in, charset), outFile);
    }

    /**
     * 解压
     * ZIP条目不使用高速缓冲
     *
     * @param zipStream zip文件流,包含编码信息
     * @param outFile   解压到的目录
     * @return 解压的目录
     */
    public static File unzip(ZipInputStream zipStream, File outFile) {
        try (final ZipReader reader = new ZipReader(zipStream)) {
            reader.readTo(outFile);
        }
        return outFile;
    }

    /**
     * 从Zip文件中提取指定的文件为bytes
     *
     * @param zipFilePath Zip文件
     * @param name        文件名,如果存在于子文件夹中,此文件名必须包含目录名,例如images/aaa.txt
     * @return 文件内容bytes
     */
    public static byte[] unzipFileBytes(String zipFilePath, String name) {
        return unzipFileBytes(zipFilePath, DEFAULT_CHARSET, name);
    }

    /**
     * 从Zip文件中提取指定的文件为bytes
     *
     * @param zipFilePath Zip文件
     * @param charset     编码
     * @param name        文件名,如果存在于子文件夹中,此文件名必须包含目录名,例如images/aaa.txt
     * @return 文件内容bytes
     */
    public static byte[] unzipFileBytes(String zipFilePath, java.nio.charset.Charset charset, String name) {
        return unzipFileBytes(FileKit.file(zipFilePath), charset, name);
    }

    /**
     * 从Zip文件中提取指定的文件为bytes
     *
     * @param zipFile Zip文件
     * @param name    文件名,如果存在于子文件夹中,此文件名必须包含目录名,例如images/aaa.txt
     * @return 文件内容bytes
     */
    public static byte[] unzipFileBytes(File zipFile, String name) {
        return unzipFileBytes(zipFile, DEFAULT_CHARSET, name);
    }

    /**
     * 从Zip文件中提取指定的文件为bytes
     *
     * @param zipFile Zip文件
     * @param charset 编码
     * @param name    文件名,如果存在于子文件夹中,此文件名必须包含目录名,例如images/aaa.txt
     * @return 文件内容bytes
     */
    public static byte[] unzipFileBytes(File zipFile, java.nio.charset.Charset charset, String name) {
        try (final ZipReader reader = ZipReader.of(zipFile, charset)) {
            return IoKit.readBytes(reader.get(name));
        }
    }

    /**
     * Gzip压缩处理
     *
     * @param content 被压缩的字符串
     * @param charset 编码
     * @return 压缩后的字节流
     * @throws InternalException IO异常
     */
    public static byte[] gzip(String content, String charset) throws InternalException {
        return gzip(StringKit.bytes(content, charset));
    }

    /**
     * Gzip压缩处理
     *
     * @param buf 被压缩的字节流
     * @return 压缩后的字节流
     * @throws InternalException IO异常
     */
    public static byte[] gzip(byte[] buf) throws InternalException {
        return gzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Gzip压缩文件
     *
     * @param file 被压缩的文件
     * @return 压缩后的字节流
     * @throws InternalException IO异常
     */
    public static byte[] gzip(File file) throws InternalException {
        BufferedInputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return gzip(in, (int) file.length());
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * Gzip压缩文件
     *
     * @param in 被压缩的流
     * @return 压缩后的字节流
     * @throws InternalException IO异常
     */
    public static byte[] gzip(InputStream in) throws InternalException {
        return gzip(in, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * Gzip压缩文件
     *
     * @param in     被压缩的流
     * @param length 预估长度
     * @return 压缩后的字节流
     * @throws InternalException IO异常
     */
    public static byte[] gzip(InputStream in, int length) throws InternalException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        Gzip.of(in, bos).gzip().close();
        return bos.toByteArray();
    }

    /**
     * Gzip解压缩处理
     *
     * @param buf     压缩过的字节流
     * @param charset 编码
     * @return 解压后的字符串
     * @throws InternalException IO异常
     */
    public static String unGzip(byte[] buf, String charset) throws InternalException {
        return StringKit.toString(unGzip(buf), charset);
    }

    /**
     * Gzip解压处理
     *
     * @param buf buf
     * @return bytes
     * @throws InternalException IO异常
     */
    public static byte[] unGzip(byte[] buf) throws InternalException {
        return unGzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Gzip解压处理
     *
     * @param in Gzip数据
     * @return 解压后的数据
     * @throws InternalException IO异常
     */
    public static byte[] unGzip(InputStream in) throws InternalException {
        return unGzip(in, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * Gzip解压处理
     *
     * @param in     Gzip数据
     * @param length 估算长度,如果无法确定请传入{@link #DEFAULT_BYTE_ARRAY_LENGTH}
     * @return 解压后的数据
     * @throws InternalException IO异常
     */
    public static byte[] unGzip(InputStream in, int length) throws InternalException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        Gzip.of(in, bos).unGzip().close();
        return bos.toByteArray();
    }

    /**
     * Zlib压缩处理
     *
     * @param content 被压缩的字符串
     * @param charset 编码
     * @param level   压缩级别,1~9
     * @return 压缩后的字节流
     */
    public static byte[] zlib(String content, String charset, int level) {
        return zlib(StringKit.bytes(content, charset), level);
    }

    /**
     * Zlib压缩文件
     *
     * @param file  被压缩的文件
     * @param level 压缩级别
     * @return 压缩后的字节流
     */
    public static byte[] zlib(File file, int level) {
        BufferedInputStream in = null;
        try {
            in = FileKit.getInputStream(file);
            return zlib(in, level, (int) file.length());
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * 打成Zlib压缩包
     *
     * @param buf   数据
     * @param level 压缩级别,0~9
     * @return 压缩后的bytes
     */
    public static byte[] zlib(byte[] buf, int level) {
        return zlib(new ByteArrayInputStream(buf), level, buf.length);
    }

    /**
     * 打成Zlib压缩包
     *
     * @param in    数据流
     * @param level 压缩级别,0~9
     * @return 压缩后的bytes
     */
    public static byte[] zlib(InputStream in, int level) {
        return zlib(in, level, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * 打成Zlib压缩包
     *
     * @param in     数据流
     * @param level  压缩级别,0~9
     * @param length 预估大小
     * @return 压缩后的bytes
     */
    public static byte[] zlib(InputStream in, int level, int length) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        Deflate.of(in, out, false).deflater(level);
        return out.toByteArray();
    }

    /**
     * Zlib解压缩处理
     *
     * @param buf     压缩过的字节流
     * @param charset 编码
     * @return 解压后的字符串
     */
    public static String unZlib(byte[] buf, String charset) {
        return StringKit.toString(unZlib(buf), charset);
    }

    /**
     * 解压缩zlib
     *
     * @param buf 数据
     * @return 解压后的bytes
     */
    public static byte[] unZlib(byte[] buf) {
        return unZlib(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * 解压缩zlib
     *
     * @param in 数据流
     * @return 解压后的bytes
     */
    public static byte[] unZlib(InputStream in) {
        return unZlib(in, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * 解压缩zlib
     *
     * @param in     数据流
     * @param length 预估长度
     * @return 解压后的bytes
     */
    public static byte[] unZlib(InputStream in, int length) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        Deflate.of(in, out, false).inflater();
        return out.toByteArray();
    }

    /**
     * 获取压缩包中的指定文件流
     *
     * @param zipFile 压缩文件
     * @param charset 编码
     * @param path    需要提取文件的文件名或路径
     * @return 压缩文件流，如果未找到返回{@code null}
     */
    public static InputStream get(File zipFile, Charset charset, String path) {
        return get(zipFile(zipFile, charset), path);
    }

    /**
     * 获取压缩包中的指定文件流
     *
     * @param zipFile 压缩文件
     * @param path    需要提取文件的文件名或路径
     * @return 压缩文件流，如果未找到返回{@code null}
     */
    public static InputStream get(ZipFile zipFile, String path) {
        final ZipEntry entry = zipFile.getEntry(path);
        if (null != entry) {
            return get(zipFile, entry);
        }
        return null;
    }

    /**
     * 获取指定{@link ZipEntry}的流，用于读取这个entry的内容
     *
     * @param zipFile  {@link ZipFile}
     * @param zipEntry {@link ZipEntry}
     * @return 流
     */
    public static InputStream get(ZipFile zipFile, ZipEntry zipEntry) {
        try {
            return zipFile.getInputStream(zipEntry);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取并处理Zip文件中的每一个{@link ZipEntry}
     *
     * @param zipFile  Zip文件
     * @param consumer {@link ZipEntry}处理器
     */
    public static void get(ZipFile zipFile, Consumer<ZipEntry> consumer) {
        try (final ZipReader reader = new ZipReader(zipFile)) {
            reader.read(consumer);
        }
    }

    /**
     * 读取并处理Zip流中的每一个{@link ZipEntry}
     *
     * @param zipStream zip文件流，包含编码信息
     * @param consumer  {@link ZipEntry}处理器
     */
    public static void get(ZipInputStream zipStream, Consumer<ZipEntry> consumer) {
        try (final ZipReader reader = new ZipReader(zipStream)) {
            reader.read(consumer);
        }
    }

    /**
     * 获得 {@link ZipOutputStream}
     *
     * @param out     压缩文件
     * @param charset 编码
     * @return {@link ZipOutputStream}
     */
    private static ZipOutputStream get(OutputStream out, Charset charset) {
        if (out instanceof ZipOutputStream) {
            return (ZipOutputStream) out;
        }
        return new ZipOutputStream(out, ObjectKit.defaultIfNull(charset, DEFAULT_CHARSET));
    }

    /**
     * 将Zip文件转换为{@link ZipFile}
     *
     * @param file    zip文件
     * @param charset 解析zip文件的编码，null表示{@link org.miaixz.bus.core.lang.Charset#UTF_8}
     * @return {@link ZipFile}
     */
    public static ZipFile zipFile(File file, Charset charset) {
        try {
            return new ZipFile(file, ObjectKit.defaultIfNull(charset, org.miaixz.bus.core.lang.Charset.UTF_8));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取Zip文件中指定目录下的所有文件，只显示文件，不显示目录
     * 此方法并不会关闭{@link ZipFile}
     *
     * @param zipFile Zip文件
     * @param dir     目录前缀(目录前缀不包含开头的/)
     * @return 文件列表
     */
    public static List<String> listFileNames(ZipFile zipFile, String dir) {
        if (StringKit.isNotBlank(dir)) {
            // 目录尾部添加"/"
            dir = StringKit.addSuffixIfNot(dir, Symbol.SLASH);
        }

        final List<String> fileNames = new ArrayList<>();
        String name;
        for (ZipEntry entry : new EnumerationIterator<>(zipFile.entries())) {
            name = entry.getName();
            if (StringKit.isEmpty(dir) || name.startsWith(dir)) {
                final String nameSuffix = StringKit.removePrefix(name, dir);
                if (StringKit.isNotEmpty(nameSuffix) && false == StringKit.contains(nameSuffix, Symbol.SLASH)) {
                    fileNames.add(nameSuffix);
                }
            }
        }
        return fileNames;
    }

    /**
     * 在zip文件中添加新文件或目录
     * 新文件添加在zip根目录，文件夹包括其本身和内容
     * 如果待添加文件夹是系统根路径（如/或c:/），则只复制文件夹下的内容
     *
     * @param zipPath        zip文件的Path
     * @param appendFilePath 待添加文件Path(可以是文件夹)
     * @param options        拷贝选项，可选是否覆盖等
     * @throws IOException IO异常
     */
    public static void append(Path zipPath, Path appendFilePath, CopyOption... options) throws IOException {
        try (FileSystem zipFileSystem = createZip(zipPath.toString())) {
            if (Files.isDirectory(appendFilePath)) {
                Path source = appendFilePath.getParent();
                if (null == source) {
                    // 如果用户提供的是根路径，则不复制目录，直接复制目录下的内容
                    source = appendFilePath;
                }
                Files.walkFileTree(appendFilePath, new ZipCopyVisitor(source, zipFileSystem, options));
            } else {
                Files.copy(appendFilePath, zipFileSystem.getPath(FileKit.getName(appendFilePath)), options);
            }
        } catch (FileAlreadyExistsException ignored) {
            // 不覆盖情况下，文件已存在, 跳过
        }
    }

    /**
     * 创建 {@link FileSystem}
     *
     * @param path 文件路径，可以是目录或Zip文件等
     * @return {@link FileSystem}
     */
    public static FileSystem create(String path) throws IOException {
        return FileSystems.newFileSystem(
                Paths.get(path).toUri(),
                MapKit.of("create", "true"));
    }

    /**
     * 创建 Zip的{@link FileSystem}，默认UTF-8编码
     *
     * @param path 文件路径，可以是目录或Zip文件等
     * @return {@link FileSystem}
     */
    public static FileSystem createZip(String path) {
        return createZip(path, null);
    }

    /**
     * 创建 Zip的{@link FileSystem}
     *
     * @param path    文件路径，可以是目录或Zip文件等
     * @param charset 编码
     * @return {@link FileSystem}
     */
    public static FileSystem createZip(String path, Charset charset) {
        if (null == charset) {
            charset = org.miaixz.bus.core.lang.Charset.UTF_8;
        }
        final HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", charset.name());

        try {
            return FileSystems.newFileSystem(
                    URI.create("jar:" + Paths.get(path).toUri()), env);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 判断压缩文件保存的路径是否为源文件路径的子文件夹,如果是,则抛出异常(防止无限递归压缩的发生)
     *
     * @param zipFile  压缩后的产生的文件路径
     * @param srcFiles 被压缩的文件或目录
     */
    private static void validateFiles(File zipFile, File... srcFiles) throws InternalException {
        if (zipFile.isDirectory()) {
            throw new InternalException("Zip file [{}] must not be a directory !", zipFile.getAbsoluteFile());
        }

        for (File srcFile : srcFiles) {
            if (null == srcFile) {
                continue;
            }
            if (false == srcFile.exists()) {
                throw new InternalException(StringKit.format("File [{}] not exist!", srcFile.getAbsolutePath()));
            }

            // 当 zipFile =  new File("temp.zip") 时, zipFile.getParentFile() == null
            File parentFile;
            try {
                parentFile = zipFile.getCanonicalFile().getParentFile();
            } catch (IOException e) {
                parentFile = zipFile.getParentFile();
            }

            // 压缩文件不能位于被压缩的目录内
            if (srcFile.isDirectory() && FileKit.isSub(srcFile, parentFile)) {
                throw new InternalException("Zip file path [{}] must not be the child directory of [{}] !", zipFile.getPath(), srcFile.getPath());
            }
        }
    }

    /**
     * 从Zip中读取文件流并写出到文件
     *
     * @param zipFile     Zip文件
     * @param zipEntry    zip文件中的子文件
     * @param outItemFile 输出到的文件
     * @throws InternalException IO异常
     */
    private static void write(ZipFile zipFile, ZipEntry zipEntry, File outItemFile) throws InternalException {
        FileKit.writeFromStream(get(zipFile, zipEntry), outItemFile);
    }

}
