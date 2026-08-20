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
 * Encodes and decodes the formal OAuth 2.x wire representations.
 * <p>
 * Authorization request and response codecs preserve query or form response parameters; token request and response
 * codecs preserve grant-specific form members and the JSON token response; the remaining codecs own RFC 8414 metadata,
 * RFC 8628 device authorization, RFC 7662 introspection, and RFC 7009 revocation representations. The codecs map only
 * between protocol models and shared query, form, header, and injected JSON primitives.
 * </p>
 * <p>
 * OAuth clients and endpoints call this package after transport and client-authentication boundaries have selected the
 * correct operation. A codec does not perform HTTP calls, resolve credentials, issue or persist tokens, validate local
 * policy, invoke Registry, create identity records, interpret Vendor envelopes, or introduce a generic success/error
 * wrapper. OIDC adds its own parameters through the OIDC codec package rather than extending OAuth JSON arbitrarily.
 * </p>
 * <p>
 * Decoding rejects duplicate singleton parameters, malformed percent or UTF-8 sequences, invalid registered enum
 * values, non-integral lifetimes, mismatched grant members, and fields forbidden for the selected operation. Encoding
 * retains standard field names, case, location, ordering rules, space-delimited scope, and omission semantics; it does
 * not infer missing {@code token_type}, expiry, scope, grant, or error values. Authorization codes, refresh tokens,
 * access tokens, client assertions, and code verifiers must not be copied into errors or diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2.codec;
