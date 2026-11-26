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
package org.miaixz.bus.errors.spi;

import org.miaixz.bus.errors.policy.ErrorLogPolicy.Level;

/**
 * Emits records prepared by the Bus error policy through a logging backend.
 *
 * @author Kimi Liu
 */
public interface ErrorLogSink {

    /**
     * Checks the backend threshold for a level selected by the policy.
     *
     * @param level policy-selected severity
     * @return whether the backend accepts records at this level
     */
    boolean isEnabled(Level level);

    /**
     * Writes a preselected severity and approved diagnostic fields.
     *
     * @param level     policy-selected severity
     * @param format    policy-selected log template
     * @param arguments approved log fields in template order
     */
    void write(Level level, String format, Object... arguments);

}
