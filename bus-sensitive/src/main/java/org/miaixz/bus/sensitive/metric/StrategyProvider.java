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
package org.miaixz.bus.sensitive.metric;

import org.miaixz.bus.core.Provider;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.sensitive.Context;

/**
 * An interface for defining a custom data desensitization strategy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface StrategyProvider extends Provider {

    /**
     * Applies the desensitization logic to the given object.
     *
     * @param object  The original object/value to be desensitized.
     * @param context The current desensitization context, providing access to field annotations and other details.
     * @return The desensitized value, typically a string.
     */
    Object build(final Object object, final Context context);

    /**
     * Returns an identifier for the type or strategy that this provider supports.
     * <p>
     * Description inherited from parent interface.
     *
     * @return the provider type, which is always {@link EnumValue.Povider#SENSITIVE}
     */
    @Override
    default Object type() {
        return EnumValue.Povider.SENSITIVE;
    }

}
