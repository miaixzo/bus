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
package org.miaixz.bus.tempus.temporal.workflow;

import java.time.Duration;

import org.miaixz.bus.logger.Logger;

import io.temporal.common.RetryOptions;

/**
 * Builds Temporal SDK retry options from {@link WorkflowBindingOptions}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultRetryOptionsFactory implements RetryOptionsFactory {

    /**
     * Creates the default retry options factory.
     */
    public DefaultRetryOptionsFactory() {
        // No initialization required.
    }

    /**
     * Creates retry options from unified workflow binding options.
     *
     * @param options      workflow binding options
     * @param activityName activity name used for diagnostics
     * @return retry options
     */
    @Override
    public RetryOptions createRetryOptions(WorkflowBindingOptions options, String activityName) {
        WorkflowBindingOptions effective = options == null ? WorkflowBindingOptions.defaults() : options;
        Logger.debug(
                true,
                "Tempus",
                "Temporal retry options creation started: activityName={}, initialIntervalSeconds={}, maxIntervalSeconds={}, maxAttempts={}",
                activityName,
                effective.resolveActivityRetryInitialIntervalSeconds(),
                effective.resolveActivityRetryMaxIntervalSeconds(),
                effective.resolveActivityRetryMaxAttempts());
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(effective.resolveActivityRetryInitialIntervalSeconds()))
                .setMaximumInterval(Duration.ofSeconds(effective.resolveActivityRetryMaxIntervalSeconds()))
                .setBackoffCoefficient(effective.resolveActivityRetryBackoffCoefficient())
                .setMaximumAttempts(effective.resolveActivityRetryMaxAttempts()).build();
        Logger.debug(
                false,
                "Tempus",
                "Temporal retry options creation completed: activityName={}, backoffCoefficient={}",
                activityName,
                effective.resolveActivityRetryBackoffCoefficient());
        return retryOptions;
    }

}
