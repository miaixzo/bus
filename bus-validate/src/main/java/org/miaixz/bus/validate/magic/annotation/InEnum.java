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

import org.miaixz.bus.validate.Builder;
import org.miaixz.bus.validate.metric.InEnumMatcher;

import java.lang.annotation.*;

/**
 * Validates that the annotated object is a member of the specified enum. By default, the object is matched against the
 * enum constant's name.
 *
 * <p>
 * By default, if the object to be validated is null, the validation passes.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Complex(value = Builder._IN_ENUM, clazz = InEnumMatcher.class)
public @interface InEnum {

    /**
     * The enum class to check against.
     *
     * @return the enum class.
     */
    @Filler("enumClass")
    Class<? extends Enum> enumClass();

    /**
     * The method to invoke on the enum constant. The result of this method will be compared with the validated object
     * for equality. Defaults to "name", which compares against the enum constant's name.
     *
     * @return the name of the method.
     */
    String method() default "name";

    /**
     * The error code to be used when validation fails.
     *
     * @return the error code.
     */
    String errcode() default Builder.DEFAULT_ERRCODE;

    /**
     * The error message to be used when validation fails. The message can be a template with placeholders.
     *
     * @return the error message.
     */
    String errmsg() default "${field} must be a value of the specified enum type: ${enumClass}";

    /**
     * The validation groups this constraint belongs to.
     *
     * @return an array of group names.
     */
    String[] group() default {};

    /**
     * The name of the field being validated.
     *
     * @return the field name.
     */
    String field() default Builder.DEFAULT_FIELD;

}
