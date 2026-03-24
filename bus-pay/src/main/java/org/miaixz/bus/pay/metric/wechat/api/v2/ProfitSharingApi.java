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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to profit sharing.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ProfitSharingApi implements Matcher {

    /**
     * Request single profit sharing.
     */
    PROFIT_SHARING("/secapi/pay/profitsharing", "Request single profit sharing"),

    /**
     * Request multiple profit sharings.
     */
    MULTI_PROFIT_SHARING("/secapi/pay/multiprofitsharing", "Request multiple profit sharings"),

    /**
     * Query profit sharing result.
     */
    PROFIT_SHARING_QUERY("/pay/profitsharingquery", "Query profit sharing result"),

    /**
     * Add profit sharing receiver.
     */
    PROFIT_SHARING_ADD_RECEIVER("/pay/profitsharingaddreceiver", "Add profit sharing receiver"),

    /**
     * Remove profit sharing receiver.
     */
    PROFIT_SHARING_REMOVE_RECEIVER("/pay/profitsharingremovereceiver", "Remove profit sharing receiver"),

    /**
     * Finish profit sharing.
     */
    PROFIT_SHARING_FINISH("/secapi/pay/profitsharingfinish", "Finish profit sharing"),

    /**
     * Query order pending profit sharing amount.
     */
    PROFIT_SHARING_ORDER_AMOUNT_QUERY("/pay/profitsharingorderamountquery",
            "Query order pending profit sharing amount"),

    /**
     * Profit sharing return.
     */
    PROFIT_SHARING_RETURN("/secapi/pay/profitsharingreturn", "Profit sharing return"),

    /**
     * Query profit sharing return result.
     */
    PROFIT_SHARING_RETURN_QUERY("/pay/profitsharingreturnquery", "Query profit sharing return result");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new ProfitSharingApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    ProfitSharingApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * Gets the transaction type.
     *
     * @return The transaction type.
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * Gets the type description.
     *
     * @return The type description.
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * Gets the API method.
     *
     * @return The API method.
     */
    @Override
    public String method() {
        return this.method;
    }

}
