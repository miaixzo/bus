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
