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
 * Declares the Facebook Login OAuth Source variant.
 * <p>
 * FacebookManifest exposes {@code facebook/default} against the fixed Graph API v26.0 authorization, token, and profile
 * endpoints. It requires CLIENT_SECRET, prohibits PKCE, defaults to {@code public_profile} and {@code email}, and
 * publishes only Source authentication and the standard OAuth authorization operation. The GET token request, query
 * client secret, missing token type, Graph error object, and profile proof are registered deviations.
 * </p>
 * <p>
 * FacebookOptions contains routing, client, secret-reference, exact callback, and registered scope values only.
 * Applications cannot select a Graph version, override endpoints, enable a token capability, configure proof material,
 * expose Graph response records, or model proprietary long-lived-token exchange and permissions deletion as OAuth
 * refresh or RFC 7009 revocation.
 * </p>
 * <p>
 * A completed Source authentication accepts only the Graph application's digit-only scoped user {@code id} as its
 * subject. Names, picture data, and optional email remain attributes; none can replace the stable identifier. Client
 * secrets, access tokens, proof values, Graph traces, request queries, and response bodies remain private.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.facebook;
