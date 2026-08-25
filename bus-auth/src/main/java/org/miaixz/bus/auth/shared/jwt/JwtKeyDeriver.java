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
package org.miaixz.bus.auth.shared.jwt;

import javax.crypto.SecretKey;

import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;

/**
 * Derives one deterministic JWT operation key from caller-owned String key material.
 * <p>
 * Implementations own their exact versioned encoding and derivation contract. The same algorithm and String must always
 * produce the same key on every runtime and cluster node. A derivation version must never change its output after
 * publication.
 * </p>
 *
 * @author Kimi Liu
 */
public interface JwtKeyDeriver {

    /**
     * Derives a key for one explicitly selected JWT algorithm.
     *
     * @param algorithm exact trusted JWT algorithm
     * @param secret    non-empty caller-owned String key material
     * @return deterministic secret key suitable for the selected algorithm
     */
    SecretKey derive(JwaAlgorithm algorithm, String secret);

    /**
     * Returns the immutable derivation profile identifier.
     *
     * @return stable derivation version
     */
    String version();

}
