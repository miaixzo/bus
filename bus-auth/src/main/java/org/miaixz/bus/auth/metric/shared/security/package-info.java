/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Provides deterministic, tenant-isolated replay-state key derivation shared by authentication protocols. Keys bind the
 * tenant, protocol, state kind, and opaque identifier through an unambiguous domain-separated encoding before hashing.
 * Returned keys expose only a lowercase digest and never contain the original identifier, token, code, assertion,
 * authenticator, secret, or tenant value. This package does not validate protocol claims, store replay state, select
 * cryptographic algorithms, or retain input values.
 * <p>
 * <strong>Bus dependencies:</strong> {@code org.miaixz.bus.crypto.Builder.sha256()} supplies the fixed SHA-256
 * implementation and {@code org.miaixz.bus.crypto.builtin.digest.Digester} performs the digest; bus-core text and
 * hexadecimal facilities provide stable UTF-8 and lowercase output handling without introducing a security facade.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.metric.shared.security;
