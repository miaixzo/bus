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
package org.miaixz.bus.core.xyz;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.io.BomReader;
import org.miaixz.bus.core.io.file.*;
import org.miaixz.bus.core.io.file.FileReader;
import org.miaixz.bus.core.io.file.FileWriter;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.stream.BOMInputStream;
import org.miaixz.bus.core.io.stream.LineCounter;
import org.miaixz.bus.core.io.unit.DataSize;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * File utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileKit extends PathResolve {

    /**
     * Constructs a new FileKit. Utility class constructor for static access.
     */
    private FileKit() {
    }

    /**
     * Regex for detecting absolute paths.
     */
    private static final Pattern PATTERN_PATH_ABSOLUTE = Pattern.compile("^[a-zA-Z]:([/\\\\].*)?", Pattern.DOTALL);

    /**
     * Checks if the current operating system is Windows.
     *
     * @return `true` if it is Windows.
     */
    public static boolean isWindows() {
        return Symbol.C_BACKSLASH == File.separatorChar;
    }

    /**
     * Lists the files and directories under a specified path.
     *
     * @param path The absolute or relative path of the directory.
     * @return An array of files (including directories).
     */
    public static File[] ls(final String path) {
        if (path == null) {
            return null;
        }

        final File file = file(path);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        throw new InternalException(StringKit.format("Path [{}] is not directory!", path));
    }

    /**
     * Checks if a file or directory is empty. For a directory, it is empty if it contains no files or subdirectories.
     * For a file, it is empty if its length is 0.
     *
     * @param file The file or directory.
     * @return `true` if it is empty.
     */
    public static boolean isEmpty(final File file) {
        if (null == file || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            final String[] subFiles = file.list();
            return ArrayKit.isEmpty(subFiles);
        } else if (file.isFile()) {
            return file.length() <= 0;
        }

        return false;
    }

    /**
     * Checks if a file or directory is not empty.
     *
     * @param file The file or directory.
     * @return `true` if it is not empty.
     */
    public static boolean isNotEmpty(final File file) {
        return !isEmpty(file);
    }

    /**
     * Checks if a directory is empty.
     *
     * @param dir The directory.
     * @return `true` if the directory is empty.
     */
    public static boolean isDirEmpty(final File dir) {
        return isDirEmpty(dir.toPath());
    }

    /**
     * Checks if a file or directory is hidden.
     *
     * @param file The file or directory.
     * @return `true` if it is hidden.
     */
    public static boolean isHidden(final File file) {
        return isHidden(file.toPath());
    }

    /**
     * Checks if a file or directory is hidden, including temporary files created by applications like Office.
     *
     * @param path The path to the file or directory.
     * @return `true` if it is hidden.
     */
    public static boolean isHidden(Path path) {
        if (path == null) {
            return false;
        }
        try {
            String fileName = path.getFileName().toString();
            // Check for Unix/Linux style hidden files (starting with a dot)
            if (fileName.startsWith(Symbol.DOT)) {
                return true;
            }
            // Check for Office temporary files (starting with ~$)
            if (fileName.startsWith(Symbol.TILDE + Symbol.DOLLAR)) {
                return true;
            }
            // Check for temporary files (ending with .tmp)
            if (fileName.toLowerCase().endsWith(".tmp")) {
                return true;
            }
            // Check for Windows style hidden files (using file attribute)
            return Files.isHidden(path);
        } catch (Exception e) {
            // In case of error, conservatively assume it's not hidden
            return false;
        }
    }

    /**
     * Recursively traverses a directory and its subdirectories to find all files that match a filter.
     *
     * @param path       The path of the directory or file.
     * @param fileFilter The filter to apply (only affects files, not directories).
     * @return A list of matching files.
     */
    public static List<File> loopFiles(final String path, final FileFilter fileFilter) {
        return loopFiles(file(path), fileFilter);
    }

    /**
     * Recursively traverses a directory and its subdirectories to find all files that match a filter.
     *
     * @param file       The directory or file.
     * @param fileFilter The filter to apply.
     * @return A list of matching files.
     */
    public static List<File> loopFiles(final File file, final FileFilter fileFilter) {
        return loopFiles(file, -1, fileFilter);
    }

    /**
     * Recursively traverses a directory and its subdirectories to find all files that match a filter, up to a max
     * depth.
     *
     * @param file       The directory or file.
     * @param maxDepth   The maximum depth to traverse (-1 for infinite).
     * @param fileFilter The filter to apply.
     * @return A list of matching files.
     */
    public static List<File> loopFiles(final File file, final int maxDepth, final FileFilter fileFilter) {
        return loopFiles(file.toPath(), maxDepth, fileFilter);
    }

    /**
     * Recursively traverses a directory and its subdirectories.
     *
     * @param path   The path of the directory or file.
     * @param hidden If `true`, hidden files will not be included.
     * @return A list of files.
     */
    public static List<File> loopFiles(final String path, final boolean hidden) {
        FileFilter fileFilter = null;
        if (hidden) {
            fileFilter = f -> !isHidden(f);
        }
        return loopFiles(file(path), fileFilter);
    }

    /**
     * Recursively traverses a directory and its subdirectories.
     *
     * @param path The path of the directory or file.
     * @return A list of files.
     */
    public static List<File> loopFiles(final String path) {
        return loopFiles(path, false);
    }

    /**
     * Recursively traverses a directory and its subdirectories.
     *
     * @param file The directory or file.
     * @return A list of files.
     */
    public static List<File> loopFiles(final File file) {
        return loopFiles(file, null);
    }

    /**
     * Recursively traverses a directory and its subdirectories.
     *
     * @param file   The directory or file.
     * @param hidden If `true`, only hidden files will be included.
     * @return A list of files.
     */
    public static List<File> loopFiles(final File file, final boolean hidden) {
        FileFilter fileFilter = null;
        if (hidden) {
            fileFilter = f -> isHidden(f);
        }
        return loopFiles(file, fileFilter);
    }

    /**
     * Recursively walks a file tree and processes files that match a predicate.
     *
     * @param file      The file or directory.
     * @param predicate The predicate to test files and directories. Traversal continues into directories that test
     *                  true.
     */
    public static void walkFiles(final File file, final Predicate<File> predicate) {
        if (predicate.test(file) && file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isNotEmpty(subFiles)) {
                for (final File tmp : subFiles) {
                    walkFiles(tmp, predicate);
                }
            }
        }
    }

    /**
     * Gets the names of all files in a specified directory (non-recursive).
     *
     * @param path The path of the directory.
     * @return A list of file names.
     * @throws InternalException for IO errors.
     */
    public static List<String> listFileNames(String path) throws InternalException {
        if (path == null) {
            return new ArrayList<>(0);
        }
        path = getAbsolutePath(path);
        int index = path.lastIndexOf(FileType.JAR_PATH_EXT);
        if (index < 0) {
            // Normal directory
            return Arrays.stream(ls(path)).filter(File::isFile).map(File::getName).toList();
        }

        // Path inside a JAR file
        index = index + FileType.JAR.length();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(path.substring(0, index));
            return ZipKit.listFileNames(jarFile, StringKit.removePrefix(path.substring(index + 1), "/"));
        } catch (final IOException e) {
            throw new InternalException(StringKit.format("Can not read file path of [{}]", path), e);
        } finally {
            IoKit.closeQuietly(jarFile);
        }
    }

    /**
     * Creates a new `File` object.
     *
     * @param path The file path.
     * @return A `File` object.
     */
    public static File newFile(final String path) {
        return new File(path);
    }

    /**
     * Creates a `File` object, automatically resolving relative paths from the classpath.
     *
     * @param path The path (can be absolute or relative to the classpath).
     * @return A `File` object.
     */
    public static File file(final String path) {
        if (null == path) {
            return null;
        }
        if (path.startsWith(Normal.PROJECT_URL_PREFIX)) {
            return new File(StringKit.subSuf(path, Normal.PROJECT_URL_PREFIX.length()));
        }
        return new File(getAbsolutePath(path));
    }

    /**
     * Creates a `File` object, checking for Zip Slip vulnerability.
     *
     * @param parent The parent directory.
     * @param path   The file path.
     * @return A `File` object.
     */
    public static File file(final String parent, final String path) {
        return file(new File(parent), path);
    }

    /**
     * Creates a `File` object, checking for Zip Slip vulnerability.
     *
     * @param parent The parent `File` object.
     * @param path   The file path.
     * @return A `File` object.
     */
    public static File file(final File parent, final String path) {
        if (StringKit.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return checkSlip(parent, buildFile(parent, path));
    }

    /**
     * Creates a `File` object from a parent directory and a sequence of path names.
     *
     * @param directory The parent directory.
     * @param names     The sequence of path names.
     * @return The resulting `File` object.
     */
    public static File file(final File directory, final String... names) {
        Assert.notNull(directory, "directory must not be null");
        if (ArrayKit.isEmpty(names)) {
            return directory;
        }

        File file = directory;
        for (final String name : names) {
            if (null != name) {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * Creates a `File` object from a sequence of path names.
     *
     * @param names The sequence of path names.
     * @return The resulting `File` object.
     */
    public static File file(final String... names) {
        if (ArrayKit.isEmpty(names)) {
            return null;
        }

        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = file(name);
            } else {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * Creates a `File` object from a `URI`.
     *
     * @param uri The file URI.
     * @return A `File` object.
     */
    public static File file(final URI uri) {
        if (uri == null) {
            throw new NullPointerException("File uri is null!");
        }
        return new File(uri);
    }

    /**
     * Creates a `File` object from a `URL`.
     *
     * @param url The file URL.
     * @return A `File` object.
     */
    public static File file(final URL url) {
        return new File(UrlKit.toURI(url));
    }

    /**
     * Gets the system's temporary file directory.
     *
     * @return The temporary directory.
     */
    public static File getTmpDir() {
        return file(Keys.getTmpDirPath());
    }

    /**
     * Gets the user's home directory.
     *
     * @return The user home directory.
     */
    public static File getUserHomeDir() {
        return file(Keys.getUserHomePath());
    }

    /**
     * Checks if a file exists.
     *
     * @param path The file path.
     * @return `true` if the file exists.
     */
    public static boolean exists(final String path) {
        return (null != path) && file(path).exists();
    }

    /**
     * Checks if a file exists.
     *
     * @param file The file.
     * @return `true` if the file exists.
     */
    public static boolean exists(final File file) {
        return (null != file) && file.exists();
    }

    /**
     * Checks if a directory contains any files matching a regex.
     *
     * @param directory The directory path.
     * @param regexp    The regular expression for the file name.
     * @return `true` if a matching file exists.
     */
    public static boolean exists(final String directory, final String regexp) {
        final File file = new File(directory);
        if (!file.exists()) {
            return false;
        }

        final String[] fileList = file.list();
        if (fileList == null) {
            return false;
        }

        for (final String fileName : fileList) {
            if (fileName.matches(regexp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the last modified time of a file.
     *
     * @param file The file.
     * @return The last modified time as a `Date`.
     */
    public static Date lastModifiedTime(final File file) {
        if (!exists(file)) {
            return null;
        }
        return new Date(file.lastModified());
    }

    /**
     * Gets the last modified time of a file at a given path.
     *
     * @param path The absolute path to the file.
     * @return The last modified time as a `Date`.
     */
    public static Date lastModifiedTime(final String path) {
        return lastModifiedTime(new File(path));
    }

    /**
     * Calculates the total size of a file or directory.
     *
     * @param file The file or directory.
     * @return The total size in bytes.
     */
    public static long size(final File file) {
        return size(file, false);
    }

    /**
     * Calculates the total size of a file or directory.
     *
     * @param file           The file or directory.
     * @param includeDirSize If `true`, includes the size of the directory entries themselves.
     * @return The total size in bytes.
     */
    public static long size(final File file, final boolean includeDirSize) {
        if (null == file || !file.exists() || isSymlink(file)) {
            return 0;
        }

        if (file.isDirectory()) {
            long size = includeDirSize ? file.length() : 0;
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isEmpty(subFiles)) {
                return 0L;
            }
            for (final File subFile : subFiles) {
                size += size(subFile, includeDirSize);
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * Calculates the total number of lines in a file.
     *
     * @param file The file.
     * @return The total number of lines.
     */
    public static int getTotalLines(final File file) {
        return getTotalLines(file, -1);
    }

    /**
     * Calculates the total number of lines in a file.
     *
     * @param file       The file.
     * @param bufferSize The buffer size to use for reading.
     * @return The total number of lines.
     */
    public static int getTotalLines(final File file, int bufferSize) {
        return getTotalLines(file, bufferSize, true);
    }

    /**
     * Calculates the total number of lines in a file.
     *
     * @param file                       The file.
     * @param bufferSize                 The buffer size to use for reading.
     * @param lastLineSeparatorAsNewLine If `true`, a final line separator is counted as a new line.
     * @return The total number of lines.
     */
    public static int getTotalLines(final File file, final int bufferSize, final boolean lastLineSeparatorAsNewLine) {
        Assert.isTrue(isFile(file), () -> new InternalException("Input must be a File"));
        try (final LineCounter lineCounter = new LineCounter(getInputStream(file), bufferSize)) {
            lineCounter.setLastLineSeparatorAsNewLine(lastLineSeparatorAsNewLine);
            return lineCounter.getCount();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if a file's last modified time is newer than a reference file's.
     *
     * @param file      The file.
     * @param reference The reference file.
     * @return `true` if the file is newer.
     */
    public static boolean newerThan(final File file, final File reference) {
        if (null == reference || !reference.exists()) {
            return true;
        }
        return newerThan(file, reference.lastModified());
    }

    /**
     * Checks if a file's last modified time is newer than a given timestamp.
     *
     * @param file       The file.
     * @param timeMillis The timestamp to compare against.
     * @return `true` if the file is newer.
     */
    public static boolean newerThan(final File file, final long timeMillis) {
        if (null == file || !file.exists()) {
            return false;
        }
        return file.lastModified() > timeMillis;
    }

    /**
     * Creates a file and its parent directories if they do not exist. If the file already exists, it is returned
     * directly.
     *
     * @param path The path to the file.
     * @return The `File` object.
     * @throws InternalException for IO errors.
     */
    public static File touch(final String path) throws InternalException {
        if (path == null) {
            return null;
        }
        return touch(file(path));
    }

    /**
     * Creates a file and its parent directories if they do not exist. If the file already exists, it is returned
     * directly.
     *
     * @param file The `File` object.
     * @return The `File` object.
     * @throws InternalException for IO errors.
     */
    public static File touch(final File file) throws InternalException {
        if (null == file) {
            return null;
        }
        if (!file.exists()) {
            mkParentDirs(file);
            try {
                file.createNewFile();
            } catch (final Exception e) {
                throw new InternalException(e);
            }
        }
        return file;
    }

    /**
     * Creates a file and its parent directories.
     *
     * @param parent The parent `File` object.
     * @param path   The path relative to the parent.
     * @return The `File` object.
     * @throws InternalException for IO errors.
     */
    public static File touch(final File parent, final String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * Creates a file and its parent directories.
     *
     * @param parent The parent directory path.
     * @param path   The path relative to the parent.
     * @return The `File` object.
     * @throws InternalException for IO errors.
     */
    public static File touch(final String parent, final String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * Creates the parent directories for a given file or directory.
     *
     * @param file The file or directory.
     * @return The parent directory.
     */
    public static File mkParentDirs(final File file) {
        if (null == file) {
            return null;
        }
        return mkdir(getParent(file, 1));
    }

    /**
     * Creates the parent directories for a given path.
     *
     * @param path The path.
     * @return The created directory.
     */
    public static File mkParentDirs(final String path) {
        if (path == null) {
            return null;
        }
        return mkParentDirs(file(path));
    }

    /**
     * Deletes a file or directory recursively.
     *
     * @param fullFileOrDirPath The path to the file or directory.
     * @throws InternalException for IO errors.
     */
    public static void remove(final String fullFileOrDirPath) throws InternalException {
        remove(file(fullFileOrDirPath));
    }

    /**
     * Deletes a file or directory recursively.
     *
     * @param file The file or directory.
     * @throws InternalException for IO errors.
     * @see Files#delete(Path)
     */
    public static void remove(final File file) throws InternalException {
        Assert.notNull(file, "File must be not null!");
        remove(file.toPath());
    }

    /**
     * Cleans the contents of a directory (deletes all files and subdirectories).
     *
     * @param dirPath The path to the directory.
     * @throws InternalException for IO errors.
     */
    public static void clean(final String dirPath) throws InternalException {
        clean(file(dirPath));
    }

    /**
     * Cleans the contents of a directory.
     *
     * @param directory The directory.
     * @throws InternalException for IO errors.
     */
    public static void clean(final File directory) throws InternalException {
        Assert.notNull(directory, "File must be not null!");
        clean(directory.toPath());
    }

    /**
     * Recursively deletes empty directories.
     *
     * @param directory The directory.
     * @return `true` if successful.
     */
    public static boolean cleanEmpty(final File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return true;
        }

        final File[] files = directory.listFiles();
        if (ArrayKit.isEmpty(files)) {
            return directory.delete();
        }

        for (final File childFile : files) {
            cleanEmpty(childFile);
        }

        final String[] fileNames = directory.list();
        if (ArrayKit.isEmpty(fileNames)) {
            return directory.delete();
        }
        return true;
    }

    /**
     * Creates a directory, including any necessary but nonexistent parent directories.
     *
     * @param dirPath The path to the directory.
     * @return The created directory.
     */
    public static File mkdir(final String dirPath) {
        if (dirPath == null) {
            return null;
        }
        final File dir = file(dirPath);
        return mkdir(dir);
    }

    /**
     * Creates a directory, including any necessary but nonexistent parent directories.
     *
     * @param dir The directory.
     * @return The created directory.
     */
    public static File mkdir(final File dir) {
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            mkdirsSafely(dir, 5, 1);
        }
        return dir;
    }

    /**
     * Safely creates a directory and its parents, handling concurrent creation attempts.
     *
     * @param dir         The directory to create.
     * @param tryCount    The maximum number of retry attempts.
     * @param sleepMillis The sleep duration between retries.
     * @return `true` if the directory was created successfully.
     */
    public static boolean mkdirsSafely(final File dir, final int tryCount, final long sleepMillis) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            return true;
        }
        for (int i = 1; i <= tryCount; i++) {
            dir.mkdirs();
            if (dir.exists()) {
                return true;
            }
            ThreadKit.sleep(sleepMillis);
        }
        return dir.exists();
    }

    /**
     * Creates a temporary file in the specified directory.
     *
     * @param dir The directory for the temporary file.
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile(final File dir) throws InternalException {
        return createTempFile("x", null, dir, true);
    }

    /**
     * Creates a temporary file in the default temporary-file directory.
     *
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile() throws InternalException {
        return createTempFile("x", null, null, true);
    }

    /**
     * Creates a temporary file with a given suffix in the default temporary-file directory.
     *
     * @param suffix    The suffix string. If `null`, ".tmp" is used.
     * @param isReCreat If `true`, deletes the file if it already exists and creates a new one.
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile(final String suffix, final boolean isReCreat) throws InternalException {
        return createTempFile("x", suffix, null, isReCreat);
    }

    /**
     * Creates a temporary file with a given prefix and suffix in the default temporary-file directory.
     *
     * @param prefix    The prefix string.
     * @param suffix    The suffix string.
     * @param isReCreat If `true`, deletes the file if it already exists.
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile(final String prefix, final String suffix, final boolean isReCreat)
            throws InternalException {
        return createTempFile(prefix, suffix, null, isReCreat);
    }

    /**
     * Creates a temporary file in the specified directory.
     *
     * @param dir       The directory for the temporary file.
     * @param isReCreat If `true`, deletes the file if it already exists.
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile(final File dir, final boolean isReCreat) throws InternalException {
        return createTempFile("x", null, dir, isReCreat);
    }

    /**
     * Creates a temporary file.
     *
     * @param prefix    The prefix string.
     * @param suffix    The suffix string.
     * @param dir       The directory for the temporary file.
     * @param isReCreat If `true`, deletes the file if it already exists.
     * @return The temporary file.
     * @throws InternalException for IO errors.
     */
    public static File createTempFile(final String prefix, final String suffix, final File dir, final boolean isReCreat)
            throws InternalException {
        try {
            final File file = PathResolve.createTempFile(prefix, suffix, null == dir ? null : dir.toPath()).toFile()
                    .getCanonicalFile();
            if (isReCreat) {
                if (!file.delete()) {
                    throw new InternalException("Failed to delete temporary file: " + file.getAbsolutePath());
                }
                if (file.exists()) {
                    throw new InternalException("File still exists after deletion: " + file.getAbsolutePath());
                }
                if (!file.createNewFile()) {
                    throw new InternalException("Failed to recreate temporary file: " + file.getAbsolutePath());
                }
                if (!file.exists() || !file.isFile() || !file.canWrite()) {
                    file.delete();
                    throw new InternalException("Created file is not valid: " + file.getAbsolutePath());
                }
            }
            return file;
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Copies a resource to a target file.
     *
     * @param src        The source resource.
     * @param target     The target file or directory.
     * @param isOverride If `true`, overwrites the target file if it exists.
     * @return The target file.
     * @throws InternalException for IO errors.
     */
    public static File copy(final Resource src, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copy(
                src,
                target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * Copies an `InputStream` to a file.
     *
     * @param src     The source `InputStream`.
     * @param target  The target file.
     * @param options The copy options.
     * @return The target file.
     * @throws InternalException for IO errors.
     */
    public static File copy(final InputStream src, final File target, final StandardCopyOption... options)
            throws InternalException {
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(target, "Target File or directory is null !");
        return copy(src, target.toPath(), options).toFile();
    }

    /**
     * Copies the contents of a file to an `OutputStream`.
     *
     * @param src The source file.
     * @param out The `OutputStream`.
     * @return The number of bytes copied.
     * @throws InternalException for IO errors.
     */
    public static long copy(final File src, final OutputStream out) throws InternalException {
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(out, "Target stream is null !");
        return copy(src.toPath(), out);
    }

    /**
     * Copies a file or directory.
     *
     * @param srcPath    The source path.
     * @param targetPath The target path.
     * @param isOverride If `true`, overwrites the target if it exists.
     * @return The target file or directory.
     * @throws InternalException for IO errors.
     */
    public static File copy(final String srcPath, final String targetPath, final boolean isOverride)
            throws InternalException {
        return copy(file(srcPath), file(targetPath), isOverride);
    }

    /**
     * Copies a file or directory.
     *
     * @param src        The source file or directory.
     * @param target     The target file or directory.
     * @param isOverride If `true`, overwrites the target if it exists.
     * @return The target file or directory.
     * @throws InternalException for IO errors.
     */
    public static File copy(final File src, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copy(
                src.toPath(),
                target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * Copies the contents of a source directory to a target directory.
     *
     * @param src        The source directory.
     * @param target     The target directory.
     * @param isOverride If `true`, overwrites files in the target.
     * @return The target directory.
     * @throws InternalException for IO errors.
     */
    public static File copyContent(final File src, final File target, final boolean isOverride)
            throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copyContent(
                src.toPath(),
                target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * Moves a file or directory.
     *
     * @param file       The source file or directory.
     * @param target     The target path.
     * @param isOverride If `true`, overwrites the target if it exists.
     * @return The target file or directory.
     * @throws InternalException for IO errors.
     * @see PathResolve#move(Path, Path, boolean)
     */
    public static File move(final File file, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(file, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return move(file.toPath(), target.toPath(), isOverride).toFile();
    }

    /**
     * Renames a file or directory without changing its path.
     *
     * @param file       The file to rename.
     * @param newName    The new name (the original extension is not retained).
     * @param isOverride If `true`, overwrites the target if it exists.
     * @return The renamed file.
     */
    public static File rename(final File file, final String newName, final boolean isOverride) {
        return rename(file, newName, false, isOverride);
    }

    /**
     * Renames a file or directory.
     *
     * @param file        The file to rename.
     * @param newName     The new name.
     * @param isRetainExt If `true`, retains the original file extension.
     * @param isOverride  If `true`, overwrites the target if it exists.
     * @return The renamed file.
     */
    public static File rename(final File file, String newName, final boolean isRetainExt, final boolean isOverride) {
        if (isRetainExt) {
            final String extName = FileName.extName(file);
            if (StringKit.isNotBlank(extName)) {
                newName = newName.concat(".").concat(extName);
            }
        }
        return rename(file.toPath(), newName, isOverride).toFile();
    }

    /**
     * Gets the canonical path of a file.
     *
     * @param file The file.
     * @return The canonical path.
     */
    public static String getCanonicalPath(final File file) {
        if (null == file) {
            return null;
        }
        try {
            return file.getCanonicalPath();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the absolute path of a resource relative to a base class.
     *
     * @param path      The relative path.
     * @param baseClass The base class for resolving the path.
     * @return The absolute path.
     */
    public static String getAbsolutePath(final String path, final Class<?> baseClass) {
        final String normalPath;
        if (path == null) {
            normalPath = Normal.EMPTY;
        } else {
            normalPath = FileName.normalize(path);
            if (isAbsolutePath(normalPath)) {
                return normalPath;
            }
        }

        final URL url = ResourceKit.getResourceUrl(normalPath, baseClass);
        if (null != url) {
            return FileName.normalize(UrlKit.getDecodedPath(url));
        }

        final String classPath = ClassKit.getClassPath();
        if (null == classPath) {
            return normalPath;
        }

        return FileName.normalize(classPath.concat(Objects.requireNonNull(normalPath)));
    }

    /**
     * Gets the absolute path of a resource relative to the classpath.
     *
     * @param path The relative path.
     * @return The absolute path.
     */
    public static String getAbsolutePath(final String path) {
        return getAbsolutePath(path, null);
    }

    /**
     * Gets the absolute path of a file.
     *
     * @param file The file.
     * @return The absolute path.
     */
    public static String getAbsolutePath(final File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (final IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Checks if a path is an absolute path.
     *
     * @param path The path to check.
     * @return `true` if the path is absolute.
     */
    public static boolean isAbsolutePath(final String path) {
        if (StringKit.isEmpty(path)) {
            return false;
        }

        return Symbol.C_SLASH == path.charAt(0) || PatternKit.isMatch(PATTERN_PATH_ABSOLUTE, path)
                || path.startsWith(Symbol.BACKSLASH + Symbol.BACKSLASH);
    }

    /**
     * Checks if a path represents a directory.
     *
     * @param path The file path.
     * @return `true` if it is a directory.
     */
    public static boolean isDirectory(final String path) {
        return (null != path) && file(path).isDirectory();
    }

    /**
     * Checks if a `File` object is a directory.
     *
     * @param file The file.
     * @return `true` if it is a directory.
     */
    public static boolean isDirectory(final File file) {
        return (null != file) && file.isDirectory();
    }

    /**
     * Checks if a path represents a file.
     *
     * @param path The file path.
     * @return `true` if it is a file.
     */
    public static boolean isFile(final String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * Checks if a `File` object is a file.
     *
     * @param file The file.
     * @return `true` if it is a file.
     */
    public static boolean isFile(final File file) {
        return (null != file) && file.isFile();
    }

    /**
     * Checks if two `File` objects represent the same file.
     *
     * @param file1 The first file.
     * @param file2 The second file.
     * @return `true` if they are the same.
     */
    public static boolean equals(final File file1, final File file2) {
        if (null == file1 || null == file2) {
            return null == file1 && null == file2;
        }

        final boolean exists1 = file1.exists();
        final boolean exists2 = file2.exists();

        if (exists1 && exists2) {
            return PathResolve.isSameFile(file1.toPath(), file2.toPath());
        }

        if (!exists1 && !exists2) {
            return pathEquals(file1, file2);
        }

        return false;
    }

    /**
     * Compares the contents of two files to determine if they are equal.
     *
     * @param file1 The first file.
     * @param file2 The second file.
     * @return `true` if the contents are equal.
     * @throws InternalException for IO errors.
     */
    public static boolean contentEquals(final File file1, final File file2) throws InternalException {
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }
        if (!file1Exists) {
            return true;
        }
        if (file1.isDirectory() || file2.isDirectory()) {
            throw new InternalException("Can't compare directories, only files");
        }
        if (file1.length() != file2.length()) {
            return false;
        }
        if (equals(file1, file2)) {
            return true;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = getInputStream(file1);
            input2 = getInputStream(file2);
            return IoKit.contentEquals(input1, input2);

        } finally {
            IoKit.closeQuietly(input1);
            IoKit.closeQuietly(input2);
        }
    }

    /**
     * Compares the contents of two files line by line, ignoring EOL differences.
     *
     * @param file1   The first file.
     * @param file2   The second file.
     * @param charset The character encoding.
     * @return `true` if the contents are equal.
     * @throws InternalException for IO errors.
     */
    public static boolean contentEqualsIgnoreEOL(
            final File file1,
            final File file2,
            final java.nio.charset.Charset charset) throws InternalException {
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }
        if (!file1Exists) {
            return true;
        }
        if (file1.isDirectory() || file2.isDirectory()) {
            throw new InternalException("Can't compare directories, only files");
        }
        if (equals(file1, file2)) {
            return true;
        }

        Reader input1 = null;
        Reader input2 = null;
        try {
            input1 = getReader(file1, charset);
            input2 = getReader(file2, charset);
            return IoKit.contentEqualsIgnoreEOL(input1, input2);
        } finally {
            IoKit.closeQuietly(input1);
            IoKit.closeQuietly(input2);
        }
    }

    /**
     * Checks if two file paths are equal (case-insensitive on Windows).
     *
     * @param file1 The first file.
     * @param file2 The second file.
     * @return `true` if the paths are equal.
     */
    public static boolean pathEquals(final File file1, final File file2) {
        if (isWindows()) {
            try {
                if (StringKit.equalsIgnoreCase(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (final Exception e) {
                if (StringKit.equalsIgnoreCase(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        } else {
            try {
                if (StringKit.equals(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (final Exception e) {
                if (StringKit.equals(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the index of the last path separator in a file path.
     *
     * @param filePath The file path.
     * @return The index of the last separator.
     */
    public static int lastIndexOfSeparator(final String filePath) {
        if (StringKit.isNotEmpty(filePath)) {
            int i = filePath.length();
            char c;
            while (--i >= 0) {
                c = filePath.charAt(i);
                if (CharKit.isFileSeparator(c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Checks if a file has been modified since a given time.
     *
     * @param file           The file.
     * @param lastModifyTime The last modification timestamp.
     * @return `true` if the file has been modified.
     */
    public static boolean isModified(final File file, final long lastModifyTime) {
        if (null == file || !file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * Normalizes a file path.
     *
     * @param path The original path.
     * @return The normalized path.
     */
    public static String normalize(final String path) {
        return FileName.normalize(path);
    }

    /**
     * Gets the relative path of a file with respect to a root directory.
     *
     * @param rootDir The absolute root directory.
     * @param file    The file.
     * @return The relative sub-path.
     */
    public static String subPath(final String rootDir, final File file) {
        try {
            return subPath(rootDir, file.getCanonicalPath());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the relative path of a file path with respect to a directory path (case-insensitive).
     *
     * @param dirPath  The parent directory path.
     * @param filePath The file path.
     * @return The relative sub-path.
     */
    public static String subPath(String dirPath, String filePath) {
        if (StringKit.isNotEmpty(dirPath) && StringKit.isNotEmpty(filePath)) {
            dirPath = StringKit.removeSuffix(FileName.normalize(dirPath), "/");
            filePath = FileName.normalize(filePath);
            final String result = StringKit.removePrefixIgnoreCase(filePath, dirPath);
            return StringKit.removePrefix(result, "/");
        }
        return filePath;
    }

    /**
     * Checks if a file path ends with a specific suffix (case-insensitive).
     *
     * @param file   The file.
     * @param suffix The suffix.
     * @return `true` if the path ends with the suffix.
     */
    public static boolean pathEndsWith(final File file, final String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }

    /**
     * Gets the file type based on its magic number (header information).
     *
     * @param file The file.
     * @return The file type (extension), or `null` if not found.
     * @throws InternalException for IO errors.
     * @see FileType#getType(File)
     */
    public static String getType(final File file) throws InternalException {
        return FileType.getType(file);
    }

    /**
     * Gets a `BufferedInputStream` for a file.
     *
     * @param file The file.
     * @return A `BufferedInputStream`.
     * @throws InternalException if the file is not found.
     * @see IoKit#toStream(File)
     */
    public static BufferedInputStream getInputStream(final File file) throws InternalException {
        return IoKit.toBuffered(IoKit.toStream(file));
    }

    /**
     * Gets a `BufferedInputStream` for a file path.
     *
     * @param path The file path.
     * @return A `BufferedInputStream`.
     * @throws InternalException if the file is not found.
     */
    public static BufferedInputStream getInputStream(final String path) throws InternalException {
        return getInputStream(file(path));
    }

    /**
     * Gets a `BOMInputStream` to handle files with a Byte Order Mark.
     *
     * @param file The file.
     * @return A `BOMInputStream`.
     * @throws InternalException if the file is not found.
     */
    public static BOMInputStream getBOMInputStream(final File file) throws InternalException {
        try {
            return new BOMInputStream(Files.newInputStream(file.toPath()));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets a `BomReader` to handle files with a Byte Order Mark.
     *
     * @param file The file.
     * @return A `BomReader`.
     */
    public static BomReader getBOMReader(final File file) {
        return IoKit.toBomReader(getBOMInputStream(file));
    }

    /**
     * Gets a `BufferedReader` for a file with UTF-8 encoding.
     *
     * @param file The file.
     * @return A `BufferedReader`.
     * @throws InternalException for IO errors.
     */
    public static BufferedReader getUtf8Reader(final File file) throws InternalException {
        return getReader(file, Charset.UTF_8);
    }

    /**
     * Gets a `BufferedReader` for a file path with UTF-8 encoding.
     *
     * @param path The file path.
     * @return A `BufferedReader`.
     * @throws InternalException for IO errors.
     */
    public static BufferedReader getUtf8Reader(final String path) throws InternalException {
        return getReader(path, Charset.UTF_8);
    }

    /**
     * Gets a `BufferedReader` for a file with a specified charset.
     *
     * @param file    The file.
     * @param charset The character set.
     * @return A `BufferedReader`.
     * @throws InternalException for IO errors.
     */
    public static BufferedReader getReader(final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return IoKit.toReader(getInputStream(file), charset);
    }

    /**
     * Gets a `BufferedReader` for a file path with a specified charset.
     *
     * @param path    The file path.
     * @param charset The character set.
     * @return A `BufferedReader`.
     * @throws InternalException for IO errors.
     */
    public static BufferedReader getReader(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return getReader(file(path), charset);
    }

    /**
     * Reads all bytes from a file.
     *
     * @param file The file.
     * @return A byte array of the file's contents.
     * @throws InternalException for IO errors.
     */
    public static byte[] readBytes(final File file) throws InternalException {
        if (null == file) {
            return null;
        }
        return readBytes(file.toPath());
    }

    /**
     * Reads all bytes from a file path.
     *
     * @param filePath The file path.
     * @return A byte array of the file's contents.
     * @throws InternalException for IO errors.
     */
    public static byte[] readBytes(final String filePath) throws InternalException {
        return readBytes(file(filePath));
    }

    /**
     * Reads the entire contents of a file into a string using UTF-8 encoding.
     *
     * @param file The file.
     * @return The file's contents as a string.
     * @throws InternalException for IO errors.
     */
    public static String readUtf8String(final File file) throws InternalException {
        return readString(file, Charset.UTF_8);
    }

    /**
     * Reads the entire contents of a file path into a string using UTF-8 encoding.
     *
     * @param path The file path.
     * @return The file's contents as a string.
     * @throws InternalException for IO errors.
     */
    public static String readUtf8String(final String path) throws InternalException {
        return readString(path, Charset.UTF_8);
    }

    /**
     * Reads the entire contents of a file into a string using a specified charset.
     *
     * @param file    The file.
     * @param charset The character set.
     * @return The file's contents as a string.
     * @throws InternalException for IO errors.
     */
    public static String readString(final File file, final java.nio.charset.Charset charset) throws InternalException {
        return FileReader.of(file, charset).readString();
    }

    /**
     * Reads the entire contents of a file path into a string using a specified charset.
     *
     * @param path    The file path.
     * @param charset The character set.
     * @return The file's contents as a string.
     * @throws InternalException for IO errors.
     */
    public static String readString(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return readString(file(path), charset);
    }

    /**
     * Reads the contents of a URL into a string.
     *
     * @param url     The URL.
     * @param charset The character set.
     * @return The contents as a string.
     * @throws InternalException for IO errors.
     */
    public static String readString(final URL url, final java.nio.charset.Charset charset) throws InternalException {
        Assert.notNull(url, "Empty url provided!");
        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.read(in, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Reads all lines from a file into a collection using UTF-8 encoding.
     *
     * @param <T>        The type of the collection.
     * @param path       The file path.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readUtf8Lines(final String path, final T collection)
            throws InternalException {
        return readLines(path, Charset.UTF_8, collection);
    }

    /**
     * Reads all lines from a file into a collection.
     *
     * @param <T>        The type of the collection.
     * @param path       The file path.
     * @param charset    The character set.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readLines(
            final String path,
            final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        return readLines(file(path), charset, collection);
    }

    /**
     * Reads all lines from a file into a collection using UTF-8 encoding.
     *
     * @param <T>        The type of the collection.
     * @param file       The file.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readUtf8Lines(final File file, final T collection)
            throws InternalException {
        return readLines(file, Charset.UTF_8, collection);
    }

    /**
     * Reads all lines from a file into a collection.
     *
     * @param <T>        The type of the collection.
     * @param file       The file.
     * @param charset    The character set.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readLines(
            final File file,
            final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        return FileReader.of(file, charset).readLines(collection);
    }

    /**
     * Reads all lines from a URL into a collection using UTF-8 encoding.
     *
     * @param <T>        The type of the collection.
     * @param url        The URL.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readUtf8Lines(final URL url, final T collection)
            throws InternalException {
        return readLines(url, Charset.UTF_8, collection);
    }

    /**
     * Reads all lines from a URL into a collection.
     *
     * @param <T>        The type of the collection.
     * @param url        The URL.
     * @param charset    The character set.
     * @param collection The collection to populate.
     * @return The populated collection.
     * @throws InternalException for IO errors.
     */
    public static <T extends Collection<String>> T readLines(
            final URL url,
            final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.readLines(in, charset, collection);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Reads all lines from a URL as a `List` using UTF-8 encoding.
     *
     * @param url The URL.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readUtf8Lines(final URL url) throws InternalException {
        return readLines(url, Charset.UTF_8);
    }

    /**
     * Reads all lines from a URL as a `List`.
     *
     * @param url     The URL.
     * @param charset The character set.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readLines(final URL url, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(url, charset, new ArrayList<>());
    }

    /**
     * Reads all lines from a file path as a `List` using UTF-8 encoding.
     *
     * @param path The file path.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readUtf8Lines(final String path) throws InternalException {
        return readLines(path, Charset.UTF_8);
    }

    /**
     * Reads all lines from a file path as a `List`.
     *
     * @param path    The file path.
     * @param charset The character set.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readLines(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(path, charset, new ArrayList<>());
    }

    /**
     * Reads all lines from a file as a `List` using UTF-8 encoding.
     *
     * @param file The file.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readUtf8Lines(final File file) throws InternalException {
        return readLines(file, Charset.UTF_8);
    }

    /**
     * Reads all lines from a file as a `List`.
     *
     * @param file    The file.
     * @param charset The character set.
     * @return A `List` of lines.
     * @throws InternalException for IO errors.
     */
    public static List<String> readLines(final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(file, charset, new ArrayList<>());
    }

    /**
     * Processes each line of a file using a `ConsumerX` with UTF-8 encoding.
     *
     * @param file        The file.
     * @param lineHandler The line handler.
     * @throws InternalException for IO errors.
     */
    public static void readUtf8Lines(final File file, final ConsumerX<String> lineHandler) throws InternalException {
        readLines(file, Charset.UTF_8, lineHandler);
    }

    /**
     * Processes each line of a file using a `ConsumerX`.
     *
     * @param file        The file.
     * @param charset     The character set.
     * @param lineHandler The line handler.
     * @throws InternalException for IO errors.
     */
    public static void readLines(
            final File file,
            final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) throws InternalException {
        FileReader.of(file, charset).readLines(lineHandler);
    }

    /**
     * Processes each line of a `RandomAccessFile` using a `ConsumerX`.
     *
     * @param file        The `RandomAccessFile`.
     * @param charset     The character set.
     * @param lineHandler The line handler.
     * @throws InternalException for IO errors.
     */
    public static void readLines(
            final RandomAccessFile file,
            final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) {
        String line;
        try {
            while ((line = file.readLine()) != null) {
                lineHandler.accept(Charset.convert(line, Charset.ISO_8859_1, charset));
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads and processes a single line from a `RandomAccessFile`.
     *
     * @param file        The `RandomAccessFile`.
     * @param charset     The character set.
     * @param lineHandler The line handler.
     * @throws InternalException for IO errors.
     */
    public static void readLine(
            final RandomAccessFile file,
            final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) {
        final String line = readLine(file, charset);
        if (null != line) {
            lineHandler.accept(line);
        }
    }

    /**
     * Reads a single line from a `RandomAccessFile`.
     *
     * @param file    The `RandomAccessFile`.
     * @param charset The character set.
     * @return The line content.
     * @throws InternalException for IO errors.
     */
    public static String readLine(final RandomAccessFile file, final java.nio.charset.Charset charset) {
        final String line;
        try {
            line = file.readLine();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (null != line) {
            return Charset.convert(line, Charset.ISO_8859_1, charset);
        }
        return null;
    }

    /**
     * Reads a file using a custom handler for the `BufferedReader`.
     *
     * @param <T>           The result type.
     * @param path          The file path.
     * @param readerHandler The handler for processing the reader.
     * @return The result from the handler.
     * @throws InternalException for IO errors.
     */
    public static <T> T readUtf8(final String path, final FunctionX<BufferedReader, T> readerHandler)
            throws InternalException {
        return read(path, Charset.UTF_8, readerHandler);
    }

    /**
     * Reads a file using a custom handler for the `BufferedReader`.
     *
     * @param <T>           The result type.
     * @param path          The file path.
     * @param charset       The character set.
     * @param readerHandler The handler for processing the reader.
     * @return The result from the handler.
     * @throws InternalException for IO errors.
     */
    public static <T> T read(
            final String path,
            final java.nio.charset.Charset charset,
            final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        return read(file(path), charset, readerHandler);
    }

    /**
     * Reads a file using a custom handler for the `BufferedReader` with UTF-8 encoding.
     *
     * @param <T>           The result type.
     * @param file          The file.
     * @param readerHandler The handler for processing the reader.
     * @return The result from the handler.
     * @throws InternalException for IO errors.
     */
    public static <T> T readUtf8(final File file, final FunctionX<BufferedReader, T> readerHandler)
            throws InternalException {
        return read(file, Charset.UTF_8, readerHandler);
    }

    /**
     * Reads a file using a custom handler for the `BufferedReader`.
     *
     * @param <T>           The result type.
     * @param file          The file.
     * @param charset       The character set.
     * @param readerHandler The handler for processing the reader.
     * @return The result from the handler.
     * @throws InternalException for IO errors.
     */
    public static <T> T read(
            final File file,
            final java.nio.charset.Charset charset,
            final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        return FileReader.of(file, charset).read(readerHandler);
    }

    /**
     * Gets a `BufferedOutputStream` for a file.
     *
     * @param file    The file.
     * @param options The open options (e.g., `StandardOpenOption.APPEND`).
     * @return A `BufferedOutputStream`.
     */
    public static BufferedOutputStream getOutputStream(final File file, final OpenOption... options) {
        return PathResolve.getOutputStream(touch(file).toPath(), options);
    }

    /**
     * Gets a `BufferedOutputStream` for a file path.
     *
     * @param path The file path.
     * @return A `BufferedOutputStream`.
     */
    public static BufferedOutputStream getOutputStream(final String path) {
        return getOutputStream(touch(path));
    }

    /**
     * Gets a `BufferedWriter` for a file path.
     *
     * @param path     The file path.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return A `BufferedWriter`.
     */
    public static BufferedWriter getWriter(
            final String path,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return getWriter(touch(path), charset, isAppend);
    }

    /**
     * Gets a `BufferedWriter` for a file.
     *
     * @param file     The file.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return A `BufferedWriter`.
     */
    public static BufferedWriter getWriter(
            final File file,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).getWriter(isAppend);
    }

    /**
     * Gets a `PrintWriter` for a file path.
     *
     * @param path     The file path.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return A `PrintWriter`.
     */
    public static PrintWriter getPrintWriter(
            final String path,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * Gets a `PrintWriter` for a file.
     *
     * @param file     The file.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return A `PrintWriter`.
     */
    public static PrintWriter getPrintWriter(
            final File file,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(file, charset, isAppend));
    }

    /**
     * Gets the system's line separator.
     *
     * @return The line separator string.
     */
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * Writes a string to a file using UTF-8 encoding (overwrite mode).
     *
     * @param content The content to write.
     * @param path    The file path.
     * @return The written file.
     */
    public static File writeUtf8String(final String content, final String path) throws InternalException {
        return writeString(content, path, Charset.UTF_8);
    }

    /**
     * Writes a string to a file using UTF-8 encoding (overwrite mode).
     *
     * @param content The content to write.
     * @param file    The file.
     * @return The written file.
     */
    public static File writeUtf8String(final String content, final File file) throws InternalException {
        return writeString(content, file, Charset.UTF_8);
    }

    /**
     * Writes a string to a file (overwrite mode).
     *
     * @param content The content to write.
     * @param path    The file path.
     * @param charset The character set.
     * @return The written file.
     */
    public static File writeString(final String content, final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return writeString(content, touch(path), charset);
    }

    /**
     * Writes a string to a file (overwrite mode).
     *
     * @param content The content to write.
     * @param file    The file.
     * @param charset The character set.
     * @return The written file.
     */
    public static File writeString(final String content, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return FileWriter.of(file, charset).write(content);
    }

    /**
     * Appends a string to a file using UTF-8 encoding.
     *
     * @param content The content to append.
     * @param path    The file path.
     * @return The written file.
     */
    public static File appendUtf8String(final String content, final String path) throws InternalException {
        return appendString(content, path, Charset.UTF_8);
    }

    /**
     * Appends a string to a file.
     *
     * @param content The content to append.
     * @param path    The file path.
     * @param charset The character set.
     * @return The written file.
     */
    public static File appendString(final String content, final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return appendString(content, touch(path), charset);
    }

    /**
     * Appends a string to a file using UTF-8 encoding.
     *
     * @param content The content to append.
     * @param file    The file.
     * @return The written file.
     */
    public static File appendUtf8String(final String content, final File file) throws InternalException {
        return appendString(content, file, Charset.UTF_8);
    }

    /**
     * Appends a string to a file.
     *
     * @param content The content to append.
     * @param file    The file.
     * @param charset The character set.
     * @return The written file.
     */
    public static File appendString(final String content, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return FileWriter.of(file, charset).append(content);
    }

    /**
     * Writes a collection of lines to a file using UTF-8 encoding (overwrite mode).
     *
     * @param <T>  The type of the elements.
     * @param list The collection of lines.
     * @param path The file path.
     * @return The written file.
     */
    public static <T> File writeUtf8Lines(final Collection<T> list, final String path) throws InternalException {
        return writeLines(list, path, Charset.UTF_8);
    }

    /**
     * Writes a collection of lines to a file using UTF-8 encoding (overwrite mode).
     *
     * @param <T>  The type of the elements.
     * @param list The collection of lines.
     * @param file The file.
     * @return The written file.
     */
    public static <T> File writeUtf8Lines(final Collection<T> list, final File file) throws InternalException {
        return writeLines(list, file, Charset.UTF_8);
    }

    /**
     * Writes a collection of lines to a file (overwrite mode).
     *
     * @param <T>     The type of the elements.
     * @param list    The collection of lines.
     * @param path    The file path.
     * @param charset The character set.
     * @return The written file.
     */
    public static <T> File writeLines(
            final Collection<T> list,
            final String path,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, path, charset, false);
    }

    /**
     * Writes a collection of lines to a file (overwrite mode).
     *
     * @param <T>     The type of the elements.
     * @param list    The collection of lines.
     * @param file    The file.
     * @param charset The character set.
     * @return The written file.
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return writeLines(list, file, charset, false);
    }

    /**
     * Appends a collection of lines to a file using UTF-8 encoding.
     *
     * @param <T>  The type of the elements.
     * @param list The collection of lines.
     * @param file The file.
     * @return The written file.
     */
    public static <T> File appendUtf8Lines(final Collection<T> list, final File file) throws InternalException {
        return appendLines(list, file, Charset.UTF_8);
    }

    /**
     * Appends a collection of lines to a file using UTF-8 encoding.
     *
     * @param <T>  The type of the elements.
     * @param list The collection of lines.
     * @param path The file path.
     * @return The written file.
     */
    public static <T> File appendUtf8Lines(final Collection<T> list, final String path) throws InternalException {
        return appendLines(list, path, Charset.UTF_8);
    }

    /**
     * Appends a collection of lines to a file.
     *
     * @param <T>     The type of the elements.
     * @param list    The collection of lines.
     * @param path    The file path.
     * @param charset The character set.
     * @return The written file.
     */
    public static <T> File appendLines(
            final Collection<T> list,
            final String path,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, path, charset, true);
    }

    /**
     * Appends a collection of lines to a file.
     *
     * @param <T>     The type of the elements.
     * @param list    The collection of lines.
     * @param file    The file.
     * @param charset The character set.
     * @return The written file.
     */
    public static <T> File appendLines(
            final Collection<T> list,
            final File file,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, file, charset, true);
    }

    /**
     * Writes a collection of lines to a file.
     *
     * @param <T>      The type of the elements.
     * @param list     The collection of lines.
     * @param path     The file path.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return The written file.
     */
    public static <T> File writeLines(
            final Collection<T> list,
            final String path,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return writeLines(list, file(path), charset, isAppend);
    }

    /**
     * Writes a collection of lines to a file.
     *
     * @param <T>      The type of the elements.
     * @param list     The collection of lines.
     * @param file     The file.
     * @param charset  The character set.
     * @param isAppend If `true`, appends to the file.
     * @return The written file.
     */
    public static <T> File writeLines(
            final Collection<T> list,
            final File file,
            final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).writeLines(list, isAppend);
    }

    /**
     * Writes a collection of lines to a file.
     *
     * @param <T>                 The type of the elements.
     * @param list                The collection of lines.
     * @param file                The file.
     * @param charset             The character set.
     * @param isAppend            If `true`, appends to the file.
     * @param appendLineSeparator If `true`, adds a line separator at the end.
     * @return The written file.
     */
    public static <T> File writeLines(
            final Collection<T> list,
            final File file,
            final java.nio.charset.Charset charset,
            final boolean isAppend,
            final boolean appendLineSeparator) throws InternalException {
        return FileWriter.of(file, charset).writeLines(list, null, isAppend, appendLineSeparator);
    }

    /**
     * Writes a map to a file using UTF-8 encoding.
     *
     * @param map         The map.
     * @param file        The file.
     * @param kvSeparator The separator between key and value.
     * @param isAppend    If `true`, appends to the file.
     * @return The written file.
     */
    public static File writeUtf8Map(
            final Map<?, ?> map,
            final File file,
            final String kvSeparator,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, Charset.UTF_8).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * Writes a map to a file.
     *
     * @param map         The map.
     * @param file        The file.
     * @param charset     The character set.
     * @param kvSeparator The separator between key and value.
     * @param isAppend    If `true`, appends to the file.
     * @return The written file.
     */
    public static File writeMap(
            final Map<?, ?> map,
            final File file,
            final java.nio.charset.Charset charset,
            final String kvSeparator,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * Writes byte data to a file.
     *
     * @param data The byte array.
     * @param path The file path.
     * @return The written file.
     */
    public static File writeBytes(final byte[] data, final String path) throws InternalException {
        return writeBytes(data, touch(path));
    }

    /**
     * Writes byte data to a file.
     *
     * @param data The byte array.
     * @param dest The destination file.
     * @return The written file.
     */
    public static File writeBytes(final byte[] data, final File dest) throws InternalException {
        return writeBytes(data, dest, 0, data.length, false);
    }

    /**
     * Writes byte data to a file.
     *
     * @param data     The byte array.
     * @param target   The target file.
     * @param off      The start offset in the data.
     * @param len      The number of bytes to write.
     * @param isAppend If `true`, appends to the file.
     * @return The written file.
     */
    public static File writeBytes(
            final byte[] data,
            final File target,
            final int off,
            final int len,
            final boolean isAppend) {
        return FileWriter.of(target).write(data, off, len, isAppend);
    }

    /**
     * Writes the contents of an `InputStream` to a file, closing the stream afterward.
     *
     * @param in     The `InputStream`.
     * @param target The target file.
     * @return The written file.
     */
    public static File writeFromStream(final InputStream in, final File target) {
        return writeFromStream(in, target, true);
    }

    /**
     * Writes the contents of an `InputStream` to a file.
     *
     * @param in        The `InputStream`.
     * @param target    The target file.
     * @param isCloseIn If `true`, closes the input stream after writing.
     * @return The written file.
     */
    public static File writeFromStream(final InputStream in, final File target, final boolean isCloseIn) {
        return FileWriter.of(target).writeFromStream(in, isCloseIn);
    }

    /**
     * Writes the contents of a file to an `OutputStream`.
     *
     * @param file The file.
     * @param out  The `OutputStream`.
     * @return The number of bytes written.
     */
    public static long writeToStream(final File file, final OutputStream out) {
        return FileReader.of(file).writeToStream(out);
    }

    /**
     * Returns a human-readable file size.
     *
     * @param file The file.
     * @return The readable file size string.
     */
    public static String readableFileSize(final File file) {
        Assert.notNull(file);
        return readableFileSize(file.length());
    }

    /**
     * Returns a human-readable file size.
     *
     * @param size The size in bytes.
     * @return The readable file size string.
     * @see DataSize#format(long)
     */
    public static String readableFileSize(final long size) {
        return DataSize.format(size);
    }

    /**
     * Converts the character encoding of a file.
     *
     * @param file        The file.
     * @param srcCharset  The source charset.
     * @param destCharset The destination charset.
     * @return The converted file.
     * @see Charset#convert(File, java.nio.charset.Charset, java.nio.charset.Charset)
     */
    public static File convertCharset(
            final File file,
            final java.nio.charset.Charset srcCharset,
            final java.nio.charset.Charset destCharset) {
        return Charset.convert(file, srcCharset, destCharset);
    }

    /**
     * Converts the line separators in a file.
     *
     * @param file          The file.
     * @param charset       The character set.
     * @param lineSeparator The target line separator.
     * @return The modified file.
     */
    public static File convertLineSeparator(
            final File file,
            final java.nio.charset.Charset charset,
            final LineSeparator lineSeparator) {
        final List<String> lines = readLines(file, charset);
        return FileWriter.of(file, charset).writeLines(lines, lineSeparator, false);
    }

    /**
     * Gets the web root directory of a web application.
     *
     * @return The web root directory.
     */
    public static File getWebRoot() {
        final String classPath = ClassKit.getClassPath();
        Console.log(classPath);
        if (StringKit.isNotBlank(classPath)) {
            return getParent(file(classPath), 2);
        }
        return null;
    }

    /**
     * Gets the parent directory of a file.
     *
     * @param file The file or directory.
     * @return The parent directory, or `null` if it doesn't exist.
     */
    public static File getParent(final File file) {
        return getParent(file, 1);
    }

    /**
     * Gets an ancestor directory at a specified level.
     *
     * @param filePath The file path.
     * @param level    The number of levels up from the parent.
     * @return The ancestor path, or `null`.
     */
    public static String getParent(final String filePath, final int level) {
        final File parent = getParent(file(filePath), level);
        try {
            return null == parent ? null : parent.getCanonicalPath();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets an ancestor directory at a specified level.
     *
     * @param file  The file or directory.
     * @param level The number of levels up from the parent (1 is the direct parent).
     * @return The ancestor directory, or `null`.
     */
    public static File getParent(final File file, final int level) {
        if (level < 1 || null == file) {
            return file;
        }
        final File parentFile;
        try {
            parentFile = file.getCanonicalFile().getParentFile();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (1 == level) {
            return parentFile;
        }
        return getParent(parentFile, level - 1);
    }

    /**
     * Checks for Zip Slip vulnerability by ensuring a file is a true subpath of its parent.
     *
     * @param parentFile The parent directory.
     * @param file       The child file.
     * @return The child file if the check passes.
     * @throws IllegalArgumentException if a Zip Slip vulnerability is detected.
     */
    public static File checkSlip(final File parentFile, final File file) throws IllegalArgumentException {
        if (null != parentFile && null != file) {
            if (!isSub(parentFile, file)) {
                throw new IllegalArgumentException(
                        StringKit.format("New file [{}] is outside of the parent dir: [{}]", file, parentFile));
            }
        }
        return file;
    }

    /**
     * Gets the MIME type of a file based on its extension.
     *
     * @param filePath     The file path or name.
     * @param defaultValue The default MIME type to return if none is found.
     * @return The MIME type.
     */
    public static String getMimeType(final String filePath, final String defaultValue) {
        return ObjectKit.defaultIfNull(getMimeType(filePath), defaultValue);
    }

    /**
     * Gets the MIME type of a file based on its extension.
     *
     * @param filePath The file path or name.
     * @return The MIME type.
     */
    public static String getMimeType(final String filePath) {
        if (StringKit.isBlank(filePath)) {
            return null;
        }

        if (StringKit.endWithIgnoreCase(filePath, ".css")) {
            return "text/css";
        } else if (StringKit.endWithIgnoreCase(filePath, ".js")) {
            return "application/x-javascript";
        } else if (StringKit.endWithIgnoreCase(filePath, ".rar")) {
            return "application/x-rar-compressed";
        } else if (StringKit.endWithIgnoreCase(filePath, ".7z")) {
            return "application/x-7z-compressed";
        } else if (StringKit.endWithIgnoreCase(filePath, ".wgt")) {
            return "application/widget";
        } else if (StringKit.endWithIgnoreCase(filePath, ".webp")) {
            return "image/webp";
        }

        String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
        if (null == contentType) {
            contentType = getMimeType(Paths.get(filePath));
        }

        return contentType;
    }

    /**
     * Checks if a file is a symbolic link.
     *
     * @param file The file to check.
     * @return `true` if it is a symbolic link.
     */
    public static boolean isSymlink(final File file) {
        return isSymlink(file.toPath());
    }

    /**
     * Checks if a file or directory is a subdirectory of a parent directory.
     *
     * @param parent The parent directory.
     * @param sub    The potential subdirectory.
     * @return `true` if it is a subdirectory.
     */
    public static boolean isSub(final File parent, final File sub) {
        Assert.notNull(parent);
        Assert.notNull(sub);
        return isSub(parent.toPath(), sub.toPath());
    }

    /**
     * Creates a `RandomAccessFile`.
     *
     * @param path The file path.
     * @param mode The access mode.
     * @return A `RandomAccessFile`.
     */
    public static RandomAccessFile createRandomAccessFile(final Path path, final FileMode mode) {
        return createRandomAccessFile(path.toFile(), mode);
    }

    /**
     * Creates a `RandomAccessFile`.
     *
     * @param file The file.
     * @param mode The access mode.
     * @return A `RandomAccessFile`.
     */
    public static RandomAccessFile createRandomAccessFile(final File file, final FileMode mode) {
        try {
            return new RandomAccessFile(file, mode.name());
        } catch (final FileNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Tails a file, similar to the `tail -f` command (this method blocks).
     *
     * @param file    The file.
     * @param handler The handler for each new line.
     */
    public static void tail(final File file, final ConsumerX<String> handler) {
        tail(file, Charset.UTF_8, handler);
    }

    /**
     * Tails a file, similar to the `tail -f` command (this method blocks).
     *
     * @param file    The file.
     * @param charset The character set.
     * @param handler The handler for each new line.
     */
    public static void tail(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> handler) {
        new FileTailer(file, charset, handler).start();
    }

    /**
     * Tails a file to the console.
     *
     * @param file    The file.
     * @param charset The character set.
     */
    public static void tail(final File file, final java.nio.charset.Charset charset) {
        tail(file, charset, FileTailer.CONSOLE_HANDLER);
    }

    /**
     * Creates a new `FileSystem` for a given path (e.g., a ZIP file).
     *
     * @param path The file path.
     * @return A new `FileSystem`.
     */
    public static FileSystem of(final String path) {
        try {
            return FileSystems.newFileSystem(Paths.get(path).toUri(), MapKit.of("create", "true"));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a new ZIP `FileSystem`.
     *
     * @param path The path to the ZIP file.
     * @return A new `FileSystem`.
     */
    public static FileSystem createZip(final String path) {
        return createZip(path, null);
    }

    /**
     * Creates a new ZIP `FileSystem` with a specified charset.
     *
     * @param path    The path to the ZIP file.
     * @param charset The character set.
     * @return A new `FileSystem`.
     */
    public static FileSystem createZip(final String path, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        final HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", charset.name());

        try {
            return FileSystems.newFileSystem(URI.create("jar:" + Paths.get(path).toUri()), env);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the root path of a `FileSystem`.
     *
     * @param fileSystem The `FileSystem`.
     * @return The root `Path`.
     */
    public static Path getRoot(final FileSystem fileSystem) {
        return fileSystem.getPath(Symbol.SLASH);
    }

    /**
     * Builds a file path, handling nested directories safely.
     *
     * @param outFile  The output directory.
     * @param fileName The file name, which can include path separators.
     * @return The constructed `File`.
     */
    private static File buildFile(File outFile, String fileName) {
        fileName = fileName.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH);
        if (!isWindows() && fileName.lastIndexOf(Symbol.C_SLASH, fileName.length() - 2) > 0) {
            final List<String> pathParts = CharsBacker.split(fileName, Symbol.SLASH, false, true);
            final int lastPartIndex = pathParts.size() - 1;
            for (int i = 0; i < lastPartIndex; i++) {
                outFile = new File(outFile, pathParts.get(i));
            }
            outFile.mkdirs();
            fileName = pathParts.get(lastPartIndex);
        }
        return new File(outFile, fileName);
    }

}
