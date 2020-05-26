/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.extra.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import org.aoju.bus.core.lang.Filter;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.extra.ftp.AbstractFtp;
import org.aoju.bus.extra.ftp.FtpConfig;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * SFTP是Secure File Transfer Protocol的缩写,安全文件传送协议 可以为传输文件提供一种安全的加密方法
 * SFTP 为 SSH的一部份,是一种传输文件到服务器的安全方式 SFTP是使用加密传输认证信息和传输的数据,所以,使用SFTP是非常安全的
 * 但是,由于这种传输方式使用了加密/解密技术,所以传输效率比普通的FTP要低得多,如果您对网络安全性要求更高时,可以使用SFTP代替FTP
 *
 * <p>
 * 此类为基于jsch的SFTP实现
 * 参考：https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html
 * </p>
 *
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
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
        super(config);
        init(config);
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
        init(SshKit.getSession(sshHost, sshPort, sshUser, sshPass), charset);
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
        init(SshKit.openSftp(session, (int) this.ftpConfig.getConnectionTimeout()), charset);
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
            throw new InstrumentException(e);
        }
        this.channel = channel;
    }

    @Override
    public Sftp reconnectIfTimeout() {
        if (false == this.cd(Symbol.SLASH) && StringKit.isNotBlank(this.ftpConfig.getHost())) {
            init(this.ftpConfig);
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
            throw new InstrumentException(e);
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
            throw new InstrumentException(e);
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
    public List<String> ls(String path, final Filter<LsEntry> filter) {
        final List<String> fileNames = new ArrayList<>();
        try {
            channel.ls(path, entry -> {
                String fileName = entry.getFilename();
                if (false == StringKit.equals(Symbol.DOT, fileName)
                        && false == StringKit.equals(Symbol.DOUBLE_DOT, fileName)) {
                    if (null == filter || filter.accept(entry)) {
                        fileNames.add(entry.getFilename());
                    }
                }
                return LsEntrySelector.CONTINUE;
            });
        } catch (SftpException e) {
            if (false == StringKit.startWithIgnoreCase(e.getMessage(), "No such file")) {
                throw new InstrumentException(e);
            }
        }
        return fileNames;
    }

    @Override
    public boolean mkdir(String dir) {
        try {
            this.channel.mkdir(dir);
            return true;
        } catch (SftpException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 打开指定目录,如果指定路径非目录或不存在返回false
     *
     * @param directory directory
     * @return 是否打开目录
     */
    @Override
    public boolean cd(String directory) {
        if (StringKit.isBlank(directory)) {
            // 当前目录
            return true;
        }
        try {
            channel.cd(directory.replaceAll("\\\\", Symbol.SLASH));
            return true;
        } catch (SftpException e) {
            return false;
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
            throw new InstrumentException(e);
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
            throw new InstrumentException(e);
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
            throw new InstrumentException(e);
        }
    }

    @Override
    public boolean upload(String destPath, File file) {
        put(FileKit.getAbsolutePath(file), destPath);
        return true;
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
            throw new InstrumentException(e);
        }
        return this;
    }

    @Override
    public void download(String src, File destFile) {
        get(src, FileKit.getAbsolutePath(destFile));
    }

    /**
     * 递归获取远程文件
     *
     * @param sourcePath 服务器目录
     * @param destPath   本地目录
     */
    @Override
    public void download(String sourcePath, String destPath) {
        try {
            Vector<LsEntry> fileAndFolderList = channel.ls(sourcePath);
            for (ChannelSftp.LsEntry item : fileAndFolderList) {
                String sourcePathPathFile = sourcePath + Symbol.SLASH + item.getFilename();
                String destinationPathFile = destPath + Symbol.SLASH + item.getFilename();
                if (!item.getAttrs().isDir()) {
                    // 本地不存在文件或者ftp上文件有修改则下载
                    if (!FileKit.exist(destinationPathFile)
                            || (item.getAttrs().getMTime() > (FileKit.lastModifiedTime(destinationPathFile).getTime() / 1000))) {
                        // Download file from source (source filename, destination filename).
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
            throw new InstrumentException(e);
        }
        return this;
    }

    @Override
    public void close() {
        SshKit.close(this.channel);
        SshKit.close(this.session);
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
