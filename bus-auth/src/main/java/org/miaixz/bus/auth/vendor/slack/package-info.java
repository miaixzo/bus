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
 * Declares the public Slack OAuth Vendor definition and externally loaded settings.
 * <p>
 * SlackDefinition fixes {@code slack/default}, authorization, token, users-info, and revocation endpoints, default
 * identity scopes, and redirect Source authentication plus standard OAuth authorization, token, and revocation
 * capabilities. Comma-delimited scope, GET query token credentials, the {@code oauth.v2.access} envelope,
 * {@code users.info} query/envelope, and Bearer GET {@code auth.revoke} envelope are explicit vendor deviations
 * confined to the private adapter; the public boundary retains AuthorizationRequest, TokenRequest/TokenResponse, and
 * RevocationRequest semantics.
 * </p>
 * <p>
 * SlackSourceSettings contains only routing, Client ID, Client Secret reference, exact registered HTTP or HTTPS
 * callback, and unique standard scope strings. Explicit scopes must contain {@code users:read} so the fixed identity
 * operation is authorized. Fixed endpoints, delimiters, query credentials, response envelopes, profile selectors, and
 * revocation transport cannot be externally configured.
 * </p>
 * <p>
 * This exported package provides registration and management metadata; execution must enter a Registry-obtained
 * Provider. Source completion requires the token envelope's {@code authed_user.id} to equal the
 * {@code users.info user.id}, and only that bound ID becomes ExternalIdentity subject. Client secrets, state, codes,
 * tokens, Slack profile values, response bodies, and platform errors must not enter diagnostics, Context, tracing,
 * logs, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.slack;
