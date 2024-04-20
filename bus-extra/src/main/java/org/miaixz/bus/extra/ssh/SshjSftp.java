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

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.extra.ftp.AbstractFtp;
import org.miaixz.bus.extra.ftp.FtpConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 在使用jsch 进行sftp协议下载文件时，总是中文乱码，而该框架源码又不允许设置编码
 *
 * <p>
 * 此类基于sshj 框架适配
 * 参考：https://github.com/hierynomus/sshj
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SshjSftp extends AbstractFtp {

    private SSHClient ssh;
    private SFTPClient sftp;

    /**
     * 构造，使用默认端口
     *
     * @param sshHost 主机
     */
    public SshjSftp(String sshHost) {
        this(new FtpConfig(sshHost, 22, null, null, org.miaixz.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshUser 用户名
     * @param sshPass 密码
     */
    public SshjSftp(String sshHost, String sshUser, String sshPass) {
        this(new FtpConfig(sshHost, 22, sshUser, sshPass, org.miaixz.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     */
    public SshjSftp(String sshHost, int sshPort, String sshUser, String sshPass) {
        this(new FtpConfig(sshHost, sshPort, sshUser, sshPass, org.miaixz.bus.core.lang.Charset.UTF_8));
    }

    /**
     * 构造
     *
     * @param sshHost 主机
     * @param sshPort 端口
     * @param sshUser 用户名
     * @param sshPass 密码
     * @param charset 编码
     */
    public SshjSftp(String sshHost, int sshPort, String sshUser, String sshPass, Charset charset) {
        this(new FtpConfig(sshHost, sshPort, sshUser, sshPass, charset));
    }

    /**
     * 构造
     *
     * @param config FTP配置
     */
    protected SshjSftp(FtpConfig config) {
        super(config);
        init();
    }

    /**
     * SSH 初始化并创建一个sftp客户端
     */
    public void init() {
        this.ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        try {
            ssh.connect(ftpConfig.getHost(), ftpConfig.getPort());
            ssh.authPassword(ftpConfig.getUser(), ftpConfig.getPassword());
            ssh.setRemoteCharset(ftpConfig.getCharset());
            this.sftp = ssh.newSFTPClient();
        } catch (IOException e) {
            throw new InternalException("sftp 初始化失败", e);
        }
    }

    @Override
    public AbstractFtp reconnectIfTimeout() {
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

    @Override
    public boolean cd(String directory) {
        String exec = String.format("cd %s", directory);
        command(exec);
        String pwd = pwd();
        return pwd.equals(directory);
    }

    @Override
    public String pwd() {
        return command("pwd");
    }

    @Override
    public boolean mkdir(String dir) {
        try {
            sftp.mkdir(dir);
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return containsFile(dir);
    }

    @Override
    public List<String> ls(String path) {
        List<RemoteResourceInfo> infoList;
        try {
            infoList = sftp.ls(path);
        } catch (IOException e) {
            throw new InternalException(e);
        }
        if (CollKit.isNotEmpty(infoList)) {
            return CollKit.map(infoList, RemoteResourceInfo::getName, true);
        }
        return null;
    }

    @Override
    public boolean delFile(String path) {
        try {
            sftp.rm(path);
            return !containsFile(path);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean delDir(String dirPath) {
        try {
            sftp.rmdir(dirPath);
            return !containsFile(dirPath);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean upload(String destPath, File file) {
        try {
            sftp.put(new FileSystemFile(file), destPath);
            return containsFile(destPath);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void download(String path, File dest) {
        try {
            sftp.get(path, new FileSystemFile(dest));
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void download(String source, String dest) {
        List<String> files = ls(source);
        if (files != null && !files.isEmpty()) {
            files.forEach(path -> download(source + Symbol.SLASH + path, FileKit.file(dest)));
        }
    }

    @Override
    public void close() {
        try {
            sftp.close();
            ssh.disconnect();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 是否包含该文件
     *
     * @param fileDir 文件绝对路径
     * @return true:包含 false:不包含
     */
    public boolean containsFile(String fileDir) {
        try {
            sftp.lstat(fileDir);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 执行Linux 命令
     *
     * @param exec 命令
     * @return 返回响应结果
     */
    public String command(String exec) {
        Session session = null;
        try {
            session = ssh.startSession();
            final Session.Command command = session.exec(exec);
            InputStream inputStream = command.getInputStream();
            return IoKit.read(inputStream, DEFAULT_CHARSET);
        } catch (Exception e) {
            throw new InternalException(e);
        } finally {
            IoKit.close(session);
        }
    }

}
