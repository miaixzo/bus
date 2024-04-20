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
package org.miaixz.bus.extra.ftp;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.CharsKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.Closeable;
import java.io.File;
import java.util.List;

/**
 * 抽象FTP类,用于定义通用的FTP方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFtp implements Closeable {

    public static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;

    protected FtpConfig ftpConfig;

    /**
     * 构造
     *
     * @param config FTP配置
     */
    protected AbstractFtp(FtpConfig config) {
        this.ftpConfig = config;
    }

    /**
     * 是否包含指定字符串,忽略大小写
     *
     * @param names      文件或目录名列表
     * @param nameToFind 要查找的文件或目录名
     * @return 是否包含
     */
    private static boolean containsIgnoreCase(List<String> names, String nameToFind) {
        if (CollKit.isEmpty(names)) {
            return false;
        }
        if (StringKit.isEmpty(nameToFind)) {
            return false;
        }
        for (String name : names) {
            if (nameToFind.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果连接超时的话,重新进行连接
     *
     * @return this
     */
    public abstract AbstractFtp reconnectIfTimeout();

    /**
     * 打开指定目录
     *
     * @param directory directory
     * @return 是否打开目录
     */
    public abstract boolean cd(String directory);

    /**
     * 打开上级目录
     *
     * @return 是否打开目录
     */
    public boolean toParent() {
        return cd(Symbol.DOUBLE_DOT);
    }

    /**
     * 远程当前目录(工作目录)
     *
     * @return 远程当前目录
     */
    public abstract String pwd();

    /**
     * 在当前远程目录(工作目录)下创建新的目录
     *
     * @param dir 目录名
     * @return 是否创建成功
     */
    public abstract boolean mkdir(String dir);

    /**
     * 判断给定路径是否为目录
     *
     * @param dir 被判断的路径
     * @return 是否为目录
     */
    public boolean isDir(String dir) {
        final String workDir = pwd();
        try {
            return cd(dir);
        } finally {
            cd(workDir);
        }
    }

    /**
     * 文件或目录是否存在
     *
     * @param path 目录
     * @return 是否存在
     */
    public boolean exist(String path) {
        if (StringKit.isBlank(path)) {
            return false;
        }
        // 验证目录
        if (isDir(path)) {
            return true;
        }
        if (CharsKit.isFileSeparator(path.charAt(path.length() - 1))) {
            return false;
        }
        final String fileName = FileKit.getName(path);
        if (Symbol.DOT.equals(fileName) || (Symbol.DOT + Symbol.DOT).equals(fileName)) {
            return false;
        }
        // 验证文件
        final String dir = StringKit.emptyToDefault(StringKit.removeSuffix(path, fileName), Symbol.DOT);
        final List<String> names;
        try {
            names = ls(dir);
        } catch (InternalException ignore) {
            return false;
        }
        return containsIgnoreCase(names, fileName);
    }

    /**
     * 遍历某个目录下所有文件和目录,不会递归遍历
     *
     * @param path 需要遍历的目录
     * @return 文件和目录列表
     */
    public abstract List<String> ls(String path);

    /**
     * 删除指定目录下的指定文件
     *
     * @param path 目录路径
     * @return 是否存在
     */
    public abstract boolean delFile(String path);

    /**
     * 删除文件夹及其文件夹下的所有文件
     *
     * @param dirPath 文件夹路径
     * @return boolean 是否删除成功
     */
    public abstract boolean delDir(String dirPath);

    /**
     * 创建指定文件夹及其父目录,从根目录开始创建,创建完成后回到默认的工作目录
     *
     * @param dir 文件夹路径,绝对路径
     */
    public void mkDirs(String dir) {
        final String[] dirs = StringKit.trim(dir).split("[\\\\/]+");

        final String now = pwd();
        if (dirs.length > 0 && StringKit.isEmpty(dirs[0])) {
            //首位为空,表示以/开头
            this.cd(Symbol.SLASH);
        }
        for (String s : dirs) {
            if (StringKit.isNotEmpty(s)) {
                boolean exist = true;
                try {
                    if (false == cd(s)) {
                        exist = false;
                    }
                } catch (InternalException e) {
                    exist = false;
                }
                if (false == exist) {
                    //目录不存在时创建
                    mkdir(s);
                    cd(s);
                }
            }
        }
        // 切换回工作目录
        cd(now);
    }

    /**
     * 将本地文件上传到目标服务器,目标文件名为destPath,若destPath为目录,则目标文件名将与srcFilePath文件名相同 覆盖模式
     *
     * @param srcFilePath 本地文件路径
     * @param destFile    目标文件
     * @return 是否成功
     */
    public abstract boolean upload(String srcFilePath, File destFile);

    /**
     * 下载文件
     *
     * @param path    文件路径
     * @param outFile 输出文件或目录
     */
    public abstract void download(String path, File outFile);

    /**
     * 获取远程文件(文件目录和服务器同步), 服务器上有新文件会覆盖本地文件
     *
     * @param sourcePath 服务器目录
     * @param destPath   本地目录
     */
    public abstract void download(String sourcePath, String destPath);

    /**
     * 下载文件-避免未完成的文件
     * 此方法原理是先在目标文件同级目录下创建临时文件，等下载完毕后重命名，避免因下载错误导致的文件不完整。
     *
     * @param path           文件路径
     * @param outFile        输出文件或目录
     * @param tempFileSuffix 临时文件后缀，默认".temp"
     */
    public void download(String path, File outFile, String tempFileSuffix) {
        if (StringKit.isBlank(tempFileSuffix)) {
            tempFileSuffix = ".temp";
        } else {
            tempFileSuffix = StringKit.addPrefixIfNot(tempFileSuffix, Symbol.DOT);
        }

        // 目标文件真实名称
        final String fileName = outFile.isDirectory() ? FileKit.getName(path) : outFile.getName();
        // 临时文件名称
        final String tempFileName = fileName + tempFileSuffix;

        // 临时文件
        outFile = new File(outFile.isDirectory() ? outFile : outFile.getParentFile(), tempFileName);
        try {
            download(path, outFile);
            // 重命名下载好的临时文件
            FileKit.rename(outFile, fileName, true);
        } catch (Throwable e) {
            // 异常则删除临时文件
            FileKit.delete(outFile);
            throw new InternalException(e);
        }
    }

}
