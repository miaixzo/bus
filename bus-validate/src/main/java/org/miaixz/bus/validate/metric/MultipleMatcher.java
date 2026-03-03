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
