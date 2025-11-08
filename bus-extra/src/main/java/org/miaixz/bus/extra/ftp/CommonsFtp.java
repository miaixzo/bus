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
package org.miaixz.bus.extra.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.extra.ssh.Connector;

/**
 * FTP client wrapper based on Apache Commons Net. Common tools for setting up FTP servers include:
 * <ul>
 * <li>FileZilla Server: The root directory is generally empty.</li>
 * <li>Linux vsftpd: Uses the system user's directory, which is often not the root directory, e.g., /home/bus/ftp.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CommonsFtp extends AbstractFtp {

    /**
     * The underlying FTP client from Apache Commons Net.
     */
    private FTPClient client;
    /**
     * The connection lifecycle (ACTIVE or PASSIVE).
     */
    private EnumValue.Lifecycle lifecycle;
    /**
     * Whether to return to the current working directory after an operation is completed.
     */
    private boolean backToPwd;

    /**
     * Constructor.
     *
     * @param config    The FTP configuration.
     * @param lifecycle The connection lifecycle (ACTIVE or PASSIVE).
     */
    public CommonsFtp(final FtpConfig config, final EnumValue.Lifecycle lifecycle) {
        super(config);
        this.lifecycle = lifecycle;
        this.init();
    }

    /**
     * Constructor with a custom-instantiated {@link FTPClient}.
     *
     * @param client The pre-configured {@link FTPClient}.
     */
    public CommonsFtp(final FTPClient client) {
        super(FtpConfig.of());
        this.client = client;
    }

    /**
     * Creates a CommonsFtp instance with anonymous login.
     *
     * @param host The domain name or IP address.
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(final String host) {
        return of(host, PORT._21.getPort());
    }

    /**
     * Creates a CommonsFtp instance with anonymous login.
     *
     * @param host The domain name or IP address.
     * @param port The port number.
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(final String host, final int port) {
        return of(host, port, "anonymous", Normal.EMPTY);
    }

    /**
     * Creates a CommonsFtp instance.
     *
     * @param host     The domain name or IP address.
     * @param port     The port number.
     * @param user     The username.
     * @param password The password.
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(final String host, final int port, final String user, final String password) {
        return of(Connector.of(host, port, user, password), DEFAULT_CHARSET);
    }

    /**
     * Creates a CommonsFtp instance.
     *
     * @param connector The connection information, including host, port, user, password, etc.
     * @param charset   The character encoding.
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(final Connector connector, final java.nio.charset.Charset charset) {
        return of(connector, charset, null, null);
    }

    /**
     * Creates a CommonsFtp instance.
     *
     * @param connector          The connection information, including host, port, user, password, etc.
     * @param charset            The character encoding.
     * @param serverLanguageCode The server language code (e.g., "zh").
     * @param systemKey          The server system key (e.g., org.apache.commons.net.ftp.FTPClientConfig.SYST_NT).
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(
            final Connector connector,
            final java.nio.charset.Charset charset,
            final String serverLanguageCode,
            final String systemKey) {
        return of(connector, charset, serverLanguageCode, systemKey, null);
    }

    /**
     * Creates a CommonsFtp instance.
     *
     * @param connector          The connection information, including host, port, user, password, etc.
     * @param charset            The character encoding.
     * @param serverLanguageCode The server language code.
     * @param systemKey          The system key.
     * @param lifecycle          The connection lifecycle.
     * @return A new CommonsFtp instance.
     */
    public static CommonsFtp of(
            final Connector connector,
            final java.nio.charset.Charset charset,
            final String serverLanguageCode,
            final String systemKey,
            final EnumValue.Lifecycle lifecycle) {
        return new CommonsFtp(new FtpConfig(connector, charset, serverLanguageCode, systemKey), lifecycle);
    }

    /**
     * Initializes the connection.
     *
     * @return this
     */
    public CommonsFtp init() {
        return this.init(this.ftpConfig, this.lifecycle);
    }

    /**
     * Initializes the connection.
     *
     * @param config    The FTP configuration.
     * @param lifecycle The connection lifecycle.
     * @return this
     */
    public CommonsFtp init(final FtpConfig config, final EnumValue.Lifecycle lifecycle) {
        final FTPClient client = new FTPClient();
        client.setRemoteVerificationEnabled(false);

        final java.nio.charset.Charset charset = config.getCharset();
        if (null != charset) {
            client.setControlEncoding(charset.toString());
        }
        client.setConnectTimeout((int) config.getConnector().getTimeout());
        final String systemKey = config.getSystemKey();
        if (StringKit.isNotBlank(systemKey)) {
            final FTPClientConfig conf = new FTPClientConfig(systemKey);
            final String serverLanguageCode = config.getServerLanguageCode();
            if (StringKit.isNotBlank(serverLanguageCode)) {
                conf.setServerLanguageCode(config.getServerLanguageCode());
            }
            client.configure(conf);
        }

        // connect
        final Connector connector = config.getConnector();
        try {
            // Connect to the FTP server
            client.connect(connector.getHost(), connector.getPort());
            client.setSoTimeout((int) config.getSoTimeout());
            // Log in to the FTP server
            client.login(connector.getUser(), connector.getPassword());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        final int replyCode = client.getReplyCode(); // Check if login was successful
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            try {
                client.disconnect();
            } catch (final IOException e) {
                // ignore
            }
            throw new InternalException("Login failed for user [{}], reply code is: [{}]", connector.getUser(),
                    replyCode);
        }
        this.client = client;
        if (lifecycle != null) {
            // noinspection resource
            setMode(lifecycle);
        }
        return this;
    }

    /**
     * Sets the FTP connection lifecycle (ACTIVE or PASSIVE).
     *
     * @param lifecycle The lifecycle enumeration.
     * @return this
     */
    public CommonsFtp setMode(final EnumValue.Lifecycle lifecycle) {
        this.lifecycle = lifecycle;
        switch (lifecycle) {
            case ACTIVE:
                this.client.enterLocalActiveMode();
                break;

            case PASSIVE:
                this.client.enterLocalPassiveMode();
                break;
        }
        return this;
    }

    /**
     * Gets whether to return to the current directory after an operation.
     *
     * @return True if it returns to the current directory, false otherwise.
     */
    public boolean isBackToPwd() {
        return this.backToPwd;
    }

    /**
     * Sets whether to return to the current directory after an operation.
     *
     * @param backToPwd True to return to the current directory, false otherwise.
     * @return this
     */
    public CommonsFtp setBackToPwd(final boolean backToPwd) {
        this.backToPwd = backToPwd;
        return this;
    }

    /**
     * Reconnects if the connection has timed out. It has been tested that when the connection times out,
     * client.isConnected() still returns true, so it is not possible to determine if the connection has timed out.
     * Therefore, the connection status is checked by sending a PWD command.
     *
     * @return this
     */
    @Override
    public CommonsFtp reconnectIfTimeout() {
        String pwd = null;
        try {
            pwd = pwd();
        } catch (final InternalException fex) {
            // ignore
        }

        if (pwd == null) {
            return this.init();
        }
        return this;
    }

    /**
     * Changes the current directory.
     *
     * @param directory The target directory.
     * @return True if successful, false otherwise.
     */
    @Override
    synchronized public boolean cd(final String directory) {
        if (StringKit.isBlank(directory)) {
            // Stay in the current directory
            return true;
        }

        try {
            return client.changeWorkingDirectory(directory);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the current remote directory.
     *
     * @return The current remote directory path.
     */
    @Override
    public String pwd() {
        try {
            return client.printWorkingDirectory();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public List<String> ls(final String path) {
        return ArrayKit.mapToList(lsFiles(path), FTPFile::getName);
    }

    /**
     * Lists all files and directories in a given path without recursion. This method automatically filters out "." and
     * "..".
     *
     * @param path      The directory path.
     * @param predicate A filter for the files. If null, no filtering is applied (besides removing "." and "..").
     * @return A list of file or directory names.
     */
    public List<String> ls(final String path, final Predicate<FTPFile> predicate) {
        return CollKit.map(lsFiles(path, predicate), FTPFile::getName);
    }

    /**
     * Lists all files and directories in a given path without recursion. This method automatically filters out "." and
     * "..".
     *
     * @param path      The directory path.
     * @param predicate A filter for the files. If null, no filtering is applied (besides removing "." and "..").
     * @return A list of files or directories.
     */
    public List<FTPFile> lsFiles(final String path, final Predicate<FTPFile> predicate) {
        final FTPFile[] ftpFiles = lsFiles(path);
        if (ArrayKit.isEmpty(ftpFiles)) {
            return ListKit.empty();
        }

        final List<FTPFile> result = new ArrayList<>(ftpFiles.length - 2 <= 0 ? ftpFiles.length : ftpFiles.length - 2);
        String fileName;
        for (final FTPFile ftpFile : ftpFiles) {
            fileName = ftpFile.getName();
            if (!StringKit.equals(".", fileName) && !StringKit.equals("..", fileName)) {
                if (null == predicate || predicate.test(ftpFile)) {
                    result.add(ftpFile);
                }
            }
        }
        return result;
    }

    /**
     * Lists all files and directories in a given path without recursion.
     *
     * @param path The directory path. If the directory does not exist, an exception is thrown.
     * @return An array of files or directories.
     * @throws InternalException if an I/O error occurs.
     */
    public FTPFile[] lsFiles(final String path) throws InternalException {
        String pwd = null;
        if (StringKit.isNotBlank(path)) {
            pwd = pwd();
            if (!cd(path)) {
                throw new InternalException("Change dir to [{}] error, maybe path not exist!", path);
            }
        }

        FTPFile[] ftpFiles;
        try {
            ftpFiles = this.client.listFiles();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            // Return to the original directory
            cd(pwd);
        }

        return ftpFiles;
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        try {
            return this.client.rename(oldPath, newPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean mkdir(final String dir) throws InternalException {
        try {
            return this.client.makeDirectory(dir);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the status of a directory on the server.
     *
     * @param path The path.
     * @return An integer status code, which varies by server.
     * @throws InternalException if an I/O error occurs.
     */
    public int stat(final String path) throws InternalException {
        try {
            return this.client.stat(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if a directory on the FTP server has any sub-elements (directories or files).
     *
     * @param path The file path.
     * @return True if it exists, false otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public boolean existFile(final String path) throws InternalException {
        final FTPFile[] ftpFileArr;
        try {
            ftpFileArr = client.listFiles(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return ArrayKit.isNotEmpty(ftpFileArr);
    }

    @Override
    public boolean delFile(final String path) throws InternalException {
        final String pwd = pwd();
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        if (!cd(dir)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", path);
        }

        boolean isSuccess;
        try {
            isSuccess = client.deleteFile(fileName);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            // Return to the original directory
            cd(pwd);
        }
        return isSuccess;
    }

    @Override
    public boolean delDir(final String dirPath) throws InternalException {
        final FTPFile[] dirs;
        try {
            dirs = client.listFiles(dirPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        String name;
        String childPath;
        for (final FTPFile ftpFile : dirs) {
            name = ftpFile.getName();
            childPath = StringKit.format("{}/{}", dirPath, name);
            if (ftpFile.isDirectory()) {
                // Exclude parent and current directories
                if (!".".equals(name) && !"..".equals(name)) {
                    delDir(childPath);
                }
            } else {
                delFile(childPath);
            }
        }

        // Delete the empty directory
        try {
            return this.client.removeDirectory(dirPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Uploads a file to the specified directory. Options:
     *
     * <pre>
     * 1. If remotePath is null or "", upload to the current path.
     * 2. If remotePath is a relative path, upload to a sub-path of the current path.
     * 3. If remotePath is an absolute path, upload to that path.
     * </pre>
     *
     * @param remotePath The server path, which can be {@code null}, a relative path, or an absolute path.
     * @param file       The file to upload.
     * @return True if the upload is successful, false otherwise.
     */
    @Override
    public boolean uploadFile(final String remotePath, final File file) {
        Assert.notNull(file, "file to upload is null !");
        if (!FileKit.isFile(file)) {
            throw new InternalException("[{}] is not a file!", file);
        }
        return uploadFile(remotePath, file.getName(), file);
    }

    /**
     * Uploads a file to the specified directory. Options are the same as above.
     *
     * @param file       The file to upload.
     * @param remotePath The server path, which can be {@code null}, a relative path, or an absolute path.
     * @param fileName   A custom file name to save on the server.
     * @return True if the upload is successful, false otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public boolean uploadFile(final String remotePath, final String fileName, final File file)
            throws InternalException {
        try (final InputStream in = FileKit.getInputStream(file)) {
            return uploadFile(remotePath, fileName, in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Uploads a file to the specified directory. Options are the same as above.
     *
     * @param remotePath The server path, which can be {@code null}, a relative path, or an absolute path.
     * @param fileName   The file name.
     * @param fileStream The file stream.
     * @return True if the upload is successful, false otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public boolean uploadFile(final String remotePath, final String fileName, final InputStream fileStream)
            throws InternalException {
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (StringKit.isNotBlank(remotePath)) {
            mkDirs(remotePath);
            if (!cd(remotePath)) {
                throw new InternalException("Change dir to [{}] error, maybe dir not exist!", remotePath);
            }
        }

        try {
            return client.storeFile(fileName, fileStream);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (this.backToPwd) {
                cd(pwd);
            }
        }
    }

    /**
     * Recursively uploads files (including directories). When uploading, if uploadFile is a directory, only its
     * contents (subdirectories and files) are copied to the target path, not the directory itself. Parent directories
     * are created automatically during upload.
     *
     * @param remotePath The directory path.
     * @param uploadFile The file or directory to upload.
     */
    public void upload(final String remotePath, final File uploadFile) {
        if (!FileKit.isDirectory(uploadFile)) {
            this.uploadFile(remotePath, uploadFile);
            return;
        }

        final File[] files = uploadFile.listFiles();
        if (ArrayKit.isEmpty(files)) {
            return;
        }

        final List<File> dirs = new ArrayList<>(files.length);
        // First, process only files to avoid incorrect directory structures
        for (final File f : files) {
            if (f.isDirectory()) {
                dirs.add(f);
            } else {
                this.uploadFile(remotePath, f);
            }
        }
        // Second, process only directories
        for (final File f : dirs) {
            final String dir = FileKit.normalize(remotePath + "/" + f.getName());
            upload(dir, f);
        }
    }

    /**
     * Downloads a file.
     *
     * @param path    The file path, including the file name.
     * @param outFile The output file or directory. If a directory, the server's file name is used.
     */
    @Override
    public void download(final String path, final File outFile) {
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        download(dir, fileName, outFile);
    }

    /**
     * Recursively downloads files from an FTP server to a local directory, syncing the structure.
     *
     * @param sourceDir The source directory on the FTP server.
     * @param targetDir The target local directory.
     */
    @Override
    public void recursiveDownloadFolder(final String sourceDir, final File targetDir) {
        String fileName;
        String srcFile;
        File destFile;
        for (final FTPFile ftpFile : lsFiles(sourceDir, null)) {
            fileName = ftpFile.getName();
            srcFile = StringKit.format("{}/{}", sourceDir, fileName);
            destFile = FileKit.file(targetDir, fileName);

            if (!ftpFile.isDirectory()) {
                // Download if the local file doesn't exist or the FTP file has been modified
                if (!FileKit.exists(destFile) || (ftpFile.getTimestamp().getTimeInMillis() > destFile.lastModified())) {
                    download(srcFile, destFile);
                }
            } else {
                // If it's still a directory on the server, recurse
                FileKit.mkdir(destFile);
                recursiveDownloadFolder(srcFile, destFile);
            }
        }
    }

    /**
     * Downloads a file.
     *
     * @param path     The file's path (remote directory), excluding the file name.
     * @param fileName The file name.
     * @param outFile  The output file or directory. If a directory, the server's file name is used.
     * @return True if the download is successful, false otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public boolean download(final String path, final String fileName, File outFile) throws InternalException {
        if (outFile.isDirectory()) {
            outFile = new File(outFile, fileName);
        }
        if (!outFile.exists()) {
            FileKit.touch(outFile);
        }
        try (final OutputStream out = FileKit.getOutputStream(outFile)) {
            return download(path, fileName, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Downloads a file to an output stream.
     *
     * @param path     The file path.
     * @param fileName The file name.
     * @param out      The output location.
     * @return True if the download is successful, false otherwise.
     */
    public boolean download(final String path, final String fileName, final OutputStream out) {
        return download(path, fileName, out, null);
    }

    /**
     * Downloads a file to an output stream.
     *
     * @param path            The server's file path.
     * @param fileName        The server's file name.
     * @param out             The output stream to write the downloaded file to.
     * @param fileNameCharset The character set for the file name.
     * @return True if the download is successful, false otherwise.
     * @throws InternalException if an I/O error occurs.
     */
    public boolean download(
            final String path,
            String fileName,
            final OutputStream out,
            final java.nio.charset.Charset fileNameCharset) throws InternalException {
        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (!cd(path)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", path);
        }

        if (null != fileNameCharset) {
            fileName = new String(fileName.getBytes(fileNameCharset), org.miaixz.bus.core.lang.Charset.ISO_8859_1);
        }
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            return client.retrieveFile(fileName, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (backToPwd) {
                cd(pwd);
            }
        }
    }

    @Override
    public InputStream getFileStream(final String path) {
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        return getFileStream(dir, fileName);
    }

    /**
     * Reads a file as an input stream.
     *
     * @param dir      The server's file directory.
     * @param fileName The server's file name.
     * @return An {@link InputStream}.
     * @throws InternalException if an I/O error occurs.
     */
    public InputStream getFileStream(final String dir, final String fileName) throws InternalException {
        String pwd = null;
        if (isBackToPwd()) {
            pwd = pwd();
        }

        if (!cd(dir)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", dir);
        }
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            return client.retrieveFileStream(fileName);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isBackToPwd()) {
                cd(pwd);
            }
        }
    }

    /**
     * Gets the FTPClient object.
     *
     * @return The {@link FTPClient}.
     */
    public FTPClient getClient() {
        return this.client;
    }

    @Override
    public void close() throws IOException {
        if (null != this.client) {
            this.client.logout();
            if (this.client.isConnected()) {
                this.client.disconnect();
            }
            this.client = null;
        }
    }

}
