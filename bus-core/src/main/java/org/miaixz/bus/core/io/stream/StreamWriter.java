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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A utility class for writing data to an {@link OutputStream}. This class provides methods to write byte arrays,
 * serializable objects, and character sequences to an output stream, with an option to close the stream after writing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamWriter {

    /**
     * The underlying output stream to write to.
     */
    private final OutputStream out;
    /**
     * Flag indicating whether the output stream should be closed after write operations.
     */
    private final boolean closeAfterWrite;

    /**
     * Constructs a new {@code StreamWriter} with the specified output stream and a flag indicating whether the stream
     * should be closed after writing.
     *
     * @param out             The {@link OutputStream} to write to.
     * @param closeAfterWrite {@code true} if the output stream should be closed after writing, {@code false} otherwise.
     */
    public StreamWriter(final OutputStream out, final boolean closeAfterWrite) {
        this.out = out;
        this.closeAfterWrite = closeAfterWrite;
    }

    /**
     * Creates a new {@code StreamWriter} instance.
     *
     * @param out             The {@link OutputStream} to write to.
     * @param closeAfterWrite {@code true} if the output stream should be closed after writing, {@code false} otherwise.
     * @return A new {@code StreamWriter} instance.
     */
    public static StreamWriter of(final OutputStream out, final boolean closeAfterWrite) {
        return new StreamWriter(out, closeAfterWrite);
    }

    /**
     * Writes a byte array to the output stream.
     *
     * @param content The byte array to write.
     * @throws InternalException If an I/O error occurs.
     */
    public void write(final byte[] content) throws InternalException {
        final OutputStream out = this.out;
        try {
            out.write(content);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.closeQuietly(out);
            }
        }
    }

    /**
     * Writes multiple serializable objects to the output stream using {@link ObjectOutputStream}. Each object must
     * implement the {@link java.io.Serializable} interface.
     *
     * @param contents An array of objects to write. Null objects will be skipped.
     * @throws InternalException If an I/O error occurs during serialization.
     */
    public void writeObject(final Object... contents) throws InternalException {
        ObjectOutputStream osw = null;
        try {
            if (ArrayKit.isEmpty(contents)) {
                return;
            }

            osw = out instanceof ObjectOutputStream ? (ObjectOutputStream) out : new ObjectOutputStream(out);
            for (final Object content : contents) {
                if (content != null) {
                    osw.writeObject(content);
                }
            }
            osw.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.closeQuietly(osw);
            }
        }
    }

    /**
     * Writes multiple character sequences to the output stream, converting them to strings. The content is written
     * using the specified character set.
     *
     * @param charset  The character set to use for encoding the content.
     * @param contents An array of character sequences to write. Null sequences will be skipped. The {@code toString()}
     *                 method will be called on each sequence.
     * @throws InternalException If an I/O error occurs.
     */
    public void writeString(final Charset charset, final CharSequence... contents) throws InternalException {
        OutputStreamWriter osw = null;
        try {
            osw = IoKit.toWriter(out, charset);
            for (final CharSequence content : contents) {
                if (content != null) {
                    osw.write(Convert.toString(content, Normal.EMPTY));
                }
            }
            osw.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (closeAfterWrite) {
                IoKit.closeQuietly(osw);
            }
        }
    }

}
