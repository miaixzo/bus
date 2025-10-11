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
package org.miaixz.bus.validate.magic.annotation;

import org.miaixz.bus.core.lang.exception.ValidateException;

import java.lang.annotation.*;

/**
 * An annotation for specifying a custom exception to be thrown upon validation failure, replacing the default
 * {@link ValidateException}.
 * <p>
 * This annotation can be used in several places to control exception handling at different levels:
 * <ul>
 * <li><strong>Global Exception:</strong> When used on a parameter of an intercepted method, it defines a global
 * exception for that validation context.</li>
 * <li><strong>Field-Specific Exception:</strong> When marked on a field within an object undergoing deep validation
 * (e.g., with {@code @Inside}), it specifies an exception for that particular field.</li>
 * <li><strong>Validator-Specific Exception:</strong> When used on a validation annotation's definition, it sets the
 * exception for that specific validator.</li>
 * </ul>
 * <strong>Exception Priority:</strong> When a validation fails, the exception to be thrown is determined in the
 * following order of precedence:
 * <ol>
 * <li>The global exception, if defined.</li>
 * <li>The field-specific exception, if defined.</li>
 * <li>The validator-specific exception, if defined on the validation annotation.</li>
 * </ol>
 * If none of the above are defined, a default {@link ValidateException} will be thrown.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD })
public @interface ValidEx {

    /**
     * The custom exception class to be thrown on validation failure.
     *
     * @return the exception class.
     */
    Class<? extends ValidateException> value() default ValidateException.class;

}
