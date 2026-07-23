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

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;

/**
 * Data encoder contract for one or more media types.
 *
 * @param <T> value type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface DataEncoder<T> extends Encoder<T, Payload> {

    /**
     * Encodes a value into a payload.
     *
     * @param value value to serialize according to the implementation's format
     * @return payload containing the encoded representation
     */
    Payload encode(T value);

    /**
     * Returns whether the media type is supported.
     *
     * @param media media type proposed for encoding
     * @return true when this encoder can produce that media type
     */
    boolean supports(MediaType media);

}
