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
 * Declares the Sign in with Apple OpenID Connect Vendor manifest and signing-key registration.
 * <p>
 * AppleManifest exposes the single {@code apple/default} OIDC variant with fixed authorization, token, revocation,
 * Discovery, and JWK Set endpoints. It declares PRIVATE_KEY client credentials, prohibited PKCE, RS256 ID Tokens,
 * {@code name email} defaults, Source authentication, token, revocation, Discovery, and JWK Set capabilities. Its
 * deviations record Apple's form_post authorization scope and ES256 client-secret JWT sent as client_secret_post.
 * </p>
 * <p>
 * AppleOptions contains the common configuration values plus Team ID and key ID used with the external private
 * signing-key reference. Users do not configure Apple's fixed issuer and endpoints, token authentication shape,
 * algorithms, response mode, or JWK Set. This package publishes neither a generic OIDC authorization capability whose
 * required {@code openid} scope would misrepresent Apple's wire nor private callback, user, token, and key DTOs.
 * </p>
 * <p>
 * Routing, Team ID, key ID, client ID, exact production HTTPS callback, and PRIVATE_KEY reference are mandatory;
 * requested scopes are unique {@code name} and {@code email} values and PKCE is forbidden. The ES256 assertion is a
 * short-lived Apple client_secret with issuer Team ID, subject and audience binding, not private_key_jwt client
 * authentication. Identity is established only from a locally verified RS256 Apple ID Token {@code sub}.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.source.vendor.apple;
