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
 * Access token issuance, HMAC-SHA256 validation and HTTP header resolution backed by CacheX.
 * <p>
 * {@code AccessToken} is the decoded token payload: RBAC role (e.g. ADMIN, PROVIDER, CONSUMER), namespace scope,
 * subject identifier (service name or user ID) and Unix-epoch-millisecond expiry timestamp. {@code AccessTokenStore}
 * issues new signed tokens via {@code issue(subject, role, namespace)}: it builds a colon-delimited payload string,
 * computes an HMAC-SHA256 signature using bus-crypto, persists the token metadata to CacheX with a TTL matching the
 * token lifetime, and returns the hex signature as the token string. Validation re-derives the signature from the
 * cached metadata and compares it to the incoming token; expired entries are evicted on demand.
 * {@code AccessTokenResolver} extracts a bearer token from an HTTP {@code Authorization} header and delegates to
 * {@code AccessTokenStore} for signature verification and decoding.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
package org.miaixz.bus.cortex.guard.token;
