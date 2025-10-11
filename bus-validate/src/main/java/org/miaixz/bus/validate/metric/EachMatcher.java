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

import org.miaixz.bus.core.lang.exception.NoSuchException;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.Provider;
import org.miaixz.bus.validate.Registry;
import org.miaixz.bus.validate.magic.Matcher;
import org.miaixz.bus.validate.magic.annotation.Each;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Validator for performing validation on each element of a container (Array, Collection, or Map).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EachMatcher implements Matcher<Object, Each> {

    /**
     * Validates each element of the given container (array, collection, or map values) against a set of validators.
     *
     * @param object     The container object to validate.
     * @param annotation The {@link Each} annotation instance, which specifies the validators to apply.
     * @param context    The validation context.
     * @return {@code true} if all elements in the container pass all specified validations, {@code false} otherwise.
     * @throws NoSuchException if a specified validator cannot be found in the registry.
     */
    @Override
    public boolean on(Object object, Each annotation, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return true; // Empty or null containers are considered valid.
        }
        List<Matcher> list = new ArrayList<>();
        for (String name : annotation.value()) {
            if (!Registry.getInstance().contains(name)) {
                throw new NoSuchException("Attempting to use a non-existent validator: " + name);
            }
            list.add((Matcher) Registry.getInstance().require(name));
        }
        for (Class<? extends Matcher> clazz : annotation.classes()) {
            if (!Registry.getInstance().contains(clazz.getSimpleName())) {
                throw new NoSuchException("Attempting to use a non-existent validator: " + clazz.getName());
            }
            list.add((Matcher) Registry.getInstance().require(clazz.getSimpleName()));
        }

        if (Provider.isArray(object)) {
            for (Object item : (Object[]) object) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }

        } else if (Provider.isCollection(object)) {
            for (Object item : (Collection<?>) object) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }
        } else if (Provider.isMap(object)) {
            for (Object item : ((Map) object).values()) {
                if (!fastValidate(list, item, context)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Executes multiple validators on an object in a fail-fast manner.
     *
     * @param validators The list of validators to apply.
     * @param object     The object to validate.
     * @param context    The validation context.
     * @return {@code true} if the object passes all validations, {@code false} as soon as one validation fails.
     */
    private boolean fastValidate(List<Matcher> validators, Object object, Context context) {
        for (Matcher validator : validators) {
            if (!validator.on(object, null, context)) {
                return false;
            }
        }
        return true;
    }

}
