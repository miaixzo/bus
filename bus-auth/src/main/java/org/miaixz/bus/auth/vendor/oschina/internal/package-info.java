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
 * Implements the non-exported OSChina OAuth wire adaptation and Source identity flow.
 * <p>
 * OsChinaSourceAdapter uses RedirectManager for atomic state generation and consumption, binds the callback to the
 * registered target, and uses the standard authorization request encoder and response decoder. Public authorization
 * accepts AuthorizationRequest, and public token handling accepts only an AuthorizationCodeGrant inside TokenRequest;
 * it returns TokenResponse and does not publish a platform token, refresh, or profile operation.
 * </p>
 * <p>
 * The private token boundary obtains an operation-scoped Client Secret lease and sends OSChina's registered GET query
 * containing {@code client_secret} and {@code dataType=json}. It strictly distinguishes the standard OAuth error branch
 * from success, requires Bearer, positive lifetime, and the documented {@code uid} extension, and rejects undeclared
 * members. Source completion then performs the fixed profile GET with query access token and maps only a valid
 * non-blank profile {@code id} to ExternalIdentity subject.
 * </p>
 * <p>
 * Private response parsing may depend on standard OAuth models and codecs, vendor flow primitives, Fabric transport,
 * JSON services, and Bus validation. It must not be imported by protocol servers, Registry loading, or external project
 * service implementations, and its profile document is not an OpenID Connect UserInfo response. Secret material, state,
 * callback codes, tokens, {@code uid}, complete profile bodies, and upstream diagnostics must remain outside Context,
 * tracing, logs, and public failure details.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.oschina.internal;
