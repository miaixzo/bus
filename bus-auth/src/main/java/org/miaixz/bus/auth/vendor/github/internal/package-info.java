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
 * Implements the non-exported GitHub OAuth App authentication flow.
 * <p>
 * GitHubSourceAdapter delegates only standard OAuth authorization. RedirectManager binds state and an S256 verifier,
 * validates the exact callback, consumes the code once, and posts GitHub's ordered JSON-requesting token form without
 * an invented {@code grant_type}. It then requests the versioned current-user REST resource with the registered vendor
 * media type, Bearer authorization, and fixed API-version header.
 * </p>
 * <p>
 * The private token decoder accepts exactly either the permanent-token branch or the complete expiring access and
 * refresh pair. It validates Bearer type, comma-delimited scopes, positive lifetimes, and the documented three-field
 * error object; it never constructs TokenResponse or publishes refresh. Profile success requires a positive integral
 * ID, required string members, and boolean {@code site_admin}.
 * </p>
 * <p>
 * One client-secret lease and one PKCE verifier lease close with their stages. Callback code, verifier, secret, token
 * pair, Authorization header, form, REST request identifier, personal fields, and complete bodies never enter
 * diagnostics or logs. Only a safe digest of a request identifier may accompany an HTTP failure.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.github.internal;
