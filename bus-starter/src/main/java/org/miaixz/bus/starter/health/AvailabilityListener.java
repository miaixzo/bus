/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
