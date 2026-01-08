/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.pay.magic;

/**
 * Represents a basic currency.
 *
 * @author Kimi Liu
 * @since Java 17+
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
