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
package org.miaixz.bus.cortex.magic.watch;

import java.util.Objects;

import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.setting.watch.Watch;

/**
 * Separates runtime watch records from the generic transport Watch type.
 *
 * @author Kimi Liu
 */
final class WatchProjection {

    /**
     * Prevents construction of the stateless projection.
     */
    private WatchProjection() {
        // No initialization required.
    }

    /**
     * Identifies a runtime watch record before applying generic transport filtering.
     *
     * @param value the candidate emitted object
     * @return true for a persisted runtime watch
     */
    static boolean runtime(Object value) {
        return value instanceof Watch;
    }

    /**
     * Applies the runtime watch selector's tenant and supported-coordinate restrictions.
     *
     * @param vector the subscription selector
     * @param value  the runtime watch record
     * @return true when the selector permits the record; key matching remains with the manager
     */
    static boolean permits(Vector vector, Object value) {
        Watch watch = (Watch) value;
        return Objects.equals(vector.getType(), Type.WATCH.getKey())
                && (vector.getTenant_id() == null || Objects.equals(vector.getTenant_id(), watch.getTenant_id()))
                && vector.getApp_id() == null && vector.getMethod() == null && vector.getVersion() == null
                && vector.getState() == null && (vector.getLabels() == null || vector.getLabels().isEmpty())
                && (vector.getSelectors() == null || vector.getSelectors().isEmpty());
    }

}
