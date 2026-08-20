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
 * Implements the non-exported Feishu OAuth authentication flow.
 * <p>
 * FeishuSourceAdapter delegates the conforming authorization operation while RedirectManager binds state and an S256
 * verifier to the exact Source and callback. Source completion consumes the code once, posts the documented ordered v3
 * JSON authorization-code request with client credentials and verifier, then calls the fixed profile endpoint with a
 * Bearer token. The platform token and profile envelopes remain private records.
 * </p>
 * <p>
 * Token success requires integral platform code zero, Bearer type, positive lifetimes, and refresh fields consistent
 * with {@code offline_access}. The private refresh request uses the same v3 endpoint and requires rotated refresh-token
 * output, but it is not published as a standard token or revocation capability. Platform codes are mapped only through
 * their registered rejection and temporary-failure categories.
 * </p>
 * <p>
 * Profile success requires code zero and non-blank {@code open_id} and {@code union_id}; only {@code union_id} becomes
 * the subject. The client-secret lease and verifier lease close with their stages. Code, verifier, secret, access and
 * refresh tokens, Authorization header, personal fields, request JSON, response JSON, and platform messages never enter
 * diagnostics or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.feishu.internal;
