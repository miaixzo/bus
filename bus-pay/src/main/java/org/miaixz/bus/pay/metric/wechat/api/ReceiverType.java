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
package org.miaixz.bus.pay.metric.wechat.api;

/**
 * Enumerates the types of receivers for profit sharing in WeChat Pay.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ReceiverType {

    /**
     * Merchant ID.
     */
    MERCHANT("MERCHANT_ID"),
    /**
     * Personal WeChat ID.
     */
    WECHATID("PERSONAL_WECHATID"),
    /**
     * Personal OpenID (converted from parent merchant appId).
     */
    OPENID("PERSONAL_OPENID"),
    /**
     * Personal sub_openid (converted from sub-merchant appId).
     */
    SUB_OPENID("PERSONAL_SUB_OPENID");

    /**
     * The type identifier string.
     */
    private final String type;

    /**
     * Constructs a new ReceiverType.
     *
     * @param type The type identifier string.
     */
    ReceiverType(String type) {
        this.type = type;
    }

    /**
     * Gets the type identifier string.
     *
     * @return The type identifier string.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the type identifier string.
     *
     * @return The type identifier string.
     */
    @Override
    public String toString() {
        return type;
    }

}
