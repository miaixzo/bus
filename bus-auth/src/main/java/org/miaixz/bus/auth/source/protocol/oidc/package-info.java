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
 * Defines the standard OpenID Connect Core, Discovery, UserInfo, and RP-Initiated Logout models.
 * <p>
 * AuthenticationRequest extends the OAuth authorization request with OIDC parameters, while authorization responses
 * reuse the OAuth authorization-response contract. IdToken and IdTokenClaims represent the signed identity assertion;
 * OpenIdProviderMetadata represents Discovery; UserInfoRequest and UserInfoResponse represent the protected claims
 * operation; EndSessionRequest represents logout. Prompt, Display, SubjectType, and ClaimType retain their registered
 * OpenID Connect wire values.
 * </p>
 * <p>
 * OIDC client, server, and codec packages consume these immutable values and compose the OAuth 2.x authorization and
 * token models instead of defining duplicate grants or authorization responses. OpenIdTokenResponse composes the
 * ordinary OAuth token success with its required ID Token. JOSE and JWT packages own cryptographic and token
 * primitives. Vendor integrations may use these public models only when their declared protocol is OIDC. This package
 * does not contain transport, storage, Roster, identity linking, OpenID Provider execution, or platform fields.
 * </p>
 * <p>
 * Models preserve standard claim names and JSON types, including NumericDate seconds and string-or-array audience.
 * Unknown extension claims remain typed JsonValue members and are never flattened into a framework map. No model
 * serializes Context, Timeout, Outcome, Bus errors, exceptions, Source options, or Vendor DTOs. Logout intentionally
 * has no invented EndSessionResponse, and OIDC-specific nonce, ID Token, UserInfo, Discovery, and session semantics are
 * never attributed to plain OAuth 2.x.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.protocol.oidc;
