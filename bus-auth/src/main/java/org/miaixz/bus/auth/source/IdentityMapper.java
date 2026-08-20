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
package org.miaixz.bus.auth.source;

/**
 * Maps an already validated protocol or private adapter value to a protocol-neutral external identity.
 * <p>
 * Implementations are pure mapping functions scoped to the adapter that owns the input type. They must not perform
 * network access, Registry calls, credential resolution, lenient global parsing, or account linking.
 * </p>
 *
 * @param <I> validated adapter-private or standard protocol input type
 * @author Kimi Liu
 */
@FunctionalInterface
public interface IdentityMapper<I> {

    /**
     * Maps a validated input to the identity namespace of the selected Source.
     *
     * @param sourceId registered Source identifier
     * @param input    validated input owned by the calling adapter
     * @return verified protocol-neutral external identity
     * @throws IllegalArgumentException if required identity data is absent or invalid
     */
    ExternalIdentity map(String sourceId, I input);

}
