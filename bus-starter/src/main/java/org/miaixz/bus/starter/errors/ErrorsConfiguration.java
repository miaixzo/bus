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
package org.miaixz.bus.starter.errors;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import org.miaixz.bus.errors.BusErrorLogObserver;
import org.miaixz.bus.errors.ErrorBoundary;

/**
 * Connects primary Bus error records and isolated handler diagnostics to the logging provider for the Spring context.
 *
 * @author Kimi Liu
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "bus.errors.logging", name = "enabled", matchIfMissing = true)
public class ErrorsConfiguration {

    /**
     * Creates the standard error logging auto-configuration.
     */
    public ErrorsConfiguration() {
    }

    /**
     * Installs standard Bus error logging and releases this context's subscription on shutdown.
     *
     * @return subscription owned by the application context
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(name = "busErrorLogging")
    ErrorBoundary.Subscription busErrorLogging() {
        return BusErrorLogObserver.install();
    }

}
