/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.metric.register;

import org.aoju.bus.metric.manual.docs.annotation.ApiDocField;
import org.hibernate.validator.constraints.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.5.8
 * @since JDK 1.8++
 */
public class ValidationAnnotationFactory {

    private static Map<Class<?>, ValidationAnnotationBuilder<?>> store = new HashMap<>(64);

    static {
        new BaseValidationAnnotationBuilder<ApiDocField>() {
        };

        // validation-api-2.0.1.Final.jar下javax.validation.constraints
        new BaseValidationAnnotationBuilder<AssertFalse>() {
        };
        new BaseValidationAnnotationBuilder<AssertTrue>() {
        };
        new BaseValidationAnnotationBuilder<DecimalMax>() {
        };
        new BaseValidationAnnotationBuilder<DecimalMin>() {
        };
        new BaseValidationAnnotationBuilder<Digits>() {
        };
        new BaseValidationAnnotationBuilder<Email>() {
        };
        new BaseValidationAnnotationBuilder<Future>() {
        };
        new BaseValidationAnnotationBuilder<Max>() {
        };
        new BaseValidationAnnotationBuilder<Min>() {
        };
        new BaseValidationAnnotationBuilder<Negative>() {
        };
        new BaseValidationAnnotationBuilder<NegativeOrZero>() {
        };
        new BaseValidationAnnotationBuilder<NotBlank>() {
        };
        new BaseValidationAnnotationBuilder<NotEmpty>() {
        };
        new BaseValidationAnnotationBuilder<NotNull>() {
        };
        new BaseValidationAnnotationBuilder<Null>() {
        };
        new BaseValidationAnnotationBuilder<Past>() {
        };
        new BaseValidationAnnotationBuilder<PastOrPresent>() {
        };
        new BaseValidationAnnotationBuilder<Pattern>() {
        };
        new BaseValidationAnnotationBuilder<Positive>() {
        };
        new BaseValidationAnnotationBuilder<PositiveOrZero>() {
        };
        new BaseValidationAnnotationBuilder<Size>() {
        };

        // hibernate-validator-6.0.10.Final.jar下org.hibernate.validator.constraints
        new BaseValidationAnnotationBuilder<CodePointLength>() {
        };
        new BaseValidationAnnotationBuilder<ConstraintComposition>() {
        };
        new BaseValidationAnnotationBuilder<CreditCardNumber>() {
        };
        new BaseValidationAnnotationBuilder<Currency>() {
        };
        new BaseValidationAnnotationBuilder<EAN>() {
        };
        new BaseValidationAnnotationBuilder<ISBN>() {
        };
        new BaseValidationAnnotationBuilder<Length>() {
        };
        new BaseValidationAnnotationBuilder<LuhnCheck>() {
        };
        new BaseValidationAnnotationBuilder<Mod10Check>() {
        };
        new BaseValidationAnnotationBuilder<Mod11Check>() {
        };
        new BaseValidationAnnotationBuilder<ParameterScriptAssert>() {
        };
        new BaseValidationAnnotationBuilder<Range>() {
        };
        new BaseValidationAnnotationBuilder<SafeHtml>() {
        };
        new BaseValidationAnnotationBuilder<ScriptAssert>() {
        };
        new BaseValidationAnnotationBuilder<UniqueElements>() {
        };
        new BaseValidationAnnotationBuilder<URL>() {
        };
    }

    /**
     * 添加注解对应的构建器
     *
     * @param annoClass 注解信息
     * @param builder   构造器
     */
    public static void addBuilder(Class<?> annoClass, ValidationAnnotationBuilder<?> builder) {
        store.put(annoClass, builder);
    }

    public static ValidationAnnotationDefinition build(Annotation annotation) {
        Class<?> jsr303Anno = annotation.annotationType();
        ValidationAnnotationBuilder validationAnnotationBuilder = store.get(jsr303Anno);
        if (validationAnnotationBuilder == null) {
            return null;
        }
        return validationAnnotationBuilder.build(annotation);
    }

}
