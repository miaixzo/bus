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
package org.miaixz.bus.shade.screw.engine;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;

import lombok.Getter;
import lombok.Setter;

/**
 * A factory for creating {@link TemplateEngine} instances based on the provided configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class EngineFactory implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * The engine configuration.
     */
    private EngineConfig engineConfig;

    /**
     * Constructs a new {@code EngineFactory} with the specified engine configuration.
     *
     * @param configuration The {@link EngineConfig} to use. Must not be null.
     */
    public EngineFactory(EngineConfig configuration) {
        Assert.notNull(configuration, "EngineConfig can not be empty!");
        this.engineConfig = configuration;
    }

    /**
     * Private default constructor to prevent direct instantiation.
     */
    private EngineFactory() {
    }

    /**
     * Creates a new instance of a {@link TemplateEngine} implementation based on the configured template type. It
     * determines the implementation class from the engine configuration and instantiates it.
     *
     * @return A new {@link TemplateEngine} instance.
     * @throws InternalException if the engine class cannot be instantiated.
     */
    public TemplateEngine newInstance() {
        try {
            // Get the implementation class from the configuration.
            Class<? extends TemplateEngine> query = this.engineConfig.getProduceType().getImplClass();
            // Get the constructor that accepts an EngineConfig.
            Constructor<? extends TemplateEngine> constructor = query.getConstructor(EngineConfig.class);
            // Instantiate the engine class.
            return constructor.newInstance(engineConfig);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

}
