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
 * Declares the GitLab.com OAuth Source variant.
 * <p>
 * GitLabManifest fixes {@code gitlab/default}, the authorization, token, refresh, revocation, and current-user
 * endpoints, CLIENT_SECRET form authentication, required S256 PKCE, and the minimum {@code read_user} scope. It
 * publishes Source authentication and the standard OAuth authorization, token, and revocation operations. The refresh
 * {@code redirect_uri}, {@code created_at} extension, and empty-JSON revocation success are registered wire deviations.
 * </p>
 * <p>
 * GitLabOptions contains routing, client, secret-reference, exact HTTPS callback, and unique scopes beginning with
 * {@code read_user}. It cannot configure a self-managed origin, switch protocols because OIDC scopes are selected,
 * disable PKCE, override endpoints, expose private REST records, or add a separate refresh operation.
 * </p>
 * <p>
 * Protocol operations retain TokenRequest, TokenResponse, RevocationRequest, and RevocationResponse. Source identity is
 * established only by the positive integral GitLab current-user {@code id}, rendered as unsigned decimal text.
 * Username, email, external identities, profile fields, and access or refresh tokens cannot replace the subject.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.gitlab;
