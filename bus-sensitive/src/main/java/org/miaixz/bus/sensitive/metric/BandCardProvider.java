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

import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.sensitive.Context;
import org.miaixz.bus.sensitive.magic.annotation.Shield;

/**
 * A desensitization provider for bank card numbers. It masks the middle digits, keeping only the first 4 and last 4
 * digits visible. For example: {@code "6227038339383938"} becomes {@code "6227********3938"}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BandCardProvider extends AbstractProvider {

    /**
     * Applies bank card-specific desensitization logic to the provided value.
     *
     * @param object  The object containing the bank card number to be desensitized.
     * @param context The current desensitization context, providing access to field annotations and other details.
     * @return The desensitized bank card number, or null if the input is empty.
     */
    @Override
    public String build(Object object, Context context) {
        if (ObjectKit.isEmpty(object)) {
            return null;
        }
        final Shield shield = context.getShield();
        String bankCard = object.toString();
        return StringKit.left(bankCard, 4).concat(
                StringKit.removePrefix(
                        StringKit.padPre(StringKit.right(bankCard, 4), StringKit.length(bankCard), shield.shadow()),
                        StringKit.fill(3, shield.shadow())));
    }

}
