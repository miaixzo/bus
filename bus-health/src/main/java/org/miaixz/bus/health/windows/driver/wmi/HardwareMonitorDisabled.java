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
package org.miaixz.bus.health.windows.driver.wmi;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;

/**
 * Tests whether hardware-monitor WMI data sources are disabled by configuration.
 *
 * <p>
 * Open Hardware Monitor and Libre Hardware Monitor publish data only while running, so bus-health queries them on every
 * sensor read. These switches allow applications that do not use either monitor to suppress those queries.
 *
 * @author Kimi Liu
 * @see Builder#_WINDOWS_OHM_DISABLED
 * @see Builder#_WINDOWS_LHM_DISABLED
 */
@ThreadSafe
public final class HardwareMonitorDisabled {

    /**
     * Prevents construction of this utility class.
     */
    private HardwareMonitorDisabled() {
        throw new AssertionError();
    }

    /**
     * Tests whether queries to a hardware monitor's WMI namespace are disabled.
     *
     * @param namespace the WMI namespace, either {@link OhmHardware#OHM_NAMESPACE} or {@link LhmSensor#LHM_NAMESPACE}
     * @return {@code true} if the namespace should not be queried, otherwise {@code false}
     */
    public static boolean isWmiDisabled(String namespace) {
        if (OhmHardware.OHM_NAMESPACE.equals(namespace)) {
            return Builder.get(Builder._WINDOWS_OHM_DISABLED, false);
        }
        if (LhmSensor.LHM_NAMESPACE.equals(namespace)) {
            return Builder.get(Builder._WINDOWS_LHM_DISABLED, false);
        }
        return false;
    }

}
