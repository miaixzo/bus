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
package org.miaixz.bus.sensitive.nimble;

import org.miaixz.bus.sensitive.Context;

/**
 * Non-instantiable marker used by strategy metadata to indicate an unresolved built-in desensitization strategy.
 * <p>
 * This marker has no masking type and cannot be registered or executed. Custom annotations must reference a concrete
 * {@link StrategyProvider}; built-in {@code Shield} processing selects providers directly by its masking type.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class BuiltInProvider extends AbstractProvider {

    /**
     * Constructor available only to marker subclasses.
     */
    public BuiltInProvider() {
        // No initialization required.
    }

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
