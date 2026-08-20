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
 * Declares the public Okta OpenID Connect Vendor definition and externally loaded settings.
 * <p>
 * OktaDefinition fixes {@code okta/default} to the issuer
 * {@code https://{instance}.okta.com/oauth2/{authorizationServerId}} and derives authorization, token, UserInfo,
 * revocation, Discovery, and JWK Set endpoints from that same authority. It publishes redirect Source authentication
 * and the standard OpenID Connect authentication, token, revocation, Discovery, JWK Set, and UserInfo capabilities,
 * requires CLIENT_SECRET_BASIC, accepts only RS256 ID Tokens, prohibits PKCE, and declares no platform wire deviation.
 * </p>
 * <p>
 * OktaSourceSettings contains only routing, Client ID, Client Secret reference, exact registered callback, unique
 * standard scopes, one canonical Okta organization label, and one bounded authorization-server identifier. Explicit
 * scopes must include {@code openid}; complete issuer or endpoint URLs, arbitrary hosts, algorithms, response models,
 * and executable Provider state cannot be supplied through settings.
 * </p>
 * <p>
 * This exported package serves external registration and management. Invocation must enter a Provider obtained from
 * Registry and proceed through the non-exported adapter. A completed identity accepts only the cryptographically
 * verified ID Token {@code sub}, after UserInfo subject equality, as ExternalIdentity subject. Client secrets, state,
 * nonce, callback codes, tokens, JWK material, claims, and upstream documents must not enter settings diagnostics,
 * Context, logs, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.okta;
