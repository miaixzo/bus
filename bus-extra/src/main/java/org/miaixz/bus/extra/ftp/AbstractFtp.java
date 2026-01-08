/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
import java.util.List;

import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Abstract base class for FTP operations. This class provides common utility methods and a basic structure for FTP
 * client implementations, handling tasks like path existence checks, directory creation, and robust file downloading.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFtp implements Ftp {

    /**
     * The FTP configuration settings used by this client.
     */
    protected FtpConfig ftpConfig;

    /**
     * Constructs an {@code AbstractFtp} instance with the specified FTP configuration.
     *
     * @param config The {@link FtpConfig} containing connection and operational settings.
     */
    protected AbstractFtp(final FtpConfig config) {
        this.ftpConfig = config;
    }

    /**
     * Checks if a given list of names contains a specific name, ignoring case.
     *
     * @param names      The {@link List} of file or directory names to search within.
     * @param nameToFind The file or directory name to search for (case-insensitive).
     * @return {@code true} if the name is found (case-insensitive); {@code false} otherwise.
     */
    private static boolean containsIgnoreCase(final List<String> names, final String nameToFind) {
        if (StringKit.isEmpty(nameToFind)) {
            return false;
        }
        return CollKit.contains(names, nameToFind::equalsIgnoreCase);
    }

    /**
     * Retrieves the FTP configuration associated with this FTP client instance.
     *
     * @return The {@link FtpConfig} object containing connection and operational settings.
     */
    @Override
    public FtpConfig getConfig() {
        return this.ftpConfig;
    }

    /**
     * Checks if a file or directory exists at the specified path on the remote server. This method handles both
     * directory and file existence checks, including special cases for empty paths, paths ending with file separators,
     * and special directory names like "." and "..".
     *
     * @param path The path to the file or directory to check.
     * @return {@code true} if the file or directory exists; {@code false} otherwise.
     */
    @Override
    public boolean exist(final String path) {
        if (StringKit.isBlank(path)) {
            return false;
        }
        // Directory validation: if it's a directory and exists, return true.
        if (isDir(path)) {
            return true;
        }
        // If path ends with a file separator but is not a directory, it's invalid.
        if (CharKit.isFileSeparator(path.charAt(path.length() - 1))) {
            return false;
        }

        final String fileName = FileName.getName(path);
        // Exclude special directory names "." and ".."
        if (Symbol.DOT.equals(fileName) || Symbol.DOUBLE_DOT.equals(fileName)) {
            return false;
        }

        // File validation: check if the parent directory exists and then list its contents.
        final String dir = StringKit.defaultIfEmpty(StringKit.removeSuffix(path, fileName), Symbol.DOT);
        // Check if the parent directory is a directory and exists
        if (!isDir(dir)) {
            return false;
        }
        final List<String> names;
        try {
            names = ls(dir);
        } catch (final InternalException ignore) {
            // If listing fails, assume the path does not exist or is inaccessible.
            return false;
        }
        return containsIgnoreCase(names, fileName);
    }

    /**
     * Creates the specified folder and its parent directories on the remote server. This method navigates through the
     * path components, creating directories as needed. After creation, the working directory is restored to its
     * original state.
     *
     * @param dir The absolute path of the folder to create.
     */
    @Override
    public void mkDirs(final String dir) {
        final String[] dirs = StringKit.trim(dir).split("[\\\\/]+");

        final String now = pwd(); // Store current working directory
        if (dirs.length > 0 && StringKit.isEmpty(dirs[0])) {
            // If the first element is empty, it means the path starts with '/', so change to root.
            this.cd(Symbol.SLASH);
        }
        for (final String s : dirs) {
            if (StringKit.isNotEmpty(s)) {
                boolean exist = true;
                try {
                    // Attempt to change to the directory. If successful, it exists.
                    if (!cd(s)) {
                        exist = false;
                    }
                } catch (final InternalException e) {
                    // If changing directory throws an exception, it likely doesn't exist.
                    exist = false;
                }
                if (!exist) {
                    // Create directory if it does not exist
                    mkdir(s);
                    cd(s); // Change into the newly created directory
                }
            }
        }
        // Switch back to the original working directory
        cd(now);
    }

    /**
     * Downloads a file from the remote server to a local file, ensuring atomicity by using a temporary file. The
     * principle of this method is to first download the file to a temporary file in the same directory as the target
     * file. Once the download is complete, the temporary file is renamed to the final target name. This prevents
     * incomplete files from being left behind in case of download errors.
     *
     * @param path           The file path on the remote server.
     * @param outFile        The local {@link File} or directory where the downloaded file should be saved. If it's a
     *                       directory, the server's filename will be used.
     * @param tempFileSuffix The suffix for temporary files, e.g., ".temp". If blank, defaults to ".temp".
     * @throws InternalException if an error occurs during download or file operations (e.g., I/O errors, renaming
     *                           issues).
     */
    public void download(final String path, File outFile, String tempFileSuffix) {
        if (StringKit.isBlank(tempFileSuffix)) {
            tempFileSuffix = ".temp";
        } else {
            tempFileSuffix = StringKit.addPrefixIfNot(tempFileSuffix, Symbol.DOT);
        }

        // Determine the real name of the target file
        final String fileName = outFile.isDirectory() ? FileName.getName(path) : outFile.getName();
        // Construct the temporary filename
        final String tempFileName = fileName + tempFileSuffix;

        // Create the temporary file object (in the same directory as the final target)
        outFile = new File(outFile.isDirectory() ? outFile : outFile.getParentFile(), tempFileName);
        try {
            // Perform the actual download to the temporary file
            download(path, outFile);
            // Rename the downloaded temporary file to its final name
            FileKit.rename(outFile, fileName, true);
        } catch (final Throwable e) {
            // If any exception occurs, delete the temporary file to clean up
            FileKit.remove(outFile);
            throw new InternalException(e);
        }
    }

}
