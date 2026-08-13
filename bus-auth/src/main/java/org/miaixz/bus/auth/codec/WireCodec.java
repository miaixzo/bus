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
package org.miaixz.bus.auth.codec;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;

/**
 * Defines deterministic bidirectional conversion between an authentication value and its internal wire representation.
 * Implementations must not select transports, construct protocol responses, or expose vendor-specific behavior. Mutable
 * inputs and outputs must be copied according to the implementation's ownership contract.
 *
 * @param <I> authentication value type
 * @param <O> encoded wire representation type
 * @author Kimi Liu
 */
public interface WireCodec<I, O> extends Encoder<I, O>, Decoder<O, I> {

    /**
     * Encodes an authentication value into its deterministic wire representation.
     *
     * @param value authentication value to encode
     * @return encoded wire representation
     */
    @Override
    O encode(I value);

    /**
     * Decodes and validates a complete wire representation.
     *
     * @param encoded complete encoded representation
     * @return decoded authentication value
     */
    @Override
    I decode(O encoded);

}
