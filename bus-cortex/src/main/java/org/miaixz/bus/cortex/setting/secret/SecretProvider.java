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
package org.miaixz.bus.cortex.setting.secret;

import java.util.Set;

/**
 * Read-only backend SPI bound to one connection identity. Implementations validate remote versions and expirations and
 * return independently owned complete payloads; authorization and JSON-field extraction remain in SecretManager. Shared
 * implementations and credential sources must tolerate concurrent reads.
 */
public interface SecretProvider {

    /**
     * Returns the stable provider identifier used to validate connection bindings.
     *
     * @return provider type identifier
     */
    String type();

    /**
     * Returns the immutable set of supported read modes. Advertising a capability does not grant authorization.
     *
     * @return supported backend operations
     */
    Set<Capability> capabilities();

    /**
     * Reads the complete backend payload using fresh credentials per HTTP attempt. Validates the actual version and
     * lifetime before returning; field extraction belongs to the manager.
     *
     * @param reference backend reference; field extraction is performed by the manager
     * @param context   authenticated caller identity and absolute request deadline; never populated from an untrusted
     *                  reference
     * @return caller-owned material with the actual backend version
     * @throws org.miaixz.bus.core.lang.exception.ForbiddenException if access is denied or the supplied credential
     *                                                               representation is not permitted
     * @throws org.miaixz.bus.core.lang.exception.CryptoException    if the deadline, lifecycle, backend or payload
     *                                                               checks fail; inspect the exception type;
     *                                                               diagnostics are deliberately sanitized
     */
    SecretMaterial read(SecretReference reference, SecretContext context);

    /**
     * Capabilities advertised and enforced by each backend.
     */
    public enum Capability {
        /**
         * Reads the backend current-version alias.
         */
        READ_CURRENT,

        /**
         * Reads an explicitly selected immutable backend version.
         */
        READ_VERSION
    }

}
