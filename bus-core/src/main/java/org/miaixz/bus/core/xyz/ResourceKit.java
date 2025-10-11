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
package org.miaixz.bus.core.xyz;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.io.resource.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Resource utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceKit {

    /**
     * Reads a classpath resource as a string using UTF-8 encoding.
     *
     * @param resource The path to the resource, relative to the classpath root.
     * @return The resource content as a string.
     */
    public static String readString(final String resource) {
        return getResource(resource).readString();
    }

    /**
     * Reads a classpath resource as a string using a specified charset.
     *
     * @param resource The path to the resource.
     * @param charset  The charset.
     * @return The resource content as a string.
     */
    public static String readString(final String resource, final java.nio.charset.Charset charset) {
        return getResource(resource).readString(charset);
    }

    /**
     * Reads a classpath resource into a byte array.
     *
     * @param resource The path to the resource.
     * @return The resource content as a byte array.
     */
    public static byte[] readBytes(final String resource) {
        return getResource(resource).readBytes();
    }

    /**
     * Gets an `InputStream` for a classpath resource.
     *
     * @param resource The classpath resource path.
     * @return An `InputStream`.
     * @throws InternalException if the resource is not found.
     */
    public static InputStream getStream(final String resource) throws InternalException {
        return getResource(resource).getStream();
    }

    /**
     * Gets an `InputStream` for a classpath resource, returning `null` if not found.
     *
     * @param resource The classpath resource path.
     * @return An `InputStream`, or `null`.
     */
    public static InputStream getStreamSafe(final String resource) {
        try {
            return getResource(resource).getStream();
        } catch (final InternalException e) {
            // ignore
        }
        return null;
    }

    /**
     * Gets a `BufferedReader` for a classpath resource using UTF-8 encoding.
     *
     * @param resource The classpath resource path.
     * @return A `BufferedReader`.
     */
    public static BufferedReader getReader(final String resource) {
        return getReader(resource, Charset.UTF_8);
    }

    /**
     * Gets a `BufferedReader` for a classpath resource.
     *
     * @param resource The classpath resource path.
     * @param charset  The charset.
     * @return A `BufferedReader`.
     */
    public static BufferedReader getReader(final String resource, final java.nio.charset.Charset charset) {
        return getResource(resource).getReader(charset);
    }

    /**
     * Gets the URL for a resource.
     *
     * @param resource The resource path relative to the classpath.
     * @return The resource URL.
     * @throws InternalException if an IO error occurs.
     */
    public static URL getResourceUrl(final String resource) throws InternalException {
        return getResourceUrl(resource, null);
    }

    /**
     * Gets a list of URLs for all resources with a given name.
     *
     * @param resource The resource path.
     * @return A list of resource URLs.
     */
    public static List<URL> getResourceUrls(final String resource) {
        return getResourceUrls(resource, null);
    }

    /**
     * Gets a filtered list of URLs for all resources with a given name.
     *
     * @param resource The resource path.
     * @param filter   A predicate to filter the resources.
     * @return A list of resource URLs.
     */
    public static List<URL> getResourceUrls(final String resource, final Predicate<URL> filter) {
        return IteratorKit.filterToList(getResourceUrlIter(resource), filter);
    }

    /**
     * Gets an iterator for all resource URLs with a given name.
     *
     * @param resource The resource path.
     * @return An iterator of resource URLs.
     */
    public static EnumerationIterator<URL> getResourceUrlIter(final String resource) {
        return getResourceUrlIter(resource, null);
    }

    /**
     * Gets an iterator for all resource URLs with a given name.
     *
     * @param resource    The resource path.
     * @param classLoader The `ClassLoader`.
     * @return An iterator of resource URLs.
     */
    public static EnumerationIterator<URL> getResourceUrlIter(final String resource, final ClassLoader classLoader) {
        final Enumeration<URL> resources;
        try {
            resources = ObjectKit.defaultIfNull(classLoader, ClassKit::getClassLoader).getResources(resource);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return new EnumerationIterator<>(resources);
    }

    /**
     * Gets the URL for a resource relative to a base class or the classpath root.
     *
     * @param resource  The resource path.
     * @param baseClass The base class for relative paths.
     * @return The resource URL.
     */
    public static URL getResourceUrl(String resource, final Class<?> baseClass) {
        resource = StringKit.toStringOrEmpty(resource);
        return (null != baseClass) ? baseClass.getResource(resource) : ClassKit.getClassLoader().getResource(resource);
    }

    /**
     * Gets a {@link Resource} object for a given path.
     *
     * @param path The path (can be absolute, classpath-relative, or a URL).
     * @return A {@link Resource} object.
     */
    public static Resource getResource(final String path) {
        if (StringKit.isNotBlank(path)) {
            if (StringKit.startWithAny(path, Normal.FILE_URL_PREFIX, Normal.PROJECT_URL_PREFIX)
                    || FileKit.isAbsolutePath(path)) {
                return new FileResource(path);
            }
        }
        return new ClassPathResource(path);
    }

    /**
     * Gets a {@link Resource} object for a given URL.
     *
     * @param url The URL.
     * @return A {@link Resource} object.
     */
    public static Resource getResource(final URL url) {
        if (Normal.isJarURL(url)) {
            return new JarResource(url);
        } else if (Normal.isFileURL(url)) {
            return new FileResource(url.getFile());
        }
        return new UrlResource(url);
    }

    /**
     * Gets a `FileResource` object for a given `File`.
     *
     * @param file The `File`.
     * @return A `FileResource` object.
     */
    public static Resource getResource(final File file) {
        return new FileResource(file);
    }

    /**
     * Gets all resources with the same name from the classpath.
     *
     * @param resource The resource name.
     * @return A {@link MultiResource}.
     */
    public static MultiResource getResources(final String resource) {
        return getResources(resource, null);
    }

    /**
     * Gets all resources with the same name from the classpath.
     *
     * @param resource    The resource name.
     * @param classLoader The `ClassLoader`.
     * @return A {@link MultiResource}.
     */
    public static MultiResource getResources(final String resource, final ClassLoader classLoader) {
        final EnumerationIterator<URL> iter = getResourceUrlIter(resource, classLoader);
        final MultiResource resources = new MultiResource();
        for (final URL url : iter) {
            resources.add(getResource(url));
        }
        return resources;
    }

    /**
     * Loads the content of a resource into a `Properties` object.
     *
     * @param properties The `Properties` object.
     * @param resource   The resource.
     * @param charset    The charset (ignored for XML files).
     */
    public static void loadTo(
            final Properties properties,
            final Resource resource,
            final java.nio.charset.Charset charset) {
        Assert.notNull(properties);
        Assert.notNull(resource);
        final String filename = resource.getName();
        if (filename != null && StringKit.endWithIgnoreCase(filename, ".xml")) {
            try (final InputStream in = resource.getStream()) {
                properties.loadFromXML(in);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        } else {
            try (final BufferedReader reader = resource.getReader(ObjectKit.defaultIfNull(charset, Charset.UTF_8))) {
                properties.load(reader);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * Loads all configuration files with a given name into a `Properties` object.
     *
     * @param properties   The `Properties` object.
     * @param resourceName The resource name.
     * @param classLoader  The `ClassLoader`.
     * @param charset      The charset.
     * @param isOverride   If `true`, later files will override properties from earlier ones.
     */
    public static void loadAllTo(
            final Properties properties,
            final String resourceName,
            final ClassLoader classLoader,
            final java.nio.charset.Charset charset,
            final boolean isOverride) {
        if (isOverride) {
            for (final Resource resource : getResources(resourceName, classLoader)) {
                loadTo(properties, resource, charset);
            }
            return;
        }

        final Properties tmpProps = new Properties();
        for (final Resource resource : getResources(resourceName, classLoader)) {
            loadTo(tmpProps, resource, charset);
            tmpProps.forEach((name, value) -> {
                if (!properties.containsKey(name)) {
                    properties.put(name, value);
                }
            });
        }
    }

}
