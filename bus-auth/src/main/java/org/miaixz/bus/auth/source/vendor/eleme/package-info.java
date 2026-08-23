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
 * Declares the Eleme service-provider OAuth Source variant.
 * <p>
 * ElemeManifest exposes only {@code eleme/default}. It fixes the authorization, token, refresh, and merchant RPC
 * endpoints, requires CLIENT_SECRET with HTTP Basic token authentication, prohibits PKCE, requires the {@code all}
 * scope, and publishes Source authentication plus the standard OAuth authorization and token operations. The merchant
 * RPC gateway is a registered platform deviation and is never represented as OAuth UserInfo.
 * </p>
 * <p>
 * ElemeOptions contains only routing, client, secret-reference, callback, and scope data. Applications cannot supply
 * endpoints, switch to a merchant client-credentials flow, change the NOP version or action, select a signing
 * algorithm, expose private RPC records, or reinterpret merchant deauthorization as RFC 7009 token revocation.
 * </p>
 * <p>
 * The callback must be an exact registered HTTPS URI and the only accepted scope is {@code all}. Standard token results
 * retain OAuth field names and semantics. A completed Source authentication accepts identity only from the signed RPC
 * result's positive integral {@code userId}; its unsigned decimal representation is the subject, while shop data
 * remains optional attributes and never supplies an alternative identity key.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.eleme;
