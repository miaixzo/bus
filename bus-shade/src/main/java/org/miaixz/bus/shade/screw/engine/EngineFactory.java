/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.screw.engine;

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
