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
 * Provides cross-platform implementation to retrieve OS, FileSystem, and Process information.
 * <p>
 * A getter generally reports a value it could not read as a sentinel:
 * <ul>
 * <li>a string is {@link Normal#UNKNOWN} or, for some free-text values, the empty string;</li>
 * <li>a collection, map, or array is empty;</li>
 * <li>a number is outside its legitimate range: {@code 0} where zero cannot be a real value, such as a size or
 * frequency, {@code -1} where it can, such as a count, and {@link Double#NaN} for some floating-point
 * measurements.</li>
 * </ul>
 * Where a getter uses a more specific sentinel, its own Javadoc names it.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.health.builtin.software;

import org.miaixz.bus.core.lang.Normal;
