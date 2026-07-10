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
package org.miaixz.bus.fabric.cache;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Protocol-neutral cache entry made of metadata and payload.
 *
 * @param metadata protocol metadata
 * @param payload  body payload
 * @author Kimi Liu
 * @since Java 21+
 */
public record CacheEntry(Headers metadata, Payload payload) {

    /**
     * Creates an entry.
     *
     * @param metadata protocol metadata
     * @param payload  body payload
     */
    public CacheEntry {
        if (metadata == null) {
            throw new ValidateException("Cache metadata must not be null");
        }
        if (payload == null) {
            throw new ValidateException("Cache payload must not be null");
        }
    }

    /**
     * Creates a cache entry.
     *
     * @param metadata protocol metadata
     * @param payload  body payload
     * @return entry
     */
    public static CacheEntry of(final Headers metadata, final Payload payload) {
        return new CacheEntry(metadata, payload);
    }

}
