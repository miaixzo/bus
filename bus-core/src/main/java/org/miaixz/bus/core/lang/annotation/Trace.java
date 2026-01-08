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
 * An annotation used for logging and tracing business operations. When applied to a method, it provides metadata that
 * can be used by an aspect or interceptor to create detailed log entries for auditing, monitoring, or debugging
 * purposes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD })
public @interface Trace {

    /**
     * The main title or a brief description of the business operation.
     *
     * @return The business title.
     */
    String value() default Normal.EMPTY;

    /**
     * A unique identifier for the business transaction or operation.
     *
     * @return The business ID.
     */
    String id() default Normal.EMPTY;

    /**
     * The business module or component to which this operation belongs.
     *
     * @return The business module name.
     */
    String module() default Normal.EMPTY;

    /**
     * The specific business function or feature being executed.
     *
     * @return The business function name.
     */
    String business() default Normal.EMPTY;

    /**
     * A description of the parameters involved in the operation. This can be used to provide additional context about
     * the data being processed.
     *
     * @return The parameter information.
     */
    String params() default Normal.EMPTY;

    /**
     * The category or type of the operator performing the action (e.g., "USER", "SYSTEM").
     *
     * @return The operator type.
     */
    String operator() default Normal.EMPTY;

    /**
     * A field for any additional or custom information that should be included in the trace log.
     *
     * @return The extended information.
     */
    String extend() default Normal.EMPTY;

    /**
     * A flag indicating whether to save the request parameters of the annotated method in the log.
     *
     * @return {@code true} to save request parameters, {@code false} otherwise.
     */
    boolean isSaveRequest() default true;

}
