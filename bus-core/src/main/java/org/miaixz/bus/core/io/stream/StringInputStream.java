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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * An {@link InputStream} implementation that reads bytes from a {@link CharSequence}. This class converts a string into
 * a byte stream using a specified character set.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringInputStream extends ByteArrayInputStream {

    /**
     * Constructs a new {@code StringInputStream} from the given character sequence and character set. The character
     * sequence is converted into bytes using the specified charset.
     *
     * @param text    The character sequence to be converted into a byte stream.
     * @param charset The character set to use for encoding the character sequence.
     */
    public StringInputStream(final CharSequence text, final java.nio.charset.Charset charset) {
        super(ByteKit.toBytes(text, charset));
    }

    /**
     * Creates a new {@code StringInputStream} from the given character sequence using UTF-8 encoding.
     *
     * @param text The character sequence to be converted into a byte stream.
     * @return A new {@code StringInputStream} instance.
     */
    public static StringInputStream of(final CharSequence text) {
        return of(text, Charset.UTF_8);
    }

    /**
     * Creates a new {@code StringInputStream} from the given character sequence and character set.
     *
     * @param text    The character sequence to be converted into a byte stream.
     * @param charset The character set to use for encoding the character sequence.
     * @return A new {@code StringInputStream} instance.
     */
    public static StringInputStream of(final CharSequence text, final java.nio.charset.Charset charset) {
        return new StringInputStream(text, charset);
    }

}
