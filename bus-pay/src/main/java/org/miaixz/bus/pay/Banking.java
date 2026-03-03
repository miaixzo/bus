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

/**
 * Financial institutions. The ranking is not in any particular order and is based on the data provided by third-party
 * payment service providers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Banking {

    /**
     * China Merchants Bank
     */
    BK_1001("1001"),
    /**
     * Industrial and Commercial Bank of China
     */
    BK_1002("1002"),
    /**
     * China Construction Bank
     */
    BK_1003("1003"),
    /**
     * Shanghai Pudong Development Bank
     */
    BK_1004("1004"),
    /**
     * Agricultural Bank of China
     */
    BK_1005("1005"),
    /**
     * China Minsheng Bank
     */
    BK_1006("1006"),
    /**
     * Industrial Bank Co., Ltd.
     */
    BK_1009("1009"),
    /**
     * Ping An Bank
     */
    BK_1010("1010"),
    /**
     * Bank of Communications
     */
    BK_1020("1020"),
    /**
     * China CITIC Bank
     */
    BK_1021("1021"),
    /**
     * China Everbright Bank
     */
    BK_1022("1022"),
    /**
     * Bank of Shanghai
     */
    BK_1024("1024"),
    /**
     * Huaxia Bank
     */
    BK_1025("1025"),
    /**
     * Bank of China
     */
    BK_1026("1026"),

    /**
     * China Guangfa Bank
     */
    BK_1027("1027"),
    /**
     * Bank of Nanjing
     */
    BK_1054("1054"),
    /**
     * Bank of Ningbo
     */
    BK_1056("1056"),
    /**
     * Postal Savings Bank of China
     */
    BK_1066("1066"),
    /**
     * Shunde Rural Commercial Bank
     */
    BK_4036("4036"),
    /**
     * Zhejiang Tailong Commercial Bank
     */
    BK_4051("4051"),
    /**
     * Ningbo Yinzhou Rural Commercial Bank
     */
    BK_4052("4052"),
    /**
     * Jiangsu Zijin Rural Commercial Bank Co., Ltd.
     */
    BK_4072("4072"),
    /**
     * Shenzhen Rural Commercial Bank
     */
    BK_4076("4076"),
    /**
     * Shaanxi Rural Credit Cooperatives
     */
    BK_4108("4108"),
    /**
     * Guangxi Zhuang Autonomous Region Rural Credit Cooperatives
     */
    BK_4113("4113"),
    /**
     * Henan Rural Credit Cooperatives
     */
    BK_4115("4115"),
    /**
     * Ningxia Yellow River Rural Commercial Bank
     */
    BK_4150("4150"),
    /**
     * Tianjin Rural Commercial Bank
     */
    BK_4153("4153"),
    /**
     * Shanxi Rural Credit Cooperatives
     */
    BK_4156("4156"),
    /**
     * Gansu Rural Credit Cooperatives
     */
    BK_4157("4157"),
    /**
     * Anhui Rural Credit Cooperatives
     */
    BK_4166("4166"),
    /**
     * Bank of Changsha
     */
    BK_4216("4216"),
    /**
     * Jiangsu Rural Credit Cooperatives Union
     */
    BK_4217("4217"),
    /**
     * Bank of Hengshui
     */
    BK_4752("4752"),
    /**
     * Zhongyuan Bank
     */
    BK_4753("4753"),
    /**
     * Changzi County Ronghui Village Bank
     */
    BK_4755("4755"),
    /**
     * Bank of Changzhi
     */
    BK_4756("4756"),
    /**
     * Haikou Union Rural Commercial Bank Co., Ltd.
     */
    BK_4758("4758"),
    /**
     * Industrial Bank of Korea (China)
     */
    BK_4761("4761"),
    /**
     * Nanyang Commercial Bank (China), Limited
     */
    BK_4763("4763"),
    /**
     * Zhejiang Rural Credit Cooperatives Union
     */
    BK_4764("4764"),
    /**
     * Bank of Zaozhuang Co., Ltd.
     */
    BK_4766("4766"),
    /**
     * Bank of Datong
     */
    BK_4767("4767"),
    /**
     * Beijing Zhongguancun Bank Corporation Limited
     */
    BK_4769("4769"),
    /**
     * DBS Bank (China) Limited
     */
    BK_4778("4778"),
    /**
     * Bank of Beijing
     */
    BK_4836("4836");

    /**
     * The code of the financial institution.
     */
    private final String code;

    /**
     * Constructs a new Banking enum.
     *
     * @param code The code of the financial institution.
     */
    Banking(String code) {
        this.code = code;
    }

    /**
     * Gets the code of the financial institution.
     *
     * @return The code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the code of the financial institution.
     *
     * @return The code.
     */
    @Override
    public String toString() {
        return code;
    }

}
