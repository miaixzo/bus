/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.MethodKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.InEnum;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * int enum 校验
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InEnumMatcher implements Matcher<Object, InEnum> {

    @Override
    public boolean on(Object object, InEnum annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        Class<? extends Enum> enumClass = annotation.enumClass();
        try {
            Method method = enumClass.getMethod(annotation.method());
            Enum[] enums = enumClass.getEnumConstants();
            for (Enum e : enums) {
                Object value = MethodKit.invoke(e, method);
                if (Objects.equals(value, object)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchMethodException e) {
            throw new InternalException(e.getMessage());
        }
    }

}
