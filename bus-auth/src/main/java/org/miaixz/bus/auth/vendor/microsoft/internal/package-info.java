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
 * Implements the non-exported Microsoft OAuth and Graph identity flow.
 * <p>
 * MicrosoftSourceAdapter resolves the selected cloud's tenant templates before delegating standard authorization and
 * token operations. RedirectManager owns state without nonce or PKCE, and the standard callback decoder enforces one
 * bound success or error branch. Authorization-code and refresh-token grants remain TokenRequest operations; token
 * success remains TokenResponse and must use Bearer.
 * </p>
 * <p>
 * Source completion redeems the code through the standard token client and calls only the selected cloud's fixed Graph
 * {@code /me} endpoint with a Bearer header. The response must be JSON, must not contain an error envelope, and must
 * have a non-blank Graph ID before optional string attributes are copied. Global and China authorities are recorded
 * independently in identity evidence.
 * </p>
 * <p>
 * The standard client owns each operation-scoped secret lease. State, code, secret, token pair, Authorization header,
 * complete Graph body, user principal name, mail, office, and upstream error text never enter Context, failure details,
 * tracing, or logs. Registry receives no Graph DTO and no executable Provider or Source instance.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.vendor.microsoft.internal;
