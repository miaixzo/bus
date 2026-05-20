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
package org.miaixz.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The health check status enum.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum HealthCheckStatus {

    /**
     * The ok health check status.
     */
    OK,
    /**
     * The failed health check status.
     */
    FAILED;

    private static JacksonJsonEnumHelper<HealthCheckStatus> enumHelper = new JacksonJsonEnumHelper<>(
            HealthCheckStatus.class);

    /**
     * Returns the value.
     *
     * @param value the value value
     * @return the result
     */

    @JsonCreator
    public static HealthCheckStatus forValue(String value) {
        return enumHelper.forValue(value);
    }

    /**
     * Returns the value.
     *
     * @return the result
     */

    @JsonValue
    public String toValue() {
        return enumHelper.toString(this);
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return enumHelper.toString(this);
    }

}
