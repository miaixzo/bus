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
 * Versioned artifact registry for tracking the release lifecycle of registered services.
 * <p>
 * {@code VersionAssets} extends the base asset definition with release-specific fields: a semantic version string (e.g.
 * "1.2.3"), a human-readable changelog, artifact coordinates or a download URL, and a {@code VersionStatus} indicating
 * the current lifecycle phase. {@code VersionStatus} is a three-state enum: ACTIVE (the release is current and in
 * active use), DEPRECATED (still supported but clients should migrate to a newer version) and RETIRED (removed from
 * service and must not be used). {@code VersionRegistry} provides full CRUD and watch support for {@code VersionAssets}
 * definitions backed by the generic {@code AbstractRegistry}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
package org.miaixz.bus.cortex.version;
