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
 * Declares GitHub.com OAuth login and GitHub Enterprise management variants.
 * <p>
 * GitHubManifest fixes {@code github/default}, the browser, token, refresh, and REST current-user endpoints,
 * CLIENT_SECRET form authentication, required S256 PKCE, and the minimum {@code read:user} scope. It publishes Source
 * authentication and standard OAuth authorization only; the missing token grant type, comma-delimited response scope,
 * optional expiring-token pair, and REST user representation remain registered private deviations.
 * </p>
 * <p>
 * GitHubOptions contains routing, client, secret-reference, exact HTTPS callback, and unique registered scopes. It
 * cannot select GitHub Enterprise Server, GitHub Apps, device flow, endpoint versions, token lifecycle switches,
 * private response records, or a platform management API disguised as RFC 7009 revocation.
 * </p>
 * <p>
 * Source authentication accepts only the positive integral durable GitHub user {@code id}, rendered as unsigned decimal
 * text, as its subject. Login, node ID, email, name, company, plan, and profile links remain attributes. PKCE verifier,
 * secret, access and refresh tokens, REST request identifiers, and personal data stay inside the operation boundary.
 * </p>
 * <p>
 * {@code github/enterprise} is a separate HTTPS Variant using a referenced administrator token. It exposes describe,
 * snapshot, and retrieve for visible enterprise and organization resources normalized as ORGANIZATION, Teams normalized
 * as GROUP, users, and membership. Coverage is PARTIAL, permission-hidden 404 responses are rejections rather than an
 * empty complete snapshot, the fixed REST API version is always sent, and no changes capability exists.
 * </p>
 * <p>
 * Organizations and Teams are platform management containers, not a natural parent-child organization chart. Equal user
 * resources may recur on bounded Team pages without retaining tenant-wide de-duplication state. External projects
 * invoke Dispatcher and own scheduling, mapping, synchronization, checkpoints, and persistence.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.github;
