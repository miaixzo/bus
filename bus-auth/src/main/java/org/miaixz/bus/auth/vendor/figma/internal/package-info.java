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
 * Implements the non-exported Figma OAuth authentication flow.
 * <p>
 * FigmaSourceAdapter delegates only the conforming authorization operation. RedirectManager binds state and an S256
 * verifier to the exact Source and callback. Source completion accepts the single documented success callback, consumes
 * its code once, sends the ordered authorization-code form with HTTP Basic client authentication, and calls
 * {@code /v1/me} with a Bearer header.
 * </p>
 * <p>
 * The private token response requires string {@code user_id_string}, access and refresh tokens, Bearer type, and a
 * positive lifetime. Private refresh uses the current token endpoint and the RFC 6749 refresh grant shape, never the
 * legacy refresh URL, while remaining unpublished because Figma has not registered a complete standard error contract.
 * Unknown error bodies are discarded and classification uses only the HTTP status boundary.
 * </p>
 * <p>
 * Profile success requires non-blank string ID, handle, image URL, and email, and the ID must equal
 * {@code user_id_string}. One client-secret lease and one verifier lease close with their stages. Callback code,
 * verifier, secret, tokens, Authorization header, email, forms, and complete response bodies never enter diagnostics or
 * logs.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.figma.internal;
