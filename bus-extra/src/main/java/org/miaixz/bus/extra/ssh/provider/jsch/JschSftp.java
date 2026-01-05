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
package org.miaixz.bus.extra.ssh.provider.jsch;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.ftp.AbstractFtp;
import org.miaixz.bus.extra.ftp.FtpConfig;
import org.miaixz.bus.extra.ssh.Connector;
import org.miaixz.bus.extra.ssh.JschKit;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;

/**
 * SFTP (Secure File Transfer Protocol) client implementation based on JSch. SFTP provides a secure, encrypted method
 * for file transfer over SSH, making it a robust alternative to traditional FTP. Due to encryption and decryption
 * overhead, its transfer efficiency is generally lower than standard FTP. This class is suitable for scenarios with
 * high network security requirements.
 * <p>
 * Reference: <a href=
 * "https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html">https://www.cnblogs.com/longyg/archive/2012/06/25/2556576.html</a>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JschSftp extends AbstractFtp {

    /**
     * The underlying JSch session.
     */
    private Session session;
    /**
     * The underlying JSch SFTP channel.
     */
    private ChannelSftp channel;

    /**
     * Constructs a {@code JschSftp} instance with the given FTP configuration and initializes it immediately.
     *
     * @param config The {@link FtpConfig} containing connection details.
     */
    public JschSftp(final FtpConfig config) {
        this(config, true);
    }

    /**
     * Constructs a {@code JschSftp} instance with the given FTP configuration and an option to initialize immediately.
     *
     * @param config The {@link FtpConfig} containing connection details.
     * @param init   If {@code true}, the SFTP connection is initialized immediately.
     */
    public JschSftp(final FtpConfig config, final boolean init) {
        super(config);
        if (init) {
            init();
        }
    }

    /**
     * Constructs a {@code JschSftp} instance with an existing JSch {@link Session}, character set, and timeout.
     *
     * @param session The pre-existing JSch {@link Session}.
     * @param charset The character set for file names.
     * @param timeOut The connection timeout duration in milliseconds.
     */
    public JschSftp(final Session session, final Charset charset, final long timeOut) {
        super(FtpConfig.of().setCharset(charset).setConnectionTimeout(timeOut));
        this.session = session;
        init();
    }

    /**
     * Constructs a {@code JschSftp} instance with an existing JSch {@link ChannelSftp}, character set, and timeout.
     *
     * @param channel The pre-existing JSch {@link ChannelSftp}.
     * @param charset The character set for file names.
     * @param timeOut The connection timeout duration in milliseconds.
     */
    public JschSftp(final ChannelSftp channel, final Charset charset, final long timeOut) {
        super(FtpConfig.of().setCharset(charset).setConnectionTimeout(timeOut));
        this.channel = channel;
        init();
    }

    /**
     * Creates a {@code JschSftp} instance with the specified SSH connection details.
     *
     * @param sshHost The remote host address.
     * @param sshPort The remote host port.
     * @param sshUser The username for authentication.
     * @param sshPass The password for authentication.
     * @return A new {@code JschSftp} instance.
     */
    public static JschSftp of(final String sshHost, final int sshPort, final String sshUser, final String sshPass) {
        return of(sshHost, sshPort, sshUser, sshPass, DEFAULT_CHARSET);
    }

    /**
     * Creates a {@code JschSftp} instance with the specified SSH connection details and character set.
     *
     * @param sshHost The remote host address.
     * @param sshPort The remote host port.
     * @param sshUser The username for authentication.
     * @param sshPass The password for authentication.
     * @param charset The character set for file names.
     * @return A new {@code JschSftp} instance.
     */
    public static JschSftp of(
            final String sshHost,
            final int sshPort,
            final String sshUser,
            final String sshPass,
            final Charset charset) {
        return new JschSftp(new FtpConfig(Connector.of(sshHost, sshPort, sshUser, sshPass), charset));
    }

    /**
     * Initializes the SFTP connection. If the session or channel is not yet established, it will be created and
     * connected. The filename encoding will be set based on the FTP configuration.
     *
     * @throws InternalException if a JSch or SftpException occurs during initialization.
     */
    public void init() {
        if (null == this.channel) {
            if (null == this.session) {
                final FtpConfig config = this.ftpConfig;
                final Connector connector = config.getConnector();
                this.session = new JschSession(Connector.of(
                        connector.getHost(),
                        connector.getPort(),
                        connector.getUser(),
                        connector.getPassword(),
                        connector.getTimeout())).getRaw();
            }

            if (!session.isConnected()) {
                try {
                    session.connect((int) this.ftpConfig.getConnector().getTimeout());
                } catch (final JSchException e) {
                    throw new InternalException(e);
                }
            }

            try {
                this.channel = (ChannelSftp) this.session.openChannel(ChannelType.SFTP.getValue());
            } catch (final JSchException e) {
                throw new InternalException(e);
            }
        }

        try {
            if (!channel.isConnected()) {
                channel.connect((int) Math.max(this.ftpConfig.getConnector().getTimeout(), 0));
            }
            channel.setFilenameEncoding(this.ftpConfig.getCharset().toString());
        } catch (final JSchException | SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reconnects to the SFTP server if the connection has timed out. This method is designed to be overridden by
     * subclasses for custom reconnection logic. When overriding, ensure proper exception handling and connection state
     * validation.
     *
     * to change to the root directory. If that fails, it closes and reinitializes the connection. Subclasses should
     * call {@code super.reconnectIfTimeout()} or implement equivalent reconnection logic.
     *
     * @return this {@code JschSftp} instance for method chaining
     */
    @Override
    public JschSftp reconnectIfTimeout() {
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

    /**
     * Retrieves the underlying SFTP channel client.
     *
     * @return The {@link ChannelSftp} client.
     * @throws InternalException if an error occurs during initialization or connection.
     */
    public ChannelSftp getClient() {
        if (!this.channel.isConnected()) {
            init();
        }
        return this.channel;
    }

    /**
     * Returns the current working directory on the SFTP server. This method is designed to be overridden by subclasses
     * for custom directory handling. When overriding, ensure proper exception handling and path normalization.
     *
     * Subclasses may override to add caching, custom path resolution, or enhanced error handling.
     *
     * @return The current working directory path
     * @throws InternalException if an SFTP error occurs
     */
    @Override
    public String pwd() {
        try {
            return getClient().pwd();
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves the HOME directory on the SFTP server.
     *
     * @return The HOME directory path.
     * @throws InternalException if an SftpException occurs.
     */
    public String home() {
        try {
            return getClient().getHome();
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Lists all files and directories in a given path, without recursion. This method is designed to be overridden by
     * subclasses for custom listing logic. When overriding, ensure proper filtering of "." and ".." entries.
     *
     * Subclasses may override to add custom filtering, sorting, or enhanced error handling.
     *
     * @param path The path to list files and directories from.
     * @return A list of file and directory names.
     */
    @Override
    public List<String> ls(final String path) {
        return ls(path, null);
    }

    /**
     * Lists all directories in a given path, without recursion.
     *
     * @param path The path to list directories from.
     * @return A list of directory names.
     */
    public List<String> lsDirs(final String path) {
        return ls(path, t -> t.getAttrs().isDir());
    }

    /**
     * Lists all files in a given path, without recursion.
     *
     * @param path The path to list files from.
     * @return A list of file names.
     */
    public List<String> lsFiles(final String path) {
        return ls(path, t -> !t.getAttrs().isDir());
    }

    /**
     * Lists all files or directories in a given path, without recursion, applying a filter. This method automatically
     * filters out "." and ".." directories.
     *
     * @param path      The path to list files or directories from.
     * @param predicate A file or directory filter. {@link Predicate#test(Object)} returning {@code true} keeps the
     *                  entry.
     * @return A list of directory or file names.
     * @throws InternalException if an SftpException occurs other than "No such file".
     */
    public List<String> ls(final String path, final Predicate<LsEntry> predicate) {
        final List<LsEntry> entries = lsEntries(path, predicate);
        if (CollKit.isEmpty(entries)) {
            return ListKit.empty();
        }
        return CollKit.map(entries, LsEntry::getFilename);
    }

    /**
     * Lists all files or directories in a given path, generating a list of {@link LsEntry} objects, without recursion.
     * This method automatically filters out "." and ".." directories.
     *
     * @param path The path to list files or directories from.
     * @return A list of {@link LsEntry} objects.
     */
    public List<LsEntry> lsEntries(final String path) {
        return lsEntries(path, null);
    }

    /**
     * Lists all files or directories in a given path, generating a list of {@link LsEntry} objects, without recursion,
     * applying a filter. This method automatically filters out "." and ".." directories.
     *
     * @param path      The path to list files or directories from.
     * @param predicate A file or directory filter. {@link Predicate#test(Object)} returning {@code true} keeps the
     *                  entry.
     * @return A list of {@link LsEntry} objects.
     * @throws InternalException if an SftpException occurs other than "No such file".
     */
    public List<LsEntry> lsEntries(final String path, final Predicate<LsEntry> predicate) {
        final List<LsEntry> entryList = new ArrayList<>();
        try {
            getClient().ls(path, entry -> {
                final String fileName = entry.getFilename();
                if (!StringKit.equals(".", fileName) && !StringKit.equals("..", fileName)) {
                    if (null == predicate || predicate.test(entry)) {
                        entryList.add(entry);
                    }
                }
                return LsEntrySelector.CONTINUE;
            });
        } catch (final SftpException e) {
            if (!StringKit.startWithIgnoreCase(e.getMessage(), "No such file")) {
                throw new InternalException(e);
            }
        }
        return entryList;
    }

    /**
     * Renames a file or directory on the SFTP server. This method is designed to be overridden by subclasses for custom
     * rename logic. When overriding, ensure proper validation of paths and handling of atomic operations.
     *
     * Subclasses may override to add validation, logging, or transaction support.
     *
     * @param oldPath The current path of the file or directory.
     * @param newPath The new path for the file or directory.
     * @return {@code true} if the rename operation was successful.
     * @throws InternalException if an SFTP error occurs during the rename operation.
     */
    @Override
    public boolean rename(final String oldPath, final String newPath) {
        try {
            getClient().rename(oldPath, newPath);
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return true;
    }

    /**
     * Creates a directory on the SFTP server. This method is designed to be overridden by subclasses for custom
     * directory creation logic. When overriding, ensure proper path validation and handling of existing directories.
     *
     * Subclasses may override to add recursive creation, permission setting, or enhanced error handling.
     *
     * @param dir The directory path to create.
     * @return {@code true} if the directory was created successfully or already exists.
     * @throws InternalException if an SFTP error occurs during directory creation.
     */
    @Override
    public boolean mkdir(final String dir) {
        if (isDir(dir)) {
            return true;
        }
        try {
            getClient().mkdir(dir);
            return true;
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if the specified path is a directory. This method is designed to be overridden by subclasses for custom
     * directory checking.
     *
     * Subclasses may override to add caching or custom validation.
     *
     * @param dir The path to check.
     * @return {@code true} if the path is a directory, {@code false} otherwise.
     * @throws InternalException if an SFTP error occurs (except for "No such file" errors).
     */
    @Override
    public boolean isDir(final String dir) {
        final SftpATTRS sftpATTRS;
        try {
            sftpATTRS = getClient().stat(dir);
        } catch (final SftpException e) {
            final String msg = e.getMessage();
            if (StringKit.containsAnyIgnoreCase(msg, "No such file", "does not exist")) {
                return false;
            }
            throw new InternalException(e);
        }
        return sftpATTRS.isDir();
    }

    /**
     * Changes the current working directory on the SFTP server. This method is designed to be overridden by subclasses
     * for custom directory handling.
     *
     * Subclasses may override to add path normalization or enhanced error handling.
     *
     * @param directory The target directory path.
     * @return {@code true} if the directory change was successful.
     * @throws InternalException if an SFTP error occurs.
     */
    @Override
    synchronized public boolean cd(final String directory) throws InternalException {
        if (StringKit.isBlank(directory)) {
            return true;
        }
        try {
            getClient().cd(directory.replace('\\', '/'));
            return true;
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Deletes a file on the SFTP server. This method is designed to be overridden by subclasses for custom file
     * deletion logic. When overriding, ensure proper validation of file paths and handling of permissions.
     *
     * Subclasses may override to add validation, logging, or undo/redo support.
     *
     * @param filePath The path of the file to delete.
     * @return {@code true} if the file was deleted successfully.
     * @throws InternalException if an SFTP error occurs during file deletion.
     */
    @Override
    public boolean delFile(final String filePath) {
        try {
            getClient().rm(filePath);
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return true;
    }

    /**
     * Deletes a directory and all its contents recursively on the SFTP server. This method is designed to be overridden
     * by subclasses for custom directory deletion logic. When overriding, ensure proper handling of symbolic links and
     * circular references.
     *
     * Subclasses may override to add non-recursive deletion, progress tracking, or enhanced error handling.
     *
     * @param dirPath The path of the directory to delete.
     * @return {@code true} if the directory was deleted successfully.
     * @throws InternalException if an SFTP error occurs during directory deletion.
     */
    @Override
    public boolean delDir(final String dirPath) {
        if (!cd(dirPath)) {
            return false;
        }

        final ChannelSftp channel = getClient();

        final Vector<LsEntry> list;
        try {
            list = channel.ls(channel.pwd());
        } catch (final SftpException e) {
            throw new InternalException(e);
        }

        String fileName;
        for (final LsEntry entry : list) {
            fileName = entry.getFilename();
            if (!".".equals(fileName) && !"..".equals(fileName)) {
                if (entry.getAttrs().isDir()) {
                    delDir(fileName);
                } else {
                    delFile(fileName);
                }
            }
        }

        if (!cd("..")) {
            return false;
        }

        try {
            channel.rmdir(dirPath);
            return true;
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Recursively uploads a local file or folder to a remote path, overwriting existing files.
     *
     * @param remotePath The remote path.
     * @param file       The local file or folder to upload.
     */
    public void upload(final String remotePath, final File file) {
        if (!FileKit.exists(file)) {
            return;
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (final File fileItem : files) {
                if (fileItem.isDirectory()) {
                    final String mkdir = FileKit.normalize(remotePath + "/" + fileItem.getName());
                    this.upload(mkdir, fileItem);
                } else {
                    this.uploadFile(remotePath, fileItem);
                }
            }
        } else {
            this.uploadFile(remotePath, file);
        }
    }

    /**
     * Uploads a file to the SFTP server. This method is designed to be overridden by subclasses for custom upload
     * logic.
     *
     * and uploads the file. Subclasses may override to add progress monitoring or validation.
     *
     * @param destPath The destination path on the server.
     * @param file     The local file to upload.
     * @return {@code true} if the upload was successful.
     * @throws InternalException if the file is invalid or an SFTP error occurs.
     */
    @Override
    public boolean uploadFile(final String destPath, final File file) {
        if (!FileKit.isFile(file)) {
            throw new InternalException("[{}] is not a file!", file);
        }
        this.mkDirs(destPath);
        put(FileKit.getAbsolutePath(file), destPath);
        return true;
    }

    /**
     * Uploads a file from an InputStream to the specified remote directory.
     *
     * @param destPath   The destination path on the server.
     * @param fileName   The filename.
     * @param fileStream The file input stream.
     * @throws InternalException if an SftpException occurs.
     */
    public void uploadFile(String destPath, final String fileName, final InputStream fileStream) {
        destPath = StringKit.addSuffixIfNot(destPath, Symbol.SLASH) + StringKit.removePrefix(fileName, Symbol.SLASH);
        put(fileStream, destPath, null, Mode.OVERWRITE);
    }

    /**
     * Uploads a local file to the target server, overwriting existing files.
     *
     * @param srcFilePath The local file path.
     * @param destPath    The target path.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp put(final String srcFilePath, final String destPath) {
        return put(srcFilePath, destPath, Mode.OVERWRITE);
    }

    /**
     * Uploads a local file to the target server with a specified transfer mode.
     *
     * @param srcFilePath The local file path.
     * @param destPath    The target path.
     * @param mode        The {@link Mode} for file transfer.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp put(final String srcFilePath, final String destPath, final Mode mode) {
        return put(srcFilePath, destPath, null, mode);
    }

    /**
     * Uploads a local file to the target server with a progress monitor and transfer mode.
     *
     * @param srcFilePath The local file path.
     * @param destPath    The target path.
     * @param monitor     The upload progress monitor.
     * @param mode        The {@link Mode} for file transfer.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp put(final String srcFilePath, String destPath, final SftpProgressMonitor monitor, final Mode mode) {
        if (StringKit.isEmpty(destPath)) {
            destPath = pwd();
        }
        try {
            getClient().put(srcFilePath, destPath, monitor, mode.ordinal());
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Uploads a local data stream to the target server.
     *
     * @param srcStream The local data stream.
     * @param destPath  The target path.
     * @param monitor   The upload progress monitor.
     * @param mode      The {@link Mode} for file transfer.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp put(
            final InputStream srcStream,
            String destPath,
            final SftpProgressMonitor monitor,
            final Mode mode) {
        if (StringKit.isEmpty(destPath)) {
            destPath = pwd();
        }
        try {
            getClient().put(srcStream, destPath, monitor, mode.ordinal());
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Downloads a file from the SFTP server. This method is designed to be overridden by subclasses for custom download
     * logic.
     *
     * Subclasses may override to add progress monitoring or resumption support.
     *
     * @param remotePath The path of the remote file to download.
     * @param destFile   The local file to save the downloaded content to.
     */
    @Override
    public void download(final String remotePath, final File destFile) {
        get(remotePath, FileKit.getAbsolutePath(destFile));
    }

    /**
     * Downloads a file to an {@link OutputStream}.
     *
     * @param src The source file path on the remote server.
     * @param out The target output stream.
     */
    public void download(final String src, final OutputStream out) {
        get(src, out);
    }

    /**
     * Recursively downloads a folder from the SFTP server. This method is designed to be overridden by subclasses for
     * custom recursive download logic.
     *
     * comparing timestamps to avoid re-downloading unchanged files. Subclasses may override to add filtering or
     * parallel download.
     *
     * @param remotePath The remote directory path to download.
     * @param targetDir  The local directory to save files to.
     */
    @Override
    public void recursiveDownloadFolder(final String remotePath, final File targetDir) {
        String fileName;
        String srcFile;
        File destFile;
        for (final LsEntry item : lsEntries(remotePath)) {
            fileName = item.getFilename();
            srcFile = StringKit.format("{}/{}", remotePath, fileName);
            destFile = FileKit.file(targetDir, fileName);

            if (!item.getAttrs().isDir()) {
                if (!FileKit.exists(destFile) || (item.getAttrs().getMTime() > (destFile.lastModified() / 1000))) {
                    download(srcFile, destFile);
                }
            } else {
                FileKit.mkdir(destFile);
                recursiveDownloadFolder(srcFile, destFile);
            }
        }
    }

    /**
     * Retrieves a remote file and saves it to a local path.
     *
     * @param src  The remote file path.
     * @param dest The target file path.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp get(final String src, final String dest) {
        try {
            getClient().get(src, dest);
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Retrieves a remote file and writes it to an output stream.
     *
     * @param src The remote file path.
     * @param out The target output stream.
     * @return This {@code JschSftp} instance.
     * @throws InternalException if an SftpException occurs.
     */
    public JschSftp get(final String src, final OutputStream out) {
        try {
            getClient().get(src, out);
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
        return this;
    }

    /**
     * Gets an input stream for reading a remote file. This method is designed to be overridden by subclasses for custom
     * stream handling. When overriding, ensure proper stream management and resource cleanup.
     *
     * Subclasses may override to add buffering, progress monitoring, or enhanced error handling.
     *
     * @param path The path of the remote file to read.
     * @return An input stream for reading the remote file.
     * @throws InternalException if an SFTP error occurs.
     */
    @Override
    public InputStream getFileStream(final String path) {
        try {
            return getClient().get(path);
        } catch (final SftpException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Closes the SFTP connection and releases all resources. This method is designed to be overridden by subclasses for
     * custom cleanup logic. When overriding, ensure the method is idempotent and all resources are properly released.
     *
     * Subclasses should call {@code super.close()} to ensure proper cleanup of inherited resources.
     */
    @Override
    public void close() {
        JschKit.close(this.channel);
        this.channel = null;
        JschKit.close(this.session);
        this.session = null;
    }

    /**
     * Returns a string representation of this SFTP client. This method is designed to be overridden by subclasses for
     * custom string representation.
     *
     * Subclasses may override to add additional connection details.
     *
     * @return A string representation of this SFTP client.
     */
    @Override
    public String toString() {
        final Connector connector = this.ftpConfig.getConnector();
        return "JschSftp{" + "host='" + connector.getHost() + '\'' + ", port=" + connector.getPort() + ", user='"
                + connector.getUser() + '\'' + '}';
    }

    /**
     * Enumeration of file transfer modes supported by JSch.
     */
    public enum Mode {
        /**
         * Overwrite mode: If the target file exists, it will be completely overwritten.
         */
        OVERWRITE,
        /**
         * Resume mode: If a file transfer is interrupted, it can be resumed from the point of interruption.
         */
        RESUME,
        /**
         * Append mode: If the target file exists, the transferred file will be appended to the end of it.
         */
        APPEND
    }

}
