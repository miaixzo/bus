/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
