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
package org.miaixz.bus.core.lang.event;

import java.io.Serial;
import java.util.EventObject;

/**
 * An event implementation based on an event source. This class extends {@link EventObject} and implements the
 * {@link Event} marker interface, providing a basic event structure that carries a source object.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SourceEvent extends EventObject implements Event {

    @Serial
    private static final long serialVersionUID = 2852251732571L;

    /**
     * Constructs a new {@code SourceEvent} with the specified event source.
     *
     * @param source The object on which the Event initially occurred. Must not be {@code null}.
     * @throws IllegalArgumentException if source is null.
     */
    public SourceEvent(final Object source) {
        super(source);
    }

}
