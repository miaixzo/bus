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
package org.miaixz.bus.logger;

import java.util.Objects;

/**
 * Immutable log event snapshot passed through the executor before provider output.
 *
 * @param level     logging level
 * @param throwable associated failure, or {@code null}
 * @param format    provider-compatible message format
 * @param arguments message arguments
 * @author Kimi Liu
 * @since Java 21+
 */
public record Loggable(Level level, Throwable throwable, String format, Object[] arguments) {

    /**
     * Validates the logging level and defensively copies the message arguments.
     */
    public Loggable {
        Objects.requireNonNull(level, "level");
        arguments = arguments == null ? new Object[0] : arguments.clone();
    }

    /**
     * Returns a defensive copy of the message arguments.
     *
     * @return copied message arguments
     */
    @Override
    public Object[] arguments() {
        return arguments.clone();
    }

}
