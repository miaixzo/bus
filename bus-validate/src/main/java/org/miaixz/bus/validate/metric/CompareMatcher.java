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
package org.miaixz.bus.validate.metric;

import java.math.BigDecimal;

import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Compare;

/**
 * Validator for comparing the value of the annotated field with another field. It supports both numeric and string
 * comparisons. <strong>Note:</strong> This validator assumes that the object passed to the {@code on} method is the
 * containing bean, not the field value itself, to access the other field for comparison.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompareMatcher implements Matcher<Object, Compare> {

    /**
     * Performs the comparison validation.
     *
     * @param object     The object to be validated. This is expected to be the bean containing the fields to compare.
     * @param annotation The {@link Compare} annotation instance, which specifies the comparison details.
     * @param context    The validation context (ignored).
     * @return {@code true} if the comparison is successful according to the specified condition, {@code false}
     *         otherwise.
     */
    @Override
    public boolean on(Object object, Compare annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return false;
        }
        boolean _matched = true;
        Object value = FieldKit.getFieldValue(object, annotation.with());

        if (value instanceof String) {
            if (MathKit.isNumber(value.toString())) {
                int _compValue = new BigDecimal(value.toString()).compareTo(new BigDecimal(object.toString()));
                switch (annotation.cond()) {
                    case EQ:
                        _matched = _compValue == 0;
                        break;

                    case NE:
                        _matched = _compValue != 0;
                        break;

                    case GT:
                        _matched = _compValue > 0;
                        break;

                    case LT:
                        _matched = _compValue < 0;
                        break;

                    case GE:
                        _matched = _compValue >= 0;
                        break;

                    case LE:
                        _matched = _compValue <= 0;
                        break;

                    default:

                }
            } else {
                switch (annotation.cond()) {
                    case EQ:
                        _matched = StringKit.equals(value.toString(), object.toString());
                        break;

                    case NE:
                        _matched = !StringKit.equals(value.toString(), object.toString());
                        break;

                    default:
                }
            }
        }
        return _matched;
    }

}
