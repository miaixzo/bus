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
package org.miaixz.bus.core.io.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Resource interface definition.
 * <p>
 * A resource is a general term for data representation. Any data can be encapsulated as a resource, and its content can
 * then be read.
 *
 * <p>
 * Resources can be files, URLs, files in the ClassPath, or files within jar (zip) packages.
 *
 * <p>
 * The purpose of providing a resource interface is to allow a single method to accept any type of data for processing,
 * eliminating the need to write multiple overloaded methods for {@code File}, {@code InputStream}, etc., and also
 * providing better extensibility.
 *
 * <p>
 * Usage is very simple. For example, if we need to read an XML from the classpath, we don't need to care whether the
 * file is in a directory or a jar:
 * 
 * <pre>
 * 
 * Resource resource = new ClassPathResource("test.xml");
 * String xmlStr = resource.readString();
 * </pre>
 * <p>
 * Similarly, we can implement the {@code Resource} interface ourselves to read data from any location according to
 * business needs, such as from a database.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Resource {

    /**
     * Retrieves the name of the resource. For file resources, this is typically the file name.
     *
     * @return The name of the resource.
     */
    String getName();

    /**
     * Retrieves the resolved {@link URL} for this resource. Returns {@code null} if no corresponding URL exists.
     *
     * @return The resolved {@link URL}, or {@code null} if not applicable.
     */
    URL getUrl();

    /**
     * Retrieves the size of the resource.
     *
     * @return The size of the resource in bytes.
     */
    long size();

    /**
     * Retrieves an {@link InputStream} for reading the resource content.
     *
     * @return An {@link InputStream} to the resource.
     */
    InputStream getStream();

    /**
     * Checks if the resource has been modified. This is typically used for file-based resources to check if the
     * underlying file has changed.
     *
     * @return {@code true} if the resource has been modified, {@code false} otherwise.
     */
    default boolean isModified() {
        return false;
    }

    /**
     * Writes the content of this resource to the given output stream. The output stream is not closed, but the
     * resource's input stream is closed.
     *
     * @param out The {@link OutputStream} to write to.
     * @throws InternalException If an I/O error occurs during the write operation.
     */
    default void writeTo(final OutputStream out) throws InternalException {
        try (final InputStream in = getStream()) {
            IoKit.copy(in, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Retrieves a {@link BufferedReader} for reading the resource content with the specified character set.
     *
     * @param charset The {@link java.nio.charset.Charset} to use for reading.
     * @return A {@link BufferedReader} for the resource.
     */
    default BufferedReader getReader(final java.nio.charset.Charset charset) {
        return IoKit.toReader(getStream(), charset);
    }

    /**
     * Reads the content of the resource as a string using the specified character set. The stream is closed after
     * reading, but this does not affect subsequent reads.
     *
     * @param charset The {@link java.nio.charset.Charset} to use for reading.
     * @return The content of the resource as a string.
     * @throws InternalException If an I/O error occurs during the read operation.
     */
    default String readString(final java.nio.charset.Charset charset) throws InternalException {
        return IoKit.read(getReader(charset));
    }

    /**
     * Reads the content of the resource as a string using UTF-8 encoding. The stream is closed after reading, but this
     * does not affect subsequent reads.
     *
     * @return The content of the resource as a string.
     * @throws InternalException If an I/O error occurs during the read operation.
     */
    default String readString() throws InternalException {
        return readString(Charset.UTF_8);
    }

    /**
     * Reads the content of the resource as a byte array. The stream is closed after reading, but this does not affect
     * subsequent reads.
     *
     * @return The content of the resource as a byte array.
     * @throws InternalException If an I/O error occurs during the read operation.
     */
    default byte[] readBytes() throws InternalException {
        return IoKit.readBytes(getStream());
    }

}
