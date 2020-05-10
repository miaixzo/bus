/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.core.utils;

import org.aoju.bus.core.io.streams.ByteArrayOutputStream;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.*;

/**
 * 压缩工具类
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public class ZipUtils {

    private static final int DEFAULT_BYTE_ARRAY_LENGTH = 32;

    /**
     * 默认编码,使用平台相关编码
     */
    private static final Charset DEFAULT_CHARSET = CharsetUtils.defaultCharset();

    /**
     * 打包到当前目录,使用默认编码UTF-8
     *
     * @param srcPath 源文件路径
     * @return 打包好的压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(String srcPath) throws InstrumentException {
        return zip(srcPath, DEFAULT_CHARSET);
    }

    /**
     * 打包到当前目录
     *
     * @param srcPath 源文件路径
     * @param charset 编码
     * @return 打包好的压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(String srcPath, Charset charset) throws InstrumentException {
        return zip(FileUtils.file(srcPath), charset);
    }

    /**
     * 打包到当前目录,使用默认编码UTF-8
     *
     * @param srcFile 源文件或目录
     * @return 打包好的压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(File srcFile) throws InstrumentException {
        return zip(srcFile, DEFAULT_CHARSET);
    }

    /**
     * 打包到当前目录
     *
     * @param srcFile 源文件或目录
     * @param charset 编码
     * @return 打包好的压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(File srcFile, Charset charset) throws InstrumentException {
        final File zipFile = FileUtils.file(srcFile.getParentFile(), FileUtils.mainName(srcFile) + ".zip");
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
     * @throws InstrumentException IO异常
     */
    public static File zip(String srcPath, String zipPath) throws InstrumentException {
        return zip(srcPath, zipPath, false);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param srcPath    要压缩的源文件路径 如果压缩一个文件,则为该文件的全路径；如果压缩一个目录,则为该目录的顶层目录路径
     * @param zipPath    压缩文件保存的路径,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param withSrcDir 是否包含被打包目录
     * @return 压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(String srcPath, String zipPath, boolean withSrcDir) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public static File zip(String srcPath, String zipPath, Charset charset, boolean withSrcDir) throws InstrumentException {
        final File srcFile = FileUtils.file(srcPath);
        final File zipFile = FileUtils.file(zipPath);
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
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, boolean withSrcDir, File... srcFiles) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, Charset charset, boolean withSrcDir, File... srcFiles) throws InstrumentException {
        validateFiles(zipFile, srcFiles);

        try (ZipOutputStream out = getZipOutputStream(zipFile, charset)) {
            String srcRootDir;
            for (File srcFile : srcFiles) {
                if (null == srcFile) {
                    continue;
                }
                // 如果只是压缩一个文件,则需要截取该文件的父目录
                srcRootDir = srcFile.getCanonicalPath();
                if (srcFile.isFile() || withSrcDir) {
                    //若是文件,则将父目录完整路径都截取掉；若设置包含目录,则将上级目录全部截取掉,保留本目录名
                    srcRootDir = srcFile.getCanonicalFile().getParentFile().getCanonicalPath();
                }
                // 调用递归压缩方法进行目录或文件压缩
                zip(srcFile, srcRootDir, out);
                out.flush();
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return zipFile;
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param out        生成的Zip到的目标流，包括文件名。注意：zipPath不能是srcPath路径下的子文件夹
     * @param charset    编码
     * @param withSrcDir 是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩）
     * @param srcFiles   要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     */
    public static void zip(OutputStream out, Charset charset, boolean withSrcDir, FileFilter filter, File... srcFiles) {
        zip(getZipOutputStream(out, charset), withSrcDir, filter, srcFiles);
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param zipOutputStream 生成的Zip到的目标流，不关闭此流
     * @param withSrcDir      是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter          文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩）
     * @param srcFiles        要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     */
    public static void zip(ZipOutputStream zipOutputStream, boolean withSrcDir, FileFilter filter, File... srcFiles) {
        String srcRootDir;
        try {
            for (File srcFile : srcFiles) {
                if (null == srcFile) {
                    continue;
                }
                // 如果只是压缩一个文件，则需要截取该文件的父目录
                srcRootDir = srcFile.getCanonicalPath();
                if (srcFile.isFile() || withSrcDir) {
                    // 若是文件，则将父目录完整路径都截取掉；若设置包含目录，则将上级目录全部截取掉，保留本目录名
                    srcRootDir = srcFile.getCanonicalFile().getParentFile().getCanonicalPath();
                }
                // 调用递归压缩方法进行目录或文件压缩
                zip(srcFile, srcRootDir, zipOutputStream, filter);
                zipOutputStream.flush();
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 递归压缩文件夹
     * srcRootDir决定了路径截取的位置
     *
     * @param out        压缩文件存储对象
     * @param srcRootDir 被压缩的文件夹根目录
     * @param file       当前递归压缩的文件或目录对象
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩）
     */
    private static void zip(File file, String srcRootDir, ZipOutputStream out, FileFilter filter) {
        if (null == file || (null != filter && false == filter.accept(file))) {
            return;
        }
        // 获取文件相对于压缩文件夹根目录的子路径
        final String subPath = FileUtils.subPath(srcRootDir, file);
        // 如果是目录，则压缩压缩目录中的文件或子目录
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (ArrayUtils.isEmpty(files) && StringUtils.isNotEmpty(subPath)) {
                // 加入目录，只有空目录时才加入目录，非空时会在创建文件时自动添加父级目录
                addDir(subPath, out);
            }
            // 压缩目录下的子文件或目录
            for (File childFile : files) {
                zip(childFile, srcRootDir, out, filter);
            }
        } else {
            // 如果是文件或其它符号，则直接压缩该文件
            addFile(file, subPath, out);
        }
    }

    /**
     * 对流中的数据加入到压缩文件,使用默认UTF-8编码
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param data    要压缩的数据
     * @return 压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String path, String data) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String path, String data, Charset charset) throws InstrumentException {
        return zip(zipFile, path, IoUtils.toStream(data, charset), charset);
    }

    /**
     * 对流中的数据加入到压缩文件
     * 使用默认编码UTF-8
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param path    流数据在压缩文件中的路径或文件名
     * @param in      要压缩的源
     * @return 压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String path, InputStream in) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String path, InputStream in, Charset charset) throws InstrumentException {
        return zip(zipFile, new String[]{path}, new InputStream[]{in}, charset);
    }

    /**
     * 对流中的数据加入到压缩文件
     * 路径列表和流列表长度必须一致
     *
     * @param zipFile 生成的Zip文件,包括文件名 注意：zipPath不能是srcPath路径下的子文件夹
     * @param paths   流数据在压缩文件中的路径或文件名
     * @param ins     要压缩的源
     * @return 压缩文件
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String[] paths, InputStream[] ins) throws InstrumentException {
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
     * @throws InstrumentException IO异常
     */
    public static File zip(File zipFile, String[] paths, InputStream[] ins, Charset charset) throws InstrumentException {
        if (ArrayUtils.isEmpty(paths) || ArrayUtils.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        ZipOutputStream out = null;
        try {
            out = getZipOutputStream(zipFile, charset);
            for (int i = 0; i < paths.length; i++) {
                addFile(ins[i], paths[i], out);
            }
        } finally {
            IoUtils.close(out);
        }
        return zipFile;
    }

    /**
     * 解压到文件名相同的目录中,默认编码UTF-8
     *
     * @param zipFilePath 压缩文件路径
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(String zipFilePath) throws InstrumentException {
        return unzip(zipFilePath, DEFAULT_CHARSET);
    }

    /**
     * 解压到文件名相同的目录中
     *
     * @param zipFilePath 压缩文件路径
     * @param charset     编码
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(String zipFilePath, Charset charset) throws InstrumentException {
        return unzip(FileUtils.file(zipFilePath), charset);
    }

    /**
     * 解压到文件名相同的目录中,使用UTF-8编码
     *
     * @param zipFile 压缩文件
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(File zipFile) throws InstrumentException {
        return unzip(zipFile, DEFAULT_CHARSET);
    }

    /**
     * 解压到文件名相同的目录中
     *
     * @param zipFile 压缩文件
     * @param charset 编码
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(File zipFile, Charset charset) throws InstrumentException {
        return unzip(zipFile, FileUtils.file(zipFile.getParentFile(), FileUtils.mainName(zipFile)), charset);
    }

    /**
     * 解压,默认UTF-8编码
     *
     * @param zipFilePath 压缩文件的路径
     * @param outFileDir  解压到的目录
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(String zipFilePath, String outFileDir) throws InstrumentException {
        return unzip(zipFilePath, outFileDir, DEFAULT_CHARSET);
    }

    /**
     * 解压
     *
     * @param zipFilePath 压缩文件的路径
     * @param outFileDir  解压到的目录
     * @param charset     编码
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(String zipFilePath, String outFileDir, Charset charset) throws InstrumentException {
        return unzip(FileUtils.file(zipFilePath), FileUtils.mkdir(outFileDir), charset);
    }

    /**
     * 解压,默认使用UTF-8编码
     *
     * @param zipFile zip文件
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(File zipFile, File outFile) throws InstrumentException {
        return unzip(zipFile, outFile, DEFAULT_CHARSET);
    }

    /**
     * 解压
     *
     * @param zipFile zip文件
     * @param outFile 解压到的目录
     * @param charset 编码
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(File zipFile, File outFile, Charset charset) throws InstrumentException {
        charset = (null == charset) ? DEFAULT_CHARSET : charset;

        ZipFile zipFileObj = null;
        try {
            zipFileObj = new ZipFile(zipFile, charset);
            final Enumeration<ZipEntry> em = (Enumeration<ZipEntry>) zipFileObj.entries();
            while (em.hasMoreElements()) {
                ZipEntry zipEntry = em.nextElement();
                File outItemFile = FileUtils.file(outFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    outItemFile.mkdirs();
                } else {
                    FileUtils.touch(outItemFile);
                    copy(zipFileObj, zipEntry, outItemFile);
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(zipFileObj);
        }
        return outFile;
    }

    /**
     * 解压
     *
     * @param zipFile zip文件,附带编码信息,使用完毕自动关闭
     * @param outFile 解压到的目录
     * @return 解压的目录
     * @throws InstrumentException IO异常
     */
    public static File unzip(ZipFile zipFile, File outFile) {
        try {
            final Enumeration<ZipEntry> em = (Enumeration<ZipEntry>) zipFile.entries();
            ZipEntry zipEntry;
            File outItemFile;
            while (em.hasMoreElements()) {
                zipEntry = em.nextElement();
                outItemFile = buildFile(outFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    // 创建对应目录
                    outItemFile.mkdirs();
                } else {
                    // 写出文件
                    write(zipFile, zipEntry, outItemFile);
                }
            }
        } finally {
            IoUtils.close(zipFile);
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
    public static File unzip(InputStream in, File outFile, Charset charset) {
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
        try {
            ZipEntry zipEntry;
            File outItemFile;
            while (null != (zipEntry = zipStream.getNextEntry())) {
                // FileUtils.file会检查slip漏洞,漏洞说明见http://blog.nsfocus.net/zip-slip-2/
                outItemFile = FileUtils.file(outFile, zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    // 目录
                    outItemFile.mkdirs();
                } else {
                    // 文件
                    FileUtils.writeFromStream(zipStream, outItemFile);
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(zipStream);
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
    public static byte[] unzipFileBytes(String zipFilePath, Charset charset, String name) {
        return unzipFileBytes(FileUtils.file(zipFilePath), charset, name);
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
    public static byte[] unzipFileBytes(File zipFile, Charset charset, String name) {
        ZipFile zipFileObj = null;
        try {
            zipFileObj = new ZipFile(zipFile, charset);
            final Enumeration<ZipEntry> em = (Enumeration<ZipEntry>) zipFileObj.entries();
            ZipEntry zipEntry;
            while (em.hasMoreElements()) {
                zipEntry = em.nextElement();
                if (zipEntry.isDirectory()) {
                    continue;
                } else if (name.equals(zipEntry.getName())) {
                    return IoUtils.readBytes(zipFileObj.getInputStream(zipEntry));
                }
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(zipFileObj);
        }
        return null;
    }

    /**
     * Gzip压缩处理
     *
     * @param content 被压缩的字符串
     * @param charset 编码
     * @return 压缩后的字节流
     * @throws InstrumentException IO异常
     */
    public static byte[] gzip(String content, String charset) throws InstrumentException {
        return gzip(StringUtils.bytes(content, charset));
    }

    /**
     * Gzip压缩处理
     *
     * @param buf 被压缩的字节流
     * @return 压缩后的字节流
     * @throws InstrumentException IO异常
     */
    public static byte[] gzip(byte[] buf) throws InstrumentException {
        return gzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Gzip压缩文件
     *
     * @param file 被压缩的文件
     * @return 压缩后的字节流
     * @throws InstrumentException IO异常
     */
    public static byte[] gzip(File file) throws InstrumentException {
        BufferedInputStream in = null;
        try {
            in = FileUtils.getInputStream(file);
            return gzip(in, (int) file.length());
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * Gzip压缩文件
     *
     * @param in 被压缩的流
     * @return 压缩后的字节流
     * @throws InstrumentException IO异常
     */
    public static byte[] gzip(InputStream in) throws InstrumentException {
        return gzip(in, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * Gzip压缩文件
     *
     * @param in     被压缩的流
     * @param length 预估长度
     * @return 压缩后的字节流
     * @throws InstrumentException IO异常
     */
    public static byte[] gzip(InputStream in, int length) throws InstrumentException {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(length);
        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(bos);
            IoUtils.copy(in, gos);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(gos);
        }
        //返回必须在关闭gos后进行,因为关闭时会自动执行finish()方法,保证数据全部写出
        return bos.toByteArray();
    }

    /**
     * Gzip解压缩处理
     *
     * @param buf     压缩过的字节流
     * @param charset 编码
     * @return 解压后的字符串
     * @throws InstrumentException IO异常
     */
    public static String unGzip(byte[] buf, String charset) throws InstrumentException {
        return StringUtils.toString(unGzip(buf), charset);
    }

    /**
     * Gzip解压处理
     *
     * @param buf buf
     * @return bytes
     * @throws InstrumentException IO异常
     */
    public static byte[] unGzip(byte[] buf) throws InstrumentException {
        return unGzip(new ByteArrayInputStream(buf), buf.length);
    }

    /**
     * Gzip解压处理
     *
     * @param in Gzip数据
     * @return 解压后的数据
     * @throws InstrumentException IO异常
     */
    public static byte[] unGzip(InputStream in) throws InstrumentException {
        return unGzip(in, DEFAULT_BYTE_ARRAY_LENGTH);
    }

    /**
     * Gzip解压处理
     *
     * @param in     Gzip数据
     * @param length 估算长度,如果无法确定请传入{@link #DEFAULT_BYTE_ARRAY_LENGTH}
     * @return 解压后的数据
     * @throws InstrumentException IO异常
     */
    public static byte[] unGzip(InputStream in, int length) throws InstrumentException {
        GZIPInputStream gzi = null;
        ByteArrayOutputStream bos;
        try {
            gzi = (in instanceof GZIPInputStream) ? (GZIPInputStream) in : new GZIPInputStream(in);
            bos = new ByteArrayOutputStream(length);
            IoUtils.copy(gzi, bos);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(gzi);
        }
        //返回必须在关闭gos后进行,因为关闭时会自动执行finish()方法,保证数据全部写出
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
        return zlib(StringUtils.bytes(content, charset), level);
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
            in = FileUtils.getInputStream(file);
            return zlib(in, level, (int) file.length());
        } finally {
            IoUtils.close(in);
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
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(length);
        deflater(in, out, level);
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
        return StringUtils.toString(unZlib(buf), charset);
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
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(length);
        inflater(in, out);
        return out.toByteArray();
    }

    /**
     * 获取Zip文件中指定目录下的所有文件，只显示文件，不显示目录
     *
     * @param zipFile Zip文件
     * @param dir     目录前缀（目录前缀不包含开头的/）
     * @return 文件列表
     */
    public static List<String> listFileNames(ZipFile zipFile, String dir) {
        if (StringUtils.isNotBlank(dir)) {
            // 目录尾部添加"/"
            dir = StringUtils.addSuffixIfNot(dir, Symbol.SLASH);
        }

        final List<String> fileNames = new ArrayList<>();
        String name;
        for (ZipEntry entry : Collections.list(zipFile.entries())) {
            name = entry.getName();
            if (StringUtils.isEmpty(dir) || name.startsWith(dir)) {
                final String nameSuffix = StringUtils.removePrefix(name, dir);
                if (StringUtils.isNotEmpty(nameSuffix) && false == StringUtils.contains(nameSuffix, Symbol.SLASH)) {
                    fileNames.add(nameSuffix);
                }
            }
        }
        return fileNames;
    }

    /**
     * 获得 {@link ZipOutputStream}
     *
     * @param zipFile 压缩文件
     * @param charset 编码
     * @return {@link ZipOutputStream}
     */
    private static ZipOutputStream getZipOutputStream(File zipFile, Charset charset) {
        return getZipOutputStream(FileUtils.getOutputStream(zipFile), charset);
    }

    /**
     * 获得 {@link ZipOutputStream}
     *
     * @param out     压缩文件
     * @param charset 编码
     * @return {@link ZipOutputStream}
     */
    private static ZipOutputStream getZipOutputStream(OutputStream out, Charset charset) {
        charset = (null == charset) ? org.aoju.bus.core.lang.Charset.DEFAULT : charset;
        return new ZipOutputStream(out, charset);
    }

    /**
     * 递归压缩文件夹
     * srcRootDir决定了路径截取的位置,例如：
     * file的路径为/data/a/b/c/d.txt,srcRootDir为/data/a/b,则压缩后的文件与目录为结构为c/d.txt
     *
     * @param out        压缩文件存储对象
     * @param srcRootDir 被压缩的文件夹根目录
     * @param file       当前递归压缩的文件或目录对象
     * @throws InstrumentException IO异常
     */
    private static void zip(File file, String srcRootDir, ZipOutputStream out) throws InstrumentException {
        if (file == null) {
            return;
        }

        final String subPath = FileUtils.subPath(srcRootDir, file); // 获取文件相对于压缩文件夹根目录的子路径
        if (file.isDirectory()) {// 如果是目录,则压缩压缩目录中的文件或子目录
            final File[] files = file.listFiles();
            if (ArrayUtils.isEmpty(files) && StringUtils.isNotEmpty(subPath)) {
                // 加入目录,只有空目录时才加入目录,非空时会在创建文件时自动添加父级目录
                addDir(subPath, out);
            }
            // 压缩目录下的子文件或目录
            for (File childFile : files) {
                zip(childFile, srcRootDir, out);
            }
        } else {// 如果是文件或其它符号,则直接压缩该文件
            addFile(file, subPath, out);
        }
    }

    /**
     * 添加文件到压缩包
     *
     * @param file 需要压缩的文件
     * @param path 在压缩文件中的路径
     * @param out  压缩文件存储对象
     * @throws InstrumentException IO异常
     */
    private static void addFile(File file, String path, ZipOutputStream out) throws InstrumentException {
        BufferedInputStream in = null;
        try {
            in = FileUtils.getInputStream(file);
            addFile(in, path, out);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 添加文件流到压缩包,不关闭输入流
     *
     * @param in   需要压缩的输入流
     * @param path 压缩的路径
     * @param out  压缩文件存储对象
     * @throws InstrumentException IO异常
     */
    private static void addFile(InputStream in, String path, ZipOutputStream out) throws InstrumentException {
        if (null == in) {
            return;
        }
        try {
            out.putNextEntry(new ZipEntry(path));
            IoUtils.copy(in, out);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            closeEntry(out);
        }
    }

    /**
     * 在压缩包中新建目录
     *
     * @param path 压缩的路径
     * @param out  压缩文件存储对象
     * @throws InstrumentException IO异常
     */
    private static void addDir(String path, ZipOutputStream out) throws InstrumentException {
        path = StringUtils.addSuffixIfNot(path, Symbol.SLASH);
        try {
            out.putNextEntry(new ZipEntry(path));
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            closeEntry(out);
        }
    }

    /**
     * 判断压缩文件保存的路径是否为源文件路径的子文件夹,如果是,则抛出异常（防止无限递归压缩的发生）
     *
     * @param zipFile  压缩后的产生的文件路径
     * @param srcFiles 被压缩的文件或目录
     */
    private static void validateFiles(File zipFile, File... srcFiles) throws InstrumentException {
        if (zipFile.isDirectory()) {
            throw new InstrumentException("Zip file [{}] must not be a directory !", zipFile.getAbsoluteFile());
        }

        for (File srcFile : srcFiles) {
            if (null == srcFile) {
                continue;
            }
            if (false == srcFile.exists()) {
                throw new InstrumentException(StringUtils.format("File [{}] not exist!", srcFile.getAbsolutePath()));
            }

            try {
                final File parentFile = zipFile.getCanonicalFile().getParentFile();
                // 压缩文件不能位于被压缩的目录内
                if (srcFile.isDirectory() && parentFile.getCanonicalPath().contains(srcFile.getCanonicalPath())) {
                    throw new InstrumentException("Zip file path [{}] must not be the child directory of [{}] !", zipFile.getCanonicalPath(), srcFile.getCanonicalPath());
                }

            } catch (IOException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 关闭当前Entry,继续下一个Entry
     *
     * @param out ZipOutputStream
     */
    private static void closeEntry(ZipOutputStream out) {
        try {
            out.closeEntry();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * 从Zip文件流中拷贝文件出来
     *
     * @param zipFile     Zip文件
     * @param zipEntry    zip文件中的子文件
     * @param outItemFile 输出到的文件
     * @throws IOException IO异常
     */
    private static void copy(ZipFile zipFile, ZipEntry zipEntry, File outItemFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = zipFile.getInputStream(zipEntry);
            out = FileUtils.getOutputStream(outItemFile);
            IoUtils.copy(in, out);
        } finally {
            IoUtils.close(out);
            IoUtils.close(in);
        }
    }

    /**
     * 将Zlib流解压到out中
     *
     * @param in  zlib数据流
     * @param out 输出
     */
    private static void inflater(InputStream in, OutputStream out) {
        final InflaterOutputStream ios = (out instanceof InflaterOutputStream) ? (InflaterOutputStream) out : new InflaterOutputStream(out, new Inflater(true));
        IoUtils.copy(in, ios);
        try {
            ios.finish();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 将普通数据流压缩成zlib到out中
     *
     * @param in    zlib数据流
     * @param out   输出
     * @param level 压缩级别,0~9
     */
    private static void deflater(InputStream in, OutputStream out, int level) {
        final DeflaterOutputStream ios = (out instanceof DeflaterOutputStream) ? (DeflaterOutputStream) out : new DeflaterOutputStream(out, new Deflater(level, true));
        IoUtils.copy(in, ios);
        try {
            ios.finish();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 根据压缩包中的路径构建目录结构,在Win下直接构建,在Linux下拆分路径单独构建
     *
     * @param outFile  最外部路径
     * @param fileName 文件名,可以包含路径
     * @return 文件或目录
     */
    private static File buildFile(File outFile, String fileName) {
        if (false == FileUtils.isWindows()
                // 检查文件名中是否包含"/"，不考虑以"/"结尾的情况
                && fileName.lastIndexOf(Symbol.SLASH, fileName.length() - 2) > 0) {
            // 在Linux下多层目录创建存在问题,/会被当成文件名的一部分,此处做处理
            // 使用/拆分路径（zip中无\）,级联创建父目录
            final String[] pathParts = StringUtils.splitToArray(fileName, Symbol.C_SLASH);
            for (int i = 0; i < pathParts.length - 1; i++) {
                //由于路径拆分,slip不检查,在最后一步检查
                outFile = new File(outFile, pathParts[i]);
            }
            //noinspection ResultOfMethodCallIgnored
            outFile.mkdirs();
            // 最后一个部分作为文件名
            fileName = pathParts[pathParts.length - 1];
        }
        return FileUtils.file(outFile, fileName);
    }

    /**
     * 从Zip中读取文件流并写出到文件
     *
     * @param zipFile     Zip文件
     * @param zipEntry    zip文件中的子文件
     * @param outItemFile 输出到的文件
     */
    private static void write(ZipFile zipFile, ZipEntry zipEntry, File outItemFile) {
        InputStream in = null;
        try {
            in = zipFile.getInputStream(zipEntry);
            FileUtils.writeFromStream(in, outItemFile);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
        }
    }

}
