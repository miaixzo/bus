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
package org.miaixz.bus.logger.magic;

import org.miaixz.bus.logger.Factory;

/**
 * Abstract base class for logger factories.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFactory implements Factory {

    /**
     * The name of the logging framework.
     */
    private final String name;

    /**
     * Constructs a new {@code AbstractFactory} with the specified name.
     *
     * @param name the name of the logging framework.
     */
    public AbstractFactory(final String name) {
        this.name = name;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Checks if the logging implementation exists.
     * <p>
     * This method is used to verify the presence of the required classes for a specific logging framework. It is called
     * during the automatic detection process (e.g., in {@code detectLogFactory}). If the implementation class does not
     * exist, this method should throw an exception (like {@code ClassNotFoundException}), which signals the detection
     * mechanism to try the next available logging framework.
     * </p>
     *
     * @param clazz the logging implementation class to check.
     */
    protected void exists(final Class<?> clazz) {
        // This method does nothing by default.
    }

}
