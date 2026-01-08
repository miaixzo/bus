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
package org.miaixz.bus.core.io.resource;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.AntPathMatcher;
import org.miaixz.bus.core.xyz.*;

/**
 * Resource finder, inspired by Spring's PathMatchingResourcePatternResolver. This class implements classpath resource
 * lookup and uses {@link AntPathMatcher} to filter resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceFinder {

    /**
     * The class loader used to define the scope of resource lookup.
     */
    private final ClassLoader classLoader;
    /**
     * The Ant-style path matcher used for filtering resources.
     */
    private final AntPathMatcher pathMatcher;

    /**
     * Constructs a {@code ResourceFinder} with the specified class loader.
     *
     * @param classLoader The class loader to use for finding resources.
     */
    public ResourceFinder(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.pathMatcher = new AntPathMatcher();
    }

    /**
     * Builds a new {@code ResourceFinder} using the current environment's class loader.
     *
     * @return A new {@code ResourceFinder} instance.
     */
    public static ResourceFinder of() {
        return of(ClassKit.getClassLoader());
    }

    /**
     * Builds a new {@code ResourceFinder} with the specified class loader.
     *
     * @param classLoader The class loader to use for limiting the search scope.
     * @return A new {@code ResourceFinder} instance.
     */
    public static ResourceFinder of(final ClassLoader classLoader) {
        return new ResourceFinder(classLoader);
    }

    /**
     * Replaces backslashes ('\') with forward slashes ('/') in the given path.
     *
     * @param path The path string to process.
     * @return The path with backslashes replaced by forward slashes, or the original path if empty.
     */
    private static String replaceBackSlash(final String path) {
        return StringKit.isEmpty(path) ? path : path.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH);
    }

    /**
     * Finds resources corresponding to the given location pattern.
     *
     * @param locationPattern The Ant-style path pattern for resource lookup.
     * @return A {@link MultiResource} containing all matching resources.
     * @throws InternalException if an I/O error occurs or an unsupported resource type is encountered.
     */
    public MultiResource find(final String locationPattern) {
        // Determine the root directory, e.g., "/WEB-INF/*.xml" returns "/WEB-INF/"
        final String rootDirPath = determineRootDir(locationPattern);
        // Determine the sub-pattern, e.g., "/WEB-INF/*.xml" returns "*.xml"
        final String subPattern = locationPattern.substring(rootDirPath.length());

        final MultiResource result = new MultiResource();
        // Iterate through all resources in the root directory and filter to keep only matching resources
        for (final Resource rootResource : ResourceKit.getResources(rootDirPath, classLoader)) {
            if (rootResource instanceof JarResource) {
                // Resource is in a JAR package
                try {
                    result.addAll(findInJar((JarResource) rootResource, subPattern));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            } else if (rootResource instanceof FileResource) {
                // Resource is in a folder
                result.addAll(findInDir((FileResource) rootResource, subPattern));
            } else {
                throw new InternalException("Unsupported resource type: {}", rootResource.getClass().getName());
            }
        }

        return result;
    }

    /**
     * Finds resources within a JAR package that match the given sub-pattern.
     *
     * @param rootResource The root resource, which should be a {@link JarResource} representing the JAR file.
     * @param subPattern   The sub-pattern to match against entries within the JAR, e.g., "*.xml".
     * @return A {@link MultiResource} containing all matching resources within the JAR.
     * @throws IOException If an I/O error occurs while accessing the JAR file.
     */
    protected MultiResource findInJar(final JarResource rootResource, final String subPattern) throws IOException {
        final URL rootDirURL = rootResource.getUrl();
        final URLConnection conn = rootDirURL.openConnection();

        final JarFile jarFile;
        String rootEntryPath;
        final boolean closeJarFile;

        if (conn instanceof JarURLConnection jarCon) {
            UrlKit.useCachesIfNecessary(jarCon);
            jarFile = jarCon.getJarFile();
            final JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : Normal.EMPTY);
            closeJarFile = !jarCon.getUseCaches();
        } else {
            // Re-obtain the JAR file after removing sub-paths
            final String urlFile = rootDirURL.getFile();
            try {
                int separatorIndex = urlFile.indexOf(Normal.WAR_URL_SEPARATOR);
                if (separatorIndex == -1) {
                    separatorIndex = urlFile.indexOf(Normal.JAR_URL_SEPARATOR);
                }
                if (separatorIndex != -1) {
                    final String jarFileUrl = urlFile.substring(0, separatorIndex);
                    rootEntryPath = urlFile.substring(separatorIndex + 2); // both separators are 2 chars
                    jarFile = ZipKit.ofJar(jarFileUrl);
                } else {
                    jarFile = new JarFile(urlFile);
                    rootEntryPath = Normal.EMPTY;
                }
                closeJarFile = true;
            } catch (final ZipException ex) {
                return new MultiResource();
            }
        }

        rootEntryPath = StringKit.addSuffixIfNot(rootEntryPath, Symbol.SLASH);
        // Iterate through entries in the JAR and filter them
        final MultiResource result = new MultiResource();

        try {
            String entryPath;
            for (final JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
                entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    final String relativePath = entryPath.substring(rootEntryPath.length());
                    if (pathMatcher.match(subPattern, relativePath)) {
                        result.add(ResourceKit.getResource(UrlKit.getURL(rootDirURL, relativePath)));
                    }
                }
            }
        } finally {
            if (closeJarFile) {
                IoKit.closeQuietly(jarFile);
            }
        }

        return result;
    }

    /**
     * Traverses a directory to find files matching the specified sub-pattern.
     *
     * @param resource   The file resource representing the root directory.
     * @param subPattern The sub-pattern to match against files within the directory.
     * @return A {@link MultiResource} containing all matching files.
     */
    protected MultiResource findInDir(final FileResource resource, final String subPattern) {
        final MultiResource result = new MultiResource();
        final File rootDir = resource.getFile();
        if (!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
            // Ensure the given file exists, is a directory, and is readable
            return result;
        }

        final String fullPattern = replaceBackSlash(rootDir.getAbsolutePath() + Symbol.SLASH + subPattern);

        FileKit.walkFiles(rootDir, (file -> {
            final String currentPath = replaceBackSlash(file.getAbsolutePath());
            if (file.isDirectory()) {
                // Check if the directory satisfies the pattern's starting rule; if so, continue searching, otherwise
                // skip.
                return pathMatcher.matchStart(fullPattern, StringKit.addSuffixIfNot(currentPath, Symbol.SLASH));
            }

            if (pathMatcher.match(fullPattern, currentPath)) {
                result.add(new FileResource(file));
                return true;
            }

            return false;
        }));

        return result;
    }

    /**
     * Determines the root directory from a given location pattern. The root directory is the part of the path that does
     * not contain any pattern characters. For example, "/WEB-INF/*.xml" returns "/WEB-INF/".
     *
     * @param location The location pattern.
     * @return The root directory path.
     */
    protected String determineRootDir(final String location) {
        final int prefixEnd = location.indexOf(Symbol.C_COLON) + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd && pathMatcher.isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf(Symbol.C_SLASH, rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

}
