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
 * Implements the non-exported JD browser, token, and Zeus profile flow.
 * <p>
 * JdSourceAdapter maps standard OAuth authorization {@code client_id} to {@code app_key}, while RedirectManager owns
 * state and the one-time callback without PKCE. Source completion uses one secret lease for the ordered JD token form
 * and signed profile query. Private refresh uses the current OAuth2 endpoint and its own lease; neither token operation
 * is published as a standard capability because the success and error wire is incomplete.
 * </p>
 * <p>
 * Token decoding accepts only the two documented error branches and the closed {@code open_id}/{@code xid} identity
 * compatibility rule. Zeus parameters are sorted after compact JSON generation, wrapped by the app secret, signed with
 * uppercase MD5 through bus-crypto, timestamped in UTC with the registered Bus format, and UTF-8 encoded only after
 * signing. Only the two documented profile-envelope spellings are accepted.
 * </p>
 * <p>
 * Code, secret, token pair, canonical signing text, signature, query, form, bodies, request identifiers, nickname, and
 * image data remain operation-local and never enter diagnostics or logs. Profile fields become attributes only after
 * business code, nested result, and canonical token subject have been validated.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.jd.internal;
