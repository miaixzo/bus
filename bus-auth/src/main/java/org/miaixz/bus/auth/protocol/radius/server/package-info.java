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
 * Implements the RADIUS Access and Accounting Provider direction.
 * <p>
 * AccessService and AccountingService validate a decoded request, resolve its client through the externally supplied
 * RadiusRequestHandler, apply version-specific wire security, invoke the appropriate handler operation, validate the
 * returned standard packet, preserve Proxy-State, and correlate the response. RadiusAuthenticator owns only legacy
 * Request/Response Authenticator and Message-Authenticator processing plus the non-cryptographic RFC 9765 Token path.
 * RadiusProviderProfile and RadiusProviderSettings declare supported versions, EAP, limits, and security requirements.
 * </p>
 * <p>
 * The framework owns packet validation and orchestration; the external project owns trusted transport adaptation,
 * client resolution, authentication decisions, accounting processing, persistence, and permissions. Services consume
 * typed packets, codecs, SecretResolver, SecurityBaseline, Context, and Budget. They perform no network I/O, direct
 * Registry lookup, Source/client behavior, Vendor login, Dynamic Authorization, or HTTP/JSON error mapping.
 * </p>
 * <p>
 * Legacy clients require a shared-secret reference and validate applicable Authenticators and Type 80 in constant time;
 * EAP always requires valid Message-Authenticator. RADIUS/1.1 clients have no shared secret and all Type 80 attributes
 * are removed without MD5 or HMAC. Accounting rejects EAP and Type 80. Invalid packets, unknown clients, failed wire
 * authentication, and malformed handler responses are silently dropped; business denial is a standard Access-Reject.
 * Every secret lease is acquired and closed within one synchronous validation or signing continuation.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.radius.server;
