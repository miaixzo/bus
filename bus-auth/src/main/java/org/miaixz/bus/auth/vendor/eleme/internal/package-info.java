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
 * Implements the non-exported Eleme service-provider authentication flow.
 * <p>
 * ElemeSourceAdapter delegates the public OAuth authorization and token operations to the standard clients. Source
 * authentication adds RedirectManager state handling, consumes the code callback, uses one client-secret lease for the
 * authorization-code token request and merchant RPC call, and accepts refresh-token grants only through the registered
 * standard token operation.
 * </p>
 * <p>
 * The identity step posts the fixed {@code eleme.user.getUser} action to the NOP gateway. It creates a fresh UUID
 * request identifier, uses an epoch-millisecond timestamp, serializes naturally ordered application-key and timestamp
 * values, and computes the required uppercase MD5 signature over the documented action, access token, parameters, and
 * secret. This private wire format cannot escape as OAuth UserInfo, token introspection, or token revocation.
 * </p>
 * <p>
 * The response identifier must equal the request identifier, success and error branches must be exclusive, and a
 * successful result must contain a positive integral user ID and non-blank user name. Code, client secret, tokens,
 * signature bytes, request body, response body, shop data, and platform messages are bounded to their operation and are
 * never copied into diagnostics or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.eleme.internal;
