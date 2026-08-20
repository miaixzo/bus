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
 * Declares the public Stack Overflow OAuth Vendor definition and externally loaded settings.
 * <p>
 * StackOverflowDefinition fixes {@code stackoverflow/default}, authorization, token, and Stack Exchange {@code /me}
 * endpoints, default {@code read_inbox} scope, and redirect Source authentication plus standard OAuth authorization. It
 * intentionally does not publish OAuth token capability because the historical token JSON omits the mandatory
 * {@code token_type}. Comma scope, query-bearing token POST, compact token JSON, profile query
 * {@code access_token}/{@code key}/{@code site}, and the Stack Exchange items envelope remain private deviations.
 * </p>
 * <p>
 * StackOverflowSourceSettings contains routing, Client ID, Client Secret reference, exact registered HTTP or HTTPS
 * callback, a unique ordered subset of the frozen scope vocabulary, Stack Apps key, and site identifier. Fixed
 * endpoints, response parsing, query transport, envelope rules, and identity selection cannot be externally supplied.
 * </p>
 * <p>
 * This exported package is registration metadata; execution enters a Registry-obtained Provider. Identity is created
 * only when the profile envelope contains exactly one registered user with a positive integral {@code user_id}; that ID
 * alone is the subject. Secrets, state, codes, access tokens, Stack Apps key, site, profile bodies, and errors must not
 * enter diagnostics, Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.stackoverflow;
