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
 * Defines management contracts for protocol-neutral Provider entities.
 * <p>
 * {@link org.miaixz.bus.auth.provider.ProviderValidator} enforces common Provider registration invariants, and
 * {@link org.miaixz.bus.auth.provider.ProviderService} leaves entity persistence to an external project.
 * </p>
 * <p>
 * The historical {@link org.miaixz.bus.auth.provider.ProviderProfile} and
 * {@link org.miaixz.bus.auth.provider.ProviderDriver} names describe server roles defined by protocol standards. Both
 * extend Source contracts and consume Source JSON; they do not make the Provider persistence entity protocol-aware.
 * </p>
 * <p>
 * Provider services must not expose credential material, instantiate protocol runtimes, perform authentication, or
 * infer protocol support from Provider-level settings.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.provider;
