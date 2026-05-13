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
package org.miaixz.bus.image.metric.api;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.metric.WebApplication;

/**
 * Defines the IWebApplicationCache contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface IWebApplicationCache {

    /**
     * Gets the stale timeout.
     *
     * @return the stale timeout.
     */
    int getStaleTimeout();

    /**
     * Sets the stale timeout.
     *
     * @param staleTimeout the stale timeout.
     */
    void setStaleTimeout(int staleTimeout);

    /**
     * Executes the clear operation.
     */
    void clear();

    /**
     * Executes the get operation.
     *
     * @param aet the aet.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    WebApplication get(String aet) throws InternalException;

    /**
     * Finds the web application.
     *
     * @param name the name.
     * @return the operation result.
     * @throws InternalException if the operation cannot be completed.
     */
    WebApplication findWebApplication(String name) throws InternalException;

}
