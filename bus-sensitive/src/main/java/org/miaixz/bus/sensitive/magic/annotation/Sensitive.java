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
package org.miaixz.bus.sensitive.magic.annotation;

import org.miaixz.bus.sensitive.Builder;

import java.lang.annotation.*;

/**
 * A comprehensive annotation for enabling data desensitization and/or encryption at the class or method level. This can
 * be applied at various layers:
 * <ol>
 * <li><b>Database Level:</b> Using handlers like {@code SensitiveResultSetHandler} for decryption/desensitization on
 * read, and {@code SensitiveStatementHandler} for encryption/desensitization on write.</li>
 * <li><b>Request Level:</b> Using AOP advice like {@code RequestBodyAdvice} for decryption/desensitization on incoming
 * requests, and {@code ResponseBodyAdvice} for encryption/desensitization on outgoing responses.</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /**
     * The data processing mode. Options are: 1. {@link Builder#ALL}: Enable both desensitization and
     * encryption/decryption. 2. {@link Builder#SENS}: Enable only desensitization. 3. {@link Builder#SAFE}: Enable only
     * encryption/decryption.
     *
     * @return The processing mode string.
     */
    String value() default Builder.ALL;

    /**
     * The data flow direction. Options are: 1. {@link Builder#ALL}: Apply rules in both directions (in and out). 2.
     * {@link Builder#IN}: Apply rules only on write/request. 3. {@link Builder#OUT}: Apply rules only on read/response.
     * 4. {@link Builder#OVERALL}: Apply rules for global encryption.
     *
     * @return The data flow stage string.
     */
    String stage() default Builder.ALL;

    /**
     * An array of specific field names to which the rules should be applied. Example: {@code {"id", "name"}}
     *
     * @return The array of field names to include.
     */
    String[] field() default {};

    /**
     * An array of specific field names to skip or ignore. Example: {@code {"created", "creator"}}
     *
     * @return The array of field names to skip.
     */
    String[] skip() default {};

    /**
     * Whether to process nested objects recursively.
     *
     * @return {@code true} to enable processing of nested objects, {@code false} otherwise. Defaults to {@code true}.
     */
    boolean inside() default true;

}
