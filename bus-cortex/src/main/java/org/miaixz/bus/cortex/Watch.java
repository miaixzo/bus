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
package org.miaixz.bus.cortex;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Diff-style change notification delivered to a watch subscriber.
 *
 * @param <T> watched value type
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class Watch<T> implements Change<T> {

    /**
     * Creates an empty watch event.
     */
    public Watch() {

    }

    /**
     * Namespace of the subscription that produced this event.
     */
    private String namespace_id;
    /**
     * Asset type scope of the subscription that produced this event, when restricted to one type.
     */
    private Type type;
    /**
     * Identifier of the watch subscription receiving this event.
     */
    private String watchId;
    /**
     * Original watch criteria used to create the subscription.
     */
    private Vector vector;
    /**
     * Values newly observed by the subscription.
     */
    private List<T> added = List.of();
    /**
     * Values removed from the subscription result set.
     */
    private List<T> removed = List.of();
    /**
     * Values whose content changed while remaining in the result set.
     */
    private List<T> updated = List.of();
    /**
     * Logical event type such as snapshot, update, delete or error.
     */
    private String eventType;
    /**
     * Source component that emitted the watch event.
     */
    private String source;
    /**
     * Optional error message attached to failure notifications.
     */
    private String errorMessage;
    /**
     * Human-readable event summary for logs and diagnostics.
     */
    private String summary;
    /**
     * Previous emitted sequence used for consumers that enforce ordering.
     */
    private long previousSequence;
    /**
     * Whether the event represents a synthetic snapshot instead of a live delta.
     */
    private boolean snapshot;
    /**
     * Ordering sequence assigned to this watch event.
     */
    private long sequence;
    /**
     * Creation time of this watch event in epoch milliseconds.
     */
    private long timestamp;

}
