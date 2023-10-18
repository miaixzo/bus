/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.validate.strategy;

import org.aoju.bus.core.toolkit.MathKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.ReflectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.validate.Context;
import org.aoju.bus.validate.annotation.Compare;
import org.aoju.bus.validate.validators.Matcher;

import java.math.BigDecimal;

/**
 * 数据长度校验
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompareStrategy implements Matcher<Object, Compare> {

    @Override
    public boolean on(Object object, Compare annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        boolean _matched = true;
        Object value = ReflectKit.getFieldValue(object, annotation.with());

        if (value instanceof String) {
            if (MathKit.isNumber(value.toString())) {
                int _compValue = new BigDecimal(value.toString()).compareTo(new BigDecimal(object.toString()));
                switch (annotation.cond()) {
                    case EQ:
                        _matched = _compValue == 0;
                        break;
                    case NOT_EQ:
                        _matched = _compValue != 0;
                        break;
                    case GT:
                        _matched = _compValue > 0;
                        break;
                    case LT:
                        _matched = _compValue < 0;
                        break;
                    case GT_EQ:
                        _matched = _compValue >= 0;
                        break;
                    case LT_EQ:
                        _matched = _compValue <= 0;
                        break;
                    default:

                }
            } else {
                switch (annotation.cond()) {
                    case EQ:
                        _matched = StringKit.equals(value.toString(), object.toString());
                        break;
                    case NOT_EQ:
                        _matched = !StringKit.equals(value.toString(), object.toString());
                        break;
                    default:
                }
            }
        }
        return _matched;
    }

}