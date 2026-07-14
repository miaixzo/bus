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
package org.miaixz.bus.fabric.codec;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;

/**
 * Bidirectional data codec contract for one media type.
 *
 * @param <T> value type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DataCodec<T> {

    /**
     * Composes an encoder and decoder into one codec.
     *
     * @param encoder encoder
     * @param decoder decoder
     * @param media   media type
     * @param <T>     value type
     * @return data codec
     */
    static <T> DataCodec<T> of(
            final DataEncoder<? super T> encoder,
            final DataDecoder<? extends T> decoder,
            final MediaType media) {
        Assert.notNull(encoder, () -> new ValidateException("Data encoder must not be null"));
        Assert.notNull(decoder, () -> new ValidateException("Data decoder must not be null"));
        Assert.notNull(media, () -> new ValidateException("Data codec media type must not be null"));
        return new DataCodec<>() {

            @Override
            public Payload encode(final T value) {
                return encoder.encode(value);
            }

            @Override
            public T decode(final Payload payload) {
                return decoder.decode(payload);
            }

            @Override
            public MediaType media() {
                return media;
            }

        };
    }

    /**
     * Encodes a value into a payload.
     *
     * @param value value
     * @return payload
     */
    Payload encode(T value);

    /**
     * Decodes a payload into a value.
     *
     * @param payload payload
     * @return decoded value
     */
    T decode(Payload payload);

    /**
     * Returns this codec media type.
     *
     * @return media type
     */
    MediaType media();

}
