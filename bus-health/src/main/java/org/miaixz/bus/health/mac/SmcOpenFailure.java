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
package org.miaixz.bus.health.mac;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.logger.Logger;

/**
 * Reports why a connection to the SMC could not be opened, at most once per condition.
 * <p>
 * Sensor readings poll, and each one opens its own connection. When the service is permanently unavailable, an
 * unlatched message repeats for the life of the process. That is the normal state on a virtual machine, which does not
 * virtualize the SMC, so callers should see the condition once and then only debug-level repeats.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class SmcOpenFailure {

    /**
     * Latch for an absent AppleSMC service.
     */
    private final AtomicBoolean serviceNotFoundLogged = new AtomicBoolean();

    /**
     * Latch for a failed AppleSMC open call.
     */
    private final AtomicBoolean openFailedLogged = new AtomicBoolean();

    /**
     * Latch for a successful open call returning no connection handle.
     */
    private final AtomicBoolean nullConnectionLogged = new AtomicBoolean();

    /**
     * Creates a new SMC open failure reporter.
     */
    public SmcOpenFailure() {
        // No initialization required.
    }

    /**
     * Reports that the {@code AppleSMC} service could not be found, so no sensor can be read.
     */
    public void serviceNotFound() {
        if (serviceNotFoundLogged.compareAndSet(false, true)) {
            Logger.warn(
                    false,
                    "Health",
                    "Unable to locate the AppleSMC service; hardware sensors are unavailable. "
                            + "This is expected on a virtual machine, which does not virtualize the SMC.");
        } else {
            Logger.debug(false, "Health", "Unable to locate AppleSMC service");
        }
    }

    /**
     * Reports that the {@code AppleSMC} service was found but would not open.
     *
     * @param result the nonzero {@code kern_return_t} from {@code IOServiceOpen}
     */
    public void openFailed(int result) {
        String error = String.format(Locale.ROOT, "%08x", result);
        if (openFailedLogged.compareAndSet(false, true)) {
            Logger.error(false, "Health", "Unable to open connection to AppleSMC service. Error: 0x{}", error);
        } else {
            Logger.debug(false, "Health", "Unable to open connection to AppleSMC service. Error: 0x{}", error);
        }
    }

    /**
     * Reports that {@code IOServiceOpen} succeeded but handed back no connection to use.
     */
    public void nullConnection() {
        String message = "IOServiceOpen reported success but returned a null AppleSMC connection handle.";
        if (nullConnectionLogged.compareAndSet(false, true)) {
            Logger.error(false, "Health", message);
        } else {
            Logger.debug(false, "Health", message);
        }
    }

}
