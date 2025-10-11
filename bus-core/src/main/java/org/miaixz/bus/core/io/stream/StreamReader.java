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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A utility class for reading data from an {@link InputStream}. This class provides various methods to read bytes,
 * convert to output streams, and deserialize objects, with options for closing the stream after reading.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamReader {

    /**
     * The underlying input stream to read from.
     */
    private final InputStream in;
    /**
     * Flag indicating whether the input stream should be closed after reading operations.
     */
    private final boolean closeAfterRead;

    /**
     * Constructs a new {@code StreamReader} with the specified input stream and a flag indicating whether the stream
     * should be closed after reading.
     *
     * @param in             The {@link InputStream} to read from.
     * @param closeAfterRead {@code true} if the input stream should be closed after reading, {@code false} otherwise.
     */
    public StreamReader(final InputStream in, final boolean closeAfterRead) {
        this.in = in;
        this.closeAfterRead = closeAfterRead;
    }

    /**
     * Creates a new {@code StreamReader} instance.
     *
     * @param in             The {@link InputStream} to read from.
     * @param closeAfterRead {@code true} if the input stream should be closed after reading, {@code false} otherwise.
     * @return A new {@code StreamReader} instance.
     */
    public static StreamReader of(final InputStream in, final boolean closeAfterRead) {
        return new StreamReader(in, closeAfterRead);
    }

    /**
     * Reads all bytes from the input stream.
     *
     * @return An array of bytes containing the entire content of the stream.
     * @throws InternalException If an I/O error occurs.
     */
    public byte[] readBytes() throws InternalException {
        return readBytes(-1);
    }

    /**
     * Reads a specified number of bytes from the input stream.
     *
     * @param length The maximum number of bytes to read. If less than 0, all available bytes will be read.
     * @return An array of bytes containing the read content.
     * @throws InternalException If an I/O error occurs.
     */
    public byte[] readBytes(final int length) throws InternalException {
        final InputStream in = this.in;
        if (null == in || length == 0) {
            return new byte[0];
        }
        return read(length).toByteArrayZeroCopyIfPossible();
    }

    /**
     * Reads the entire content of the input stream into a {@link FastByteArrayOutputStream}. The input stream may be
     * closed after reading, depending on the {@code closeAfterRead} flag.
     *
     * @return A {@link FastByteArrayOutputStream} containing the content of the stream.
     * @throws InternalException If an I/O error occurs.
     */
    public FastByteArrayOutputStream read() throws InternalException {
        return read(-1);
    }

    /**
     * Reads a limited amount of content from the input stream into a {@link FastByteArrayOutputStream}. The input
     * stream may be closed after reading, depending on the {@code closeAfterRead} flag.
     *
     * @param limit The maximum number of bytes to copy. A value of -1 indicates no limit.
     * @return A {@link FastByteArrayOutputStream} containing the read content.
     * @throws InternalException If an I/O error occurs.
     */
    public FastByteArrayOutputStream read(final int limit) throws InternalException {
        final InputStream in = this.in;
        final FastByteArrayOutputStream out = FastByteArrayOutputStream.of(in, limit);
        try {
            IoKit.copyNio(in, out, Normal._8192, limit, null);
        } finally {
            if (closeAfterRead) {
                IoKit.closeQuietly(in);
            }
        }
        return out;
    }

    /**
     * Reads content from the input stream until a given token satisfies the provided {@link Predicate}.
     *
     * @param predicate The predicate to test each read byte. Reading stops when the predicate returns {@code true}.
     * @return A {@link FastByteArrayOutputStream} containing the content read until the predicate was satisfied.
     * @throws InternalException If an I/O error occurs.
     */
    public FastByteArrayOutputStream readTo(final Predicate<Integer> predicate) throws InternalException {
        final InputStream in = this.in;
        final FastByteArrayOutputStream out = FastByteArrayOutputStream.of(in, -1);
        int read;
        try {
            while ((read = in.read()) > 0) {
                if (null != predicate && predicate.test(read)) {
                    break;
                }
                out.write(read);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return out;
    }

    /**
     * Reads an object from the input stream by deserialization.
     *
     * <p>
     * WARNING: This method does not perform deserialization safety checks and may be vulnerable to deserialization
     * attacks!
     *
     * <p>
     * This method uses a whitelist/blacklist approach via {@link ValidateObjectInputStream} to filter classes, helping
     * to mitigate deserialization vulnerabilities. You can configure allowed or forbidden classes by constructing a
     * {@link ValidateObjectInputStream} and calling {@link ValidateObjectInputStream#accept(Class[])} or
     * {@link ValidateObjectInputStream#refuse(Class[])} methods.
     *
     * @param <T>           The type of the object to be read.
     * @param acceptClasses An array of classes that are explicitly allowed for deserialization.
     * @return The deserialized object.
     * @throws InternalException If an I/O error occurs during deserialization.
     * @throws InternalException If the class of a serialized object cannot be found.
     */
    public <T> T readObject(final Class<?>... acceptClasses) throws InternalException {
        final InputStream in = this.in;
        if (null == in) {
            return null;
        }

        // Convert or create ValidateObjectInputStream
        final ValidateObjectInputStream validateIn;
        if (in instanceof ValidateObjectInputStream) {
            validateIn = (ValidateObjectInputStream) in;
            validateIn.accept(acceptClasses);
        } else {
            try {
                validateIn = new ValidateObjectInputStream(in, acceptClasses);
            } catch (final IOException e) {
                throw new InternalException(e);
            }
        }

        // Read the object
        try {
            return (T) validateIn.readObject();
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final ClassNotFoundException e) {
            throw new InternalException(e);
        }
    }

}
