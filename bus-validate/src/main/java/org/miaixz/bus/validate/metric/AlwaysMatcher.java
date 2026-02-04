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
package org.miaixz.bus.validate.metric;

import org.miaixz.bus.validate.Context;
import org.miaixz.bus.validate.magic.Matcher;

/**
 * A validator that always returns {@code true}. This can be useful for testing or for conditional validations where a
 * certain branch should always pass.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class AlwaysMatcher implements Matcher<Object, Object> {

    /**
     * Always returns {@code true}, indicating that the validation passes.
     *
     * @param object     The object to be validated (ignored).
     * @param annotation The annotation associated with the validation (ignored).
     * @param context    The validation context (ignored).
     * @return always {@code true}.
     */
    @Override
    public boolean on(Object object, Object annotation, Context context) {
        return true;
    }

}
