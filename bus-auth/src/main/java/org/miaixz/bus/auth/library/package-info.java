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
 * Defines management and safe launch contracts for the authentication application library.
 * <p>
 * {@link org.miaixz.bus.auth.library.LibraryService} leaves Library persistence to an external project,
 * {@link org.miaixz.bus.auth.library.LibraryValidator} enforces the framework-owned entity invariants. External
 * projects load complete Library, Provider, and Source entities directly through the registration boundary.
 * {@link org.miaixz.bus.auth.library.LibraryLaunchService} resolves allowed Principal placeholders in the persisted
 * Library URL template and returns the resulting URL directly without adding derived state to the entity.
 * </p>
 * <p>
 * Provider, Source, and protocol packages may contribute metadata used by an external Hub, but they do not call back
 * into Library persistence. Authorization of operators, field visibility, database mapping, configuration loading, and
 * transaction handling remain responsibilities of the external project.
 * </p>
 * <p>
 * A launch operation validates namespace and registration ownership before returning redirect information. It does not
 * follow redirects, invoke arbitrary user-supplied URLs, resolve plaintext credentials, expose hidden registrations, or
 * weaken the Registry's single invocation entry and capability checks.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.library;
