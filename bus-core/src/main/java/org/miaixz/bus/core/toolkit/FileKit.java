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
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.io.LineHandler;
import org.miaixz.bus.core.io.file.FileReader;
import org.miaixz.bus.core.io.file.FileWriter;
import org.miaixz.bus.core.io.file.*;
import org.miaixz.bus.core.io.resource.ClassPathResource;
import org.miaixz.bus.core.io.resource.FileResource;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.resource.UriResource;
import org.miaixz.bus.core.io.stream.BOMInputStream;
import org.miaixz.bus.core.io.stream.EmptyOutputStream;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.function.XConsumer;

import java.io.*;
import java.lang.System;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * 文件工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileKit {

    /**
     * Windows下文件名中的无效字符
     */
    private static final Pattern FILE_NAME_INVALID_PATTERN_WIN = Pattern.compile("[\\\\/:*?\"<>|\r\n]");
    /**
     * 特殊后缀
     */
    private static final CharSequence[] SPECIAL_SUFFIX = {"tar.bz2", "tar.Z", "tar.gz", "tar.xz"};

    /**
     * 是否为Windows环境
     *
     * @return 是否为Windows环境
     */
    public static boolean isWindows() {
        return Symbol.C_BACKSLASH == File.separatorChar;
    }

    /**
     * 文件是否为空
     * 目录：里面没有文件时为空 文件：文件大小为0时为空
     *
     * @param file 文件
     * @return 是否为空, 当提供非目录时, 返回false
     */
    public static boolean isEmpty(File file) {
        if (null == file || false == file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            String[] subFiles = file.list();
            return ArrayKit.isEmpty(subFiles);
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
     * @throws InternalException IOException
     */
    public static boolean isDirEmpty(Path dirPath) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath)) {
            return false == dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new InternalException(e);
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
     * 列出指定路径下的目录和文件
     * 给定的绝对路径不能是压缩包中的路径
     *
     * @param path 目录绝对路径或者相对路径
     * @return 文件列表(包含目录)
     */
    public static File[] ls(String path) {
        if (null == path) {
            return null;
        }

        path = getAbsolutePath(path);

        File file = file(path);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        throw new InternalException(StringKit.format("Path [{}] is not directory!", path));
    }

    /**
     * 判断是否为文件,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果为文件true
     */
    public static boolean isFile(String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * 判断是否为文件,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(File file) {
        return (null != file) && file.isFile();
    }

    /**
     * 判断是否为文件,如果file为null,则返回false
     *
     * @param path          文件
     * @param isFollowLinks 是否跟踪软链(快捷方式)
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
     * 判断是否为目录,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果为目录true
     */
    public static boolean isDirectory(String path) {
        return (null != path) && file(path).isDirectory();
    }

    /**
     * 判断是否为目录，如果file为null，则返回false
     * 此方法不会追踪到软链对应的真实地址，即软链被当作文件
     *
     * @param path {@link Path}
     * @return 如果为目录true
     */
    public static boolean isDirectory(Path path) {
        return isDirectory(path, false);
    }

    /**
     * 判断是否为目录,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果为目录true
     */
    public static boolean isDirectory(File file) {
        return (null != file) && file.isDirectory();
    }

    /**
     * 判断是否为目录,如果file为null,则返回false
     *
     * @param path          {@link Path}
     * @param isFollowLinks 是否追踪到软链对应的真实地址
     * @return 如果为目录true
     */
    public static boolean isDirectory(Path path, boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.isDirectory(path, options);
    }

    /**
     * 判断是否为非目录
     * <ul>
     *     <li>如果path为{@code null}，返回{@code false}</li>
     *     <li>如果path不存在，返回{@code false}</li>
     * </ul>
     *
     * @param path          {@link Path}
     * @param isFollowLinks 是否追踪到软链对应的真实地址
     * @return 如果为目录true
     */
    public static boolean isExistsAndNotDirectory(final Path path, final boolean isFollowLinks) {
        return exists(path, isFollowLinks) && false == isDirectory(path, isFollowLinks);
    }

    /**
     * 创建File对象,自动识别相对或绝对路径,相对路径将自动从ClassPath下寻找
     *
     * @param path 文件路径
     * @return File
     */
    public static File file(String path) {
        if (StringKit.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return new File(getAbsolutePath(path));
    }

    /**
     * 通过多层目录创建文件
     * <p>
     * 元素名(多层目录名)
     *
     * @param names 文件名
     * @return the file 文件
     */
    public static File file(String... names) {
        if (ArrayKit.isEmpty(names)) {
            return null;
        }

        File file = null;
        for (String name : names) {
            if (null == file) {
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
        if (null == uri) {
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
        return new File(UriKit.toURI(url));
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
        if (StringKit.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return checkSlip(parent, buildFile(parent, path));
    }

    /**
     * 通过多层目录参数创建文件
     * 此方法会检查slip漏洞,漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param directory 父目录
     * @param names     元素名(多层目录名)
     * @return the file 文件
     */
    public static File file(File directory, String... names) {
        Assert.notNull(directory, "Directory must not be null");
        if (ArrayKit.isEmpty(names)) {
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
     * 递归遍历目录以及子目录中的所有文件
     *
     * @param path 当前遍历文件或目录的路径
     * @return 文件列表
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
     * 递归遍历目录以及子目录中的所有文件
     * 如果提供file为文件,直接返回过滤结果
     *
     * @param path       当前遍历文件或目录的路径
     * @param fileFilter 文件过滤规则对象,选择要保留的文件,只对文件有效,不过滤目录
     * @return 文件列表
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
        List<File> fileList = new ArrayList<>();
        if (null == file) {
            return fileList;
        } else if (false == file.exists()) {
            return fileList;
        }

        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isNotEmpty(subFiles)) {
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
     * 如果提供path为文件，直接返回过滤结果
     *
     * @param path       当前遍历文件或目录
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录，null表示接收全部文件
     * @return 文件列表
     */
    public static List<File> loopFiles(Path path, FileFilter fileFilter) {
        return loopFiles(path, -1, fileFilter);
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     * 如果提供file为文件，直接返回过滤结果
     *
     * @param path       当前遍历文件或目录
     * @param maxDepth   遍历最大深度，-1表示遍历到没有目录为止
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录，null表示接收全部文件
     * @return 文件列表
     */
    public static List<File> loopFiles(Path path, int maxDepth, final FileFilter fileFilter) {
        final List<File> fileList = new ArrayList<>();

        if (null == path || false == Files.exists(path)) {
            return fileList;
        } else if (false == isDirectory(path)) {
            final File file = path.toFile();
            if (null == fileFilter || fileFilter.accept(file)) {
                fileList.add(file);
            }
            return fileList;
        }

        walkFiles(path, maxDepth, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                final File file = path.toFile();
                if (null == fileFilter || fileFilter.accept(file)) {
                    fileList.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return fileList;
    }

    /**
     * 遍历指定path下的文件并做处理
     *
     * @param start   起始路径，必须为目录
     * @param visitor {@link FileVisitor} 接口，用于自定义在访问文件时，访问目录前后等节点做的操作
     * @see Files#walkFileTree(Path, Set, int, FileVisitor)
     */
    public static void walkFiles(Path start, FileVisitor<? super Path> visitor) {
        walkFiles(start, -1, visitor);
    }

    /**
     * 递归遍历目录并处理目录下的文件，可以处理目录或文件：
     * <ul>
     *     <li>非目录则直接调用{@link Consumer}处理</li>
     *     <li>目录则递归调用此方法处理</li>
     * </ul>
     *
     * @param file     文件或目录，文件直接处理
     * @param consumer 文件处理器，只会处理文件
     */
    public static void walkFiles(File file, Consumer<File> consumer) {
        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isNotEmpty(subFiles)) {
                for (File tmp : subFiles) {
                    walkFiles(tmp, consumer);
                }
            }
        } else {
            consumer.accept(file);
        }
    }

    /**
     * 遍历指定path下的文件并做处理
     *
     * @param start    起始路径，必须为目录
     * @param maxDepth 最大遍历深度，-1表示不限制深度
     * @param visitor  {@link FileVisitor} 接口，用于自定义在访问文件时，访问目录前后等节点做的操作
     * @see Files#walkFileTree(Path, Set, int, FileVisitor)
     */
    public static void walkFiles(Path start, int maxDepth, FileVisitor<? super Path> visitor) {
        if (maxDepth < 0) {
            maxDepth = Integer.MAX_VALUE;
        }

        try {
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), maxDepth, visitor);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 判断文件是否存在,如果path为null,则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exists(String path) {
        return (null != path) && file(path).exists();
    }

    /**
     * 判断文件是否存在,如果file为null,则返回false
     *
     * @param file 文件
     * @return 如果存在返回true
     */
    public static boolean exists(File file) {
        return (null != file) && file.exists();
    }

    /**
     * 是否存在匹配文件
     *
     * @param directory 文件夹路径
     * @param regexp    文件夹中所包含文件名的正则表达式
     * @return 如果存在匹配文件返回true
     */
    public static boolean exists(String directory, String regexp) {
        final File file = new File(directory);
        if (false == file.exists()) {
            return false;
        }

        final String[] fileList = file.list();
        if (null == fileList) {
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
     * 判断文件或目录是否存在
     *
     * @param path          文件
     * @param isFollowLinks 是否跟踪软链（快捷方式）
     * @return 是否存在
     */
    public static boolean exists(Path path, boolean isFollowLinks) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.exists(path, options);
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
     * @param path 文件的全路径,使用POSIX风格
     * @return 文件, 若路径为null, 返回null
     * @throws InternalException 异常
     */
    public static File touch(String path) throws InternalException {
        if (null == path) {
            return null;
        }
        return touch(file(path));
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param file 文件对象
     * @return 文件, 若路径为null, 返回null
     * @throws InternalException 异常
     */
    public static File touch(File file) throws InternalException {
        if (null == file) {
            return null;
        }
        if (false == file.exists()) {
            mkdir(file.getParentFile());
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new InternalException(e);
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
     * @throws InternalException 异常
     */
    public static File touch(File parent, String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * 创建文件及其父目录,如果这个文件存在,直接返回这个文件
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws InternalException 异常
     */
    public static File touch(String parent, String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * 删除文件或者文件夹
     * 路径如果为相对路径，会转换为ClassPath路径！ 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param fileOrPath 文件或者目录的路径
     * @throws InternalException IO异常
     */
    public static void delete(String fileOrPath) throws InternalException {
        delete(file(fileOrPath));
    }

    /**
     * 删除文件或者文件夹
     * 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param file 文件对象
     * @throws InternalException IO异常
     */
    public static void delete(File file) throws InternalException {
        Assert.notNull(file, "File must be not null!");
        delete(file.toPath());
    }

    /**
     * 删除文件或者文件夹
     * 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param path 文件对象
     * @throws InternalException IO异常
     */
    public static void delete(Path path) throws InternalException {
        FileDeleter.of(path).delete();
    }

    /**
     * 清空文件夹
     * 注意：清空文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param dirPath 文件夹路径
     * @throws InternalException 异常
     */
    public static void clean(String dirPath) throws InternalException {
        clean(file(dirPath));
    }

    /**
     * 清空文件夹
     * 注意：清空文件夹时不会判断文件夹是否为空,如果不空则递归删除子文件或文件夹
     * 某个文件删除失败会终止删除操作
     *
     * @param directory 文件夹
     * @throws InternalException 异常
     */
    public static void clean(File directory) throws InternalException {
        Assert.notNull(directory, "File must be not null!");
        clean(directory.toPath());
    }

    /**
     * 清空目录
     *
     * @param path 目录路径
     */
    public static void clean(final Path path) {
        FileDeleter.of(path).delete();
    }

    /**
     * 创建文件夹,如果存在直接返回此文件夹
     * 此方法不对File对象类型做判断,如果File不存在,无法判断其类型
     *
     * @param dirPath 文件夹路径,使用POSIX格式,无论哪个平台
     * @return 创建的目录
     */
    public static File mkdir(String dirPath) {
        if (null == dirPath) {
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
        if (null == dir) {
            return null;
        }
        if (false == dir.exists()) {
            mkdirsSafely(dir, 5, 1);
        }
        return dir;
    }

    /**
     * 创建所给目录及其父目录
     *
     * @param dir 目录
     * @return 目录
     */
    public static Path mkdir(Path dir) {
        if (null != dir && false == exists(dir, false)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }
        return dir;
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param path 文件或目录
     * @return 父目录
     */
    public static Path mkParentDirs(final Path path) {
        return mkdir(path.getParent());
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(File file) {
        if (null == file) {
            return null;
        }
        return mkdir(getParent(file, 1));
    }

    /**
     * 创建父文件夹，如果存在直接返回此文件夹
     *
     * @param path 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkParentDirs(String path) {
        if (path == null) {
            return null;
        }
        return mkParentDirs(file(path));
    }

    /**
     * 安全地级联创建目录 (确保并发环境下能创建成功)
     *
     * <pre>
     *     并发环境下，假设 test 目录不存在，如果线程A mkdirs "test/A" 目录，线程B mkdirs "test/B"目录，
     *     其中一个线程可能会失败，进而导致以下代码抛出 FileNotFoundException 异常
     *
     *     file.getParentFile().mkdirs(); // 父目录正在被另一个线程创建中，返回 false
     *     file.createNewFile(); // 抛出 IO 异常，因为该线程无法感知到父目录已被创建
     * </pre>
     *
     * @param dir         待创建的目录
     * @param tryCount    最大尝试次数
     * @param sleepMillis 线程等待的毫秒数
     * @return true表示创建成功，false表示创建失败
     */
    public static boolean mkdirsSafely(File dir, int tryCount, long sleepMillis) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            return true;
        }
        for (int i = 1; i <= tryCount; i++) { // 高并发场景下，可以看到 i 处于 1 ~ 3 之间
            // 如果文件已存在，也会返回 false，所以该值不能作为是否能创建的依据，因此不对其进行处理
            dir.mkdirs();
            if (dir.exists()) {
                return true;
            }
            ThreadKit.sleep(sleepMillis);
        }
        return dir.exists();
    }


    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir 临时文件创建的所在目录
     * @return 临时文件
     * @throws InternalException 异常
     */
    public static File createTempFile(File dir) throws InternalException {
        return createTempFile("create", null, dir, true);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Randon].tmp
     * 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定
     * 在 UNIX 系统上，此属性的默认值通常是 {@code "tmp"} 或 {@code "vartmp"}
     * 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}
     * 调用 Java 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响
     *
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile() throws InternalException {
        return createTempFile("bus", null, null, true);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Randon].suffix
     * 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定
     * 在 UNIX 系统上，此属性的默认值通常是 {@code "tmp"} 或 {@code "vartmp"}
     * 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}
     * 调用 Java 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响
     *
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(String suffix, boolean isReCreat) throws InternalException {
        return createTempFile("bus", suffix, null, isReCreat);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Randon].suffix
     * 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定
     * 在 UNIX 系统上，此属性的默认值通常是 {@code "tmp"} 或 {@code "vartmp"}
     * 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}
     * 调用 Java 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响
     *
     * @param prefix    前缀，至少3个字符
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(String prefix, String suffix, boolean isReCreat) throws InternalException {
        return createTempFile(prefix, suffix, null, isReCreat);
    }

    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].tmp
     *
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件(删掉原来的,创建新的)
     * @return 临时文件
     * @throws InternalException 异常
     */
    public static File createTempFile(File dir, boolean isReCreat) throws InternalException {
        return createTempFile("create", null, dir, isReCreat);
    }

    /**
     * 创建临时文件
     * 创建后的文件名为 prefix[Randon].suffix
     *
     * @param prefix    前缀,至少3个字符
     * @param suffix    后缀,如果null则使用默认.tmp
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件(删掉原来的,创建新的)
     * @return 临时文件
     * @throws InternalException 异常
     */
    public static File createTempFile(String prefix, String suffix, File dir, boolean isReCreat) throws InternalException {
        int exceptionsCount = 0;
        while (true) {
            try {
                File file = File.createTempFile(prefix, suffix, mkdir(dir)).getCanonicalFile();
                if (isReCreat) {
                    file.delete();
                    file.createNewFile();
                }
                return file;
            } catch (IOException ex) {
                if (++exceptionsCount >= 50) {
                    throw new InternalException(ex);
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
     * @throws InternalException 异常
     */
    public static File copyFile(String src, String dest, StandardCopyOption... options) throws InternalException {
        Assert.notBlank(src, "Source File path is blank !");
        Assert.notBlank(dest, "Destination File path is blank !");
        return copyFile(Paths.get(src), Paths.get(dest), options).toFile();
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件
     * @param dest    目标文件或目录,如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static File copyFile(File src, File dest, StandardCopyOption... options) throws InternalException {
        Assert.notNull(src, "Source File is null !");
        if (false == src.exists()) {
            throw new InternalException("File not exist: " + src);
        }
        Assert.notNull(dest, "Destination File or directory is null !");
        if (equals(src, dest)) {
            throw new InternalException("Files '{}' and '{}' are equal", src, dest);
        }
        return copyFile(src.toPath(), dest.toPath(), options).toFile();
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件
     *
     * @param src     源文件路径
     * @param target  目标文件或目录,如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return Path
     * @throws InternalException 异常
     */
    public static Path copyFile(Path src, Path target, StandardCopyOption... options) throws InternalException {
        return FileCopier.of(src, target, options).copy();
    }

    /**
     * 将字节从<code>File</code>复制到<code>OutputStream</code>
     * 这个方法在内部缓冲输入，所以不需要使用 <code>BufferedInputStream</code>
     *
     * @param input  要读取的 <code>File</code>
     * @param output 要写入的输出流 <code>OutputStream</code>
     * @return 复制的字节数
     * @throws NullPointerException 如果输入或输出为空
     * @throws IOException          如果发生I/O错误
     */
    public static long copyFile(final File input, final OutputStream output) throws IOException {
        try (FileInputStream fis = new FileInputStream(input)) {
            return IoKit.copy(fis, output);
        }
    }

    /**
     * 复制文件或目录
     * 如果目标文件为目录,则将源文件以相同文件名拷贝到目标目录
     *
     * @param srcPath    源文件或目录
     * @param destPath   目标文件或目录,目标不存在会自动创建(目录、文件都创建)
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException 异常
     */
    public static File copy(String srcPath, String destPath, boolean isOverride) throws InternalException {
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
     * @param dest       目标文件或目录,目标不存在会自动创建(目录、文件都创建)
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException 异常
     */
    public static File copy(File src, File dest, boolean isOverride) throws InternalException {
        return FileCopier.of(src.toPath(), dest.toPath(), isOverride).copy().toFile();
    }

    /**
     * 修改文件或目录的文件名，不变更路径，只是简单修改文件名，不保留扩展名
     *
     * <pre>
     * FileKit.rename(file, "aaa.png", true) xx/xx.png =》xx/aaa.png
     * </pre>
     *
     * @param file       被修改的文件
     * @param newName    新的文件名，如需扩展名，需自行在此参数加上，原文件名的扩展名不会被保留
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件
     */
    public static File rename(File file, String newName, boolean isOverride) {
        return rename(file, newName, false, isOverride);
    }

    /**
     * 修改文件或目录的文件名，不变更路径，只是简单修改文件名
     *
     * <pre>
     * FileKit.rename(file, "aaa.jpg", false) xx/xx.png =》xx/aaa.jpg
     * </pre>
     *
     * @param path       被修改的文件
     * @param newName    新的文件名，包括扩展名
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件Path
     */
    public static Path rename(Path path, String newName, boolean isOverride) {
        return move(path, path.resolveSibling(newName), isOverride);
    }

    /**
     * 修改文件或目录的文件名，不变更路径，只是简单修改文件名
     * 重命名有两种模式：
     * 1、isRetainExt为true时，保留原扩展名：
     *
     * <pre>
     * FileKit.rename(file, "aaa", true) xx/xx.png =》xx/aaa.png
     * </pre>
     *
     * <p>
     * 2、isRetainExt为false时，不保留原扩展名，需要在newName中
     *
     * <pre>
     * FileKit.rename(file, "aaa.jpg", false) xx/xx.png =》xx/aaa.jpg
     * </pre>
     *
     * @param file        被修改的文件
     * @param newName     新的文件名，包括扩展名
     * @param isRetainExt 是否保留原文件的扩展名，如果保留，则newName不需要加扩展名
     * @param isOverride  是否覆盖目标文件
     * @return 目标文件
     */
    public static File rename(File file, String newName, boolean isRetainExt, boolean isOverride) {
        if (isRetainExt) {
            final String suffix = getSuffix(file);
            if (StringKit.isNotBlank(suffix)) {
                newName = newName.concat(Symbol.DOT).concat(suffix);
            }
        }
        return rename(file.toPath(), newName, isOverride).toFile();
    }

    /**
     * 移动文件或目录到目标中，例如：
     * <ul>
     *     <li>如果src为文件，target为目录，则移动到目标目录下，存在同名文件则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为文件，则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为不存在的路径，则重命名源文件到目标指定的文件，如moveContent("/a/b", "/c/d"), d不存在，则b变成d</li>
     *     <li>如果src为目录，target为文件，抛出{@link IllegalArgumentException}</li>
     *     <li>如果src为目录，target为目录，则将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     *     <li>如果src为目录，target为不存在的路径，则创建目标路径为目录，将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     * </ul>
     *
     * @param src        源文件或目录路径
     * @param target     目标路径，如果为目录，则移动到此目录下
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件或目录
     */
    public static File move(final File src, final File target, final boolean isOverride) {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return move(src.toPath(), target.toPath(), isOverride).toFile();
    }

    /**
     * 移动文件或目录到目标中，例如：
     * <ul>
     *     <li>如果src为文件，target为目录，则移动到目标目录下，存在同名文件则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为文件，则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为不存在的路径，则重命名源文件到目标指定的文件，如moveContent("/a/b", "/c/d"), d不存在，则b变成d</li>
     *     <li>如果src为目录，target为文件，抛出{@link IllegalArgumentException}</li>
     *     <li>如果src为目录，target为目录，则将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     *     <li>如果src为目录，target为不存在的路径，则创建目标路径为目录，将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     * </ul>
     *
     * @param src        源文件或目录路径
     * @param target     目标路径，如果为目录，则移动到此目录下
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件Path
     */
    public static Path move(final Path src, final Path target, final boolean isOverride) {
        return FileMover.of(src, target, isOverride).move();
    }

    /**
     * 移动文件或目录内容到目标中，例如：
     * <ul>
     *     <li>如果src为文件，target为目录，则移动到目标目录下，存在同名文件则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为文件，则按照是否覆盖参数执行</li>
     *     <li>如果src为文件，target为不存在的路径，则重命名源文件到目标指定的文件，如moveContent("/a/b", "/c/d"), d不存在，则b变成d</li>
     *     <li>如果src为目录，target为文件，抛出{@link IllegalArgumentException}</li>
     *     <li>如果src为目录，target为目录，则将源目录下的内容移动到目标路径目录中</li>
     *     <li>如果src为目录，target为不存在的路径，则创建目标路径为目录，将源目录下的内容移动到目标路径目录中</li>
     * </ul>
     *
     * @param src        源文件或目录路径
     * @param target     目标路径，如果为目录，则移动到此目录下
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件Path
     */
    public static Path moveContent(final Path src, final Path target, final boolean isOverride) {
        return FileMover.of(src, target, isOverride).moveContent();
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
            throw new InternalException(e);
        }
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
     * 获取绝对路径
     * 此方法不会判定给定路径是否有效(文件或目录存在)
     *
     * @param path      相对路径
     * @param baseClass 相对路径所相对的类
     * @return 绝对路径
     */
    public static String getAbsolutePath(String path, Class<?> baseClass) {
        String normalPath;
        if (null == path) {
            normalPath = Normal.EMPTY;
        } else {
            normalPath = normalize(path);
            if (isAbsolutePath(normalPath)) {
                return normalPath;
            }
        }

        final URL url = getUrl(normalPath, baseClass);
        if (null != url) {
            return FileKit.normalize(UriKit.getDecodedPath(url));
        }

        final String classPath = ClassKit.getClassPath();
        if (null == classPath) {
            return path;
        }
        return normalize(classPath.concat(path));
    }

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {
        if (null == file) {
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
     * 此方法并没有针对路径做标准化，建议先执行{@link #normalize(String)}方法标准化路径后判断
     * 绝对路径判断条件是：
     * <ul>
     *     <li>以/开头的路径</li>
     *     <li>满足类似于 c:/xxxxx，其中祖母随意，不区分大小写</li>
     *     <li>满足类似于 d:\xxxxx，其中祖母随意，不区分大小写</li>
     * </ul>
     *
     * @param path 需要检查的Path
     * @return 是否已经是绝对路径
     */
    public static boolean isAbsolutePath(String path) {
        if (StringKit.isEmpty(path)) {
            return false;
        }
        return Symbol.C_SLASH == path.charAt(0) || PatternKit.isMatch("^[a-zA-Z]:[/\\\\].*", path);
    }


    /**
     * 检查两个文件是否是同一个文件
     * 所谓文件相同，是指Path对象是否指向同一个文件或文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     * @throws InternalException IO异常
     * @see Files#isSameFile(Path, Path)
     */
    public static boolean equals(final Path file1, final Path file2) throws InternalException {
        try {
            return Files.isSameFile(file1, file2);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 检查两个文件是否是同一个文件
     * 所谓文件相同,是指File对象是否指向同一个文件或文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     * @throws InternalException 异常
     * @see Files#isSameFile(Path, Path)
     */
    public static boolean equals(File file1, File file2) throws InternalException {
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
            throw new InternalException(e);
        }
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
            String tempString;
            String all = Normal.EMPTY;
            // 一次读入一行,直到读入null为文件结束
            while (null != (tempString = reader.readLine())) {
                // 显示行号
                all += tempString;
            }
            reader.close();
            return all;
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    /**
     * 获得指定目录下所有文件
     * 不会扫描子目录
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 文件路径列表(如果是jar中的文件, 则给定类似.jar ! / xxx / xxx的路径)
     * @throws InternalException 异常
     */
    public static List<String> listFileNames(String path) throws InternalException {
        if (null == path) {
            return new ArrayList<>(0);
        }
        int index = path.lastIndexOf(FileType.JAR_PATH_EXT);
        if (index < 0) {
            // 普通目录
            final List<String> paths = new ArrayList<>();
            final File[] files = ls(path);
            for (File file : files) {
                if (file.isFile()) {
                    paths.add(file.getName());
                }
            }
            return paths;
        }

        // jar文件
        path = getAbsolutePath(path);
        // jar文件中的路径
        index = index + FileType.JAR_PATH_EXT.length();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(path.substring(0, index));
            // 防止出现jar!/org/miaixz/这类路径导致文件找不到
            return ZipKit.listFileNames(jarFile, StringKit.removePrefix(path.substring(index + 1), Symbol.SLASH));
        } catch (IOException e) {
            throw new InternalException(StringKit.format("Can not read file path of [{}]", path), e);
        } finally {
            IoKit.close(jarFile);
        }
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
     * 获取临时文件路径(绝对路径)
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
     * 获取用户路径(绝对路径)
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
     * 返回最后一个扩展分隔符的索引点.
     *
     * @param filename 查找最后一个路径分隔符的文件名
     * @return 最后一个分隔符字符的索引，如果没有这样的字符，则为-1
     */
    public static int indexOfSuffix(String filename) {
        if (null == filename) {
            return Normal.__1;
        }
        int extensionPos = filename.lastIndexOf(Symbol.DOT);
        int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? -1 : extensionPos;
    }

    /**
     * 返回最后一个目录分隔符的索引
     * <p>
     * 此方法将处理Unix或Windows格式的文件
     * 返回最后一个正斜杠或反斜杠的位置.
     * </p>
     *
     * @param filename 查找最后一个路径分隔符的文件名
     * @return 最后一个分隔符字符的索引，如果没有这样的字符，则为-1
     */
    public static int indexOfLastSeparator(String filename) {
        if (null == filename) {
            return Normal.__1;
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
        if (false == exists(file)) {
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
     * 当给定对象为文件时，直接调用 {@link File#length()}
     * 当给定对象为目录时，遍历目录下的所有文件和目录，递归计算其大小，求和返回
     * 此方法不包括目录本身的占用空间大小
     *
     * @param file 目录或文件,null或者文件不存在返回0
     * @return 总大小，bytes长度
     */
    public static long size(File file) {
        return size(file, false);
    }

    /**
     * 计算目录或文件的总大小
     * 当给定对象为文件时,直接调用 {@link File#length()}
     * 当给定对象为目录时,遍历目录下的所有文件和目录,递归计算其大小,求和返回
     *
     * @param file           目录或文件
     * @param includeDirSize 是否包括每层目录本身的大小
     * @return 总大小, bytes长度
     */
    public static long size(File file, boolean includeDirSize) {
        if (null == file || false == file.exists() || isSymlink(file)) {
            return 0;
        }

        if (file.isDirectory()) {
            long size = includeDirSize ? file.length() : 0;
            File[] subFiles = file.listFiles();
            if (ArrayKit.isEmpty(subFiles)) {
                return 0L;
            }
            for (File subFile : subFiles) {
                size += size(subFile, includeDirSize);
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * 判断是否为符号链接文件
     *
     * @param file 被检查的文件
     * @return 是否为符号链接文件
     */
    public static boolean isSymlink(File file) {
        return Files.isSymbolicLink(file.toPath());
    }

    /**
     * 比较两个文件内容是否相同
     * 首先比较长度,长度一致再比较内容
     * 此方法来自Apache Commons io
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 两个文件内容一致返回true, 否则false
     * @throws InternalException 异常
     */
    public static boolean contentEquals(File file1, File file2) throws InternalException {
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
            throw new InternalException("Can't compare directories, only file");
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
            return IoKit.contentEquals(input1, input2);

        } finally {
            IoKit.close(input1);
            IoKit.close(input2);
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
     * @throws InternalException 异常
     */
    public static boolean contentEqualsIgnoreEOL(File file1, File file2, java.nio.charset.Charset charset) throws InternalException {
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
            throw new InternalException("Can't compare directories, only file");
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
            return IoKit.contentEqualsIgnoreEOL(input1, input2);
        } finally {
            IoKit.close(input1);
            IoKit.close(input2);
        }
    }

    /**
     * 文件路径是否相同
     * 取两个文件的绝对路径比较,在Windows下忽略大小写,在Linux下不忽略
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 文件路径是否相同
     */
    public static boolean pathEquals(File file1, File file2) {
        if (isWindows()) {
            // Windows环境
            try {
                if (StringKit.equalsIgnoreCase(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringKit.equalsIgnoreCase(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        } else {
            // 类Unix环境
            try {
                if (StringKit.equals(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringKit.equals(file1.getAbsolutePath(), file2.getAbsolutePath())) {
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
        if (StringKit.isNotEmpty(filePath)) {
            int i = filePath.length();
            char c;
            while (i-- >= 0) {
                c = filePath.charAt(i);
                if (CharsKit.isFileSeparator(c)) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 判断文件是否被改动
     * 如果文件对象为 null 或者文件不存在,被视为改动
     *
     * @param file           文件对象
     * @param lastModifyTime 上次的改动时间
     * @return 是否被改动
     */
    public static boolean isModified(File file, long lastModifyTime) {
        if (null == file || false == file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * 修复路径
     * 如果原路径尾部有分隔符,则保留为标准分隔符(/),否则不保留
     * <ol>
     * <li>1. 统一用 /</li>
     * <li>2. 多个 / 转换为一个 /</li>
     * <li>3. 去除左边空格</li>
     * <li>4. .. 和 . 转换为绝对路径,当..多于已有路径时,直接返回根路径</li>
     * </ol>
     * <p>
     * 栗子：
     *
     * <pre>
     * "/foo//" =  "/foo/"
     * "/foo/./" =  "/foo/"
     * "/foo/../bar" =  "/bar"
     * "/foo/../bar/" =  "/bar/"
     * "/foo/../bar/../baz" =  "/baz"
     * "foo/bar/.." =  "foo"
     * "foo/../bar" =  "bar"
     * "foo/../../bar" =  "bar"
     * "//server/foo/../bar" =  "/server/bar"
     * "//server/../bar" =  "/bar"
     * "~/foo/../bar/" =  "~/bar/"
     * "~/../bar" =》 普通用户运行是'bar的home目录'，ROOT用户运行是'/bar'
     * </pre>
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(String path) {
        if (null == path) {
            return null;
        }

        // 兼容Spring风格的ClassPath路径，去除前缀，不区分大小写
        String pathToUse = StringKit.removePrefixIgnoreCase(path, Normal.CLASSPATH);
        // 去除file:前缀
        pathToUse = StringKit.removePrefixIgnoreCase(pathToUse, Normal.FILE_URL_PREFIX);

        // 识别home目录形式，并转换为绝对路径
        if (StringKit.startWith(pathToUse, Symbol.TILDE)) {
            pathToUse = getUserHomePath() + pathToUse.substring(1);
        }

        // 统一使用斜杠
        pathToUse = pathToUse.replaceAll("[/\\\\]+", Symbol.SLASH);
        // 去除开头空白符，末尾空白符合法，不去除
        pathToUse = StringKit.trim(pathToUse, Normal.__1);
        //兼容Windows下的共享目录路径（原始路径如果以\\开头，则保留这种路径）
        if (path.startsWith("\\\\")) {
            pathToUse = Symbol.BACKSLASH + pathToUse;
        }

        String prefix = Normal.EMPTY;
        int prefixIndex = pathToUse.indexOf(Symbol.COLON);
        if (prefixIndex > Normal.__1) {
            // 可能Windows风格路径
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (StringKit.startWith(prefix, Symbol.C_SLASH)) {
                // 去除类似于/C:这类路径开头的斜杠
                prefix = prefix.substring(1);
            }
            if (false == prefix.contains(Symbol.SLASH)) {
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

        List<String> pathList = StringKit.split(pathToUse, Symbol.C_SLASH);
        List<String> pathElements = new LinkedList<>();
        int tops = 0;

        String element;
        for (int i = pathList.size() - 1; i >= 0; i--) {
            element = pathList.get(i);
            // 只处理非.的目录，即只处理非当前目录
            if (false == Symbol.DOT.equals(element)) {
                if (Symbol.DOUBLE_DOT.equals(element)) {
                    tops++;
                } else {
                    if (tops > 0) {
                        // 有上级目录标记时按照个数依次跳过
                        tops--;
                    } else {
                        pathElements.add(0, element);
                    }
                }
            }
        }

        if (tops > 0 && StringKit.isEmpty(prefix)) {
            // 只有相对路径补充开头的..，绝对路径直接忽略之
            while (tops-- > 0) {
                //遍历完节点发现还有上级标注（即开头有一个或多个..），补充之
                pathElements.add(0, Symbol.DOUBLE_DOT);
            }
        }

        return prefix + CollKit.join(pathElements, Symbol.SLASH);
    }

    /**
     * 获得相对子路径
     * <p>
     * 栗子：
     *
     * <pre>
     * dirPath: /data/aaa/bbb    filePath: /data/aaa/bbb/ccc     =     ccc
     * dirPath: /data/Aaa/bbb    filePath: /data/aaa/bbb/ccc.txt     =     ccc.txt
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
            throw new InternalException(e);
        }
    }

    /**
     * 获得相对子路径,忽略大小写
     * <p>
     * 栗子：
     *
     * <pre>
     * dirPath: /data/aaa/bbb    filePath: /data/aaa/bbb/ccc     =     ccc
     * dirPath: /data/Aaa/bbb    filePath: /data/aaa/bbb/ccc.txt     =     ccc.txt
     * dirPath: /data/Aaa/bbb    filePath: /data/aaa/bbb/     =     ""
     * </pre>
     *
     * @param dirPath  父路径
     * @param filePath 文件路径
     * @return 相对子路径
     */
    public static String subPath(String dirPath, String filePath) {
        if (StringKit.isNotEmpty(dirPath) && StringKit.isNotEmpty(filePath)) {

            dirPath = StringKit.removeSuffix(normalize(dirPath), Symbol.SLASH);
            filePath = normalize(filePath);

            final String result = StringKit.removePrefixIgnoreCase(filePath, dirPath);
            return StringKit.removePrefix(result, Symbol.SLASH);
        }
        return filePath;
    }

    /**
     * 获取指定位置的子路径部分,支持负数,例如index为-1表示从后数第一个节点位置
     *
     * @param path  路径
     * @param index 路径节点位置,支持负数(负数从后向前计数)
     * @return 获取的子路径
     */
    public static Path getPathEle(Path path, int index) {
        return subPath(path, index, index == Normal.__1 ? path.getNameCount() : index + 1);
    }

    /**
     * 获取指定位置的最后一个子路径部分
     *
     * @param path 路径
     * @return 获取的最后一个子路径
     */
    public static Path getLastPathEle(Path path) {
        return getPathEle(path, path.getNameCount() - 1);
    }

    /**
     * 获取指定位置的子路径部分,支持负数,例如起始为-1表示从后数第一个节点位置
     *
     * @param path      路径
     * @param fromIndex 起始路径节点(包括)
     * @param toIndex   结束路径节点(不包括)
     * @return 获取的子路径
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
        if (CharsKit.isFileSeparator(filePath.charAt(len - 1))) {
            //以分隔符结尾的去掉结尾分隔符
            len--;
        }

        int begin = 0;
        char c;
        for (int i = len - 1; i > Normal.__1; i--) {
            c = filePath.charAt(i);
            if (CharsKit.isFileSeparator(c)) {
                //查找最后一个路径分隔符(/或者\)
                begin = i + 1;
                break;
            }
        }

        return filePath.substring(begin, len);
    }

    /**
     * 获取{@link Path}文件名
     *
     * @param path {@link Path}
     * @return 文件名
     */
    public static String getName(Path path) {
        if (null == path) {
            return null;
        }
        return path.getFileName().toString();
    }

    /**
     * 获取文件后缀名，扩展名不带“.”
     *
     * @param file 文件
     * @return 扩展名
     */
    public static String getSuffix(File file) {
        if (null == file) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        return getSuffix(file.getName());
    }

    /**
     * 获得文件后缀名，扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getSuffix(String fileName) {
        if (null == fileName) {
            return null;
        }
        final int index = fileName.lastIndexOf(Symbol.DOT);
        if (index == Normal.__1) {
            return Normal.EMPTY;
        } else {
            final int secondToLastIndex = fileName.substring(0, index).lastIndexOf(Symbol.DOT);
            final String substr = fileName.substring(secondToLastIndex == -1 ? index : secondToLastIndex + 1);
            if (StringKit.containsAny(substr, SPECIAL_SUFFIX)) {
                return substr;
            }
            String ext = fileName.substring(index + 1);
            // 扩展名中不能包含路径相关的符号
            return StringKit.containsAny(ext, Symbol.C_SLASH, Symbol.C_BACKSLASH) ? Normal.EMPTY : ext;
        }
    }

    /**
     * 返回主文件名
     *
     * @param file 文件
     * @return 主文件名
     */
    public static String getPrefix(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        return getPrefix(file.getName());
    }

    /**
     * 返回主文件名
     *
     * @param fileName 完整文件名
     * @return 主文件名
     */
    public static String getPrefix(String fileName) {
        if (null == fileName) {
            return fileName;
        }
        int len = fileName.length();
        if (0 == len) {
            return fileName;
        }

        // 多级扩展名的主文件名
        for (final CharSequence specialSuffix : SPECIAL_SUFFIX) {
            if (StringKit.endWith(fileName, "." + specialSuffix)) {
                return StringKit.subPre(fileName, len - specialSuffix.length() - 1);
            }
        }

        int begin = 0;
        int end = len;
        char c;
        for (int i = len - 1; i > Normal.__1; i--) {
            c = fileName.charAt(i);
            if (len == end && Symbol.C_DOT == c) {
                // 查找最后一个文件名和扩展名的分隔符：.
                end = i;
            }
            if (0 == begin || begin > end) {
                if (CharsKit.isFileSeparator(c)) {
                    // 查找最后一个路径分隔符(/或者\),如果这个分隔符在.之后,则继续查找,否则结束
                    begin = i + 1;
                    break;
                }
            }
        }

        return fileName.substring(begin, end);
    }

    /**
     * 判断文件路径是否有指定后缀,忽略大小写
     * 常用语判断扩展名
     *
     * @param file   文件或目录
     * @param suffix 后缀
     * @return 是否有指定后缀
     */
    public static boolean endsWith(File file, String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * @param file 文件 {@link File}
     * @return 类型, 文件的扩展名, 未找到为<code>null</code>
     * @throws InternalException 异常
     * @see FileType#getType(File)
     */
    public static String getType(File file) throws InternalException {
        return FileType.getType(file);
    }

    /**
     * 获取文件属性
     *
     * @param path          文件路径{@link Path}
     * @param isFollowLinks 是否跟踪到软链对应的真实路径
     * @return {@link BasicFileAttributes}
     * @throws InternalException 异常
     */
    public static BasicFileAttributes getAttributes(Path path, boolean isFollowLinks) throws InternalException {
        if (null == path) {
            return null;
        }

        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        try {
            return Files.readAttributes(path, BasicFileAttributes.class, options);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得输入流
     *
     * @param path Path
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BufferedInputStream getInputStream(Path path) throws InternalException {
        try {
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BufferedInputStream getInputStream(File file) throws InternalException {
        return new BufferedInputStream(IoKit.toStream(file));
    }

    /**
     * 获得输入流
     *
     * @param path 文件路径
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BufferedInputStream getInputStream(String path) throws InternalException {
        return getInputStream(file(path));
    }

    /**
     * 获得BOM输入流,用于处理带BOM头的文件
     *
     * @param file 文件
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BOMInputStream getBOMInputStream(File file) throws InternalException {
        try {
            return new BOMInputStream(Files.newInputStream(file.toPath()));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取带BOM头的文件为Reader
     *
     * @param file 文件
     * @return BufferedReader对象
     */
    public static BufferedReader getBOMReader(File file) {
        return IoKit.getReader(getBOMInputStream(file));
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件Path
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(Path path) throws InternalException {
        return getReader(path, Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file 文件
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(File file) throws InternalException {
        return getReader(file, Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件路径
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(String path) throws InternalException {
        return getReader(path, Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    文件Path
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(Path path, java.nio.charset.Charset charset) throws InternalException {
        return IoKit.getReader(getInputStream(path), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(File file, String charsetName) throws InternalException {
        return IoKit.getReader(getInputStream(file), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file    文件
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(File file, java.nio.charset.Charset charset) throws InternalException {
        return IoKit.getReader(getInputStream(file), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path        绝对路径
     * @param charsetName 字符集
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(String path, String charsetName) throws InternalException {
        return getReader(file(path), charsetName);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    绝对路径
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedReader getReader(String path, java.nio.charset.Charset charset) throws InternalException {
        return getReader(file(path), charset);
    }

    /**
     * 读取文件所有数据
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param file 文件
     * @return 字节码
     * @throws InternalException 异常
     */
    public static byte[] readBytes(File file) throws InternalException {
        return FileReader.create(file).readBytes();
    }

    /**
     * 读取文件所有数据
     * 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param filePath 文件路径
     * @return 字节码
     * @throws InternalException 异常
     */
    public static byte[] readBytes(String filePath) throws InternalException {
        return readBytes(file(filePath));
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(File file) throws InternalException {
        return readString(file, Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(String path) throws InternalException {
        return readString(path, Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param file        文件
     * @param charsetName 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(File file, String charsetName) throws InternalException {
        return readString(file, Charset.charset(charsetName));
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(File file, java.nio.charset.Charset charset) throws InternalException {
        return FileReader.create(file, charset).readString();
    }

    /**
     * 读取文件内容
     *
     * @param path        文件路径
     * @param charsetName 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(String path, String charsetName) throws InternalException {
        return readString(file(path), charsetName);
    }

    /**
     * 读取文件内容
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(String path, java.nio.charset.Charset charset) throws InternalException {
        return readString(file(path), charset);
    }

    /**
     * 读取文件内容
     *
     * @param url     文件URL
     * @param charset 字符集
     * @return 内容
     * @throws InternalException 异常
     */
    public static String readString(URL url, String charset) throws InternalException {
        if (null == url) {
            throw new NullPointerException("Empty url provided!");
        }

        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.read(in, charset);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * 从文件中读取每一行的UTF-8编码数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(String path, T collection) throws InternalException {
        return readLines(path, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(String path, String charset, T collection) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(String path, java.nio.charset.Charset charset, T collection) throws InternalException {
        return readLines(file(path), charset, collection);
    }

    /**
     * 从文件中读取每一行数据,数据编码为UTF-8
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(File file, T collection) throws InternalException {
        return readLines(file, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(File file, String charset, T collection) throws InternalException {
        return FileReader.create(file, Charset.charset(charset)).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(File file, java.nio.charset.Charset charset, T collection) throws InternalException {
        return FileReader.create(file, charset).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据,编码为UTF-8
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(URL url, T collection) throws InternalException {
        return readLines(url, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>         集合类型
     * @param url         文件的URL
     * @param charsetName 字符集
     * @param collection  集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(URL url, String charsetName, T collection) throws InternalException {
        return readLines(url, Charset.charset(charsetName), collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException 异常
     */
    public static <T extends Collection<String>> T readLines(URL url, java.nio.charset.Charset charset, T collection) throws InternalException {
        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.readLines(in, charset, collection);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(in);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url 文件的URL
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(URL url) throws InternalException {
        return readLines(url, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(URL url, String charset) throws InternalException {
        return readLines(url, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(URL url, java.nio.charset.Charset charset) throws InternalException {
        return readLines(url, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据,编码为UTF-8
     *
     * @param path 文件路径
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(String path) throws InternalException {
        return readLines(path, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(String path, String charset) throws InternalException {
        return readLines(path, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(String path, java.nio.charset.Charset charset) throws InternalException {
        return readLines(path, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file 文件
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(File file) throws InternalException {
        return readLines(file, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(File file, String charset) throws InternalException {
        return readLines(file, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException 异常
     */
    public static List<String> readLines(File file, java.nio.charset.Charset charset) throws InternalException {
        return readLines(file, charset, new ArrayList<>());
    }

    /**
     * 按行处理文件内容,编码为UTF-8
     *
     * @param file        文件
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InternalException 异常
     */
    public static void readLines(File file, XConsumer<String> lineHandler) throws InternalException {
        readLines(file, Charset.UTF_8, lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InternalException 异常
     */
    public static void readLines(File file, java.nio.charset.Charset charset, XConsumer<String> lineHandler) throws InternalException {
        FileReader.create(file, charset).readLines(lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     * @throws InternalException 异常
     */
    public static void readLines(RandomAccessFile file, java.nio.charset.Charset charset, XConsumer<String> lineHandler) {
        String line;
        try {
            while (null != (line = file.readLine())) {
                lineHandler.accept(Charset.convert(line, Charset.ISO_8859_1, charset));
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link LineHandler}行处理器
     */
    public static void readLine(RandomAccessFile file, java.nio.charset.Charset charset, XConsumer<String> lineHandler) {
        final String line = readLine(file, charset);
        if (null != line) {
            lineHandler.accept(line);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file    {@link RandomAccessFile}文件
     * @param charset 编码
     * @return 行内容
     */
    public static String readLine(RandomAccessFile file, java.nio.charset.Charset charset) {
        String line;
        try {
            line = file.readLine();
        } catch (IOException e) {
            throw new InternalException(e);
        }
        if (null != line) {
            return Charset.convert(line, Charset.ISO_8859_1, charset);
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
     * @throws InternalException 异常
     */
    public static <T> T load(String path, FileReader.ReaderHandler<T> readerHandler) throws InternalException {
        return load(path, Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InternalException 异常
     */
    public static <T> T load(String path, String charset, FileReader.ReaderHandler<T> readerHandler) throws InternalException {
        return FileReader.create(file(path), Charset.charset(charset)).read(readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InternalException 异常
     */
    public static <T> T load(String path, java.nio.charset.Charset charset, FileReader.ReaderHandler<T> readerHandler) throws InternalException {
        return FileReader.create(file(path), charset).read(readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @return 从文件中load出的数据
     * @throws InternalException 异常
     */
    public static <T> T load(File file, FileReader.ReaderHandler<T> readerHandler) throws InternalException {
        return load(file, Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InternalException 异常
     */
    public static <T> T load(File file, java.nio.charset.Charset charset, FileReader.ReaderHandler<T> readerHandler) throws InternalException {
        return FileReader.create(file, charset).read(readerHandler);
    }

    /**
     * 获得一个输出流对象
     *
     * @param file 文件
     * @return 输出流对象
     * @throws InternalException 异常
     */
    public static BufferedOutputStream getOutputStream(File file) throws InternalException {
        try {
            return new BufferedOutputStream(Files.newOutputStream(touch(file).toPath()));
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得一个输出流对象
     *
     * @param path 输出到的文件路径,绝对路径
     * @return 输出流对象
     * @throws InternalException 异常
     */
    public static BufferedOutputStream getOutputStream(String path) throws InternalException {
        return getOutputStream(touch(path));
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path        输出路径,绝对路径
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedWriter getWriter(String path, String charsetName, boolean isAppend) throws InternalException {
        return getWriter(touch(path), java.nio.charset.Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedWriter getWriter(String path, java.nio.charset.Charset charset, boolean isAppend) throws InternalException {
        return getWriter(touch(path), charset, isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file        输出文件
     * @param charsetName 字符集
     * @param isAppend    是否追加
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedWriter getWriter(File file, String charsetName, boolean isAppend) throws InternalException {
        return getWriter(file, java.nio.charset.Charset.forName(charsetName), isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file     输出文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InternalException 异常
     */
    public static BufferedWriter getWriter(File file, java.nio.charset.Charset charset, boolean isAppend) throws InternalException {
        return FileWriter.create(file, charset).getWriter(isAppend);
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException 异常
     */
    public static PrintWriter getPrintWriter(String path, String charset, boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param path     输出路径,绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException 异常
     */
    public static PrintWriter getPrintWriter(String path, java.nio.charset.Charset charset, boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象,可以有print
     *
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException 异常
     */
    public static PrintWriter getPrintWriter(final File file, final String charset, final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(file, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException 异常
     */
    public static PrintWriter getPrintWriter(final File file, final java.nio.charset.Charset charset, final boolean isAppend) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final String path) throws InternalException {
        return writeString(content, path, Charset.UTF_8);
    }

    /**
     * 将String写入文件,覆盖模式,字符集为UTF-8
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final File file) throws InternalException {
        return writeString(content, file, Charset.UTF_8);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final String path, final String charset) throws InternalException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final String path, final java.nio.charset.Charset charset) throws InternalException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final File file, final String charset) throws InternalException {
        return FileWriter.create(file, Charset.charset(charset)).write(content);
    }

    /**
     * 将String写入文件,覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws InternalException 异常
     */
    public static File writeString(final String content, final File file, final java.nio.charset.Charset charset) throws InternalException {
        return FileWriter.create(file, charset).write(content);
    }

    /**
     * 将String写入文件,UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final String path) throws InternalException {
        return appendString(content, path, Charset.UTF_8);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final String path, final String charset) throws InternalException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final String path, final java.nio.charset.Charset charset) throws InternalException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件,UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final File file) throws InternalException {
        return appendString(content, file, Charset.UTF_8);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final File file, final String charset) throws InternalException {
        return FileWriter.create(file, Charset.charset(charset)).append(content);
    }

    /**
     * 将String写入文件,追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException 异常
     */
    public static File appendString(final String content, final File file, final java.nio.charset.Charset charset) throws InternalException {
        return FileWriter.create(file, charset).append(content);
    }

    /**
     * 将列表写入文件,覆盖模式,编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 绝对路径
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path) throws InternalException {
        return writeLines(list, path, Charset.UTF_8);
    }

    /**
     * 将列表写入文件,覆盖模式,编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 绝对路径
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file) throws InternalException {
        return writeLines(list, file, Charset.UTF_8);
    }

    /**
     * 将列表写入文件,覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path, final String charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path, final java.nio.charset.Charset charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, File file, final String charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, File file, final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, file, charset, false);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 文件
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final File file) throws InternalException {
        return appendLines(list, file, Charset.UTF_8);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 文件路径
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final String path) throws InternalException {
        return appendLines(list, path, Charset.UTF_8);
    }

    /**
     * 将列表写入文件,追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final String path, final String charset) throws InternalException {
        return writeLines(list, path, charset, true);
    }

    /**
     * 将列表写入文件，追加模式，策略为：
     * <ul>
     *     <li>当文件为空，从开头追加，尾部不加空行</li>
     *     <li>当有内容，换行追加，尾部不加空行</li>
     *     <li>当有内容，并末尾有空行，依旧换行追加</li>
     * </ul>
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final File file, final String charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final String path, final java.nio.charset.Charset charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File appendLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path, final String charset, final boolean isAppend) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path, final java.nio.charset.Charset charset, final boolean isAppend) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final String charset, final boolean isAppend) throws InternalException {
        return FileWriter.create(file, Charset.charset(charset)).writeLines(list, isAppend);
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
     * @throws InternalException 异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset, final boolean isAppend) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static File writeMap(final Map<?, ?> map, final File file, final String kvSeparator, final boolean isAppend) throws InternalException {
        return FileWriter.create(file, Charset.UTF_8).writeMap(map, kvSeparator, isAppend);
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
     * @throws InternalException 异常
     */
    public static File writeMap(final Map<?, ?> map, final File file, final java.nio.charset.Charset charset, final String kvSeparator, final boolean isAppend) throws InternalException {
        return FileWriter.create(file, charset).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param path 目标文件
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static File writeBytes(final byte[] data, final String path) throws InternalException {
        return writeBytes(data, touch(path));
    }

    /**
     * 写数据到文件中
     *
     * @param dest 目标文件
     * @param data 数据
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static File writeBytes(final byte[] data, final File dest) throws InternalException {
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
     * @throws InternalException 异常
     */
    public static File writeBytes(final byte[] data, final File dest, final int off, final int len, final boolean isAppend) throws InternalException {
        return FileWriter.create(dest).write(data, off, len, isAppend);
    }

    /**
     * 将流的内容写入文件
     *
     * @param dest 目标文件
     * @param in   输入流
     * @return dest
     * @throws InternalException 异常
     */
    public static File writeFromStream(final InputStream in, final File dest) throws InternalException {
        return writeFromStream(in, dest, true);
    }

    /**
     * 将流的内容写入文件
     *
     * @param dest      目标文件
     * @param in        输入流
     * @param isCloseIn 关闭输入流
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static File writeFromStream(final InputStream in, final File dest, final boolean isCloseIn) throws InternalException {
        return FileWriter.create(dest).writeFromStream(in, isCloseIn);
    }

    /**
     * 将路径对应文件写入流中，此方法不会关闭输出流
     *
     * @param in   输入流
     * @param path 文件绝对路径
     * @return 目标文件
     * @throws InternalException 异常
     */
    public static File writeFromStream(final InputStream in, final String path) throws InternalException {
        return writeFromStream(in, touch(path));
    }

    /**
     * 将文件写入流中，此方法不会关闭输出流
     *
     * @param file 文件
     * @param out  流
     * @return 写出的流byte数
     * @throws InternalException 异常
     */
    public static long writeToStream(final File file, final OutputStream out) throws InternalException {
        return FileReader.create(file).writeToStream(out);
    }

    /**
     * 将流的内容写入文件
     *
     * @param path 文件绝对路径
     * @param out  输出流
     * @return 写出的流byte数
     * @throws InternalException 异常
     */
    public static long writeToStream(final String path, final OutputStream out) throws InternalException {
        return writeToStream(touch(path), out);
    }

    /**
     * 可读的文件大小
     *
     * @param file 文件
     * @return 大小
     */
    public static String readableFileSize(final File file) {
        return readableFileSize(file.length());
    }

    /**
     * 可读的文件大小
     * 参考 http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String readableFileSize(final long size) {
        if (size <= 0) {
            return Symbol.ZERO;
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "EB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(Normal._1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(Normal._1024, digitGroups)) + Symbol.SPACE + units[digitGroups];
    }

    /**
     * 转换文件编码
     * 此方法用于转换文件编码,读取的文件实际编码必须与指定的srcCharset编码一致,否则导致乱码
     *
     * @param file        文件
     * @param srcCharset  原文件的编码,必须与文件内容的编码保持一致
     * @param destCharset 转码后的编码
     * @return 被转换编码的文件
     */
    public static File convertCharset(final File file, final java.nio.charset.Charset srcCharset, final java.nio.charset.Charset destCharset) {
        return Charset.convert(file, srcCharset, destCharset);
    }

    /**
     * 转换换行符
     * 将给定文件的换行符转换为指定换行符
     *
     * @param file          文件
     * @param charset       编码
     * @param lineSeparator 换行符枚举
     * @return 被修改的文件
     */
    public static File convertLineSeparator(final File file, final java.nio.charset.Charset charset, final LineSeparator lineSeparator) {
        final List<String> lines = readLines(file, charset);
        return FileWriter.create(file, charset).writeLines(lines, lineSeparator, false);
    }

    /**
     * 清除文件名中的在Windows下不支持的非法字符,包括： \ / : * ? " &lt; &gt; |
     *
     * @param fileName 文件名(必须不包括路径,否则路径符将被替换)
     * @return 清理后的文件名
     */
    public static String cleanInvalid(final String fileName) {
        return StringKit.isBlank(fileName) ? fileName : PatternKit.delAll(FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * 文件名中是否包含在Windows下不支持的非法字符,包括： \ / : * ? " &lt; &gt; |
     *
     * @param fileName 文件名(必须不包括路径,否则路径符将被替换)
     * @return 是否包含非法字符
     */
    public static boolean containsInvalid(final String fileName) {
        return !StringKit.isBlank(fileName) && PatternKit.contains(FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * 计算文件CRC32校验码
     *
     * @param file 文件,不能为目录
     * @return CRC32值
     * @throws InternalException 异常
     */
    public static long checksumCRC32(final File file) throws InternalException {
        return checksum(file, new CRC32()).getValue();
    }

    /**
     * 计算流CRC32校验码,计算后关闭流
     *
     * @param in 文件,不能为目录
     * @return CRC32值
     * @throws InternalException 异常
     */
    public static long checksumCRC32(InputStream in) throws InternalException {
        return checksum(in, new CRC32()).getValue();
    }

    /**
     * 计算文件校验码
     *
     * @param file     文件,不能为目录
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InternalException 异常
     */
    public static Checksum checksum(final File file, final Checksum checksum) throws InternalException {
        Assert.notNull(file, "File is null !");
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Checksums can't be computed on directories");
        }
        try {
            return checksum(Files.newInputStream(file.toPath()), checksum);
        } catch (FileNotFoundException e) {
            throw new InternalException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算流的校验码,计算后关闭流
     *
     * @param in       流
     * @param checksum {@link Checksum}
     * @return Checksum
     * @throws InternalException 异常
     */
    public static Checksum checksum(InputStream in, Checksum checksum) throws InternalException {
        Assert.notNull(in, "InputStream is null !");
        if (null == checksum) {
            checksum = new CRC32();
        }
        try {
            in = new CheckedInputStream(in, checksum);
            IoKit.copy(in, EmptyOutputStream.INSTANCE);
        } finally {
            IoKit.close(in);
        }
        return checksum;
    }

    /**
     * 获取Web项目下的web root路径
     * 原理是首先获取ClassPath路径,由于在web项目中ClassPath位于 WEB-INF/classes/下,故向上获取两级目录即可
     *
     * @return web root路径
     */
    public static File getWebRoot() {
        final String classPath = ClassKit.getClassPath();
        if (StringKit.isNotBlank(classPath)) {
            return getParent(file(classPath), 2);
        }
        return null;
    }

    /**
     * 获取指定文件的父路径
     *
     * <pre>
     * getParent(file("d:/aaa/bbb/cc/ddd")) -》 "d:/aaa/bbb/cc"
     * </pre>
     *
     * @param file 目录或文件
     * @return 路径File，如果不存在返回null
     * @since 6.0.0
     */
    public static File getParent(final File file) {
        return getParent(file, 1);
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent("/data/aaa/bbb/cc/ddd", 0) - "/data/aaa/bbb/cc/ddd"
     * getParent("/data/aaa/bbb/cc/ddd", 2) - "/data/aaa/bbb"
     * getParent("/data/aaa/bbb/cc/ddd", 4) - "/data/"
     * getParent("/data/aaa/bbb/cc/ddd", 5) - null
     * </pre>
     *
     * @param filePath 目录或文件路径
     * @param level    层级
     * @return 路径File, 如果不存在返回null
     */
    public static String getParent(final String filePath, final int level) {
        final File parent = getParent(file(filePath), level);
        try {
            return null == parent ? null : parent.getCanonicalPath();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent(file("/data/aaa/bbb/cc/ddd", 0)) - "/data/aaa/bbb/cc/ddd"
     * getParent(file("/data/aaa/bbb/cc/ddd", 2)) - "/data/aaa/bbb"
     * getParent(file("/data/aaa/bbb/cc/ddd", 4)) - "/data/"
     * getParent(file("/data/aaa/bbb/cc/ddd", 5)) - null
     * </pre>
     *
     * @param file  目录或文件
     * @param level 层级
     * @return 路径File, 如果不存在返回null
     */
    public static File getParent(final File file, final int level) {
        if (level < 1 || null == file) {
            return file;
        }

        File parentFile;
        try {
            parentFile = file.getCanonicalFile().getParentFile();
        } catch (IOException e) {
            throw new InternalException(e);
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
    public static File checkSlip(final File parentFile, final File file) throws IllegalArgumentException {
        if (null != parentFile && null != file) {
            String parentCanonicalPath;
            String canonicalPath;
            try {
                parentCanonicalPath = parentFile.getCanonicalPath();
                canonicalPath = file.getCanonicalPath();
            } catch (IOException e) {
                // getCanonicalPath有时会抛出奇怪的IO异常，此时忽略异常，使用AbsolutePath判断
                parentCanonicalPath = parentFile.getAbsolutePath();
                canonicalPath = file.getAbsolutePath();
            }
            if (false == canonicalPath.startsWith(parentCanonicalPath)) {
                throw new IllegalArgumentException("New file is outside of the parent dir: " + file.getName());
            }
        }
        return file;
    }

    /**
     * 根据文件扩展名获得MediaType
     *
     * @param path 文件路径或文件名
     * @return the string {@link MediaType}
     */
    public static String getMediaType(final String path) {
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(path);
        if (null == contentType) {
            // 补充一些常用的mimeType
            if (StringKit.endWithIgnoreCase(path, ".css")) {
                contentType = "text/css";
            } else if (StringKit.endWithIgnoreCase(path, ".js")) {
                contentType = "application/x-javascript";
            } else if (StringKit.endWithIgnoreCase(path, ".rar")) {
                contentType = "application/x-rar-compressed";
            } else if (StringKit.endWithIgnoreCase(path, ".7z")) {
                contentType = "application/x-7z-compressed";
            } else if (StringKit.endWithIgnoreCase(path, ".wgt")) {
                contentType = "application/widget";
            }
        }

        // 补充
        if (null == contentType) {
            contentType = getMediaType(Paths.get(path));
        }

        return contentType;
    }

    /**
     * 获得文件的MediaType
     *
     * @param file 文件
     * @return the string {@link MediaType}
     */
    public static String getMediaType(final Path file) {
        try {
            return Files.probeContentType(file);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 根据文件扩展名获得MimeType
     *
     * @param filePath     文件路径或文件名
     * @param defaultValue 当获取MimeType为null时的默认值
     * @return MimeType
     */
    public static String getMediaType(final String filePath, final String defaultValue) {
        return ObjectKit.defaultIfNull(getMediaType(filePath), defaultValue);
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
    public static String removeSuffix(final String filename) {
        if (null == filename) {
            return null;
        }

        final int len = filename.length();
        for (int i = 0; i < len; i++) {
            if (filename.charAt(i) == 0) {
                throw new IllegalArgumentException("Null byte present in file/path name. There are no " +
                        "known legitimate use cases for such data, but several injection attacks may use it");
            }
        }

        final int index = indexOfSuffix(filename);
        if (index == Normal.__1) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }

    /**
     * 检查文件名的扩展名是否指定的扩展名
     *
     * @param filename 要查询的文件名，null返回false
     * @param suffix   要检查的扩展名，null检查是否没有扩展名
     * @return 如果文件名是扩展名之一，则为true
     */
    public static boolean isSuffix(final String filename, final String suffix) {
        if (null == filename) {
            return false;
        }
        failIfNullBytePresent(filename);

        if (null == suffix || suffix.isEmpty()) {
            return indexOfSuffix(filename) == Normal.__1;
        }
        final String fileSuffix = getSuffix(filename);
        return fileSuffix.equals(suffix);
    }

    /**
     * 检查文件名的扩展名是否为指定的扩展名之一
     *
     * @param filename 要查询的文件名，null返回false
     * @param suffixs  要检查的扩展名，null检查是否没有扩展名
     * @return 如果文件名是扩展名之一，则为true
     */
    public static boolean isSuffix(final String filename, final String[] suffixs) {
        if (null == filename) {
            return false;
        }
        failIfNullBytePresent(filename);

        if (null == suffixs || suffixs.length == 0) {
            return indexOfSuffix(filename) == Normal.__1;
        }
        final String fileSuffix = getSuffix(filename);
        for (final String suffix : suffixs) {
            if (fileSuffix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查文件名的扩展名是否为指定的扩展名之一
     *
     * @param filename 要查询的文件名，null返回false
     * @param suffixs  要检查的扩展名，null检查是否没有扩展名
     * @return 如果文件名是扩展名之一，则为true
     */
    public static boolean isSuffix(final String filename, final Collection<String> suffixs) {
        if (null == filename) {
            return false;
        }
        failIfNullBytePresent(filename);

        if (null == suffixs || suffixs.isEmpty()) {
            return indexOfSuffix(filename) == Normal.__1;
        }
        final String fileSuffix = getSuffix(filename);
        for (final String suffix : suffixs) {
            if (fileSuffix.equals(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查输入是否为空字节，未经过处理的数据的迹象将传递给文件级功能
     *
     * @param path 检查的路径
     */
    private static void failIfNullBytePresent(final String path) {
        final int len = path.length();
        for (int i = 0; i < len; i++) {
            if (path.charAt(i) == 0) {
                throw new IllegalArgumentException("Null byte present in file/path name. There are no " +
                        "known legitimate use cases for such data, but several injection attacks may use it");
            }
        }
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能
     * 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param handler 行处理器
     */
    public static void tail(final File file, final XConsumer<String> handler) {
        tail(file, Charset.UTF_8, handler);
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能
     * 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param charset 编码
     * @param handler 行处理器
     */
    public static void tail(final File file, final java.nio.charset.Charset charset, final XConsumer<String> handler) {
        new Tailer(file, charset, handler).start();
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能
     * 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param charset 编码
     */
    public static void tail(final File file, final java.nio.charset.Charset charset) {
        tail(file, charset, new Tailer.ConsoleLineHandler());
    }

    /**
     * 创建{@link RandomAccessFile}
     *
     * @param path 文件Path
     * @param mode 模式，见{@link FileMode}
     * @return {@link RandomAccessFile}
     */
    public static RandomAccessFile createRandomAccessFile(Path path, FileMode mode) {
        return createRandomAccessFile(path.toFile(), mode);
    }

    /**
     * 创建{@link RandomAccessFile}
     *
     * @param file 文件
     * @param mode 模式，见{@link FileMode}
     * @return {@link RandomAccessFile}
     */
    public static RandomAccessFile createRandomAccessFile(File file, FileMode mode) {
        try {
            return new RandomAccessFile(file, mode.name());
        } catch (FileNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取Classpath下的资源为字符串,使用UTF-8编码
     *
     * @param resource 资源路径,使用相对ClassPath的路径
     * @return 资源内容
     */
    public static String readers(String resource) {
        return getResource(resource).readString(Charset.UTF_8);
    }

    /**
     * 读取Classpath下的资源为字符串
     *
     * @param resource 资源路径,使用相对ClassPath的路径
     * @param charset  编码
     * @return 资源内容
     */
    public static String readers(String resource, java.nio.charset.Charset charset) {
        return new ClassPathResource(resource).readString(charset);
    }

    /**
     * 从ClassPath资源中获取{@link BufferedReader}
     *
     * @param resurce ClassPath资源
     * @param charset 编码
     * @return {@link InputStream}
     */
    public static BufferedReader getReaders(String resurce, java.nio.charset.Charset charset) {
        return new ClassPathResource(resurce).getReader(charset);
    }

    /**
     * 从ClassPath资源中获取{@link InputStream}
     *
     * @param resurce ClassPath资源
     * @return {@link InputStream}
     * @throws InternalException 资源不存在异常
     */
    public static InputStream getStream(String resurce) {
        return new ClassPathResource(resurce).getStream();
    }

    /**
     * 从ClassPath资源中获取{@link InputStream},当资源不存在时返回null
     *
     * @param resurce ClassPath资源
     * @return {@link InputStream}
     */
    public static InputStream getStreamSafe(String resurce) {
        try {
            return new ClassPathResource(resurce).getStream();
        } catch (InternalException e) {
            // ignore
        }
        return null;
    }

    /**
     * 获得资源的URL
     * 路径用/分隔，例如:
     *
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     */
    public static URL getUrl(final String resource) {
        return getUrl(resource, null);
    }

    /**
     * 获得资源相对路径对应的URL
     *
     * @param resource  资源相对路径，{@code null}和""都表示classpath根路径
     * @param baseClass 基准Class，获得的相对路径相对于此Class所在路径，如果为{@code null}则相对ClassPath
     * @return {@link URL}
     */
    public static URL getUrl(String resource, final Class<?> baseClass) {
        resource = StringKit.emptyIfNull(resource);
        return (null != baseClass) ? baseClass.getResource(resource) : ClassKit.getClassLoader().getResource(resource);
    }

    /**
     * 获取指定路径下的资源列表
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     */
    public static List<URL> getUrls(final String resource) {
        return getUrls(resource, null);
    }

    /**
     * 获取指定路径下的资源列表
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @param filter   过滤器，用于过滤不需要的资源，{@code null}表示不过滤，保留所有元素
     * @return 资源列表
     */
    public static List<URL> getUrls(final String resource, final Predicate<URL> filter) {
        final Enumeration<URL> resources;
        try {
            resources = ClassKit.getClassLoader().getResources(resource);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return IterKit.filterToList(new EnumerationIterator<>(resources), filter);
    }

    /**
     * 获取{@link Resource} 资源对象
     * 如果提供路径为绝对路径或路径以file:开头，返回{@link FileResource}，否则返回{@link ClassPathResource}
     *
     * @param path 路径，可以是绝对路径，也可以是相对路径（相对ClassPath）
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final String path) {
        if (StringKit.isNotBlank(path)) {
            if (path.startsWith(Normal.FILE_URL_PREFIX) || FileKit.isAbsolutePath(path)) {
                return new FileResource(path);
            }
        }
        return new ClassPathResource(path);
    }

    /**
     * 获取{@link UriResource} 资源对象
     *
     * @param url URL
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final URL url) {
        return new UriResource(url);
    }

    /**
     * 获取{@link FileResource} 资源对象
     *
     * @param file {@link File}
     * @return {@link Resource} 资源对象
     */
    public static Resource getResource(final File file) {
        return new FileResource(file);
    }

    /**
     * 根据文件名检查文件类型，忽略大小写
     *
     * @param fileName 文件名，例如bus.png
     * @param suffix   被检查的扩展名数组，同一文件类型可能有多种扩展名，扩展名不带“.”
     * @return 是否是指定扩展名的类型
     */
    public static boolean isType(String fileName, String... suffix) {
        return StringKit.equalsAnyIgnoreCase(getSuffix(fileName), suffix);
    }

    /**
     * 判断文件或目录是否存在
     *
     * @param path          文件
     * @param isFollowLinks 是否跟踪软链（快捷方式）
     * @return 是否存在
     */
    public static boolean isEexist(Path path, boolean isFollowLinks) {
        final LinkOption[] options = isFollowLinks ? new LinkOption[0] : new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.exists(path, options);
    }

    /**
     * 判断给定的目录是否为给定文件或文件夹的子目录
     *
     * @param parent 父目录
     * @param sub    子目录
     * @return 子目录是否为父目录的子目录
     */
    public static boolean isSub(File parent, File sub) {
        Assert.notNull(parent);
        Assert.notNull(sub);
        return isSub(parent.toPath(), sub.toPath());
    }

    /**
     * 判断给定的目录是否为给定文件或文件夹的子目录
     *
     * @param parent 父目录
     * @param sub    子目录
     * @return 子目录是否为父目录的子目录
     */
    public static boolean isSub(Path parent, Path sub) {
        return toAbsNormal(sub).startsWith(toAbsNormal(parent));
    }

    /**
     * 将Path路径转换为标准的绝对路径
     *
     * @param path 文件或目录Path
     * @return 转换后的Path
     */
    public static Path toAbsNormal(Path path) {
        Assert.notNull(path);
        return path.toAbsolutePath().normalize();
    }

    /**
     * 向文件头部添加版权等内容
     *
     * @param dir     地址
     * @param content 内容
     */
    public static void addContent(File dir, String content) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                try {
                    BufferedReader br = new BufferedReader(new java.io.FileReader(file));
                    String line;
                    String text = Normal.EMPTY;
                    // 读取一行,一定要加上换行符
                    String lineSeperator = System.getProperty(org.miaixz.bus.core.lang.System.LINE_SEPARATOR);
                    while ((line = br.readLine()) != null) {
                        text += line + lineSeperator;
                    }
                    br.close();
                    // 把拼接后的字符串写回去
                    java.io.FileWriter fileWriter = new java.io.FileWriter(file);
                    fileWriter.write(content);
                    fileWriter.write(text);
                    fileWriter.close();
                } catch (FileNotFoundException e) {
                    throw new InternalException("File NotFound !");
                } catch (IOException ex) {
                    throw new InternalException("I/O exception of some sort has occurred");
                }
            } else {
                addContent(file, content);
            }
        }
    }

    /**
     * 根据压缩包中的路径构建目录结构，在Win下直接构建，在Linux下拆分路径单独构建
     *
     * @param outFile  最外部路径
     * @param fileName 文件名，可以包含路径
     * @return 文件或目录
     */
    private static File buildFile(File outFile, String fileName) {
        // 替换Windows路径分隔符为Linux路径分隔符，便于统一处理
        fileName = fileName.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH);
        if (false == isWindows()
                // 检查文件名中是否包含"/"，不考虑以"/"结尾的情况
                && fileName.lastIndexOf(Symbol.C_SLASH, fileName.length() - 2) > 0) {
            // 在Linux下多层目录创建存在问题，/会被当成文件名的一部分，此处做处理
            // 使用/拆分路径（zip中无\），级联创建父目录
            final List<String> pathParts = StringKit.split(fileName, Symbol.C_SLASH, false, true);
            final int lastPartIndex = pathParts.size() - 1;//目录个数
            for (int i = 0; i < lastPartIndex; i++) {
                //由于路径拆分，slip不检查，在最后一步检查
                outFile = new File(outFile, pathParts.get(i));
            }
            outFile.mkdirs();
            // 最后一个部分如果非空，作为文件名
            fileName = pathParts.get(lastPartIndex);
        }
        return new File(outFile, fileName);
    }

}
