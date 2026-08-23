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
 * Declares Okta OpenID Connect login and service-app Management API variants.
 * <p>
 * OktaManifest fixes {@code okta/default} to the issuer
 * {@code https://{instance}.okta.com/oauth2/{authorizationServerId}} and derives authorization, token, UserInfo,
 * revocation, Discovery, and JWK Set endpoints from that same authority. It publishes redirect Source authentication
 * and the standard OpenID Connect authentication, token, revocation, Discovery, JWK Set, and UserInfo capabilities,
 * requires CLIENT_SECRET_BASIC, accepts only RS256 ID Tokens, prohibits PKCE, and declares no platform wire deviation.
 * </p>
 * <p>
 * OktaOptions contains only routing, Client ID, Client Secret reference, exact registered callback, unique standard
 * scopes, one canonical Okta organization label, and one bounded authorization-server identifier. Explicit scopes must
 * include {@code openid}; complete issuer or endpoint URLs, arbitrary hosts, algorithms, response models, and
 * executable Provider state cannot be supplied through options.
 * </p>
 * <p>
 * This exported package serves Source configuration and Realm management. Invocation must enter a Provider obtained
 * from Roster and proceed through the non-exported adapter. A completed identity accepts only the cryptographically
 * verified ID Token {@code sub}, after UserInfo subject equality, as Identity subject. Client secrets, state, nonce,
 * callback codes, tokens, JWK material, claims, and upstream documents must not enter options diagnostics, Context,
 * logs, or public failure details.
 * </p>
 * <p>
 * {@code okta/management} is an independent HTTPS Variant using a referenced RSA private key and private_key_jwt at the
 * organization token endpoint. Its fixed read scopes expose describe, snapshot, and retrieve for visible users, groups,
 * administrator roles, group membership, and role assignments. Coverage is UNKNOWN because administrator-role scope and
 * service-app grants control the visible projection; no changes capability is implemented.
 * </p>
 * <p>
 * An Okta browser login Session or OIDC access token is never reused as a Management Token. Link continuations and
 * opaque Cursors are validated against the configured organization, while equal role resources may reappear on later
 * bounded pages without tenant-wide de-duplication state. External projects invoke Dispatcher and own synchronization,
 * checkpointing, and persistence.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.okta;
