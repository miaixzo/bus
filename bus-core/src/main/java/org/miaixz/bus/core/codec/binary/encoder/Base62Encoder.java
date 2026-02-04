/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.binary.encoder;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Normal;

/**
 * Encodes a byte array into a Base62 byte array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Encoder implements Encoder<byte[], byte[]> {

    /**
     * The GMP-style Base62 alphabet.
     */
    public static final byte[] GMP = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z' };

    /**
     * The inverted-style Base62 alphabet, which swaps the case of letters from the GMP style.
     */
    public static final byte[] INVERTED = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z' };

    /**
     * The GMP-style Base62 encoder.
     */
    public static Base62Encoder GMP_ENCODER = new Base62Encoder(GMP);
    /**
     * The inverted-style Base62 encoder.
     */
    public static Base62Encoder INVERTED_ENCODER = new Base62Encoder(INVERTED);

    /**
     * The alphabet used for encoding.
     */
    private final byte[] alphabet;

    /**
     * Constructs a new Base62Encoder with a custom alphabet.
     *
     * @param alphabet The alphabet to use for encoding.
     */
    public Base62Encoder(final byte[] alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * Encodes a byte array into a Base62 byte array.
     *
     * @param data The byte array to encode.
     * @return The Base62 encoded byte array.
     */
    @Override
    public byte[] encode(final byte[] data) {
        final byte[] indices = Base62Provider.convert(data, Normal._256, 62);
        return Base62Provider.translate(indices, alphabet);
    }

}
