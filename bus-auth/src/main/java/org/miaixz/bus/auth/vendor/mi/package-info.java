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
 * Declares the public Xiaomi OAuth Vendor definition and externally loaded settings.
 * <p>
 * MiDefinition fixes the sole {@code mi/default} variant, its Xiaomi-owned authorization, token, refresh, and
 * account-resource endpoints, and its public Source-authentication and OAuth authorization/token capabilities. The
 * profile records Xiaomi's GET query authentication, {@code skip_confirm}, prefixed token JSON, token extensions, and
 * profile query parameters as explicit vendor deviations; it does not present the token endpoint's {@code OIDC} path
 * segment as OpenID Connect conformance or expose the private profile documents as OAuth responses.
 * </p>
 * <p>
 * MiSourceSettings accepts only the registered Xiaomi routing identifiers, a Client ID, a Client Secret reference, one
 * exact absolute credential-free callback, and unique account scopes from the frozen Xiaomi vocabulary. Endpoint URLs,
 * issuer data, arbitrary hosts, PKCE controls, response prefixes, profile selectors, and platform response models are
 * not externally configurable in this package.
 * </p>
 * <p>
 * The package is exported for registration and management data only. Runtime invocation enters the Provider obtained
 * from Registry and is delegated to the non-exported adapter; callers must not construct platform execution state from
 * these metadata types. Identity completion accepts only Xiaomi's token {@code openId} as the external subject, while
 * nickname, icon, and email remain attributes. Client secrets, callback codes, state, access and refresh tokens, MAC
 * material, and complete upstream documents must never enter settings diagnostics, Context, or failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.mi;
