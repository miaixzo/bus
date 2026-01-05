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
package org.miaixz.bus.logger.metric.apache.commons;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;

/**
 * A factory for creating {@link org.apache.commons.logging.Log} instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CommonsLoggingFactory extends AbstractFactory {

    /**
     * Constructs a new {@code CommonsLoggingFactory}. This factory is responsible for creating loggers based on the
     * Apache Commons Logging framework. It also checks for the existence of the
     * {@link org.apache.commons.logging.LogFactory} class.
     */
    public CommonsLoggingFactory() {
        super("org.apache.commons.logging.Log");
        exists(org.apache.commons.logging.LogFactory.class);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final String name) {
        return new CommonsLoggingProvider(name);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Provider of(final Class<?> clazz) {
        return new CommonsLoggingProvider(clazz);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    protected void exists(final Class<?> logClassName) {
        super.exists(logClassName);
        // This is to ensure that the logging framework is initialized.
        of(CommonsLoggingFactory.class);
    }

}
