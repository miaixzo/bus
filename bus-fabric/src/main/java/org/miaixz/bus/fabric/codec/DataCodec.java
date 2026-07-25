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
 * @param <T> application value encoded and decoded by this codec
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DataCodec<T> {

    /**
     * Composes an encoder and decoder into one codec.
     *
     * @param encoder contravariant encoder used for every value
     * @param decoder covariant decoder used for every payload
     * @param media   media type advertised by the composed codec
     * @param <T>     application value type exposed by the composition
     * @return codec delegating directly to the supplied collaborators
     * @throws ValidateException if the encoder, decoder, or media type is {@code null}
     */
    static <T> DataCodec<T> of(
            final DataEncoder<? super T> encoder,
            final DataDecoder<? extends T> decoder,
            final MediaType media) {
        Assert.notNull(encoder, () -> new ValidateException("Data encoder must not be null"));
        Assert.notNull(decoder, () -> new ValidateException("Data decoder must not be null"));
        Assert.notNull(media, () -> new ValidateException("Data codec media type must not be null"));
        return new DataCodec<>() {

            /**
             * Encodes a value with the supplied encoder.
             *
             * @param value application value passed unchanged to the supplied encoder
             * @return payload produced by the supplied encoder
             */
            @Override
            public Payload encode(final T value) {
                return encoder.encode(value);
            }

            /**
             * Decodes a payload with the supplied decoder.
             *
             * @param payload encoded payload passed unchanged to the supplied decoder
             * @return application value produced by the supplied decoder
             */
            @Override
            public T decode(final Payload payload) {
                return decoder.decode(payload);
            }

            /**
             * Returns the codec media type.
             *
             * @return media type captured when this codec was composed
             */
            @Override
            public MediaType media() {
                return media;
            }

        };
    }

    /**
     * Encodes a value into a payload.
     *
     * @param value application value to encode
     * @return encoded payload
     */
    Payload encode(T value);

    /**
     * Decodes a payload into a value.
     *
     * @param payload encoded payload to decode
     * @return decoded application value
     */
    T decode(Payload payload);

    /**
     * Returns this codec media type.
     *
     * @return media type represented by this codec
     */
    MediaType media();

}
