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
package org.miaixz.bus.extra.ssh;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.extra.ftp.AbstractFtp;
import org.miaixz.bus.extra.ftp.FtpConfig;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

/**
 * SFTP是Secure File Transfer Protocol的缩写,安全文件传送协议 可以为传输文件提供一种安全的加密方法
 * SFTP 为 SSH的一部份,是一种传输文件到服务器的安全方式 SFTP是使用加密传输认证信息和传输的数据,所以,使用SFTP是非常安全的
 * 但是,由于这种传输方式使用了加密/解密技术,所以传输效率比普通的FTP要低得多,如果您对网络安全性要求更高时,可以使用SFTP代替FTP
 *
 * <p>
 * 此类基于jsch的SFTP实现
 * 参考：https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sftp extends AbstractFtp {

    private Session session;
    private ChannelSftp channel;

    /**
     * 构造
     *
     * @param sshHost 远程主机
     * @param sshPort 远程主机端口
     * @param sshUser 远程主机用户名
     * @param sshPass 远程主机密码
     */
    public Sftp(String sshHost, int sshPort, String sshUser, String sshPass) {
        this(sshHost, sshPort, sshUser, sshPass, DEFAULT_CHARSET);
    }

    /**
     * 构造
     *
     * @param sshHost 远程主机
     * @param sshPort 远程主机端口
     * @param sshUser 远程主机用户名
     * @param sshPass 远程主机密码
     * @param charset 编码
     */
    public Sftp(String sshHost, int sshPort, String sshUser, String sshPass, Charset charset) {
        this(new FtpConfig(sshHost, sshPort, sshUser, sshPass, charset));
    }

    /**
     * 构造
     *
     * @param config FTP配置
     */
    public Sftp(FtpConfig config) {
        this(config, true);
    }

    /**
     * 构造
     *
     * @param config FTP配置
     * @param init   是否立即初始化
     */
    public Sftp(FtpConfig config, boolean init) {
        super(config);
        if (init) {
            init(config);
        }
    }

    /**
     * 构造
     *
     * @param session {@link Session}
     */
    public Sftp(Session session) {
        this(session, DEFAULT_CHARSET);
    }

    /**
     * 构造
     *
     * @param session {@link Session}
     * @param charset 编码
     */
    public Sftp(Session session, Charset charset) {
        super(FtpConfig.create().setCharset(charset));
        init(session, charset);
    }

    /**
     * 构造
     *
     * @param channel {@link ChannelSftp}
     * @param charset 编码
     */
    public Sftp(ChannelSftp channel, Charset charset) {
        super(FtpConfig.create().setCharset(charset));
        init(channel, charset);
    }

    /**
     * 构造
     *
     * @param sshHost 远程主机
     * @param sshPort 远程主机端口
     * @param sshUser 远程主机用户名
     * @param sshPass 远程主机密码
     * @param charset 编码
     */
    public void init(String sshHost, int sshPort, String sshUser, String sshPass, Charset charset) {
        init(JSchKit.getSession(sshHost, sshPort, sshUser, sshPass), charset);
    }

    /**
     * 初始化
     */
    public void init() {
        init(this.ftpConfig);
    }

    /**
     * 初始化
     *
     * @param config FTP配置
     */
    public void init(FtpConfig config) {
        init(config.getHost(), config.getPort(), config.getUser(), config.getPassword(), config.getCharset());
    }

    /**
     * 初始化
     *
     * @param session {@link Session}
     * @param charset 编码
     */
    public void init(Session session, Charset charset) {
        this.session = session;
        init(JSchKit.openSftp(session, (int) this.ftpConfig.getConnectionTimeout()), charset);
    }

    /**
     * 初始化
     *
     * @param channel {@link ChannelSftp}
     * @param charset 编码
     */
    public void init(ChannelSftp channel, Charset charset) {
        this.ftpConfig.setCharset(charset);
        try {
            channel.setFilenameEncoding(charset.toString());
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        this.channel = channel;
    }

    @Override
    public Sftp reconnectIfTimeout() {
        if (StringKit.isBlank(this.ftpConfig.getHost())) {
            throw new InternalException("Host is blank!");
        }
        try {
            this.cd(Symbol.SLASH);
        } catch (InternalException e) {
            close();
            init();
        }
        return this;
    }

    /**
     * 获取SFTP通道客户端
     *
     * @return 通道客户端
     */
    public ChannelSftp getClient() {
        return this.channel;
    }

    /**
     * 远程当前目录
     *
     * @return 远程当前目录
     */
    @Override
    public String pwd() {
        try {
            return channel.pwd();
        } catch (SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取HOME路径
     *
     * @return HOME路径
     */
    public String home() {
        try {
            return channel.getHome();
        } catch (SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 遍历某个目录下所有文件或目录,不会递归遍历
     *
     * @param path 遍历某个目录下所有文件或目录
     * @return 目录或文件名列表
     */
    @Override
    public List<String> ls(String path) {
        return ls(path, null);
    }

    /**
     * 遍历某个目录下所有目录,不会递归遍历
     *
     * @param path 遍历某个目录下所有目录
     * @return 目录名列表
     */
    public List<String> lsDirs(String path) {
        return ls(path, t -> t.getAttrs().isDir());
    }

    /**
     * 遍历某个目录下所有文件,不会递归遍历
     *
     * @param path 遍历某个目录下所有文件
     * @return 文件名列表
     */
    public List<String> lsFiles(String path) {
        return ls(path, t -> false == t.getAttrs().isDir());
    }

    /**
     * 遍历某个目录下所有文件或目录,不会递归遍历
     *
     * @param path   遍历某个目录下所有文件或目录
     * @param filter 文件或目录过滤器,可以实现过滤器返回自己需要的文件或目录名列表
     * @return 目录或文件名列表
     */
    public List<String> ls(String path, final Predicate<LsEntry> filter) {
        final List<String> fileNames = new ArrayList<>();
        try {
            channel.ls(path, entry -> {
                String fileName = entry.getFilename();
                if (false == StringKit.equals(Symbol.DOT, fileName)
                        && false == StringKit.equals(Symbol.DOUBLE_DOT, fileName)) {
                    if (null == filter || filter.test(entry)) {
                        fileNames.add(entry.getFilename());
                    }
                }
                return LsEntrySelector.CONTINUE;
            });
        } catch (SftpException e) {
            if (false == StringKit.startWithIgnoreCase(e.getMessage(), "No such file")) {
                throw new InternalException(e);
            }
        }
        return fileNames;
    }

    @Override
    public boolean mkdir(String dir) {
        if (isDir(dir)) {
            // 目录已经存在，创建直接返回
            return true;
        }
        try {
            this.channel.mkdir(dir);
            return true;
        } catch (SftpException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean isDir(String dir) {
        final SftpATTRS sftpATTRS;
        try {
            sftpATTRS = this.channel.stat(dir);
        } catch (SftpException e) {
            final String msg = e.getMessage();
            if (StringKit.containsAnyIgnoreCase(msg, "No such file", "does not exist")) {
                return false;
            }
            throw new InternalException(e);
        }
        return sftpATTRS.isDir();
    }

    /**
     * 打开指定目录,如果指定路径非目录或不存在抛出异常
     *
     * @param directory directory
     * @return 是否打开目录
     */
    @Override
    public synchronized boolean cd(String directory) {
        if (StringKit.isBlank(directory)) {
            // 当前目录
            return true;
        }
        try {
            channel.cd(directory.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH));
            return true;
        } catch (SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 要删除的文件绝对路径
     */
    @Override
    public boolean delFile(String filePath) {
        try {
            channel.rm(filePath);
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        return true;
    }

    /**
     * 删除文件夹及其文件夹下的所有文件
     *
     * @param dirPath 文件夹路径
     * @return boolean 是否删除成功
     */
    @Override
    public boolean delDir(String dirPath) {
        if (false == cd(dirPath)) {
            return false;
        }

        Vector<LsEntry> list;
        try {
            list = channel.ls(channel.pwd());
        } catch (SftpException e) {
            throw new InternalException(e);
        }

        String fileName;
        for (LsEntry entry : list) {
            fileName = entry.getFilename();
            if (false == StringKit.equals(fileName, Symbol.DOT)
                    && false == StringKit.equals(fileName, Symbol.DOUBLE_DOT)) {
                if (entry.getAttrs().isDir()) {
                    delDir(fileName);
                } else {
                    delFile(fileName);
                }
            }
        }

        if (false == cd(Symbol.DOUBLE_DOT)) {
            return false;
        }

        // 删除空目录
        try {
            channel.rmdir(dirPath);
            return true;
        } catch (SftpException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean upload(String destPath, File file) {
        put(FileKit.getAbsolutePath(file), destPath);
        return true;
    }

    /**
     * 将本地文件或者文件夹同步（覆盖）上传到远程路径
     *
     * @param file       文件或者文件夹
     * @param remotePath 远程路径
     */
    public void upload(File file, String remotePath) {
        if (false == FileKit.exists(file)) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File fileItem : files) {
                if (fileItem.isDirectory()) {
                    String mkdir = FileKit.normalize(remotePath + "/" + fileItem.getName());
                    this.upload(fileItem, mkdir);
                } else {
                    this.upload(fileItem, remotePath);
                }
            }
        } else {
            this.mkDirs(remotePath);
            this.upload(remotePath, file);
        }
    }

    /**
     * 将本地文件上传到目标服务器,目标文件名为destPath,若destPath为目录,则目标文件名将与srcFilePath文件名相同 覆盖模式
     *
     * @param srcFilePath 本地文件路径
     * @param destPath    目标路径,
     * @return this
     */
    public Sftp put(String srcFilePath, String destPath) {
        return put(srcFilePath, destPath, Mode.OVERWRITE);
    }

    /**
     * 将本地文件上传到目标服务器,目标文件名为destPath,若destPath为目录,则目标文件名将与srcFilePath文件名相同
     *
     * @param srcFilePath 本地文件路径
     * @param destPath    目标路径
     * @param mode        {@link Mode} 模式
     * @return this
     */
    public Sftp put(String srcFilePath, String destPath, Mode mode) {
        return put(srcFilePath, destPath, null, mode);
    }

    /**
     * 将本地文件上传到目标服务器，目标文件名为destPath，若destPath为目录，则目标文件名将与srcFilePath文件名相同。
     *
     * @param srcFilePath 本地文件路径
     * @param destPath    目标路径，
     * @param monitor     上传进度监控，通过实现此接口完成进度显示
     * @param mode        {@link Mode} 模式
     * @return this
     */
    public Sftp put(String srcFilePath, String destPath, SftpProgressMonitor monitor, Mode mode) {
        try {
            channel.put(srcFilePath, destPath, monitor, mode.ordinal());
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 将本地数据流上传到目标服务器，目标文件名为destPath，目标必须为文件
     *
     * @param srcStream 本地的数据流
     * @param destPath  目标路径，
     * @param monitor   上传进度监控，通过实现此接口完成进度显示
     * @param mode      {@link Mode} 模式
     * @return this
     */
    public Sftp put(InputStream srcStream, String destPath, SftpProgressMonitor monitor, Mode mode) {
        try {
            channel.put(srcStream, destPath, monitor, mode.ordinal());
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void download(String src, File dest) {
        get(src, FileKit.getAbsolutePath(dest));
    }

    /**
     * 递归获取远程文件
     *
     * @param source 服务器目录
     * @param dest   本地目录
     */
    @Override
    public void download(String source, String dest) {
        try {
            Vector<LsEntry> fileAndFolderList = channel.ls(source);
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
                String sourcePathPathFile = source + Symbol.SLASH + item.getFilename();
                String destinationPathFile = dest + Symbol.SLASH + item.getFilename();
                if (!item.getAttrs().isDir()) {
                    // 本地不存在文件或者ftp上文件有修改则下载
                    if (!FileKit.exists(destinationPathFile)
                            || (item.getAttrs().getMTime() > (FileKit.lastModifiedTime(destinationPathFile).getTime() / 1000))) {
                        channel.get(sourcePathPathFile, destinationPathFile);
                    }
                } else if (!(Symbol.DOT.equals(item.getFilename()) || Symbol.DOUBLE_DOT.equals(item.getFilename()))) {
                    FileKit.mkdir(destinationPathFile);
                    download(sourcePathPathFile, destinationPathFile);
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件到{@link OutputStream}中
     *
     * @param src 源文件路径，包括文件名
     * @param out 目标流
     */
    public void download(String src, OutputStream out) {
        get(src, out);
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. path为null或""上传到当前路径
     * 2. path为相对路径则相对于当前路径的子路径
     * 3. path为绝对路径则上传到此路径
     * </pre>
     *
     * @param destPath   服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 是否上传成功
     */
    public boolean upload(String destPath, String fileName, InputStream fileStream) {
        destPath = StringKit.addSuffixIfNot(destPath, Symbol.SLASH) + StringKit.removePrefix(fileName, Symbol.SLASH);
        put(fileStream, destPath, null, Mode.OVERWRITE);
        return true;
    }

    /**
     * 获取远程文件
     *
     * @param src  远程文件路径
     * @param dest 目标文件路径
     * @return this
     */
    public Sftp get(String src, String dest) {
        try {
            channel.get(src, dest);
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * 获取远程文件
     *
     * @param src 远程文件路径
     * @param out 目标流
     * @return this
     */
    public Sftp get(String src, OutputStream out) {
        try {
            channel.get(src, out);
        } catch (SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        JSchKit.close(this.channel);
        JSchKit.close(this.session);
    }

    /**
     * JSch支持的三种文件传输模式
     */
    public enum Mode {
        /**
         * 完全覆盖模式,这是JSch的默认文件传输模式,即如果目标文件已经存在,传输的文件将完全覆盖目标文件,产生新的文件
         */
        OVERWRITE,
        /**
         * 恢复模式,如果文件已经传输一部分,这时由于网络或其他任何原因导致文件传输中断,如果下一次传输相同的文件,则会从上一次中断的地方续传
         */
        RESUME,
        /**
         * 追加模式,如果目标文件已存在,传输的文件将在目标文件后追加
         */
        APPEND
    }

}
