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
 * Declares the Alipay public-application Vendor manifest and external RSA2 key references.
 * <p>
 * AlipayManifest exposes the single {@code alipay/default} VENDOR_AUTH variant, the fixed public-authorization and
 * gateway endpoints, {@code auth_user} scope, PRIVATE_KEY signing credential, browser initiate and complete manifest,
 * and the exact {@code app_id}, {@code auth_code}, signed gateway, envelope, and {@code auth_token} deviations.
 * AlipayOptions adds the Alipay response-verification key identifier to the common registration values.
 * </p>
 * <p>
 * Vendor catalog and compilation consume this package; the signed gateway adapter is co-located and instantiated only
 * by the framework Vendor suite. Users supply the application ID, private signing-key reference, public
 * verification-key ID, exact callback, and optional registered scope only. They do not supply fixed endpoints, key
 * material, gateway method names, response envelopes, or timestamp and signing fields. No platform scope enum, OAuth
 * TokenResponse, UserInfo model, refresh capability, or private DTO is exported.
 * </p>
 * <p>
 * Routing is exact, signing credentials must reference PRIVATE_KEY, {@code auth_user} occurs at most once, callbacks
 * cannot use localhost and must be HTTPS in production, and both key identifiers are non-blank. The manifest declares
 * VENDOR_AUTH because Alipay's RSA2 gateway methods, forms, signatures, response envelopes, and identity response are
 * proprietary rather than OAuth wire. Only a signature-verified {@code user_id} may become the external subject.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.alipay;
