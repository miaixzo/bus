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
 * Implements the OpenID Provider direction.
 * <p>
 * AuthenticationService processes a validated Authentication Request and produces the OIDC authorization result.
 * Discovery, JWK Set, UserInfo, and end-session services own their corresponding standard operations, while endpoint
 * types enforce each HTTP representation and OpenIdErrorMapper selects the protocol-defined error form.
 * {@link org.miaixz.bus.auth.protocol.oidc.server.OpenIdProviderProfile} composes the implemented OIDC capabilities
 * with the OAuth token-side capabilities declared by its settings.
 * </p>
 * <p>
 * This package consumes OIDC and OAuth models and codecs, verified client and subject context, consent and claim ports,
 * ID Token issuance, JOSE/JWT signing, session state, SecurityBaseline, and Fabric transport. It does not implement
 * client registration, project persistence, end-user authentication, permission checks, Vendor behavior, or Registry
 * lookup, and it never serializes Outcome, Context, Timeout, exceptions, or Bus errors as an OIDC response.
 * </p>
 * <p>
 * Authentication binds issuer, client, exact redirect URI, response type, scope, state, nonce, prompt, max_age, PKCE,
 * authenticated subject, consent, and one Budget. ID Tokens use an allowed algorithm and active key and preserve
 * issuer, audience, azp, auth_time, nonce, acr, amr, sid, and applicable hash semantics. UserInfo authorization and
 * subject binding are mandatory. Discovery and JWK Set metadata advertise only operational capabilities. End-session
 * processing either performs the validated redirect with state or ends without inventing a JSON response entity.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.protocol.oidc.server;
