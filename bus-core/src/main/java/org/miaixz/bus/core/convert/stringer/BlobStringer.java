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
package org.miaixz.bus.core.convert.stringer;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ConvertException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Converts a {@link Blob} to a String.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BlobStringer implements Function<Object, String> {

    /**
     * Constructs a new BlobStringer. Utility class constructor for static access.
     */
    private BlobStringer() {
    }

    /**
     * Singleton instance.
     */
    public static final BlobStringer INSTANCE = new BlobStringer();

    /**
     * Converts a {@link Blob} object to a String.
     *
     * @param blob The {@link Blob} to be converted.
     * @return The String representation of the Blob.
     * @throws ConvertException if a {@link SQLException} occurs.
     */
    private static String toString(final java.sql.Blob blob) {
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            return IoKit.read(in, Charset.UTF_8);
        } catch (final SQLException e) {
            throw new ConvertException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Apply method.
     *
     * @return the String value
     */
    @Override
    public String apply(final Object o) {
        return toString((Blob) o);
    }

}
