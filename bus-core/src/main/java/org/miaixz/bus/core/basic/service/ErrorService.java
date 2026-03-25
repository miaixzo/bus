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
package org.miaixz.bus.core.basic.service;

/**
 * An interface for handling exceptions. If no implementations are found, a default implementation is used.
 * <p>
 * This interface can be implemented to define custom business logic for exception handling. Implementations are
 * discovered using the Service Provider Interface (SPI) mechanism. To add a custom implementation, create a file named
 * {@code org.miaixz.bus.core.basic.service.ErrorService} in the {@code META-INF/services} directory, and add the fully
 * qualified name of your implementation class to it.
 *
 * <p>
 * Example: in {@code META-INF/services/org.miaixz.bus.core.basic.service.ErrorService}
 * 
 * <pre>
 * <code>
 * org.miaixz.bus.xxx.BusinessErrorService
 * ...
 * </code>
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface ErrorService {

    /**
     * Called before the main request processing.
     *
     * @param ex The exception object.
     * @return {@code true} if the execution chain should continue, {@code false} otherwise.
     */
    default boolean before(Exception ex) {
        return true;
    }

    /**
     * Callback invoked after the main request processing is complete.
     *
     * @param ex The exception object.
     * @return {@code true} if the execution chain should continue, {@code false} otherwise.
     */
    default boolean after(Exception ex) {
        return true;
    }

}
