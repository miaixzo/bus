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
 * Defines pure parsers for project-loaded authentication records.
 * <p>
 * Parsers synchronously validate, normalize, and freeze the explicit records supplied by the matching loader in
 * {@link org.miaixz.bus.auth.worker}. They contain no external loading, persistence, cache, network, executor, Context,
 * Budget, Registry, audit, consent, or project-business behavior.
 * </p>
 * <p>
 * Protocol and runtime services explicitly call a Loader first and then pass only its successful record to the matching
 * Parser. Rejection and operational failure remain Loader outcomes and are never invented by a Parser.
 * </p>
 * <p>
 * Parser results are immutable authentication-domain values. Plaintext material remains owned by
 * {@link org.miaixz.bus.auth.shared.SecretLease}; parsers neither retain nor cache it.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.resolver;
