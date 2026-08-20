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
 * Defines validation for protocol-neutral Provider entities.
 * <p>
 * {@link org.miaixz.bus.auth.provider.ProviderValidator} enforces common Provider registration invariants. Provider
 * persistence and management operations remain external project responsibilities.
 * </p>
 * <p>
 * Server-role protocol implementations use the root {@link org.miaixz.bus.auth.Scheme} declaration and the common
 * {@link org.miaixz.bus.auth.source.SourceDriver} compilation boundary directly. This package does not define parallel
 * Provider settings, profile, or driver abstractions and does not make the Provider persistence entity protocol-aware.
 * </p>
 * <p>
 * Provider contracts must not expose credential material, instantiate protocol runtimes, perform authentication, or
 * infer protocol support from Provider-level settings.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.provider;
