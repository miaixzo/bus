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
package org.miaixz.bus.fabric.protocol.http.http2;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable HTTP/2 client policy applied while destinations are built.
 *
 * @param maxMultiplexStreams positive maximum logical streams retained by one destination
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2Policy(int maxMultiplexStreams) implements Policy {

    /**
     * Validates the stream limit.
     */
    public Http2Policy {
        if (maxMultiplexStreams <= 0) {
            throw new ValidateException("Max multiplex streams must be positive");
        }
    }

    /**
     * Creates an HTTP/2 policy.
     *
     * @param maxMultiplexStreams positive logical stream limit
     * @return validated policy
     */
    public static Http2Policy of(final int maxMultiplexStreams) {
        return new Http2Policy(maxMultiplexStreams);
    }

    /**
     * Writes the stream limit into its network-owned destination option.
     *
     * @param options immutable option source
     * @return option snapshot containing the stream limit
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null"))
                .with(Builder.OPTION_MAX_MULTIPLEX_STREAMS, maxMultiplexStreams);
    }

}
