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
 * Payment methods for WeChat Pay.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum TradeType {

    /**
     * WeChat Official Account Payment or Mini Program Payment.
     */
    JSAPI("JSAPI"),
    /**
     * WeChat Scan Code Payment.
     */
    NATIVE("NATIVE"),
    /**
     * WeChat APP Payment.
     */
    APP("APP"),
    /**
     * Payment Code Payment.
     */
    MICROPAY("MICROPAY"),
    /**
     * H5 Payment.
     */
    MWEB("MWEB");

    /**
     * The trade type string.
     */
    private final String tradeType;

    /**
     * Constructs a new TradeType.
     *
     * @param tradeType The trade type string.
     */
    TradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    /**
     * Gets the trade type string.
     *
     * @return The trade type string.
     */
    public String getTradeType() {
        return tradeType;
    }

}
