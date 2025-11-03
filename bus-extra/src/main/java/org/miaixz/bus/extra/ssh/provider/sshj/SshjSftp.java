/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.FileKit;
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
 * An SFTP client implementation based on the SSHJ library. This class was created to address issues with character
 * encoding (e.g., garbled Chinese characters) when using other libraries like JSch for SFTP file downloads, as SSHJ
 * provides better control over character sets.
 *
 * <p>
 * Adapted from the SSHJ library. For more details, see the project homepage:
 * <a href="https://github.com/hierynomus/sshj">https://github.com/hierynomus/sshj</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SshjSftp extends AbstractFtp {

    /**
     * The underlying SSHJ SSH client.
     */
    private SSHClient ssh;
    /**
     * The underlying SSHJ SFTP client.
     */
    private SFTPClient sftp;
    /**
     * The SSHJ session, used for executing commands.
     */
    private Session session;
    /**
     * The current working directory on the remote server.
     */
    private String workingDir;

    /**
     * Constructs an {@code SshjSftp} instance with the given FTP configuration and initializes it.
     *
     * @param config The {@link FtpConfig} containing connection details.
     */
    public SshjSftp(final FtpConfig config) {
        super(config);
        init();
    }

    /**
     * Constructs an {@code SshjSftp} instance with an existing {@link SSHClient} and character set, and initializes it.
     *
     * @param sshClient The pre-configured {@link SSHClient} instance.
     * @param charset   The character set for file names.
     */
    public SshjSftp(final SSHClient sshClient, final Charset charset) {
        super(FtpConfig.of().setCharset(charset));
        this.ssh = sshClient;
        init();
    }

    /**
     * Creates an {@code SshjSftp} instance with the specified SSH host, username, and password, using the default port
     * (22).
     *
     * @param sshHost The SSH host address.
     * @param sshUser The SSH username.
     * @param sshPass The SSH password.
     * @return A new {@code SshjSftp} instance.
     */
    public static SshjSftp of(final String sshHost, final String sshUser, final String sshPass) {
        return of(sshHost, 22, sshUser, sshPass);
    }

    /**
     * Creates an {@code SshjSftp} instance with the specified SSH host, port, username, and password.
     *
     * @param sshHost The SSH host address.
     * @param sshPort The SSH port.
     * @param sshUser The SSH username.
     * @param sshPass The SSH password.
     * @return A new {@code SshjSftp} instance.
     */
    public static SshjSftp of(final String sshHost, final int sshPort, final String sshUser, final String sshPass) {
        return of(sshHost, sshPort, sshUser, sshPass, DEFAULT_CHARSET);
    }

    /**
     * Creates an {@code SshjSftp} instance with the specified SSH host, port, username, password, and character set.
     *
     * @param sshHost The SSH host address.
     * @param sshPort The SSH port.
     * @param sshUser The SSH username.
     * @param sshPass The SSH password.
     * @param charset The character set for file names.
     * @return A new {@code SshjSftp} instance.
     */
    public static SshjSftp of(
            final String sshHost,
            final int sshPort,
            final String sshUser,
            final String sshPass,
            final Charset charset) {
        return new SshjSftp(new FtpConfig(Connector.of(sshHost, sshPort, sshUser, sshPass), charset));
    }

    /**
     * Initializes the SSH connection and creates an SFTP client. If the SSH client is not already initialized, it will
     * be created and connected.
     *
     * @throws InternalException if SFTP initialization fails due to an I/O error.
     */
    public void init() {
        if (null == this.ssh) {
            this.ssh = SshjKit.openClient(this.ftpConfig.getConnector());
        }

        try {
            ssh.setRemoteCharset(ftpConfig.getCharset());
            this.sftp = ssh.newSFTPClient();
        } catch (final IOException e) {
            throw new InternalException("sftp initialization failed.", e);
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
        final String newPath = getPath(directory);
        try {
            sftp.ls(newPath);
            this.workingDir = newPath;
            return true;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String pwd() {
        return getPath(null);
    }

    @Override
    public boolean rename(final String oldPath, final String newPath) {
        try {
            sftp.rename(oldPath, newPath);
            return containsFile(newPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean mkdir(final String dir) {
        try {
            sftp.mkdir(getPath(dir));
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return containsFile(getPath(dir));
    }

    @Override
    public List<String> ls(final String path) {
        final List<RemoteResourceInfo> infoList;
        try {
            infoList = sftp.ls(getPath(path));
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
            sftp.rm(getPath(path));
            return !containsFile(getPath(path));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean delDir(final String dirPath) {
        try {
            sftp.rmdir(getPath(dirPath));
            return !containsFile(getPath(dirPath));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean uploadFile(String destPath, final File file) {
        if (null == destPath) {
            destPath = pwd();
        }
        try {
            if (StringKit.endWith(destPath, Symbol.SLASH)) {
                destPath += file.getName();
            }
            sftp.put(new FileSystemFile(file), getPath(destPath));
            return containsFile(getPath(destPath));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void download(final String destPath, final File outFile) {
        try {
            sftp.get(getPath(destPath), new FileSystemFile(outFile));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public void recursiveDownloadFolder(final String destPath, final File targetDir) {
        if (!targetDir.exists()) {
            if (!targetDir.mkdirs()) {
                throw new InternalException("Failed to create directory " + targetDir.getAbsolutePath());
            }
        } else if (!targetDir.isDirectory()) {
            throw new InternalException("Target is not a directory!");
        }

        final List<String> files = ls(getPath(destPath));
        if (CollKit.isNotEmpty(files)) {
            files.forEach(file -> download(destPath + Symbol.SLASH + file, FileKit.file(targetDir, file)));
        }
    }

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
     * Checks if the specified file exists on the remote server.
     *
     * @param fileDir The absolute path of the file.
     * @return {@code true} if the file exists, {@code false} otherwise.
     */
    public boolean containsFile(final String fileDir) {
        try {
            sftp.lstat(getPath(fileDir));
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Executes a command on the remote server.
     *
     * @param exec The command to execute.
     * @return The command's output as a string.
     * @throws InternalException if an error occurs during command execution.
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
     * Initializes and returns an SSHJ Session for command execution.
     *
     * @return An active SSHJ {@link Session} instance.
     * @throws InternalException if an error occurs during session creation.
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

    /**
     * Resolves a given path to an absolute path on the remote server.
     *
     * @param path The path to resolve. Can be relative or absolute.
     * @return The resolved absolute path.
     * @throws InternalException if an I/O error occurs when canonicalizing the path.
     */
    private String getPath(String path) {
        if (StringKit.isBlank(this.workingDir)) {
            try {
                this.workingDir = sftp.canonicalize(Normal.EMPTY);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        if (StringKit.isBlank(path)) {
            return this.workingDir;
        }

        if (StringKit.startWith(path, Symbol.SLASH)) {
            return path;
        } else {
            String tmp = StringKit.removeSuffix(this.workingDir, Symbol.SLASH);
            return StringKit.format("{}/{}", tmp, path);
        }
    }

}
