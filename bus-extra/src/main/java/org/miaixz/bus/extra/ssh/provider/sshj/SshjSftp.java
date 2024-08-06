/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.extra.ssh.provider.sshj;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ftp.AbstractFtp;
import org.miaixz.bus.extra.ftp.FtpConfig;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.SshjKit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

/**
 * 在使用jsch 进行sftp协议下载文件时，总是中文乱码，而该框架源码又不允许设置编码。故：站在巨人的肩膀上，此类便孕育而出。
 *
 * <p>
 * 基于sshj 框架适配。 参考：<a href="https://github.com/hierynomus/sshj">https://github.com/hierynomus/sshj</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SshjSftp extends AbstractFtp {

    private SSHClient ssh;
    private SFTPClient sftp;
    private Session session;

    /**
     * 构造
     *
     * @param config FTP配置
     */
    public SshjSftp(final FtpConfig config) {
        super(config);
        init();
    }

    /**
     * 构造
     *
     * @param sshClient {@link SSHClient}
     * @param charset   编码
     */
    public SshjSftp(final SSHClient sshClient, final Charset charset) {
        super(FtpConfig.of().setCharset(charset));
        this.ssh = sshClient;
        init();
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshUser 用户名
     * @param sshPass 密码
     * @return SshjSftp
     */
    public static SshjSftp of(final String sshHost, final String sshUser, final String sshPass) {
        return of(sshHost, 22, sshUser, sshPass);
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @return SshjSftp
     */
    public static SshjSftp of(final String sshHost, final int sshPort, final String sshUser, final String sshPass) {
        return of(sshHost, sshPort, sshUser, sshPass, DEFAULT_CHARSET);
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param charset 编码
     * @return SshjSftp
     */
    public static SshjSftp of(final String sshHost, final int sshPort, final String sshUser, final String sshPass,
            final Charset charset) {
        return new SshjSftp(new FtpConfig(Connector.of(sshHost, sshPort, sshUser, sshPass), charset));
    }

    /**
     * SSH 初始化并创建一个sftp客户端
     */
    public void init() {
        if (null == this.ssh) {
            this.ssh = SshjKit.openClient(this.ftpConfig.getConnector());
        }

        try {
            ssh.setRemoteCharset(ftpConfig.getCharset());
            this.sftp = ssh.newSFTPClient();
        } catch (final IOException e) {
            throw new InternalException("sftp 初始化失败.", e);
        }
    }

    @Override
    public AbstractFtp reconnectIfTimeout() {
        if (StringKit.isBlank(this.ftpConfig.getConnector().getHost())) {
            throw new InternalException("Host is blank!");
        }
        try {
            this.cd(Symbol.SLASH);
        } catch (final InternalException e) {
            close();
            init();
        }
        return this;
    }

    @Override
    public boolean cd(final String directory) {
        final String exec = String.format("cd %s", directory);
        command(exec);
        final String pwd = pwd();
        return pwd.equals(directory);
    }

    @Override
    public String pwd() {
        return command("pwd");
    }

    @Override
    public boolean mkdir(final String dir) {
        try {
            sftp.mkdir(dir);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return containsFile(dir);
    }

    @Override
    public List<String> ls(final String path) {
        final List<RemoteResourceInfo> infoList;
        try {
            infoList = sftp.ls(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (CollKit.isNotEmpty(infoList)) {
            return CollKit.map(infoList, RemoteResourceInfo::getName);
        }
        return null;
    }

    @Override
    public boolean delFile(final String path) {
        try {
            sftp.rm(path);
            return !containsFile(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean delDir(final String dirPath) {
        try {
            sftp.rmdir(dirPath);
            return !containsFile(dirPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean uploadFile(final String destPath, final File file) {
        try {
            sftp.put(new FileSystemFile(file), destPath);
            return containsFile(destPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void download(final String path, final File outFile) {
        try {
            sftp.get(path, new FileSystemFile(outFile));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void recursiveDownloadFolder(final String sourcePath, final File destDir) {
        final List<String> files = ls(sourcePath);
        if (files != null && !files.isEmpty()) {
            files.forEach(path -> download(sourcePath + "/" + path, destDir));
        }
    }

    /**
     * 读取远程文件输入流
     *
     * @param path 远程文件路径
     * @return {@link InputStream}
     */
    @Override
    public InputStream getFileStream(final String path) {
        final RemoteFile remoteFile;
        try {
            remoteFile = sftp.open(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        return remoteFile.new ReadAheadRemoteFileInputStream(16);
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.session);
        IoKit.closeQuietly(this.sftp);
        IoKit.closeQuietly(this.ssh);
    }

    /**
     * 是否包含该文件
     *
     * @param fileDir 文件绝对路径
     * @return true:包含 false:不包含
     */
    public boolean containsFile(final String fileDir) {
        try {
            sftp.lstat(fileDir);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * 执行Linux 命令
     *
     * @param exec 命令
     * @return 返回响应结果
     */
    public String command(final String exec) {
        final Session session = this.initSession();

        Session.Command command = null;
        try {
            command = session.exec(exec);
            final InputStream inputStream = command.getInputStream();
            return IoKit.read(inputStream, this.ftpConfig.getCharset());
        } catch (final Exception e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(command);
        }
    }

    /**
     * 初始化Session并返回
     *
     * @return session
     */
    private Session initSession() {
        Session session = this.session;
        if (null == session || !session.isOpen()) {
            IoKit.closeQuietly(session);
            try {
                session = this.ssh.startSession();
            } catch (final Exception e) {
                throw new InternalException(e);
            }
            this.session = session;
        }
        return session;
    }

}
