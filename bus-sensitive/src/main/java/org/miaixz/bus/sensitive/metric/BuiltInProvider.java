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
package org.miaixz.bus.sensitive.metric;

import org.miaixz.bus.sensitive.Context;

/**
 * A marker class used to indicate that a built-in desensitization strategy should be used, based on the
 * {@code Builder.Type} specified in the {@code @Shield} annotation.
 * <p>
 * This class itself does not perform any desensitization; it acts as a placeholder to trigger the framework's internal
 * logic for selecting a pre-defined strategy. Do not use this for custom strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BuiltInProvider extends AbstractProvider {

    /**
     * This method is not intended to be called directly and returns null. The framework uses this class as a marker to
     * select a built-in strategy.
     *
     * @param object  The object to be desensitized.
     * @param context The desensitization context.
     * @return Always returns null.
     */
    @Override
    public Object build(Object object, Context context) {
        return null;
    }

}
