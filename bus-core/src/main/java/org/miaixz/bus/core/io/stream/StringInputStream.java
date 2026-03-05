/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
