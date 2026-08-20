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
 * Declares the GitHub.com OAuth App Source variant.
 * <p>
 * GitHubDefinition fixes {@code github/default}, the browser, token, refresh, and REST current-user endpoints,
 * CLIENT_SECRET form authentication, required S256 PKCE, and the minimum {@code read:user} scope. It publishes Source
 * authentication and standard OAuth authorization only; the missing token grant type, comma-delimited response scope,
 * optional expiring-token pair, and REST user representation remain registered private deviations.
 * </p>
 * <p>
 * GitHubSourceSettings contains routing, client, secret-reference, exact HTTPS callback, and unique registered scopes.
 * It cannot select GitHub Enterprise Server, GitHub Apps, device flow, endpoint versions, token lifecycle switches,
 * private response records, or a platform management API disguised as RFC 7009 revocation.
 * </p>
 * <p>
 * Source authentication accepts only the positive integral durable GitHub user {@code id}, rendered as unsigned decimal
 * text, as its subject. Login, node ID, email, name, company, plan, and profile links remain attributes. PKCE verifier,
 * secret, access and refresh tokens, REST request identifiers, and personal data stay inside the operation boundary.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.github;
