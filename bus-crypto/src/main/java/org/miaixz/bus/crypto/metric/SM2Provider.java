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
package org.miaixz.bus.crypto.metric;

import org.bouncycastle.crypto.engines.SM2Engine;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;
import org.miaixz.bus.crypto.center.SM2;

/**
 * SM2 encryption and decryption algorithm provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SM2Provider implements Provider {

    /**
     * Encrypts the given content.
     *
     * @param key     The key.
     * @param content The content to be encrypted.
     * @return The encrypted result.
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        String[] array = StringKit.splitToArray(key, Symbol.COMMA);
        SM2 sm2 = new SM2(array[0], array[1]);
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        return sm2.encrypt(content, KeyType.valueOf(array[2]));
    }

    /**
     * Decrypts the given content.
     *
     * @param key     The key.
     * @param content The content to be decrypted.
     * @return The decrypted result.
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        String[] array = StringKit.splitToArray(key, Symbol.COMMA);
        SM2 sm2 = new SM2(array[0], array[1]);
        sm2.setMode(SM2Engine.Mode.C1C3C2);
        return sm2.decrypt(content, KeyType.valueOf(array[2]));
    }

}
