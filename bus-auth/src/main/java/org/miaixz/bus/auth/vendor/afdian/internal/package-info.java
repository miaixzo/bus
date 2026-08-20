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
 * Implements the non-exported Afdian browser authorization and identity exchange.
 * <p>
 * AfdianSourceAdapter uses RedirectManager to create and consume the Source-bound state value. It builds the fixed GET
 * authorization request in Afdian's registered parameter order, validates the callback, resolves one client-secret
 * lease, and sends the private POST form with {@code grant_type}, {@code client_id}, {@code client_secret},
 * {@code code}, and {@code redirect_uri}. The bounded JSON response is consumed entirely within the adapter.
 * </p>
 * <p>
 * VendorSource reaches this package only through its typed factory registration. The adapter may use shared query and
 * form codecs, JsonProvider, SecretResolver, SecurityBaseline, and Fabric. It does not expose a protocol client,
 * standard token operation, platform response record, endpoint override, refresh or revoke operation, or a separate
 * profile request.
 * </p>
 * <p>
 * Callback state and exact redirect ownership are mandatory and one-time. HTTP method, form order, media type, response
 * size, and integral {@code ec} are strict. Only an {@code ec=200} response containing a non-blank {@code data.user_id}
 * succeeds; that value is the ExternalIdentity subject. The authorization code, client secret, form bytes, raw JSON,
 * and platform error fields are discarded before completion and never enter failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.afdian.internal;
