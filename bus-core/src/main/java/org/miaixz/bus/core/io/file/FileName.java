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
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.StringTrimer;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A utility class for file name operations, providing methods to extract, modify, and validate file names and their
 * components.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileName {

    /**
     * Constructs a new FileName. Utility class constructor for static access.
     */
    private FileName() {
    }

    /**
     * An array of special double-file extensions (e.g., for compressed tarballs like "tar.bz2", "tar.gz", "tar.xz").
     * These suffixes are treated as a single extension for operations like {@link #mainName(String)}.
     */
    private static final CharSequence[] SPECIAL_SUFFIX = { "tar.bz2", "tar.Z", "tar.gz", "tar.xz" };

    /**
     * Returns the name of the file.
     *
     * @param file The file object.
     * @return The file name, or {@code null} if the file is {@code null}.
     */
    public static String getName(final File file) {
        return (null != file) ? file.getName() : null;
    }

    /**
     * Returns the file name from a file path. This method extracts the last component of the path.
     *
     * <pre>
     * "d:/test/aaa"    -&gt; "aaa"
     * "/test/aaa.jpg" -&gt; "aaa.jpg"
     * "/test/"        -&gt; "test"
     * </pre>
     *
     * @param filePath The file path string.
     * @return The file name, or {@code null} if the file path is {@code null}.
     */
    public static String getName(String filePath) {
        if (null == filePath) {
            return null;
        }
        int len = filePath.length();
        if (0 == len) {
            return filePath;
        }
        if (CharKit.isFileSeparator(filePath.charAt(len - 1))) {
            len--;
        }

        int begin = 0;
        for (int i = len - 1; i > -1; i--) {
            char c = filePath.charAt(i);
            if (CharKit.isFileSeparator(c)) {
                begin = i + 1;
                break;
            }
        }
        return filePath.substring(begin, len);
    }

    /**
     * Returns the main name of a file, which is the file name without the extension. If the file is a directory, its
     * name is returned.
     *
     * @param file The file object.
     * @return The main name of the file.
     */
    public static String mainName(final File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        return mainName(file.getName());
    }

    /**
     * Returns the main name of a file from its full name, excluding the extension. This method handles special double
     * extensions like "tar.gz".
     *
     * @param fileName The full file name string.
     * @return The main name of the file, or {@code null} if the file name is {@code null}.
     */
    public static String mainName(String fileName) {
        if (null == fileName) {
            return null;
        }
        int len = fileName.length();
        if (0 == len) {
            return fileName;
        }

        for (final CharSequence specialSuffix : SPECIAL_SUFFIX) {
            if (StringKit.endWith(fileName, "." + specialSuffix)) {
                return StringKit.subPre(fileName, len - specialSuffix.length() - 1);
            }
        }

        if (CharKit.isFileSeparator(fileName.charAt(len - 1))) {
            len--;
        }

        int begin = 0;
        int end = len;
        for (int i = len - 1; i >= 0; i--) {
            char c = fileName.charAt(i);
            if (len == end && Symbol.C_DOT == c) {
                end = i;
            }
            if (CharKit.isFileSeparator(c)) {
                begin = i + 1;
                break;
            }
        }
        return fileName.substring(begin, end);
    }

    /**
     * Gets the file extension (suffix), without the leading dot. If the file is a directory, {@code null} is returned.
     *
     * @param file The file object.
     * @return The extension string, or {@code null} if it's a directory or the file is {@code null}.
     */
    public static String extName(final File file) {
        if (null == file) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        return extName(file.getName());
    }

    /**
     * Gets the file extension from a file name, without the leading dot. This method handles special double extensions
     * and returns an empty string if no extension is found.
     *
     * @param fileName The file name string.
     * @return The extension string, or an empty string if there is no extension, or {@code null} if the file name is
     *         {@code null}.
     */
    public static String extName(final String fileName) {
        if (fileName == null) {
            return null;
        }
        final int index = fileName.lastIndexOf(Symbol.DOT);
        if (index == -1) {
            return Normal.EMPTY;
        }
        final int secondToLastIndex = fileName.substring(0, index).lastIndexOf(Symbol.DOT);
        final String substr = fileName.substring(secondToLastIndex == -1 ? index : secondToLastIndex + 1);
        if (StringKit.equalsAny(substr, SPECIAL_SUFFIX)) {
            return substr;
        }

        final String ext = fileName.substring(index + 1);
        return StringKit.containsAny(ext, Symbol.C_SLASH, Symbol.C_BACKSLASH) ? Normal.EMPTY : ext;
    }

    /**
     * Gets the file suffix (extension). This is an alias for {@link #extName(File)}.
     *
     * @param file The file object.
     * @return The extension string.
     */
    public static String getSuffix(final File file) {
        return extName(file);
    }

    /**
     * Gets the file suffix (extension). This is an alias for {@link #extName(String)}.
     *
     * @param fileName The file name string.
     * @return The extension string.
     */
    public static String getSuffix(final String fileName) {
        return extName(fileName);
    }

    /**
     * Gets the main part of the file name (prefix before the extension). This is an alias for {@link #mainName(File)}.
     *
     * @param file The file object.
     * @return The main name (prefix) of the file.
     */
    public static String getPrefix(final File file) {
        return mainName(file);
    }

    /**
     * Gets the main part of the file name (prefix before the extension). This is an alias for
     * {@link #mainName(String)}.
     *
     * @param fileName The full file name string.
     * @return The main name (prefix) of the file.
     */
    public static String getPrefix(final String fileName) {
        return mainName(fileName);
    }

    /**
     * Adds a temporary suffix to a file name. If the provided suffix is blank, ".temp" is used. The suffix will be
     * prefixed with a dot if it's not already.
     *
     * @param fileName The original file name string.
     * @param suffix   The temporary suffix to add. If blank, ".temp" is used.
     * @return The new file name with the temporary suffix appended.
     */
    public static String addTempSuffix(final String fileName, String suffix) {
        if (StringKit.isBlank(suffix)) {
            suffix = ".temp";
        } else {
            suffix = StringKit.addPrefixIfNot(suffix, Symbol.DOT);
        }
        return fileName + suffix;
    }

    /**
     * Removes invalid characters for a file name on Windows systems. Invalid characters include:
     * {@code \ / : * ? " < > |}.
     *
     * @param fileName The file name string (should not include the path).
     * @return The cleaned file name string, or the original file name if it's blank.
     */
    public static String cleanInvalid(final String fileName) {
        return StringKit.isBlank(fileName) ? fileName
                : PatternKit.delAll(Pattern.FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * Checks if a file name contains invalid characters for Windows systems. Invalid characters include:
     * {@code \ / : * ? " < > |}.
     *
     * @param fileName The file name string to check.
     * @return {@code true} if the file name contains invalid characters, {@code false} otherwise.
     */
    public static boolean containsInvalid(final String fileName) {
        return !StringKit.isBlank(fileName) && PatternKit.contains(Pattern.FILE_NAME_INVALID_PATTERN_WIN, fileName);
    }

    /**
     * Checks if a file has one of the specified extensions (case-insensitive).
     *
     * @param fileName The file name string.
     * @param extNames The extensions to check against (without the leading dot). Can be multiple.
     * @return {@code true} if the file's extension matches one of the given extensions (case-insensitive),
     *         {@code false} otherwise.
     */
    public static boolean isType(final String fileName, final String... extNames) {
        return StringKit.equalsAnyIgnoreCase(extName(fileName), extNames);
    }

    /**
     * Normalizes a path string by resolving "." and ".." segments and standardizing separators to '/'. It also handles
     * special prefixes like "classpath:" and "file:", and user home directory (~) expansion.
     *
     * @param path The original path string.
     * @return The normalized path string, or {@code null} if the input path is {@code null}.
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("\\\\")) {
            return path; // SMB path
        }

        String pathToUse = StringKit.removePrefixIgnoreCase(path, Normal.CLASSPATH);
        pathToUse = StringKit.removePrefixIgnoreCase(pathToUse, Normal.FILE_URL_PREFIX);
        if (StringKit.startWith(pathToUse, Symbol.C_TILDE)) {
            pathToUse = Keys.getUserHomePath() + pathToUse.substring(1);
        }

        pathToUse = pathToUse.replaceAll("[/\\\\]+", Symbol.SLASH);
        pathToUse = StringKit.trimPrefix(pathToUse);
        pathToUse = StringKit.trim(pathToUse, StringTrimer.TrimMode.SUFFIX, (c) -> c == '\n' || c == '\r');

        String prefix = Normal.EMPTY;
        final int prefixIndex = pathToUse.indexOf(Symbol.COLON);
        if (prefixIndex > -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (StringKit.startWith(prefix, Symbol.C_SLASH)) {
                prefix = prefix.substring(1);
            }
            if (!prefix.contains(Symbol.SLASH)) {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            } else {
                prefix = Normal.EMPTY;
            }
        }
        if (pathToUse.startsWith(Symbol.SLASH)) {
            prefix += Symbol.SLASH;
            pathToUse = pathToUse.substring(1);
        }

        return prefix
                + CollKit.join(resolePathElements(StringKit.split(pathToUse, Symbol.SLASH), prefix), Symbol.SLASH);
    }

    /**
     * Renames the main part of a file name, preserving its original extension.
     *
     * @param filePath        The full file path string.
     * @param newFileMainName The new main name for the file (without extension).
     * @return The new file name string with the updated main name and original extension.
     */
    public static String rename(final String filePath, final String newFileMainName) {
        String fileName = getName(filePath);
        if (StringKit.isBlank(fileName)) {
            return newFileMainName;
        }

        final String suffix = getSuffix(fileName);
        return StringKit.isBlank(suffix) ? newFileMainName : newFileMainName + "." + suffix;
    }

    /**
     * Resolves path elements by handling "." (current directory) and ".." (parent directory) segments. This is a helper
     * method for path normalization.
     *
     * @param pathList The list of path segments (strings).
     * @return The resolved list of path segments, with "." and ".." handled.
     */
    private static List<String> resolePathElements(final List<String> pathList, final String prefix) {
        final List<String> pathElements = new LinkedList<>();
        int tops = 0;
        for (int i = pathList.size() - 1; i >= 0; i--) {
            String element = pathList.get(i);
            if (!Symbol.DOT.equals(element)) {
                if (Symbol.DOUBLE_DOT.equals(element)) {
                    tops++;
                } else {
                    if (tops > 0) {
                        tops--;
                    } else {
                        pathElements.add(0, element);
                    }
                }
            }
        }
        if (tops > 0 && StringKit.isEmpty(prefix)) {
            // 只有相对路径补充开头的..，绝对路径直接忽略之
            while (tops-- > 0) {
                // 遍历完节点发现还有上级标注（即开头有一个或多个..），补充之
                pathElements.add(0, Symbol.DOUBLE_DOT);
            }
        }
        return pathElements;
    }

}
