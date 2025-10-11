/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.health;

import org.miaixz.bus.logger.Logger;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A component that listens for application availability events.
 * <p>
 * Based on Spring's event listening mechanism, this class captures availability state changes and logs them. This is
 * useful for observing the application's lifecycle, especially in containerized environments where liveness and
 * readiness probes are critical.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
public class AvailabilityListener {

    /**
     * Listens for {@link AvailabilityChangeEvent} and logs the state transition. This method handles the following
     * states:
     * <ul>
     * <li>{@link LivenessState#CORRECT}: The application is live and running correctly.</li>
     * <li>{@link LivenessState#BROKEN}: The application is in a broken state, which may trigger a restart by the
     * orchestrator.</li>
     * <li>{@link ReadinessState#ACCEPTING_TRAFFIC}: The application is ready to accept traffic.</li>
     * <li>{@link ReadinessState#REFUSING_TRAFFIC}: The application is not ready to accept traffic, which may cause it
     * to be removed from the load balancer.</li>
     * </ul>
     *
     * @param event The availability state change event, containing the new state and a timestamp.
     */
    @EventListener
    public void onStateChange(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        AvailabilityState state = event.getState();
        long timestamp = event.getTimestamp();
        String stateName = state.toString();

        // Log the state change with its type and timestamp.
        switch (state) {
            case ReadinessState.ACCEPTING_TRAFFIC -> Logger
                    .debug("System is ready to accept traffic at {}: {}", timestamp, stateName);
            case ReadinessState.REFUSING_TRAFFIC -> Logger
                    .debug("System is refusing traffic at {}: {}", timestamp, stateName);
            case LivenessState.BROKEN -> Logger.debug("System is in a broken state at {}: {}", timestamp, stateName);
            case LivenessState.CORRECT -> Logger.debug("System is in a correct state at {}: {}", timestamp, stateName);
            default -> Logger.warn("Unknown availability state detected at {}: {}", timestamp, stateName);
        }
    }

}
