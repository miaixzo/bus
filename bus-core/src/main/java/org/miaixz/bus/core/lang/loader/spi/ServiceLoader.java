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
package org.miaixz.bus.core.lang.loader.spi;

import java.util.List;

/**
 * SPI (Service Provider Interface) service loading interface. Users implement this interface to define different ways
 * of loading services.
 *
 * @param <S> The type of the service object.
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ServiceLoader<S> extends Iterable<S> {

    /**
     * Loads the services.
     */
    void load();

    /**
     * Gets the total number of services.
     *
     * @return The total number of services.
     */
    int size();

    /**
     * Gets the list of service names.
     *
     * @return The list of service names.
     */
    List<String> getServiceNames();

    /**
     * Gets the implementation class for the specified service.
     *
     * @param serviceName The name of the service.
     * @return The implementation class corresponding to the service name.
     */
    Class<S> getServiceClass(String serviceName);

    /**
     * Gets the service corresponding to the specified name.
     *
     * @param serviceName The name of the service.
     * @return The service object.
     */
    S getService(String serviceName);

}
