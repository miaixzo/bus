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
 * Encodes and decodes the formal OpenID Connect wire representations.
 * <p>
 * Authentication Request encoding adds only OIDC parameters to the shared OAuth authorization representation, while
 * OAuth codecs retain ownership of authorization responses. OpenIdTokenResponseCodec composes the ordinary OAuth token
 * success with id_token. IdTokenCodec preserves the compact JWT assertion and typed claims; OpenIdProviderMetadataCodec
 * composes RFC 8414 metadata with OIDC Discovery increments; JwkSetCodec and UserInfoCodec own their standard
 * documents, and EndSessionRequestCodec owns RP-Initiated Logout.
 * </p>
 * <p>
 * OIDC clients and endpoints call this package after selecting the exact operation. Codecs do not fetch metadata or
 * keys, perform cryptographic verification, resolve credentials, manage state or sessions, apply consent, issue ID
 * Tokens, invoke Registry, create identities, interpret Vendor envelopes, or expose framework success/error wrappers.
 * </p>
 * <p>
 * Decoding rejects duplicate singleton parameters, malformed percent or UTF-8 sequences, invalid JSON types, ambiguous
 * audience values, invalid NumericDate values, and representations forbidden for the selected response mode. Encoding
 * retains registered claim names, case, JSON type, omission, space-delimited scope, and the standard query/form/JSON or
 * compact-JWT location. JWK Sets use {@code application/jwk-set+json}; unknown extension claims remain typed JsonValue.
 * Tokens, authorization codes, nonce values, subject claims, or logout hints are never copied into diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oidc.codec;
