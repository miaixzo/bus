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
package org.miaixz.bus.cortex.registry;

import java.util.List;

import org.miaixz.bus.cortex.Listener;
import org.miaixz.bus.cortex.Vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Single watch subscription entry.
 *
 * @param <T> watched value type
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@AllArgsConstructor
public class WatchSubscription<T> {

    /** Vector that defines what this watch subscription is tracking. */
    private Vector vector;
    /** Listener to invoke when the watched values change. */
    private Listener<T> listener;
    /** Previous snapshot of watched values for diff calculation. */
    private List<T> lastSnapshot;
    /** Unix epoch milliseconds of the last access, used for expiry. */
    private long lastAccess;

}
