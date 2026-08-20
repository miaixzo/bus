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
 * Implements the non-exported current LinkedIn OpenID Connect flow.
 * <p>
 * LinkedInSourceAdapter uses RedirectManager state without nonce or PKCE, validates the exact callback, and posts the
 * ordered authorization-code form with the client secret in the body rather than the URL. Its private token decoder
 * requires the documented access token, compact ID Token, Bearer type, positive lifetime, and scope, while retaining
 * partner refresh fields only when present and never publishing TokenResponse or refresh.
 * </p>
 * <p>
 * The incomplete Discovery object is strictly checked against the compiled issuer and endpoints without inventing
 * missing metadata or publishing Discovery. JWKS accepts one matching public RSA signing key. The ID Token is locally
 * verified for RS256 signature, current or closed historical issuer, audience, time, critical headers, and subject;
 * standard Bearer UserInfo must return the same subject.
 * </p>
 * <p>
 * One client-secret lease spans the private token and UserInfo stages. State, code, secret, token values, compact JWT,
 * Authorization header, complete form and bodies, email, and profile claims remain operation-local and never enter
 * Context, Outcome details, tracing, or logs. Retired localized profile and email envelopes are not decoded.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.linkedin.internal;
