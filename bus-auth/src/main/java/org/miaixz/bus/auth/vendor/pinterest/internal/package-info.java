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
 * Implements the non-exported Pinterest OAuth wire adaptation and Source identity flow.
 * <p>
 * PinterestSourceAdapter exposes only Source authentication and the standard OAuth authorization and token models
 * declared by the definition. RedirectManager owns one-time state and callback correlation without nonce or PKCE. The
 * private authorization encoder preserves Pinterest's comma-delimited scope, while callback decoding accepts exactly
 * one registered standard success or error branch.
 * </p>
 * <p>
 * Token execution accepts only an AuthorizationCodeGrant in TokenRequest, obtains an operation-scoped Client Secret
 * lease, and sends Pinterest's registered query-authenticated empty-form POST. Strict parsing closes the status/message
 * envelope, requires a successful Bearer token, and maps it to TokenResponse without publishing a Pinterest token type.
 * Source completion performs the fixed query-authenticated profile GET, validates the status/message/data envelope and
 * exact profile vocabulary, and selects the historical {@code 60x60} image variant.
 * </p>
 * <p>
 * Only the validated profile {@code id} leaves the package as ExternalIdentity subject; all other profile values are
 * provider-neutral attributes. The adapter may depend on standard OAuth models and codecs, vendor browser flow, Fabric,
 * JSON services, and Bus validation, but protocol servers, Registry loaders, and external projects must not depend on
 * its parsing records or wire rules. Secret leases, state, codes, access tokens, response bodies, image URLs,
 * biography, names, and upstream platform messages must not escape through Context, tracing, logs, or public failures.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.pinterest.internal;
