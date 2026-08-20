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
 * Implements the non-exported Slack OAuth wire adaptation and Source identity flow.
 * <p>
 * SlackSourceAdapter routes only Source authentication and the declared standard authorization, token, and revocation
 * capabilities. RedirectManager atomically owns state and exact callback correlation without nonce or PKCE. Public
 * requests remain AuthorizationRequest, TokenRequest containing AuthorizationCodeGrant, and RevocationRequest; the
 * adapter applies comma scope and Slack-specific transports only inside private wire methods.
 * </p>
 * <p>
 * Token execution obtains an operation-scoped Client Secret lease, sends the registered GET query, closes the
 * {@code oauth.v2.access} success/error vocabulary, requires Bearer, and retains {@code authed_user} only as a
 * registered TokenResponse extension. Source completion invokes fixed {@code users.info}, validates its closed
 * envelope, and requires its user ID to match the authenticated-user ID before creating ExternalIdentity. Revocation
 * maps the standard request to Bearer GET {@code auth.revoke} and succeeds only for the exact successful revoked
 * envelope.
 * </p>
 * <p>
 * Private parsing may depend on standard OAuth models and codecs, vendor flow, Fabric, JSON, secret resolution, and Bus
 * validation, but protocol servers, Registry loaders, and external projects must not depend on it. Secret leases,
 * state, codes, tokens, authenticated-user data, profile bodies, Authorization headers, and Slack diagnostics must not
 * escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.slack.internal;
