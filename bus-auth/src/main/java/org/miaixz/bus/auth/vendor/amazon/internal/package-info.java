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
 * Implements the non-exported Login with Amazon OAuth 2.0 and identity adapter.
 * <p>
 * AmazonSourceAdapter uses RedirectManager for one-time state and optional S256 verifier storage, standard OAuth
 * authorization request and response codecs, and standard token request and response codecs for authorization-code and
 * refresh-token grants. For Source authentication it then calls Amazon token-info with the registered query parameter,
 * verifies the token audience, and calls the customer-profile endpoint with Bearer authentication.
 * </p>
 * <p>
 * Standard authorization and token capabilities retain their formal OAuth models. The token-info and profile responses
 * remain private JSON values used only by the Source authentication chain. The adapter may compose SecretResolver,
 * JsonProvider, SecurityBaseline, Fabric, and shared codecs. It does not publish RFC 7662 introspection, OIDC UserInfo,
 * endpoint discovery, revoke, independent refresh, Amazon response DTOs, or a fallback that omits configured PKCE.
 * </p>
 * <p>
 * Callback target, state, grant members, redirect URI, scope, client authentication, optional code verifier, and Budget
 * are exact. Token success must satisfy standard Bearer and lifetime rules. Token-info must be bounded JSON with an
 * audience equal to the configured client ID; profile must be bounded JSON with non-blank {@code user_id}. Only that
 * value becomes ExternalIdentity.subject, while name, email, and postal code remain optional attributes. Client secret,
 * code, verifier, access and refresh tokens, query, profile, and response bodies never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.amazon.internal;
