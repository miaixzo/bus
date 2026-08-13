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
package org.miaixz.bus.auth.protocol.jwt;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable redacted JWT/JOSE key material returned by a tenant-scoped resolver.
 *
 * @param keyId     non-blank key identifier
 * @param use       exact JOSE key use
 * @param algorithm exact JOSE algorithm identifier
 * @param material  non-empty encoded public key or raw symmetric secret
 * @author Kimi Liu
 */
public record KeyMaterial(String keyId, String use, String algorithm, byte[] material) {

    /**
     * Validates metadata and takes ownership of an independent key copy.
     *
     * @throws ValidateException if metadata is blank or material is empty
     */
    public KeyMaterial {
        if (keyId == null || keyId.isBlank() || use == null || use.isBlank() || algorithm == null || algorithm.isBlank()
                || material == null || material.length == 0) {
            throw new ValidateException("JWT key material is incomplete");
        }
        material = material.clone();
    }

    /**
     * @return independent key material copy
     */
    @Override
    public byte[] material() {
        return material.clone();
    }

    /**
     * @return representation excluding all key metadata and bytes
     */
    @Override
    public String toString() {
        return "KeyMaterial[REDACTED]";
    }

}
