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
package org.miaixz.bus.pay.magic;

/**
 * Represents a basic currency.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Currency {

    /**
     * Chinese Yuan.
     */
    CNY("人民币"),
    /**
     * United States Dollar.
     */
    USD("美元"),
    /**
     * Hong Kong Dollar.
     */
    HKD("港币"),
    /**
     * Macanese Pataca.
     */
    MOP("澳门元"),
    /**
     * Euro.
     */
    EUR("欧元"),
    /**
     * New Taiwan Dollar.
     */
    TWD("新台币"),
    /**
     * South Korean Won.
     */
    KRW("韩元"),
    /**
     * Japanese Yen.
     */
    JPY("日元"),
    /**
     * Singapore Dollar.
     */
    SGD("新加坡元"),
    /**
     * Australian Dollar.
     */
    AUD("澳大利亚元");

    /**
     * The name of the currency.
     */
    private final String name;

    /**
     * Constructs a new Currency.
     *
     * @param name The name of the currency.
     */
    Currency(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the currency.
     *
     * @return The name of the currency.
     */
    public String getName() {
        return name;
    }

}
