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
package org.miaixz.bus.pay;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.pay.magic.Voucher;

/**
 * Common interface for {@code Provider}. All platform {@code Provider}s need to implement this interface.
 *
 * @param <T> The global object type, extending {@link Voucher}.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Provider<T extends Voucher> extends org.miaixz.bus.core.Provider {

    /**
     * Returns the type of this provider.
     *
     * @return The provider type, which is {@link EnumValue.Povider#PAY}.
     */
    @Override
    default Object type() {
        return EnumValue.Povider.PAY;
    }

}
