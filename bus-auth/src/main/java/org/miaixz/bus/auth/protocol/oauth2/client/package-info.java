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
 * Implements the OAuth 2.x client and generic Source direction.
 * <p>
 * {@link org.miaixz.bus.auth.protocol.oauth2.client.OAuth2Client} aggregates authorization, token, introspection,
 * revocation, device authorization, and metadata operations selected by
 * {@link org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientOptions}. Operation-specific clients preserve their
 * corresponding standard request and response types, and
 * {@link org.miaixz.bus.auth.protocol.oauth2.client.OAuth2ClientScheme} publishes only the capabilities supported by
 * the generic client registration.
 * </p>
 * <p>
 * This package composes OAuth models and codecs, client authentication, PKCE or DPoP when enabled, resolvers, stores,
 * SecurityBaseline, and Fabric HTTP transport. It does not implement an authorization server, issue tokens, create a
 * local identity, select a Vendor, discover undeclared endpoints, or treat an arbitrary profile API as UserInfo.
 * </p>
 * <p>
 * Each operation binds the exact client, endpoint, redirect URI, scope, grant, authentication method, Context, and one
 * Budget. Redirect state, PKCE verifier, device code, and nonce lifecycles use isolated atomic storage; secrets and
 * encoded forms remain operation-scoped, endpoints pass Fabric address policy, and responses use strict standard
 * decoders without post-failure vendor repair.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oauth2.client;
