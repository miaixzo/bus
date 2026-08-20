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
 * Implements the non-exported Gitee browser authentication flow.
 * <p>
 * GiteeSourceAdapter delegates only standard OAuth authorization. RedirectManager owns state without PKCE, validates
 * the exact success or OAuth error callback, consumes the code once, resolves one client-secret lease, posts the
 * ordered authorization-code form with the required stable User-Agent, and retrieves the current user through Gitee's
 * private query-token resource request.
 * </p>
 * <p>
 * Token success is strict JSON containing the complete registered member set, Bearer type, positive {@code expires_in}
 * and {@code created_at}, a refresh token, and scope retaining {@code user_info}. It remains a private access record
 * and is never converted into TokenResponse. Undocumented error bodies are classified by HTTP status and discarded
 * rather than copied into failure details.
 * </p>
 * <p>
 * The profile requires a positive integral ID and all registered required strings before identity creation. The secret
 * lease closes around the token and profile chain. Code, secret, tokens, sensitive query, forms, complete bodies,
 * email, and platform messages never enter Context, Outcome details, attributes, tracing, metrics, exceptions, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.gitee.internal;
