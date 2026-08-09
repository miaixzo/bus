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
package org.miaixz.bus.fabric.registry.connection;

/**
 * Pure physical-connection admission rules for {@link ConnectionPool}.
 *
 * @author Kimi Liu
 */
final class PoolAdmission {

    /**
     * Prevents instantiation.
     */
    private PoolAdmission() {
        // No initialization required.
    }

    /**
     * Decides whether another physical connection may be reserved.
     *
     * @param physicalTotal       current physical connections
     * @param creatingTotal       in-flight connection creations
     * @param physicalDestination physical connections for the destination
     * @param creatingDestination in-flight creations for the destination
     * @param maximumTotal        global physical limit
     * @param maximumDestination  destination physical limit
     * @param normalDestination   normal pre-expansion destination limit
     * @param provenHttp1         whether protocol negotiation proved HTTP/1
     * @param provenMultiplex     whether protocol negotiation proved multiplexing
     * @param sustainedWait       whether H1 queue pressure permits expansion
     * @return true when creation is admitted
     */
    static boolean allows(
            final int physicalTotal,
            final int creatingTotal,
            final int physicalDestination,
            final int creatingDestination,
            final int maximumTotal,
            final int maximumDestination,
            final int normalDestination,
            final boolean provenHttp1,
            final boolean provenMultiplex,
            final boolean sustainedWait) {
        final int destinationTotal = physicalDestination + creatingDestination;
        if (physicalDestination == 0 && creatingDestination > 0
                || !provenHttp1 && provenMultiplex && destinationTotal > 0) {
            return false;
        }
        return physicalTotal + creatingTotal < maximumTotal && destinationTotal < maximumDestination
                && (destinationTotal < normalDestination || sustainedWait);
    }

}
