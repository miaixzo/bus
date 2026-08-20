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
 * Implements the non-exported Xiaomi browser authorization and identity-completion flow.
 * <p>
 * MiSourceAdapter exposes only the capabilities declared by the selected Xiaomi definition. RedirectManager owns the
 * one-time state lifecycle and exact registered callback binding. Authorization retains Xiaomi's ordered query and
 * {@code skip_confirm=false}; authorization-code and refresh-token requests remain standard TokenRequest operations
 * even though their private wire encoding uses Xiaomi's registered GET query authentication.
 * </p>
 * <p>
 * Token handling obtains a fresh Client Secret lease for each operation, requires the exact {@code &&&START&&&} prefix
 * and strict JSON member vocabulary, and maps standard token members into TokenResponse. Xiaomi's {@code openId} and
 * paired MAC values remain registered extensions rather than new OAuth fields. Source completion calls only the fixed
 * profile resource and treats the sibling phone-and-email projection as optional; failure of that projection cannot
 * invalidate a verified primary profile.
 * </p>
 * <p>
 * The adapter accepts only token {@code openId} as ExternalIdentity subject and records the Xiaomi account-resource
 * authority as evidence. Its response records, query details, and parsing rules remain private to this package and may
 * depend on shared vendor flow, protocol models, Fabric transport, JSON services, and Bus validation primitives; no
 * protocol server package, Registry loader, or external project may depend on them. Secret leases, state, callback
 * codes, access and refresh tokens, MAC keys, profile/contact bodies, and upstream diagnostic text must not escape into
 * Context, logs, tracing, or public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.mi.internal;
