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
package org.miaixz.bus.image.galaxy.io;

/**
 * Represents options for image encoding, controlling aspects like group length and undefined length encoding.
 *
 * @author Kimi Liu
 * @since Java 21+
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
