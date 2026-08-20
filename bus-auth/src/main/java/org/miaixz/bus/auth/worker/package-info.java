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
 * Defines the complete project-facing authentication data boundary and compiled framework workers.
 * <p>
 * Loader interfaces are the only entry points for project-owned databases, files, directories, key stores, remote
 * services, and configuration systems. Output ports deliver audit, consent, dynamic credentials, and Registry
 * notifications. {@link org.miaixz.bus.auth.worker.WorkerSet} only freezes these ports for runtime assembly.
 * </p>
 * <p>
 * {@link org.miaixz.bus.auth.worker.SourceWorker} remains the compiled capability worker produced by a SourceDriver.
 * Loaders do not parse authentication-domain records, enforce protocol policy, mutate Registry state, manage caches, or
 * execute unrelated security and audit responsibilities.
 * </p>
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.auth.worker;
