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
 * Implements the non-exported GitLab.com OAuth and identity behavior.
 * <p>
 * GitLabSourceAdapter combines RedirectManager state and S256 correlation with standard public request and response
 * types. Authorization-code and refresh grants use their exact ordered forms, including the registered refresh callback
 * extension, and preserve {@code created_at} as a TokenResponse extension. Refresh remains the standard
 * {@code token(RefreshTokenGrant)} operation and rotates both token values.
 * </p>
 * <p>
 * Revocation accepts only RevocationRequest, posts client credentials and token with an optional standard hint, and
 * maps either an empty body or an exact empty JSON object to standard success. OAuth error members and status classes
 * are strictly mapped without exposing descriptions. The REST current-user request uses only Bearer authorization and
 * is never represented as OIDC UserInfo.
 * </p>
 * <p>
 * Profile success requires the registered numeric and typed members before the user ID becomes the subject. Each
 * independent token or revocation operation owns one secret lease; Source completion owns one secret and one verifier
 * lease for its full chain. Codes, verifier, secrets, tokens, headers, forms, email, IP addresses, and bodies never
 * enter Context, Outcome details, attributes, tracing, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.gitlab.internal;
