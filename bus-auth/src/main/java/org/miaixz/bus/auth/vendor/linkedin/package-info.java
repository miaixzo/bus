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
 * Declares the current LinkedIn OpenID Connect Source variant.
 * <p>
 * LinkedInDefinition fixes {@code linkedin/default}, issuer {@code https://www.linkedin.com/oauth}, authorization,
 * token, Discovery, JWKS, and standard UserInfo endpoints, CLIENT_SECRET form authentication, prohibited PKCE, RS256,
 * and current {@code openid profile email} scopes. It publishes Source authentication, standard authentication, JWKS,
 * and UserInfo; incomplete Discovery and token contracts remain private to Source completion.
 * </p>
 * <p>
 * LinkedInSourceSettings contains routing, client, secret reference, exact HTTPS callback, and supported scopes
 * containing {@code openid} and {@code profile}. It cannot restore the retired lite-profile/email APIs or scopes,
 * configure native PKCE, endpoints, issuer, projections, token capability, refresh, or revocation. The obsolete
 * duplicate refresh endpoint registration is intentionally absent.
 * </p>
 * <p>
 * Identity is the locally verified ID Token {@code sub}, which must equal standard UserInfo {@code sub}. Email, names,
 * picture, locale, old member ID, and legacy profile data cannot replace it. The documented historical issuer and
 * incomplete metadata/token wire are closed deviations and never rewrite the expected issuer or public capabilities.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.linkedin;
