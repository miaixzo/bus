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
 * Implements the non-exported Sign in with Apple OIDC and client-secret signing flow.
 * <p>
 * AppleSourceAdapter generates one-time state and nonce, emits Apple's authorization-code request with
 * {@code response_mode=form_post} and {@code name email} scope, and strictly decodes the POST callback. It creates a
 * five-minute ES256 client-secret JWT from the configured private key for each token or revocation operation, sends it
 * as {@code client_secret}, validates Discovery and Apple's JWK Set, and verifies callback and token-endpoint ID
 * Tokens.
 * </p>
 * <p>
 * Standard token, revocation, Discovery, and JWK Set capabilities preserve their formal models; the authorization
 * deviation exists only inside Source authentication. The adapter composes KeyResolver, JOSE/JWT, OIDC and OAuth
 * codecs, RedirectManager, JsonProvider, SecurityBaseline, and Fabric. It does not publish private_key_jwt, PKCE,
 * UserInfo, generic authorization, endpoint override, long-lived client-secret cache, Apple callback DTOs, or token
 * response extensions as application identity.
 * </p>
 * <p>
 * Callback method, target, state, code/error branch, and optional one-time user JSON are strict. Client-secret JWT
 * header and claims bind ES256, key ID, Team ID issuer, client-ID subject, Apple audience, issued-at, and bounded
 * expiration. Discovery must reproduce fixed endpoints and RS256; JWK selection requires a unique RSA signing key
 * matching protected {@code kid}. ID Token signature, issuer, audience, azp, time, nonce, and applicable hashes are
 * verified, and any callback user identifier must match {@code sub}. Only that subject is emitted; keys, JWTs, codes,
 * tokens, user JSON, claims, and bodies never enter diagnostics.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.apple.internal;
