/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.annotation;

import java.lang.annotation.*;

import org.miaixz.bus.core.lang.Normal;

/**
 * Marks an interface as a Service Provider Interface (SPI), enabling a flexible mechanism for framework extension and
 * component replacement. This annotation facilitates a service provider discovery mechanism where an interface is
 * defined and third parties can provide implementations.
 * <p>
 * This SPI mechanism offers several enhancements over the standard Java {@link java.util.ServiceLoader}:
 * 
 * <pre>
 *   1. Supports both singleton and prototype scopes for implementation classes.
 *   2. Allows setting a default implementation class.
 *   3. Supports ordering of implementation classes via the {@link Order} annotation.
 *   4. Supports defining a 'category' attribute to distinguish between different implementation types.
 *   5. Supports searching for implementations based on their category.
 *   6. Supports automatic scanning for implementation classes.
 *   7. Supports manually adding implementation classes.
 *   8. Supports retrieving all implementation classes.
 *   9. Supports lazy instantiation (only creating instances as needed), unlike the native JDK SPI which instantiates all found services.
 *  10. Supports using a custom ClassLoader to load implementation classes.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {

    /**
     * Specifies the name or ID of the default implementation to be used when no specific implementation is requested.
     *
     * @return The identifier of the default implementation.
     */
    String value() default Normal.EMPTY;

    /**
     * Declares whether a single instance of the service implementation should be created and reused (singleton) or if a
     * new instance should be created for each request (prototype).
     *
     * @return {@code true} for a singleton scope, {@code false} for a prototype scope.
     */
    boolean single() default false;

}
