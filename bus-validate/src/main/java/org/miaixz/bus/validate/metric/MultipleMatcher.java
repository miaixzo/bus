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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.Registry;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Multiple;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for the {@link Multiple} annotation, allowing multiple validation rules to be applied sequentially.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipleMatcher implements Matcher<Object, Multiple> {

    /**
     * Applies multiple validators to the given object. Validation stops and returns {@code false} upon the first
     * failure.
     *
     * @param object   The object to validate.
     * @param multiple The {@link Multiple} annotation instance, specifying the validators to apply.
     * @param context  The validation context.
     * @return {@code true} if the object passes all specified validations, {@code false} otherwise.
     * @throws NoSuchException if a specified validator cannot be found in the registry.
     */
    @Override
    public boolean on(Object object, Multiple multiple, Context context) {
        List<Matcher> validators = new ArrayList<>();
        for (String validatorName : multiple.value()) {
            if (!Registry.getInstance().contains(validatorName)) {
                throw new NoSuchException("Attempting to use a non-existent validator: " + validatorName);
            }
            validators.add((Matcher) Registry.getInstance().require(validatorName));
        }
        for (Class<? extends Matcher> clazz : multiple.classes()) {
            if (!Registry.getInstance().contains(clazz.getSimpleName())) {
                throw new NoSuchException("Attempting to use a non-existent validator: " + clazz.getName());
            }
            validators.add((Matcher) Registry.getInstance().require(clazz.getSimpleName()));
        }
        for (Matcher validator : validators) {
            if (!validator.on(object, null, context)) {
                return false;
            }
        }
        return true;
    }

}
