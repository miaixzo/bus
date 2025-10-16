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
 * @since Java 17+
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
