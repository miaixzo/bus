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
package org.aoju.bus.core.utils;

import org.aoju.bus.core.io.BOMInputStream;
import org.aoju.bus.core.io.LineHandler;
import org.aoju.bus.core.io.file.FileCopier;
import org.aoju.bus.core.io.file.FileReader;
import org.aoju.bus.core.io.file.FileWriter;
import org.aoju.bus.core.io.file.LineSeparator;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.FileType;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * 文件工具类
 *
 * @author Kimi Liu
 * @version 5.3.5
 * @since JDK 1.8+
 */
public class FileUtils {

    /**
     * Windows下文件名中的无效字符
     */
    private static Pattern FILE_NAME_INVALID_PATTERN_WIN = Pattern.compile("[\\\\/:*?\"<>|]");

    /**
     * 是否为Windows环境
     *
     * @return 是否为Windows环境
     * @since 3.1.9
     */
    public static boolean isWindows() {
        return Symbol.C_BACKSLASH == File.separatorChar;
    }

    /**
     * 读取文件
     *
     * @param file 文件
     * @return 内容
     */
    public static String readFile(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new java.io.FileReader(file));
            String tempString = null;
            String all = "";
            // 一次读入一行,直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                all += tempString;
            }
            reader.close();
            return all;
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    /**
     * 列出目录文件
     * 给定的绝对路径不能是压缩包中的路径
     *
     * @param path 目录绝对路径或者相对路径
     * @return 文件列表（包含目录）
     */
    public static File[] ls(String path) {
        if (path == null) {
            return null;
        }

        path = getAbsolutePath(path);

        File file = file(path);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        throw new InstrumentException(StringUtils.format("Path [{}] is not directory!", path));
    }

    /**
     * 文件是否为空
     * 目录：里面没有文件时为空 文件：文件大小为0时为空
     *
     * @param file 文件
     * @return 是否为空, 当提供非目录时, 返回false
     */
    public static boolean isEmpty(File file) {
        if (null == file) {
            return true;
        }

        if (file.isDirectory()) {
            String[] subFiles = file.list();
            return ArrayUtils.isEmpty(subFiles);
        } else if (file.isFile()) {
            return file.length() <= 0;
        }

        return false;
    }

    /**
     * 目录是否为空
     *
     * @param file 目录
     * @return 是否为空, 当提供非目录时, 返回false
     */
    public static boolean isNotEmpty(File file) {
        return false == isEmpty(file);
    }

    /**
     * 目录是否为空
     *
     * @param dirPath 目录
     * @return 是否为空
     * @throws InstrumentException IOException
     */
    public static boolean isDirEmpty(Path dirPath) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
            return false == dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 目录是否为空
     *
     * @param dir 目录
     * @return 是否为空
     */
    public static boolean isDirEmpty(File dir) {
        return isDirEmpty(dir.toPath());
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     * 如果提供file为文件,直接返回过滤结果
     *
     * @param path       当前遍历文件或目录的路径
     * @param fileFilter 文件过滤规则对象,选择要保留的文件,只对文件有效,不过滤目录
     * @return 文件列表
     * @since 5.3.5
     */
    public static List<File> loopFiles(String path, FileFilter fileFilter) {
        return loopFiles(file(path), fileFilter);
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     * 如果提供file为文件,直接返回过滤结果
     *
     * @param file       当前遍历文件或目录
     * @param fileFilter 文件过滤规则对象,选择要保留的文件,只对文件有效,不过滤目录
     * @return 文件列表
     */
    public static List<File> loopFiles(File file, FileFilter fileFilter) {
        List<File> fileList = new ArrayList<File>();
        if (null == file) {
            return fileList;
        } else if (false == file.exists()) {
            return fileList;
        }

        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayUtils.isNotEmpty(subFiles)) {
                for (File tmp : subFiles) {
                    fileList.addAll(loopFiles(tmp, fileFilter));
                }
            }
        } else {
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
        }

        return fileList;
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     *
     * @param path 当前遍历文件或目录的路径
     * @return 文件列表
     * @since 5.3.5
     */
    public static List<File> loopFiles(String path) {
        return loopFiles(file(path));
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     *
     * @param file 当前遍历文件
     * @return 文件列表
     */
    public static List<File> loopFiles(File file) {
        return loopFiles(file, null);
    }

    /**
     * 获得指定目录下所有文件
     * 不会扫描子目录
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 文件路径列表（如果是jar中的文件,则给定类似.jar!/xxx/xxx的路径）
     * @throws InstrumentException 异常
     */
    public static List<String> listFileNames(String path) throws InstrumentException {
        if (path == null) {
            return null;
        }
        List<String> paths = new ArrayList<String>();

        int index = path.lastIndexOf(FileType.JAR_PATH_EXT);
        if (index == -1) {
            // 普通目录路径
            File[] files = ls(path);
            for (File file : files) {
                if (file.isFile()) {
                    paths.add(file.getName());
                }
            }
        } else {
            // jar文件
            path = getAbsolutePath(path);
            if (false == StringUtils.endWith(path, Symbol.C_SLASH)) {
                path = path + Symbol.C_SLASH;
            }
            // jar文件中的路径
            index = index + FileType.JAR.length();
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(path.substring(0, index));
                final String subPath = path.substring(index + 2);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    final String name = entry.getName();
                    if (name.startsWith(subPath)) {
                        final String nameSuffix = StringUtils.removePrefix(name, subPath);
                        if (false == StringUtils.contains(nameSuffix, Symbol.C_SLASH)) {
                            paths.add(nameSuffix);
                        }
                    }
                }
            } catch (IOException e) {
                throw new InstrumentException(StringUtils.format("Can not read file path of [{}]", path), e);
            } finally {
                IoUtils.close(jarFile);
            }
        }
        return paths;
    }

    /**
     * 创建File对象,相当于调用new File(),不做任何处理
     *
     * @param path 文件路径
     * @return File
     */
    public static File newFile(String path) {
        return new File(path);
    }

    /**
     * 创建File对象,自动识别相对或绝对路径,相对路径将自动从ClassPath下寻找
     *
     * @param path 文件路径
     * @return File
     */
    public static File file(String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return new File(getAbsolutePath(path));
    }

    /**
     * 创建File对象
     * 此方法会检查slip漏洞,漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parent 父目录
     * @param path   文件路径
     * @return File
     */
    public static File file(String parent, String path) {
        return file(new File(parent), path);
    }

    /**
     * 创建File对象
     * 此方法会检查slip漏洞,漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     */
    public static File file(File parent, String path) {
        if (StringUtils.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return checkSlip(parent, new File(parent, path));
    }

    /**
     * 通过多层目录参数创建文件
     * 此方法会检查slip漏洞,漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param directory 父目录
     * @param names     元素名（多层目录名）
     * @return the file 文件
     */
    public static File file(File directory, String... names) {
        Assert.notNull(directory, "directorydirectory must not be null");
        if (ArrayUtils.isEmpty(names)) {
            return directory;
        }

        File file = directory;
        for (String name : names) {
            if (null != name) {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * 通过多层目录创建文件
     * <p>
     * 元素名（多层目录名）
     *
     * @param names 文件名
     * @return the file 文件
     */
    public static File file(String... names) {
        if (ArrayUtils.isEmpty(names)) {
            return null;
        }

        File file = null;
        for (String name : names) {
            if (file == null) {
                file = file(name);
            } else {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * 创建File对象
     *
     * @param uri 文件URI
     * @return File
     */
    public static File file(URI uri) {
        if (uri == null) {
            throw new NullPointerException("File uri is null!");
        }
        return new File(uri);
    }

    /**
     * 创建File对象
     *
     * @param url 文件URL
     * @return File
     */
    public static File file(URL url) {
        return new File(UriUtils.toURI(url));
    }

    /**
     * 获取临时文件路径（绝对路径）
     *
     * @return 临时文件路径
     */
    public static String getTmpDirPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 获取临时文件目录
     *
     * @return 临时文件目录
     */
    public static File getTmpDir() {
        return file(getTmpDirPath());
    }

    /**
     * 获取用户路径（绝对路径）
     *
     * @return 用户路径
     */
    public static String getUserHomePath() {
        return System.getProperty("user.home");
    }

    /**
     * 获取用户目录
     *
     * @return 用户目录
     */
    public static File getUserHomeDir() {
        return file(getUserHomePath());
    }

    /**
     * 判断文件是否存在,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exist(String path) {
        return (path != null) && file(path).exists();
    }

    /**
     * 判断文件是否存在,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果存在返回true
     */
    public static boolean exist(File file) {
        return (file != null) && file.exists();
    }

    /**
     * 是否存在匹配文件
     *
     * @param directory 文件夹路径
     * @param regexp    文件夹中所包含文件名的正则表达式
     * @return 如果存在匹配文件返回true
     */
    public static boolean exist(String directory, String regexp) {
        final File file = new File(directory);
        if (false == file.exists()) {
            return false;
        }

        final String[] fileList = file.list();
        if (fileList == null) {
            return false;
        }

        for (String fileName : fileList) {
            if (fileName.matches(regexp)) {
                return true;
            }

        }
        return false;
    }

    /**
     * 获取文件名的扩展名.
     * <p>
     * 此方法返回文件名最后一个点之后的文本部分,点后面必须没有目录分隔符.
     * <pre>
     * foo.txt      -- "txt"
     * a/b/c.jpg    -- "jpg"
     * a/b.txt/c    -- ""
     * a/b/c        -- ""
     * </pre>
     * <p>
     * 不管运行代码的操作系统是什么，输出结果都是一样的.
     *
     * @param filename 检索的扩展名的文件名.
     * @return 返回文件的扩展名或空字符串.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * 返回最后一个扩展分隔符的索引点.
     *
     * @param filename 查找最后一个路径分隔符的文件名
     * @return 最后一个分隔符字符的索引，如果没有这样的字符，则为-1
     */
    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        }
        int extensionPos = filename.lastIndexOf(Symbol.DOT);
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    /**
     * 返回最后一个目录分隔符的索引
     * <p>
     * 此方法将处理Unix或Windows格式的文件。
     * 返回最后一个正斜杠或反斜杠的位置.
     * </p>
     *
     * @param filename 查找最后一个路径分隔符的文件名
     * @return 最后一个分隔符字符的索引，如果没有这样的字符，则为-1
     */
    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(Symbol.SLASH);
        int lastWindowsPos = filename.lastIndexOf(Symbol.BACKSLASH);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * 指定文件最后修改时间
     *
     * @param file 文件
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(File file) {
        if (!exist(file)) {
            return null;
        }

        return new Date(file.lastModified());
    }

    /**
     * 指定路径文件最后修改时间
     *
     * @param path 绝对路径
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(String path) {
        return lastModifiedTime(new File(path));
    }

    /**
     * 计算目录或文件的总大小
     * 当给定对象为文件时,直接调用 {@link File#length()}
     * 当给定对象为目录时,遍历目录下的所有文件和目录,递归计算其大小,求和返回
     *
     * @param file 目录或文件
     * @return 总大小, bytes长度
     */
    public static long size(File file) {
        Assert.notNull(file, "file argument is null !");
        if (false == file.exists()) {
            throw new IllegalArgumentException(StringUtils.format("File [{}] not exist !", file.getAbsolutePath()));
        }

        if (file.isDirectory()) {
            long size = 0L;
            File[] subFiles = file.listFiles();
            if (ArrayUtils.isEmpty(subFiles)) {
                return 0L;// empty directory
            }
            for (int i = 0; i < subFiles.length; i++) {
                size += size(subFiles[i]);
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file      文件或目录
     * @param reference 参照文件
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(File file, File reference) {
        if (null == reference || false == reference.exists()) {
            return true;// 文件一定比一个不存在的文件新
        }
        return newerThan(file, reference.lastModified());
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file       文件或目录
     * @param timeMillis 做为对比的时间
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(File file, long timeMillis) {
        if (null == file || false == file.exists()) {
            return false;// 不存在的文件一定比任何时间旧
        }
        return file.lastModified() > timeMillis;
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param fullFilePath 文件的全路径,使用POSIX风格
     * @return 文件, 若路径为null, 返回null
     * @throws InstrumentException 异常
     */
    public static File touch(String fullFilePath) throws InstrumentException {
        if (fullFilePath == null) {
            return null;
        }
        return touch(file(fullFilePath));
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param file 文件对象
     * @return 文件, 若路径为null, 返回null
     * @throws InstrumentException 异常
     */
    public static File touch(File file) throws InstrumentException {
        if (null == file) {
            return null;
        }
        if (false == file.exists()) {
            mkParentDirs(file);
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        }
        return file;
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws InstrumentException 异常
     */
    public static File touch(File parent, String path) throws InstrumentException {
        return touch(file(parent, path));
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws InstrumentException 异常
     */
    public static File touch(String parent, String path) throws InstrumentException {
        return touch(file(parent, path));
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(File file) {
        final File parentFile = file.getParentFile();
        if (null != parentFile && false == parentFile.exists()) {
            parentFile.mkdirs();
        }
        return parentFile;
    }

    /**
     * 创建父文件夹,如果存在直接返回此文件夹
     *
     * @param path 文件夹路径,使用POSIX格式,无论哪个平台
     * @return 创建的目录
     */
    public static File mkParentDirs(String path) {
        if (path == null) {
            return null;
        }
        return mkParentDirs(file(path));
    }

    /**
     * 删除文件或者文件夹
     * 路径如果为相对路径,会转换为ClassPath路径！ 注意：删除文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param fullFileOrDirPath 文件或者目录的路径
     * @return 成功与否
     * @throws InstrumentException 异常
     */
    public static boolean delete(String fullFileOrDirPath) throws InstrumentException {
        return delete(file(fullFileOrDirPath));
    }

    /**
     * 删除文件或者文件夹
     * 注意：删除文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param file 文件对象
     * @return 成功与否
     * @throws InstrumentException 异常
     */
    public static boolean delete(File file) throws InstrumentException {
        if (file == null || false == file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            clean(file);
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        return true;
    }

    /**
     * 清空文件夹
     * 注意：清空文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param dirPath 文件夹路径
     * @return 成功与否
     * @throws InstrumentException 异常
     */
    public static boolean clean(String dirPath) throws InstrumentException {
        return clean(file(dirPath));
    }

    /**
     * 清空文件夹
     * 注意：清空文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param directory 文件夹
     * @return 成功与否
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static boolean clean(File directory) throws InstrumentException {
        if (directory == null || directory.exists() == false || false == directory.isDirectory()) {
            return true;
        }

        final File[] files = directory.listFiles();
        for (File childFile : files) {
            boolean isOk = delete(childFile);
            if (isOk == false) {
                // 删除一个出错则本次删除任务失败
                return false;
            }
        }
        return true;
    }

    /**
     * 创建文件夹,如果存在直接返回此文件夹
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param dirPath 文件夹路径,使用POSIX格式,无论哪个平台
     * @return 创建的目录
     */
    public static File mkdir(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        final File dir = file(dirPath);
        return mkdir(dir);
    }

    /**
     * 创建文件夹,会递归自动创建其不存在的父文件夹,如果存在直接返回此文件夹
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param dir 目录
     * @return 创建的目录
     */
    public static File mkdir(File dir) {
        if (dir == null) {
            return null;
        }
        if (false == dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir 临时文件创建的所在目录
     * @return 临时文件
     * @throws InstrumentException 异常
     */
    public static File createTempFile(File dir) throws InstrumentException {
        return createTempFile("create", null, dir, true);
    }

    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的,创建新的）
     * @return 临时文件
     * @throws InstrumentException 异常
     */
    public static File createTempFile(File dir, boolean isReCreat) throws InstrumentException {
        return createTempFile("create", null, dir, isReCreat);
    }

    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].suffix From com.jodd.io.FileUtils
     *
     * @param prefix    前缀,至少3个字符
     * @param suffix    后缀,如果null则使用默认.tmp
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的,创建新的）
     * @return 临时文件
     * @throws InstrumentException 异常
     */
    public static File createTempFile(String prefix, String suffix, File dir, boolean isReCreat) throws InstrumentException {
        int exceptionsCount = 0;
        while (true) {
            try {
                File file = File.createTempFile(prefix, suffix, dir).getCanonicalFile();
                if (isReCreat) {
                    file.delete();
                    file.createNewFile();
                }
                return file;
            } catch (IOException ioex) { // fixes java.io.WinNTFileSystem.createFileExclusively access denied
                if (++exceptionsCount >= 50) {
                    throw new InstrumentException(ioex);
                }
            }
        }
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件路径
     * @param dest    目标文件或目录路径,如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return File
     * @throws InstrumentException 异常
     */
    public static File copyFile(String src, String dest, StandardCopyOption... options) throws InstrumentException {
        Assert.notBlank(src, "Source File path is blank !");
        Assert.notNull(src, "Destination File path is null !");
        return copyFile(Paths.get(src), Paths.get(dest), options).toFile();
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件
     * @param dest    目标文件或目录,如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File copyFile(File src, File dest, StandardCopyOption... options) throws InstrumentException {
        Assert.notNull(src, "Source File is null !");
        if (false == src.exists()) {
            throw new InstrumentException("File not exist: " + src);
        }
        Assert.notNull(dest, "Destination File or directiory is null !");
        if (equals(src, dest)) {
            throw new InstrumentException("Files '{}' and '{}' are equal", src, dest);
        }
        return copyFile(src.toPath(), dest.toPath(), options).toFile();
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件路径
     * @param dest    目标文件或目录,如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return Path
     * @throws InstrumentException 异常
     */
    public static Path copyFile(Path src, Path dest, StandardCopyOption... options) throws InstrumentException {
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(dest, "Destination File or directiory is null !");

        Path destPath = dest.toFile().isDirectory() ? dest.resolve(src.getFileName()) : dest;
        try {
            return Files.copy(src, destPath, options);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 复制文件或目录
     * 如果目标文件为目录,则将源文件以相同文件名拷贝到目标目录
     *
     * @param srcPath    源文件或目录
     * @param destPath   目标文件或目录,目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InstrumentException 异常
     */
    public static File copy(String srcPath, String destPath, boolean isOverride) throws InstrumentException {
        return copy(file(srcPath), file(destPath), isOverride);
    }

    /**
     * 复制文件或目录
     * 情况如下：
     *
     * <pre>
     * 1、src和dest都为目录,则将src目录及其目录下所有文件目录拷贝到dest下
     * 2、src和dest都为文件,直接复制,名字为dest
     * 3、src为文件,dest为目录,将src拷贝到dest目录下
     * </pre>
     *
     * @param src        源文件
     * @param dest       目标文件或目录,目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InstrumentException 异常
     */
    public static File copy(File src, File dest, boolean isOverride) throws InstrumentException {
        return FileCopier.create(src, dest).setOverride(isOverride).copy();
    }

    /**
     * 复制文件或目录
     * 情况如下：
     *
     * <pre>
     * 1、src和dest都为目录,则讲src下所有文件目录拷贝到dest下
     * 2、src和dest都为文件,直接复制,名字为dest
     * 3、src为文件,dest为目录,将src拷贝到dest目录下
     * </pre>
     *
     * @param src        源文件
     * @param dest       目标文件或目录,目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InstrumentException 异常
     */
    public static File copyContent(File src, File dest, boolean isOverride) throws InstrumentException {
        return FileCopier.create(src, dest).setCopyContentIfDir(true).setOverride(isOverride).copy();
    }

    /**
     * 复制文件或目录
     * 情况如下：
     *
     * <pre>
     * 1、src和dest都为目录,则讲src下所有文件（包括子目录）拷贝到dest下
     * 2、src和dest都为文件,直接复制,名字为dest
     * 3、src为文件,dest为目录,将src拷贝到dest目录下
     * </pre>
     *
     * @param src        源文件
     * @param dest       目标文件或目录,目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InstrumentException 异常
     */
    public static File copyFilesFromDir(File src, File dest, boolean isOverride) throws InstrumentException {
        return FileCopier.create(src, dest).setCopyContentIfDir(true).setOnlyCopyFile(true).setOverride(isOverride).copy();
    }

    /**
     * Copy bytes from a <code>File</code> to an <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     * </p>
     *
     * @param input  the <code>File</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws NullPointerException if the input or output is null
     * @throws IOException          if an I/O error occurs
     * @since 2.1
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (FileInputStream fis = new FileInputStream(input)) {
            return IoUtils.copy(fis, output);
        }
    }

    /**
     * 移动文件或者目录
     *
     * @param src        源文件或者目录
     * @param dest       目标文件或者目录
     * @param isOverride 是否覆盖目标,只有目标为文件才覆盖
     * @throws InstrumentException 异常
     */
    public static void move(File src, File dest, boolean isOverride) throws InstrumentException {
        // check
        if (false == src.exists()) {
            throw new InstrumentException("File not found: " + src);
        }

        // 来源为文件夹,目标为文件
        if (src.isDirectory() && dest.isFile()) {
            throw new InstrumentException(StringUtils.format("Can not move directory [{}] to file [{}]", src, dest));
        }

        if (isOverride && dest.isFile()) {// 只有目标为文件的情况下覆盖之
            dest.delete();
        }

        // 来源为文件,目标为文件夹
        if (src.isFile() && dest.isDirectory()) {
            dest = new File(dest, src.getName());
        }

        if (false == src.renameTo(dest)) {
            // 在文件系统不同的情况下,renameTo会失败,此时使用copy,然后删除原文件
            try {
                copy(src, dest, isOverride);
                src.delete();
            } catch (Exception e) {
                throw new InstrumentException(StringUtils.format("Move [{}] to [{}] failed!", src, dest), e);
            }

        }
    }

    /**
     * 修改文件或目录的文件名,不变更路径,只是简单修改文件名
     * 重命名有两种模式：
     * 1、isRetainExt为true时,保留原扩展名：
     *
     * <pre>
     * FileUtils.rename(file, "aaa", true) xx/xx.png =》xx/aaa.png
     * </pre>
     * <p>
     * 2、isRetainExt为false时,不保留原扩展名,需要在newName中
     *
     * <pre>
     * FileUtils.rename(file, "aaa.jpg", false) xx/xx.png =》xx/aaa.jpg
     * </pre>
     *
     * @param file        被修改的文件
     * @param newName     新的文件名,包括扩展名
     * @param isRetainExt 是否保留原文件的扩展名,如果保留,则newName不需要加扩展名
     * @param isOverride  是否覆盖目标文件
     * @return 目标文件
     * @since 3.1.9
     */
    public static File rename(File file, String newName, boolean isRetainExt, boolean isOverride) {
        if (isRetainExt) {
            newName = newName.concat(".").concat(FileUtils.extName(file));
        }
        final Path path = file.toPath();
        final CopyOption[] options = isOverride ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{};
        try {
            return Files.move(path, path.resolveSibling(newName), options).toFile();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取规范的绝对路径
     *
     * @param file 文件
     * @return 规范绝对路径, 如果传入file为null, 返回null
     */
    public static String getCanonicalPath(File file) {
        if (null == file) {
            return null;
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取绝对路径
     * 此方法不会判定给定路径是否有效（文件或目录存在）
     *
     * @param path      相对路径
     * @param baseClass 相对路径所相对的类
     * @return 绝对路径
     */
    public static String getAbsolutePath(String path, Class<?> baseClass) {
        String normalPath;
        if (path == null) {
            normalPath = Normal.EMPTY;
        } else {
            normalPath = normalize(path);
            if (isAbsolutePath(normalPath)) {
                return normalPath;
            }
        }

        final URL url = ResourceUtils.getResource(normalPath, baseClass);
        if (null != url) {
            return FileUtils.normalize(UriUtils.getDecodedPath(url));
        }

        final String classPath = ClassUtils.getClassPath();
        if (null == classPath) {
            return path;
        }
        return normalize(classPath.concat(path));
    }

    /**
     * 获取绝对路径,相对于ClassPath的目录
     * 如果给定就是绝对路径,则返回原路径,原路径把所有\替换为/
     * 兼容Spring风格的路径表示,例如：classpath:config/example.setting也会被识别后转换
     *
     * @param path 相对路径
     * @return 绝对路径
     */
    public static String getAbsolutePath(String path) {
        return getAbsolutePath(path, null);
    }

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 给定路径已经是绝对路径
     * 此方法并没有针对路径做标准化,建议先执行{@link #normalize(String)}方法标准化路径后判断
     *
     * @param path 需要检查的Path
     * @return 是否已经是绝对路径
     */
    public static boolean isAbsolutePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        return Symbol.C_SLASH == path.charAt(0) || path.matches("^[a-zA-Z]:[/\\\\].*");
    }

    /**
     * 判断是否为目录,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果为目录true
     */
    public static boolean isDirectory(String path) {
        return (path != null) && file(path).isDirectory();
    }

    /**
     * 判断是否为目录,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果为目录true
     */
    public static boolean isDirectory(File file) {
        return (file != null) && file.isDirectory();
    }

    /**
     * 判断是否为目录,如果file为null,则返回false
     *
     * @param path          {@link Path}
     * @param isFollowLinks 是否追踪到软链对应的真实地址
     * @return 如果为目录true
     * @since 3.1.9
     */
    public static boolean isDirectory(Path path, boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.isDirectory(path, options);
    }

    /**
     * 判断是否为文件,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果为文件true
     */
    public static boolean isFile(String path) {
        return (path != null) && file(path).isFile();
    }

    /**
     * 判断是否为文件,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(File file) {
        return (file != null) && file.isFile();
    }

    /**
     * 判断是否为文件,如果file为null,则返回false
     *
     * @param path          文件
     * @param isFollowLinks 是否跟踪软链（快捷方式）
     * @return 如果为文件true
     */
    public static boolean isFile(Path path, boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.isRegularFile(path, options);
    }

    /**
     * 检查两个文件是否是同一个文件
     * 所谓文件相同,是指File对象是否指向同一个文件或文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     * @throws InstrumentException 异常
     * @see Files#isSameFile(Path, Path)
     */
    public static boolean equals(File file1, File file2) throws InstrumentException {
        Assert.notNull(file1);
        Assert.notNull(file2);
        if (false == file1.exists() || false == file2.exists()) {
            // 两个文件都不存在判断其路径是否相同
            return false == file1.exists() && false == file2.exists() && pathEquals(file1, file2);
            // 对于一个存在一个不存在的情况,一定不相同
        }
        try {
            return Files.isSameFile(file1.toPath(), file2.toPath());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 比较两个文件内容是否相同
     * 首先比较长度,长度一致再比较内容
     * 此方法来自Apache Commons io
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 两个文件内容一致返回true, 否则false
     * @throws InstrumentException 异常
     */
    public static boolean contentEquals(File file1, File file2) throws InstrumentException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (false == file1Exists) {
            // 两个文件都不存在,返回true
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // 不比较目录
            throw new InstrumentException("Can't compare directories, only file");
        }

        if (file1.length() != file2.length()) {
            // 文件长度不同
            return false;
        }

        if (equals(file1, file2)) {
            // 同一个文件
            return true;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = getInputStream(file1);
            input2 = getInputStream(file2);
            return IoUtils.contentEquals(input1, input2);

        } finally {
            IoUtils.close(input1);
            IoUtils.close(input2);
        }
    }

    /**
     * 比较两个文件内容是否相同
     * 首先比较长度,长度一致再比较内容,比较内容采用按行读取,每行比较
     * 此方法来自Apache Commons io
     *
     * @param file1   文件1
     * @param file2   文件2
     * @param charset 编码,null表示使用平台默认编码 两个文件内容一致返回true,否则false
     * @return the boolean
     * @throws InstrumentException 异常
     */
    public static boolean contentEqualsIgnoreEOL(File file1, File file2, Charset charset) throws InstrumentException {
        boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // 两个文件都不存在,返回true
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // 不比较目录
            throw new InstrumentException("Can't compare directories, only file");
        }

        if (equals(file1, file2)) {
            // 同一个文件
            return true;
        }

        Reader input1 = null;
        Reader input2 = null;
        try {
            input1 = getReader(file1, charset);
            input2 = getReader(file2, charset);
            return IoUtils.contentEqualsIgnoreEOL(input1, input2);
        } finally {
            IoUtils.close(input1);
            IoUtils.close(input2);
        }
    }

    /**
     * 文件路径是否相同
     * 取两个文件的绝对路径比较,在Windows下忽略大小写,在Linux下不忽略
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 文件路径是否相同
     * @since 3.1.9
     */
    public static boolean pathEquals(File file1, File file2) {
        if (isWindows()) {
            // Windows环境
            try {
                if (StringUtils.equalsIgnoreCase(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringUtils.equalsIgnoreCase(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        } else {
            // 类Unix环境
            try {
                if (StringUtils.equals(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringUtils.equals(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获得最后一个文件路径分隔符的位置
     *
     * @param filePath 文件路径
     * @return 最后一个文件路径分隔符的位置
     */
    public static int lastIndexOfSeparator(String filePath) {
        if (StringUtils.isNotEmpty(filePath)) {
            int i = filePath.length();
            char c;
            while (i-- >= 0) {
                c = filePath.charAt(i);
                if (CharUtils.isFileSeparator(c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 判断文件是否被改动
     * 如果文件对象为 null 或者文件不存在,被视为改动
     *
     * @param file           文件对象
     * @param lastModifyTime 上次的改动时间
     * @return 是否被改动
     */
    public static boolean isModifed(File file, long lastModifyTime) {
        if (null == file || false == file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * 修复路径
     * 如果原路径尾部有分隔符,则保留为标准分隔符（/）,否则不保留
     * <ol>
     * <li>1. 统一用 /</li>
     * <li>2. 多个 / 转换为一个 /</li>
     * <li>3. 去除两边空格</li>
     * <li>4. .. 和 . 转换为绝对路径,当..多于已有路径时,直接返回根路径</li>
     * </ol>
     * <p>
     * 栗子：
     *
     * <pre>
     * "/foo//" =》 "/foo/"
     * "/foo/./" =》 "/foo/"
     * "/foo/../bar" =》 "/bar"
     * "/foo/../bar/" =》 "/bar/"
     * "/foo/../bar/../baz" =》 "/baz"
     * "/../" =》 "/"
     * "foo/bar/.." =》 "foo"
     * "foo/../bar" =》 "bar"
     * "foo/../../bar" =》 "bar"
     * "//server/foo/../bar" =》 "/server/bar"
     * "//server/../bar" =》 "/bar"
     * "C:\\foo\\..\\bar" =》 "C:/bar"
     * "C:\\..\\bar" =》 "C:/bar"
     * "~/foo/../bar/" =》 "~/bar/"
     * "~/../bar" =》 "bar"
     * </pre>
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }

        // 兼容Spring风格的ClassPath路径,去除前缀,不区分大小写
        String pathToUse = StringUtils.removePrefixIgnoreCase(path, "classpath:");
        // 去除file:前缀
        pathToUse = StringUtils.removePrefixIgnoreCase(pathToUse, "file:");
        // 统一使用斜杠
        pathToUse = pathToUse.replaceAll("[/\\\\]{1,}", "/").trim();

        int prefixIndex = pathToUse.indexOf(Symbol.COLON);
        String prefix = "";
        if (prefixIndex > -1) {
            // 可能Windows风格路径
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (StringUtils.startWith(prefix, Symbol.C_SLASH)) {
                // 去除类似于/C:这类路径开头的斜杠
                prefix = prefix.substring(1);
            }
            if (false == prefix.contains("/")) {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            } else {
                // 如果前缀中包含/,说明非Windows风格path
                prefix = Normal.EMPTY;
            }
        }
        if (pathToUse.startsWith(Symbol.SLASH)) {
            prefix += Symbol.SLASH;
            pathToUse = pathToUse.substring(1);
        }

        List<String> pathList = StringUtils.split(pathToUse, Symbol.C_SLASH);
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        String element;
        for (int i = pathList.size() - 1; i >= 0; i--) {
            element = pathList.get(i);
            if (Symbol.DOT.equals(element)) {
                // 当前目录,丢弃
            } else if (Symbol.DOUBLE_DOT.equals(element)) {
                tops++;
            } else {
                if (tops > 0) {
                    // 有上级目录标记时按照个数依次跳过
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        return prefix + CollUtils.join(pathElements, Symbol.SLASH);
    }

    /**
     * 获得相对子路径
     * <p>
     * 栗子：
     *
     * <pre>
     * dirPath: d:/aaa/bbb    filePath: d:/aaa/bbb/ccc     =》    ccc
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/ccc.txt     =》    ccc.txt
     * </pre>
     *
     * @param rootDir 绝对父路径
     * @param file    文件
     * @return 相对子路径
     */
    public static String subPath(String rootDir, File file) {
        try {
            return subPath(rootDir, file.getCanonicalPath());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得相对子路径,忽略大小写
     * <p>
     * 栗子：
     *
     * <pre>
     * dirPath: d:/aaa/bbb    filePath: d:/aaa/bbb/ccc     =》    ccc
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/ccc.txt     =》    ccc.txt
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/     =》    ""
     * </pre>
     *
     * @param dirPath  父路径
     * @param filePath 文件路径
     * @return 相对子路径
     */
    public static String subPath(String dirPath, String filePath) {
        if (StringUtils.isNotEmpty(dirPath) && StringUtils.isNotEmpty(filePath)) {

            dirPath = StringUtils.removeSuffix(normalize(dirPath), "/");
            filePath = normalize(filePath);

            final String result = StringUtils.removePrefixIgnoreCase(filePath, dirPath);
            return StringUtils.removePrefix(result, "/");
        }
        return filePath;
    }

    /**
     * 获取指定位置的子路径部分,支持负数,例如index为-1表示从后数第一个节点位置
     *
     * @param path  路径
     * @param index 路径节点位置,支持负数（负数从后向前计数）
     * @return 获取的子路径
     * @since 3.1.9
     */
    public static Path getPathEle(Path path, int index) {
        return subPath(path, index, index == -1 ? path.getNameCount() : index + 1);
    }

    /**
     * 获取指定位置的最后一个子路径部分
     *
     * @param path 路径
     * @return 获取的最后一个子路径
     * @since 3.1.9
     */
    public static Path getLastPathEle(Path path) {
        return getPathEle(path, path.getNameCount() - 1);
    }

    /**
     * 获取指定位置的子路径部分,支持负数,例如起始为-1表示从后数第一个节点位置
     *
     * @param path      路径
     * @param fromIndex 起始路径节点（包括）
     * @param toIndex   结束路径节点（不包括）
     * @return 获取的子路径
     * @since 3.1.9
     */
    public static Path subPath(Path path, int fromIndex, int toIndex) {
        if (null == path) {
            return null;
        }
        final int len = path.getNameCount();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return null;
        }
        return path.subpath(fromIndex, toIndex);
    }

    /**
     * 返回文件名
     *
     * @param file 文件
     * @return 文件名
     */
    public static String getName(File file) {
        return (null != file) ? file.getName() : null;
    }

    /**
     * 返回文件名
     *
     * @param filePath 文件
     * @return 文件名
     */
    public static String getName(String filePath) {
        if (null == filePath) {
            return filePath;
        }
        int len = filePath.length();
        if (0 == len) {
            return filePath;
        }
        if (CharUtils.isFileSeparator(filePath.charAt(len - 1))) {
            //以分隔符结尾的去掉结尾分隔符
            len--;
        }

        int begin = 0;
        char c;
        for (int i = len - 1; i > -1; i--) {
            c = filePath.charAt(i);
            if (CharUtils.isFileSeparator(c)) {
                //查找最后一个路径分隔符（/或者\）
                begin = i + 1;
                break;
            }
        }

        return filePath.substring(begin, len);
    }

    /**
     * 返回主文件名
     *
     * @param file 文件
     * @return 主文件名
     */
    public static String mainName(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        return mainName(file.getName());
    }

    /**
     * 返回主文件名
     *
     * @param fileName 完整文件名
     * @return 主文件名
     */
    public static String mainName(String fileName) {
        if (null == fileName) {
            return fileName;
        }
        int len = fileName.length();
        if (0 == len) {
            return fileName;
        }
        if (CharUtils.isFileSeparator(fileName.charAt(len - 1))) {
            len--;
        }

        int begin = 0;
        int end = len;
        char c;
        for (int i = len - 1; i > -1; i--) {
            c = fileName.charAt(i);
            if (len == end && Symbol.C_DOT == c) {
                //查找最后一个文件名和扩展名的分隔符：.
                end = i;
            }
            if (0 == begin || begin > end) {
                if (CharUtils.isFileSeparator(c)) {
                    //查找最后一个路径分隔符（/或者\）,如果这个分隔符在.之后,则继续查找,否则结束
                    begin = i + 1;
                    break;
                }
            }
        }

        return fileName.substring(begin, end);
    }

    /**
     * 获取文件扩展名,扩展名不带“.”
     *
     * @param file 文件
     * @return 扩展名
     */
    public static String extName(File file) {
        if (null == file) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        return extName(file.getName());
    }

    /**
     * 获得文件的扩展名,扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String extName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf(Symbol.DOT);
        if (index == -1) {
            return Normal.EMPTY;
        } else {
            String ext = fileName.substring(index + 1);
            // 扩展名中不能包含路径相关的符号
            return StringUtils.containsAny(ext, Symbol.C_SLASH, Symbol.C_BACKSLASH) ? Normal.EMPTY : ext;
        }
    }

    /**
     * 判断文件路径是否有指定后缀,忽略大小写
     * 常用语判断扩展名
     *
     * @param file   文件或目录
     * @param suffix 后缀
     * @return 是否有指定后缀
     */
    public static boolean pathEndsWith(File file, String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * @param file 文件 {@link File}
     * @return 类型, 文件的扩展名, 未找到为<code>null</code>
     * @throws InstrumentException 异常
     * @see FileType#getType(File)
     */
    public static String getType(File file) throws InstrumentException {
        return FileType.getType(file);
    }

    /**
     * 获取文件属性
     *
     * @param path          文件路径{@link Path}
     * @param isFollowLinks 是否跟踪到软链对应的真实路径
     * @return {@link BasicFileAttributes}
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static BasicFileAttributes getAttributes(Path path, boolean isFollowLinks) throws InstrumentException {
        if (null == path) {
            return null;
        }

        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, options);
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得输入流
     *
     * @param path Path
     * @return 输入流
     * @throws InstrumentException 文件未找到
     */
    public static BufferedInputStream getInputStream(Path path) throws InstrumentException {
        try {
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws InstrumentException 文件未找到
     */
    public static BufferedInputStream getInputStream(File file) throws InstrumentException {
        return new BufferedInputStream(IoUtils.toStream(file));
    }

    /**
     * 获得输入流
     *
     * @param path 文件路径
     * @return 输入流
     * @throws InstrumentException 文件未找到
     */
    public static BufferedInputStream getInputStream(String path) throws InstrumentException {
        return getInputStream(file(path));
    }

    /**
     * 获得BOM输入流,用于处理带BOM头的文件
     *
     * @param file 文件
     * @return 输入流
     * @throws InstrumentException 文件未找到
     */
    public static BOMInputStream getBOMInputStream(File file) throws InstrumentException {
        try {
            return new BOMInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件Path
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getUtf8Reader(Path path) throws InstrumentException {
        return getReader(path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file 文件
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getUtf8Reader(File file) throws InstrumentException {
        return getReader(file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件路径
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getUtf8Reader(String path) throws InstrumentException {
        return getReader(path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    文件Path
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getReader(Path path, Charset charset) throws InstrumentException {
        return IoUtils.getReader(getInputStream(path), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getReader(File file, String charsetName) throws InstrumentException {
        return IoUtils.getReader(getInputStream(file), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file    文件
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getReader(File file, Charset charset) throws InstrumentException {
        return IoUtils.getReader(getInputStream(file), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path        绝对路径
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getReader(String path, String charsetName) throws InstrumentException {
        return getReader(file(path), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    绝对路径
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedReader getReader(String path, Charset charset) throws InstrumentException {
        return getReader(file(path), charset);
    }

    /**
     * 读取文件所有数据
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param file 文件
     * @return 字节码
     * @throws InstrumentException 异常
     */
    public static byte[] readBytes(File file) throws InstrumentException {
        return FileReader.create(file).readBytes();
    }

    /**
     * 读取文件所有数据
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param filePath 文件路径
     * @return 字节码
     * @throws InstrumentException 异常
     * @since 5.3.5
     */
    public static byte[] readBytes(String filePath) throws InstrumentException {
        return readBytes(file(filePath));
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readUtf8String(File file) throws InstrumentException {
        return readString(file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readUtf8String(String path) throws InstrumentException {
        return readString(path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readString(File file, String charsetName) throws InstrumentException {
        return readString(file, CharsetUtils.charset(charsetName));
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readString(File file, Charset charset) throws InstrumentException {
        return FileReader.create(file, charset).readString();
    }

    /**
     * 读取文件内容
     *
     * @param path        文件路径
     * @param charsetName 字符集
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readString(String path, String charsetName) throws InstrumentException {
        return readString(file(path), charsetName);
    }

    /**
     * 读取文件内容
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readString(String path, Charset charset) throws InstrumentException {
        return readString(file(path), charset);
    }

    /**
     * 读取文件内容
     *
     * @param url     文件URL
     * @param charset 字符集
     * @return 内容
     * @throws InstrumentException 异常
     */
    public static String readString(URL url, String charset) throws InstrumentException {
        if (url == null) {
            throw new NullPointerException("Empty url provided!");
        }

        InputStream in = null;
        try {
            in = url.openStream();
            return IoUtils.read(in, charset);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 从文件中读取每一行的UTF-8编码数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T extends Collection<String>> T readUtf8Lines(String path, T collection) throws InstrumentException {
        return readLines(path, org.aoju.bus.core.lang.Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readLines(String path, String charset, T collection) throws InstrumentException {
        return readLines(file(path), charset, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readLines(String path, Charset charset, T collection) throws InstrumentException {
        return readLines(file(path), charset, collection);
    }

    /**
     * 从文件中读取每一行数据,数据编码为UTF-8
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T extends Collection<String>> T readUtf8Lines(File file, T collection) throws InstrumentException {
        return readLines(file, org.aoju.bus.core.lang.Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readLines(File file, String charset, T collection) throws InstrumentException {
        return FileReader.create(file, CharsetUtils.charset(charset)).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readLines(File file, Charset charset, T collection) throws InstrumentException {
        return FileReader.create(file, charset).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据,编码为UTF-8
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(URL url, T collection) throws InstrumentException {
        return readLines(url, org.aoju.bus.core.lang.Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>         集合类型
     * @param url         文件的URL
     * @param charsetName 字符集
     * @param collection  集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     */
    public static <T extends Collection<String>> T readLines(URL url, String charsetName, T collection) throws InstrumentException {
        return readLines(url, CharsetUtils.charset(charsetName), collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T extends Collection<String>> T readLines(URL url, Charset charset, T collection) throws InstrumentException {
        InputStream in = null;
        try {
            in = url.openStream();
            return IoUtils.readLines(in, charset, collection);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url 文件的URL
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readUtf8Lines(URL url) throws InstrumentException {
        return readLines(url, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readLines(URL url, String charset) throws InstrumentException {
        return readLines(url, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readLines(URL url, Charset charset) throws InstrumentException {
        return readLines(url, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据,编码为UTF-8
     *
     * @param path 文件路径
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static List<String> readUtf8Lines(String path) throws InstrumentException {
        return readLines(path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readLines(String path, String charset) throws InstrumentException {
        return readLines(path, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static List<String> readLines(String path, Charset charset) throws InstrumentException {
        return readLines(path, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file 文件
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static List<String> readUtf8Lines(File file) throws InstrumentException {
        return readLines(file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readLines(File file, String charset) throws InstrumentException {
        return readLines(file, charset, new ArrayList<String>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InstrumentException 异常
     */
    public static List<String> readLines(File file, Charset charset) throws InstrumentException {
        return readLines(file, charset, new ArrayList<String>());
    }

    /**
     * 按行处理文件内容,编码为UTF-8
     *
     * @param file        文件
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InstrumentException 异常
     */
    public static void readUtf8Lines(File file, LineHandler lineHandler) throws InstrumentException {
        readLines(file, org.aoju.bus.core.lang.Charset.UTF_8, lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InstrumentException 异常
     */
    public static void readLines(File file, Charset charset, LineHandler lineHandler) throws InstrumentException {
        FileReader.create(file, charset).readLines(lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InstrumentException 异常
     */
    public static void readLines(RandomAccessFile file, Charset charset, LineHandler lineHandler) {
        String line = null;
        try {
            while ((line = file.readLine()) != null) {
                lineHandler.handle(CharsetUtils.convert(line, org.aoju.bus.core.lang.Charset.ISO_8859_1, charset));
            }
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     */
    public static void readLine(RandomAccessFile file, Charset charset, LineHandler lineHandler) {
        final String line = readLine(file, charset);
        if (null != line) {
            lineHandler.handle(line);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file    {@link RandomAccessFile}文件
     * @param charset 编码
     * @return 行内容
     */
    public static String readLine(RandomAccessFile file, Charset charset) {
        String line = null;
        try {
            line = file.readLine();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        if (null != line) {
            return CharsetUtils.convert(line, org.aoju.bus.core.lang.Charset.ISO_8859_1, charset);
        }

        return null;
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @return 从文件中load出的数据
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T> T loadUtf8(String path, FileReader.ReaderHandler<T> readerHandler) throws InstrumentException {
        return load(path, org.aoju.bus.core.lang.Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T> T load(String path, String charset, FileReader.ReaderHandler<T> readerHandler) throws InstrumentException {
        return FileReader.create(file(path), CharsetUtils.charset(charset)).read(readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T> T load(String path, Charset charset, FileReader.ReaderHandler<T> readerHandler) throws InstrumentException {
        return FileReader.create(file(path), charset).read(readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @return 从文件中load出的数据
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T> T loadUtf8(File file, FileReader.ReaderHandler<T> readerHandler) throws InstrumentException {
        return load(file, org.aoju.bus.core.lang.Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InstrumentException 异常
     * @since 3.1.1
     */
    public static <T> T load(File file, Charset charset, FileReader.ReaderHandler<T> readerHandler) throws InstrumentException {
        return FileReader.create(file, charset).read(readerHandler);
    }

    /**
     * 获得一个输出流对象
     *
     * @param file 文件
     * @return 输出流对象
     * @throws InstrumentException 异常
     */
    public static BufferedOutputStream getOutputStream(File file) throws InstrumentException {
        try {
            return new BufferedOutputStream(new FileOutputStream(touch(file)));
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获得一个输出流对象
     *
     * @param path 输出到的文件路径,绝对路径
     * @return 输出流对象
     * @throws InstrumentException 异常
     */
    public static BufferedOutputStream getOutputStream(String path) throws InstrumentException {
        return getOutputStream(touch(path));
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path        输出路径,绝对路径
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedWriter getWriter(String path, String charsetName, boolean isAppend) throws InstrumentException {
        return getWriter(touch(path), Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedWriter getWriter(String path, Charset charset, boolean isAppend) throws InstrumentException {
        return getWriter(touch(path), charset, isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file        输出文件
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedWriter getWriter(File file, String charsetName, boolean isAppend) throws InstrumentException {
        return getWriter(file, Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file     输出文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InstrumentException 异常
     */
    public static BufferedWriter getWriter(File file, Charset charset, boolean isAppend) throws InstrumentException {
        return FileWriter.create(file, charset).getWriter(isAppend);
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InstrumentException 异常
     */
    public static PrintWriter getPrintWriter(String path, String charset, boolean isAppend) throws InstrumentException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InstrumentException 异常
     */
    public static PrintWriter getPrintWriter(String path, Charset charset, boolean isAppend) throws InstrumentException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InstrumentException 异常
     */
    public static PrintWriter getPrintWriter(File file, String charset, boolean isAppend) throws InstrumentException {
        return new PrintWriter(getWriter(file, charset, isAppend));
    }

    /**
     * 获取当前系统的换行分隔符
     *
     * <pre>
     * Windows: \r\n
     * Mac: \r
     * Linux: \n
     * </pre>
     *
     * @return 换行符
     */
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * 将String写入文件,覆盖模式,字符集为UTF-8
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeUtf8String(String content, String path) throws InstrumentException {
        return writeString(content, path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将String写入文件,覆盖模式,字符集为UTF-8
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeUtf8String(String content, File file) throws InstrumentException {
        return writeString(content, file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeString(String content, String path, String charset) throws InstrumentException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeString(String content, String path, Charset charset) throws InstrumentException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeString(String content, File file, String charset) throws InstrumentException {
        return FileWriter.create(file, CharsetUtils.charset(charset)).write(content);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws InstrumentException 异常
     */
    public static File writeString(String content, File file, Charset charset) throws InstrumentException {
        return FileWriter.create(file, charset).write(content);
    }

    /**
     * 将String写入文件,UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static File appendUtf8String(String content, String path) throws InstrumentException {
        return appendString(content, path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File appendString(String content, String path, String charset) throws InstrumentException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File appendString(String content, String path, Charset charset) throws InstrumentException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static File appendUtf8String(String content, File file) throws InstrumentException {
        return appendString(content, file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File appendString(String content, File file, String charset) throws InstrumentException {
        return FileWriter.create(file, CharsetUtils.charset(charset)).append(content);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws InstrumentException 异常
     */
    public static File appendString(String content, File file, Charset charset) throws InstrumentException {
        return FileWriter.create(file, charset).append(content);
    }

    /**
     * 将列表写入文件,覆盖模式,编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 绝对路径
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 5.3.5
     */
    public static <T> File writeUtf8Lines(Collection<T> list, String path) throws InstrumentException {
        return writeLines(list, path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将列表写入文件,覆盖模式,编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 绝对路径
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 5.3.5
     */
    public static <T> File writeUtf8Lines(Collection<T> list, File file) throws InstrumentException {
        return writeLines(list, file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将列表写入文件,覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, String path, String charset) throws InstrumentException {
        return writeLines(list, path, charset, false);
    }

    /**
     * 将列表写入文件,覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, String path, Charset charset) throws InstrumentException {
        return writeLines(list, path, charset, false);
    }

    /**
     * 将列表写入文件,覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, File file, String charset) throws InstrumentException {
        return writeLines(list, file, charset, false);
    }

    /**
     * 将列表写入文件,覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, File file, Charset charset) throws InstrumentException {
        return writeLines(list, file, charset, false);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 文件
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static <T> File appendUtf8Lines(Collection<T> list, File file) throws InstrumentException {
        return appendLines(list, file, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 文件路径
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static <T> File appendUtf8Lines(Collection<T> list, String path) throws InstrumentException {
        return appendLines(list, path, org.aoju.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File appendLines(Collection<T> list, String path, String charset) throws InstrumentException {
        return writeLines(list, path, charset, true);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static <T> File appendLines(Collection<T> list, File file, String charset) throws InstrumentException {
        return writeLines(list, file, charset, true);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File appendLines(Collection<T> list, String path, Charset charset) throws InstrumentException {
        return writeLines(list, path, charset, true);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InstrumentException 异常
     * @since 3.1.9
     */
    public static <T> File appendLines(Collection<T> list, File file, Charset charset) throws InstrumentException {
        return writeLines(list, file, charset, true);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param path     文件路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, String path, String charset, boolean isAppend) throws InstrumentException {
        return writeLines(list, file(path), charset, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param path     文件路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, String path, Charset charset, boolean isAppend) throws InstrumentException {
        return writeLines(list, file(path), charset, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, File file, String charset, boolean isAppend) throws InstrumentException {
        return FileWriter.create(file, CharsetUtils.charset(charset)).writeLines(list, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static <T> File writeLines(Collection<T> list, File file, Charset charset, boolean isAppend) throws InstrumentException {
        return FileWriter.create(file, charset).writeLines(list, isAppend);
    }

    /**
     * 将Map写入文件,每个键值对为一行,一行中键与值之间使用kvSeparator分隔
     *
     * @param map         Map
     * @param file        文件
     * @param kvSeparator 键和值之间的分隔符,如果传入null使用默认分隔符" = "
     * @param isAppend    是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeUtf8Map(Map<?, ?> map, File file, String kvSeparator, boolean isAppend) throws InstrumentException {
        return FileWriter.create(file, org.aoju.bus.core.lang.Charset.UTF_8).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * 将Map写入文件,每个键值对为一行,一行中键与值之间使用kvSeparator分隔
     *
     * @param map         Map
     * @param file        文件
     * @param charset     字符集编码
     * @param kvSeparator 键和值之间的分隔符,如果传入null使用默认分隔符" = "
     * @param isAppend    是否追加
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeMap(Map<?, ?> map, File file, Charset charset, String kvSeparator, boolean isAppend) throws InstrumentException {
        return FileWriter.create(file, charset).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param path 目标文件
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeBytes(byte[] data, String path) throws InstrumentException {
        return writeBytes(data, touch(path));
    }

    /**
     * 写数据到文件中
     *
     * @param dest 目标文件
     * @param data 数据
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeBytes(byte[] data, File dest) throws InstrumentException {
        return writeBytes(data, dest, 0, data.length, false);
    }

    /**
     * 写入数据到文件
     *
     * @param data     数据
     * @param dest     目标文件
     * @param off      数据开始位置
     * @param len      数据长度
     * @param isAppend 是否追加模式
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeBytes(byte[] data, File dest, int off, int len, boolean isAppend) throws InstrumentException {
        return FileWriter.create(dest).write(data, off, len, isAppend);
    }

    /**
     * 将流的内容写入文件
     *
     * @param dest 目标文件
     * @param in   输入流
     * @return dest
     * @throws InstrumentException 异常
     */
    public static File writeFromStream(InputStream in, File dest) throws InstrumentException {
        return FileWriter.create(dest).writeFromStream(in);
    }

    /**
     * 将流的内容写入文件
     *
     * @param in           输入流
     * @param fullFilePath 文件绝对路径
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeFromStream(InputStream in, String fullFilePath) throws InstrumentException {
        return writeFromStream(in, touch(fullFilePath));
    }

    /**
     * 将文件写入流中
     *
     * @param file 文件
     * @param out  流
     * @return 目标文件
     * @throws InstrumentException 异常
     */
    public static File writeToStream(File file, OutputStream out) throws InstrumentException {
        return FileReader.create(file).writeToStream(out);
    }

    /**
     * 将流的内容写入文件
     *
     * @param fullFilePath 文件绝对路径
     * @param out          输出流
     * @throws InstrumentException 异常
     */
    public static void writeToStream(String fullFilePath, OutputStream out) throws InstrumentException {
        writeToStream(touch(fullFilePath), out);
    }

    /**
     * 可读的文件大小
     *
     * @param file 文件
     * @return 大小
     */
    public static String readableFileSize(File file) {
        return readableFileSize(file.length());
    }

    /**
     * 可读的文件大小
     * 参考 http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "EB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 转换文件编码
     * 此方法用于转换文件编码,读取的文件实际编码必须与指定的srcCharset编码一致,否则导致乱码
     *
     * @param file        文件
     * @param srcCharset  原文件的编码,必须与文件内容的编码保持一致
     * @param destCharset 转码后的编码
     * @return 被转换编码的文件
     * @since 3.1.9
     */
    public static File convertCharset(File file, Charset srcCharset, Charset destCharset) {
        return CharsetUtils.convert(file, srcCharset, destCharset);
    }

    /**
     * 转换换行符
     * 将给定文件的换行符转换为指定换行符
     *
     * @param file          文件
     * @param charset       编码
     * @param lineSeparator 换行符枚举
     * @return 被修改的文件
     * @since 3.1.9
     */
    public static File convertLineSeparator(File file, Charset charset, LineSeparator lineSeparator) {
        final List<String> lines = readLines(file, charset);
        return FileWriter.create(file, charset).writeLines(lines, lineSeparator, false);
    }

    /**
     * 清除文件名中的在Windows下不支持的非法字符,包括： \ / : * ? " &lt; &gt; |
     *
     * @param fileName 文件名（必须不包括路径,否则路径符将被替换）
     * @return 清理后的文件名
     * @since 3.3.1
     */
    public static String cleanInvalid(String fileName) {
        return StringUtils.isBlank(fileName) ? fileName : PatternUtils.delAll(FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * 文件名中是否包含在Windows下不支持的非法字符,包括： \ / : * ? " &lt; &gt; |
     *
     * @param fileName 文件名（必须不包括路径,否则路径符将被替换）
     * @return 是否包含非法字符
     * @since 3.3.1
     */
    public static boolean containsInvalid(String fileName) {
        return !StringUtils.isBlank(fileName) && PatternUtils.contains(FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * 计算文件CRC32校验码
     *
     * @param file 文件,不能为目录
     * @return CRC32值
     * @throws InstrumentException 异常
     */
    public static long checksumCRC32(File file) throws InstrumentException {
        return checksum(file, new CRC32()).getValue();
    }

    /**
     * 计算文件校验码
     *
     * @param file     文件,不能为目录
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InstrumentException 异常
     */
    public static Checksum checksum(File file, Checksum checksum) throws InstrumentException {
        Assert.notNull(file, "File is null !");
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        try {
            return IoUtils.checksum(new FileInputStream(file), checksum);
        } catch (FileNotFoundException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取Web项目下的web root路径
     * 原理是首先获取ClassPath路径,由于在web项目中ClassPath位于 WEB-INF/classes/下,故向上获取两级目录即可
     *
     * @return web root路径
     */
    public static File getWebRoot() {
        final String classPath = ClassUtils.getClassPath();
        if (StringUtils.isNotBlank(classPath)) {
            return getParent(file(classPath), 2);
        }
        return null;
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent("d:/aaa/bbb/cc/ddd", 0) - "d:/aaa/bbb/cc/ddd"
     * getParent("d:/aaa/bbb/cc/ddd", 2) - "d:/aaa/bbb"
     * getParent("d:/aaa/bbb/cc/ddd", 4) - "d:/"
     * getParent("d:/aaa/bbb/cc/ddd", 5) - null
     * </pre>
     *
     * @param filePath 目录或文件路径
     * @param level    层级
     * @return 路径File, 如果不存在返回null
     */
    public static String getParent(String filePath, int level) {
        final File parent = getParent(file(filePath), level);
        try {
            return null == parent ? null : parent.getCanonicalPath();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent(file("d:/aaa/bbb/cc/ddd", 0)) - "d:/aaa/bbb/cc/ddd"
     * getParent(file("d:/aaa/bbb/cc/ddd", 2)) - "d:/aaa/bbb"
     * getParent(file("d:/aaa/bbb/cc/ddd", 4)) - "d:/"
     * getParent(file("d:/aaa/bbb/cc/ddd", 5)) - null
     * </pre>
     *
     * @param file  目录或文件
     * @param level 层级
     * @return 路径File, 如果不存在返回null
     */
    public static File getParent(File file, int level) {
        if (level < 1 || null == file) {
            return file;
        }

        File parentFile;
        try {
            parentFile = file.getCanonicalFile().getParentFile();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
        if (1 == level) {
            return parentFile;
        }
        return getParent(parentFile, level - 1);
    }

    /**
     * 检查父完整路径是否为自路径的前半部分,如果不是说明不是子路径,可能存在slip注入
     * <p>
     * 见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parentFile 父文件或目录
     * @param file       子文件或目录
     * @return 子文件或目录
     * @throws IllegalArgumentException 检查创建的子文件不在父目录中抛出此异常
     */
    public static File checkSlip(File parentFile, File file) throws IllegalArgumentException {
        if (null != parentFile && null != file) {
            String parentCanonicalPath;
            String canonicalPath;
            try {
                parentCanonicalPath = parentFile.getCanonicalPath();
                canonicalPath = file.getCanonicalPath();
            } catch (IOException e) {
                throw new InstrumentException(e);
            }
            if (false == canonicalPath.startsWith(parentCanonicalPath)) {
                throw new IllegalArgumentException("New file is outside of the parent dir: " + file.getName());
            }
        }
        return file;
    }

    /**
     * 根据文件扩展名获得MimeType
     *
     * @param filePath 文件路径或文件名
     * @return MimeType
     */
    public static String getMimeType(String filePath) {
        return URLConnection.getFileNameMap().getContentTypeFor(filePath);
    }

    /**
     * 从完整的文件名中获取基名减去完整路径和扩展名.
     * <p>
     * 此方法将处理Unix或Windows格式的文件.
     * 最后一个正斜杠或反斜杠后面，最后一个点之前的文本.
     * <pre>
     * a/b/c.txt --&gt; c
     * a.txt     --&gt; a
     * a/b/c     --&gt; c
     * a/b/c/    --&gt; ""
     * </pre>
     * <p>
     *
     * @param filename 要查询的文件名null返回null
     * @return 没有路径的文件名，如果不存在空字符串，则为空字符串
     */
    public static String getBaseName(final String filename) {
        return removeExtension(getName(filename));
    }

    /**
     * 从文件名中删除扩展名.
     *
     * <p>
     * 此方法返回文件名最后一个点之前的文本部分,点之后必须没有目录分隔符.
     * </p>
     *
     * <pre>
     * foo.txt    --&gt; foo
     * a\b\c.jpg  --&gt; a\b\c
     * a\b\c      --&gt; a\b\c
     * a.b\c      --&gt; a.b\c
     * </pre>
     *
     * @param filename 要查询的文件名null返回null
     * @return 文件名减去扩展名
     */
    public static String removeExtension(final String filename) {
        if (filename == null) {
            return null;
        }

        final int len = filename.length();
        for (int i = 0; i < len; i++) {
            if (filename.charAt(i) == 0) {
                throw new IllegalArgumentException("Null byte present in file/path name. There are no " +
                        "known legitimate use cases for such data, but several injection attacks may use it");
            }
        }

        final int index = indexOfExtension(filename);
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

}
