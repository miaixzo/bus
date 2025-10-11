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

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Unified interface for FTP (File Transfer Protocol) operations. This interface defines a set of common file transfer
 * and management operations that can be performed on a remote FTP server, abstracting away the specifics of different
 * FTP client implementations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Ftp extends Closeable {

    /**
     * The default character set used for FTP operations, typically UTF-8.
     */
    java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;

    /**
     * Retrieves the FTP configuration associated with this FTP client instance.
     *
     * @return The {@link FtpConfig} object containing connection and operational settings.
     */
    FtpConfig getConfig();

    /**
     * Reconnects to the FTP server if the current connection has timed out or become stale. Implementations should
     * handle the logic for checking connection validity and re-establishing it.
     *
     * @return This {@code Ftp} instance, allowing for method chaining.
     */
    Ftp reconnectIfTimeout();

    /**
     * Retrieves the current remote directory (working directory) on the FTP server.
     *
     * @return The absolute path of the current working directory as a {@link String}.
     */
    String pwd();

    /**
     * Changes the current working directory on the remote FTP server to the specified directory. The behavior for
     * invalid directories (e.g., non-existent) may vary between implementations.
     *
     * @param directory The path to the directory to change to.
     * @return {@code true} if the directory was successfully changed; {@code false} otherwise.
     */
    boolean cd(String directory);

    /**
     * Changes the current working directory to the parent directory. This is a convenience method that calls
     * {@link #cd(String)} with {@link Symbol#DOUBLE_DOT}.
     *
     * @return {@code true} if the parent directory was successfully changed; {@code false} otherwise.
     */
    default boolean toParent() {
        return cd(Symbol.DOUBLE_DOT);
    }

    /**
     * Checks if a file or directory exists at the specified path on the remote server. Special handling is provided for
     * empty paths, and paths ending with directory separators but not representing actual directories, or special
     * directory names like "." or "..".
     *
     * @param path The path to the file or directory to check.
     * @return {@code true} if the file or directory exists; {@code false} otherwise.
     */
    boolean exist(final String path);

    /**
     * Determines if the given path on the remote server refers to a directory. This method temporarily changes the
     * directory to the given path to verify if it's a directory, then reverts to the original working directory.
     *
     * @param dir The path to check.
     * @return {@code true} if the path is a directory; {@code false} otherwise.
     */
    default boolean isDir(final String dir) {
        final String workDir = pwd();
        try {
            return cd(dir);
        } finally {
            cd(workDir);
        }
    }

    /**
     * Renames a file or directory on the remote FTP server.
     *
     * @param oldPath The current path or name of the file/directory to rename.
     * @param newPath The new path or name for the file/directory.
     * @return {@code true} if the rename operation was successful; {@code false} otherwise.
     */
    boolean rename(String oldPath, String newPath);

    /**
     * Creates a new directory in the current remote working directory.
     *
     * @param dir The name of the directory to create.
     * @return {@code true} if the directory was created successfully; {@code false} otherwise.
     */
    boolean mkdir(String dir);

    /**
     * Creates the specified folder and any necessary parent directories on the remote server. This method ensures that
     * the full path exists. After creation, the working directory is reset to its default or initial state.
     *
     * @param dir The absolute path of the folder to create.
     */
    void mkDirs(final String dir);

    /**
     * Lists all file and directory names within a specified remote directory (non-recursive).
     *
     * @param path The path to the directory to list.
     * @return A {@link List} of {@link String}s, where each string is the name of a file or directory.
     */
    List<String> ls(String path);

    /**
     * Deletes a specified file on the remote FTP server.
     *
     * @param path The path to the file to delete.
     * @return {@code true} if the file was deleted successfully; {@code false} otherwise.
     */
    boolean delFile(String path);

    /**
     * Deletes a directory and all its contents (files and subdirectories) recursively on the remote FTP server.
     *
     * @param dirPath The path to the directory to delete.
     * @return {@code true} if the directory and its contents were deleted successfully; {@code false} otherwise.
     */
    boolean delDir(String dirPath);

    /**
     * Uploads a local file to the target server. The destination path on the server can be specified. If
     * {@code destPath} is a directory, the file will be uploaded with its original name into that directory. This
     * operation typically overwrites existing files with the same name.
     *
     * @param destPath The destination path on the server. Can be {@code null} (uploads to current working directory), a
     *                 relative path, or an absolute path. If it's a directory, the file's original name is used.
     * @param file     The local {@link File} object to upload.
     * @return {@code true} if the upload was successful; {@code false} otherwise.
     */
    boolean uploadFile(String destPath, File file);

    /**
     * Downloads a file from the remote FTP server to a specified local file or directory. If {@code outFile} is a
     * directory, the downloaded file will be saved into it with its original name.
     *
     * @param path    The path to the file on the remote FTP server.
     * @param outFile The local {@link File} or directory where the downloaded file should be saved.
     */
    void download(String path, File outFile);

    /**
     * Recursively downloads files and directories from the FTP server to the local machine, synchronizing the file
     * structures. Existing local files will be overwritten by newer files from the server.
     *
     * @param sourceDir The source directory on the FTP server to download from.
     * @param targetDir The target directory on the local machine where files will be saved.
     */
    void recursiveDownloadFolder(String sourceDir, File targetDir);

    /**
     * Reads a file from the FTP server and provides its content as an {@link InputStream}. This allows for streaming
     * processing of remote file content.
     *
     * @param path The path to the file on the FTP server.
     * @return An {@link InputStream} providing access to the file's content.
     */
    InputStream getFileStream(String path);

}
