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

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.CharKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.File;
import java.util.List;

/**
 * 抽象FTP类，用于定义通用的FTP方法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFtp implements Ftp {

    protected FtpConfig ftpConfig;

    /**
     * 构造
     *
     * @param config FTP配置
     */
    protected AbstractFtp(final FtpConfig config) {
        this.ftpConfig = config;
    }

    /**
     * 是否包含指定字符串，忽略大小写
     *
     * @param names      文件或目录名列表
     * @param nameToFind 要查找的文件或目录名
     * @return 是否包含
     */
    private static boolean containsIgnoreCase(final List<String> names, final String nameToFind) {
        if (StringKit.isEmpty(nameToFind)) {
            return false;
        }
        return CollKit.contains(names, nameToFind::equalsIgnoreCase);
    }

    @Override
    public FtpConfig getConfig() {
        return this.ftpConfig;
    }

    @Override
    public boolean exist(final String path) {
        if (StringKit.isBlank(path)) {
            return false;
        }
        // 目录验证
        if (isDir(path)) {
            return true;
        }
        if (CharKit.isFileSeparator(path.charAt(path.length() - 1))) {
            return false;
        }

        final String fileName = FileName.getName(path);
        if (Symbol.DOT.equals(fileName) || Symbol.DOUBLE_DOT.equals(fileName)) {
            return false;
        }

        // 文件验证
        final String dir = StringKit.defaultIfEmpty(StringKit.removeSuffix(path, fileName), Symbol.DOT);
        // 检查父目录为目录且是否存在
        if (!isDir(dir)) {
            return false;
        }
        final List<String> names;
        try {
            names = ls(dir);
        } catch (final InternalException ignore) {
            return false;
        }
        return containsIgnoreCase(names, fileName);
    }

    @Override
    public void mkDirs(final String dir) {
        final String[] dirs = StringKit.trim(dir).split("[\\\\/]+");

        final String now = pwd();
        if (dirs.length > 0 && StringKit.isEmpty(dirs[0])) {
            //首位为空，表示以/开头
            this.cd(Symbol.SLASH);
        }
        for (final String s : dirs) {
            if (StringKit.isNotEmpty(s)) {
                boolean exist = true;
                try {
                    if (!cd(s)) {
                        exist = false;
                    }
                } catch (final InternalException e) {
                    exist = false;
                }
                if (!exist) {
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
     * 下载文件-避免未完成的文件
     * 此方法原理是先在目标文件同级目录下创建临时文件，下载之，等下载完毕后重命名，避免因下载错误导致的文件不完整。
     *
     * @param path           文件路径
     * @param outFile        输出文件或目录
     * @param tempFileSuffix 临时文件后缀，默认".temp"
     */
    public void download(final String path, File outFile, String tempFileSuffix) {
        if (StringKit.isBlank(tempFileSuffix)) {
            tempFileSuffix = ".temp";
        } else {
            tempFileSuffix = StringKit.addPrefixIfNot(tempFileSuffix, Symbol.DOT);
        }

        // 目标文件真实名称
        final String fileName = outFile.isDirectory() ? FileName.getName(path) : outFile.getName();
        // 临时文件名称
        final String tempFileName = fileName + tempFileSuffix;

        // 临时文件
        outFile = new File(outFile.isDirectory() ? outFile : outFile.getParentFile(), tempFileName);
        try {
            download(path, outFile);
            // 重命名下载好的临时文件
            FileKit.rename(outFile, fileName, true);
        } catch (final Throwable e) {
            // 异常则删除临时文件
            FileKit.del(outFile);
            throw new InternalException(e);
        }
    }

}
