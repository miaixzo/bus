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
 * Declares the public VK OAuth Vendor definition and externally loaded settings.
 * <p>
 * VkDefinition fixes {@code vk/default}, authorization, token/refresh, private-user, and revocation endpoints,
 * mandatory S256, default {@code vkid.personal_info email}, and redirect Source authentication plus standard OAuth
 * authorization, token, and revocation capabilities. Callback and token {@code device_id} binding, VK error and
 * {@code user_id} fields, the private user envelope, and revocation form/one-marker response are explicit deviations;
 * an unverified ID token is never identity evidence.
 * </p>
 * <p>
 * VkSourceSettings contains routing, Client ID, external Client Secret reference, exact registered HTTP or HTTPS
 * callback, unique ordered VK scopes covering {@code vkid.personal_info}, and a mandatory true PKCE flag. Fixed
 * endpoints, device binding, token/profile envelopes, revocation wire, and identity rules cannot be externally
 * supplied.
 * </p>
 * <p>
 * This exported package provides registration metadata; execution enters a Registry-obtained Provider. Only the
 * non-blank private profile {@code user.user_id} becomes ExternalIdentity subject and must equal token {@code user_id}
 * when present. Secrets, state, PKCE verifier, device ID, codes, tokens, profile bodies, and platform errors must not
 * enter diagnostics, Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.vk;
