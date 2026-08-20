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
 * Declares the Figma OAuth Source variant.
 * <p>
 * FigmaManifest exposes {@code figma/default} with fixed commercial authorization, token, refresh, and current-user
 * endpoints. It requires CLIENT_SECRET with HTTP Basic token authentication, requires S256 PKCE, defaults to
 * {@code current_user:read}, and publishes Source authentication plus the standard OAuth authorization operation. The
 * platform token identity field and incompletely registered token error wire remain explicit deviations.
 * </p>
 * <p>
 * FigmaOptions contains only routing, client, secret-reference, exact callback, and registered scope data. Applications
 * cannot configure government hosts, legacy refresh endpoints, token authentication, PKCE policy, private response
 * records, error assumptions, or token and refresh capabilities. The callback is an exact registered HTTPS URI and
 * scopes are unique and must include {@code current_user:read}.
 * </p>
 * <p>
 * Source authentication requires the profile's non-blank string {@code id} to match the token response's
 * {@code user_id_string} byte for byte; that value alone becomes the subject. Deprecated numeric user IDs, handle,
 * image URL, and email cannot supply or replace the identity key. Secrets, tokens, verifiers, personal data, and
 * unknown error bodies remain private to the operation.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.figma;
