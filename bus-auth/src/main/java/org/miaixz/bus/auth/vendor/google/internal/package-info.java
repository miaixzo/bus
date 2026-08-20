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
 * Implements the non-exported Google OpenID Connect authentication flow.
 * <p>
 * GoogleSourceAdapter binds state, nonce, and an S256 verifier through RedirectManager, requires the RFC 9207 issuer
 * callback parameter, and routes standard AuthenticationRequest, TokenRequest, RevocationRequest, Discovery, JWKS, and
 * UserInfo models. Authorization-code and refresh-token forms retain their standard operation names, and refresh tokens
 * are accepted only when the authentication request explicitly requested offline access.
 * </p>
 * <p>
 * Discovery must exactly match every compiled endpoint, issuer, client authentication, scope, response-issuer support,
 * S256 method, public subject type, and RS256 algorithm. JWKS processing accepts public RSA signing keys with an exact
 * algorithm and unique key ID. ID Tokens are locally verified for signature, issuer, audience, authorized party, time,
 * nonce, access-token hash, critical headers, and bounded subject before any profile data is used.
 * </p>
 * <p>
 * UserInfo uses only a Bearer header and must return the same subject. Google's unauthenticated revocation wire is
 * handled as a registered deviation behind the standard revocation contract. Secret and verifier leases close at their
 * stages; code, state, nonce, verifier, secret, tokens, compact JWT, headers, response bodies, email, and personal
 * claims never enter Context, failure details, tracing, or logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.google.internal;
