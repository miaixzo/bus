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
 * Declares the Afdian creator-account Vendor manifest and externally managed client options.
 * <p>
 * AfdianManifest exposes the single {@code afdian/default} proprietary HTTPS variant with fixed public authorization
 * and identity-exchange endpoints, the {@code basic} default scope, CLIENT_SECRET credential type, and browser initiate
 * and complete capabilities. AfdianOptions accepts only the platform routing keys, public client identifier, credential
 * reference, exact registered callback, and ordered requested scopes.
 * </p>
 * <p>
 * Management catalogs and Vendor compilation consume this package. The concrete adapter is co-located with its manifest
 * and options and is instantiated only by the framework Vendor suite. Callers do not provide Afdian's fixed endpoints
 * or its private response shape, and this package does not publish an OAuth TokenResponse, OAuth UserInfo, platform
 * token DTO, custom scope enum, transport behavior, or credential value.
 * </p>
 * <p>
 * Routing is fixed to one variant, credentials must reference CLIENT_SECRET, callback ownership is exact, and scopes
 * are immutable non-blank platform values. The platform and capability declarations identify the proprietary exchange,
 * while its real transport protocol is HTTPS. The token endpoint's private JSON envelope returns {@code data.user_id}
 * as the terminal identity rather than a standards-compliant OAuth token response. Only that verified stable user
 * identifier may become the external subject.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.afdian;
