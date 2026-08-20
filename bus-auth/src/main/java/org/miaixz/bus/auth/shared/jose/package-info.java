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
/**
 * Defines JOSE header, JWK, JWS, and JWE orchestration shared by formal protocols.
 * <p>
 * {@link org.miaixz.bus.auth.shared.jose.JoseHeader} and {@link org.miaixz.bus.auth.shared.jose.JwaAlgorithm} retain
 * registered wire values; {@link org.miaixz.bus.auth.shared.jose.Jwk}, {@link org.miaixz.bus.auth.shared.jose.JwkSet},
 * and {@link org.miaixz.bus.auth.shared.jose.JwkSelector} model and select exact key material; and
 * {@link org.miaixz.bus.auth.shared.jose.JwsService} and {@link org.miaixz.bus.auth.shared.jose.JweService} compose
 * compact signing, verification, encryption, and decryption operations.
 * </p>
 * <p>
 * JWT, OpenID Connect, OAuth extensions, SAML integrations, and Vendor adapters call these typed services. JOSE
 * delegates cryptographic implementation to bus-crypto and obtains key material through resolvers; it does not invoke
 * JCA primitives directly, load keys globally, select a protocol role, access Registry, or perform remote key lookup.
 * </p>
 * <p>
 * Callers provide an explicit algorithm allow-list, intended use, key identifier policy, critical-header policy, and
 * bounded payload. Selection rejects ambiguity, algorithm or key-type confusion, {@code none} where a protected result
 * is required, unknown critical headers, weak material, and invalid authentication tags. Private keys, content keys,
 * plaintext, compact tokens, and complete protected headers must not enter logs or failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.shared.jose;
