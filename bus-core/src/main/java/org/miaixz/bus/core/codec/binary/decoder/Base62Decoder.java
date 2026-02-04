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
package org.miaixz.bus.core.codec.binary.decoder;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base62Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Normal;

/**
 * Decodes a Base62 encoded byte array into a byte array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base62Decoder implements Decoder<byte[], byte[]> {

    /**
     * The GMP-style Base62 decoder.
     */
    public static Base62Decoder GMP_DECODER = new Base62Decoder(Base62Encoder.GMP);

    /**
     * The inverted-style Base62 decoder, which swaps the case of letters from the GMP style.
     */
    public static Base62Decoder INVERTED_DECODER = new Base62Decoder(Base62Encoder.INVERTED);

    /**
     * A lookup table for decoding Base62 characters.
     */
    private final byte[] lookupTable;

    /**
     * Constructs a new Base62Decoder with a custom alphabet.
     *
     * @param alphabet The alphabet to use for decoding.
     */
    public Base62Decoder(final byte[] alphabet) {
        lookupTable = new byte['z' + 1];
        for (int i = 0; i < alphabet.length; i++) {
            lookupTable[alphabet[i]] = (byte) i;
        }
    }

    /**
     * Decodes a Base62 encoded byte array.
     *
     * @param encoded The Base62 encoded data.
     * @return The decoded byte array.
     */
    @Override
    public byte[] decode(final byte[] encoded) {
        final byte[] prepared = Base62Provider.translate(encoded, lookupTable);
        return Base62Provider.convert(prepared, 62, Normal._256);
    }

}
