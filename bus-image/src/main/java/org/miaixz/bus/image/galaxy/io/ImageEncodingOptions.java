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
package org.miaixz.bus.image.galaxy.io;

/**
 * Represents options for image encoding, controlling aspects like group length and undefined length encoding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageEncodingOptions {

    /**
     * Default image encoding options: no group length, undefined sequence length, no undefined empty sequence length,
     * undefined item length, no undefined empty item length.
     */
    public static final ImageEncodingOptions DEFAULT = new ImageEncodingOptions(false, true, false, true, false);

    /**
     * Indicates whether group length should be encoded.
     */
    public final boolean groupLength;
    /**
     * Indicates whether sequence lengths should be encoded as undefined.
     */
    public final boolean undefSequenceLength;
    /**
     * Indicates whether empty sequence lengths should be encoded as undefined.
     */
    public final boolean undefEmptySequenceLength;
    /**
     * Indicates whether item lengths should be encoded as undefined.
     */
    public final boolean undefItemLength;
    /**
     * Indicates whether empty item lengths should be encoded as undefined.
     */
    public final boolean undefEmptyItemLength;

    /**
     * Constructs an {@code ImageEncodingOptions} instance with the specified encoding preferences.
     *
     * @param groupLength          {@code true} to include group length, {@code false} otherwise.
     * @param undefSeqLength       {@code true} to encode sequence lengths as undefined, {@code false} otherwise.
     * @param undefEmptySeqLength  {@code true} to encode empty sequence lengths as undefined, {@code false} otherwise.
     * @param undefItemLength      {@code true} to encode item lengths as undefined, {@code false} otherwise.
     * @param undefEmptyItemLength {@code true} to encode empty item lengths as undefined, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code undefEmptySeqLength} is {@code true} but {@code undefSeqLength} is
     *                                  {@code false}, or if {@code undefEmptyItemLength} is {@code true} but
     *                                  {@code undefItemLength} is {@code false}.
     */
    public ImageEncodingOptions(boolean groupLength, boolean undefSeqLength, boolean undefEmptySeqLength,
            boolean undefItemLength, boolean undefEmptyItemLength) {
        if (undefEmptySeqLength && !undefSeqLength)
            throw new IllegalArgumentException("undefEmptySeqLength && !undefSeqLength");
        if (undefEmptyItemLength && !undefItemLength)
            throw new IllegalArgumentException("undefEmptyItemLength && !undefItemLength");
        this.groupLength = groupLength;
        this.undefSequenceLength = undefSeqLength;
        this.undefEmptySequenceLength = undefEmptySeqLength;
        this.undefItemLength = undefItemLength;
        this.undefEmptyItemLength = undefEmptyItemLength;
    }

}
