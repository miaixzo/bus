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
package org.miaixz.bus.starter.office;

import lombok.RequiredArgsConstructor;
import org.miaixz.bus.office.Provider;
import org.miaixz.bus.office.Registrar;
import org.springframework.stereotype.Component;

/**
 * Service for providing online document viewing capabilities.
 * <p>
 * This service is responsible for registering different document conversion providers (e.g., for local and online
 * conversions) into a central registry upon application startup.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Component
@RequiredArgsConstructor
public class OfficeService {

    /**
     * Constructs the OfficeService and registers the local and online providers.
     * <p>
     * This constructor is automatically called by Spring, which injects the configured local and online provider beans.
     * These providers are then registered with the global {@link Registrar}.
     * </p>
     *
     * @param localProvider  The provider for local document conversions.
     * @param onlineProvider The provider for online or remote document conversions.
     */
    public OfficeService(Provider localProvider, Provider onlineProvider) {
        Registrar.getInstance().register(Registrar.LOCAL, localProvider);
        Registrar.getInstance().register(Registrar.ONLINE, onlineProvider);
    }

}
