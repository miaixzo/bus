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
 * Declares the Gitee.com OAuth Source variant.
 * <p>
 * GiteeManifest fixes {@code gitee/default}, its authorization, token, refresh, and current-user endpoints,
 * CLIENT_SECRET form authentication, prohibited PKCE, and the required leading {@code user_info} scope. It publishes
 * Source authentication and only the conforming OAuth authorization operation; private token, unauthenticated refresh,
 * query-token profile, and {@code created_at} behavior are registered deviations rather than public protocol models.
 * </p>
 * <p>
 * GiteeOptions contains routing, client, secret-reference, exact HTTPS callback, and ordered unique scopes. It cannot
 * carry endpoints, passwords, PKCE values, User-Agent policy, private response records, token capabilities, or a
 * revocation endpoint. Fixed platform addresses and client secrets remain outside management output.
 * </p>
 * <p>
 * Source authentication accepts only the positive integral current-user {@code id}, rendered as unsigned decimal text,
 * as its subject. Login, name, email, profile links, and enterprise data remain attributes and cannot replace that key.
 * Access tokens carried in platform query parameters require Fabric redaction and never become identity attributes.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.gitee;
