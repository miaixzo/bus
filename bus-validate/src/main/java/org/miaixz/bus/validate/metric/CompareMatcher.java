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
