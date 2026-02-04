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
package org.miaixz.bus.pay.metric.wechat.api.v2;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V2 API interfaces related to enterprise transfers.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum TransferApi implements Matcher {

    /**
     * Enterprise transfer.
     */
    TRANSFER("/mmpaymkttransfers/promotion/transfers", "Enterprise transfer"),

    /**
     * Query enterprise transfer.
     */
    GET_TRANSFER_INFO("/mmpaymkttransfers/gettransferinfo", "Query enterprise transfer"),

    /**
     * Pay to bank card.
     */
    TRANSFER_BANK("/mmpaysptrans/pay_bank", "Pay to bank card"),

    /**
     * Query pay to bank card.
     */
    GET_TRANSFER_BANK_INFO("/mmpaysptrans/query_bank", "Query pay to bank card"),

    /**
     * Get RSA encryption public key.
     */
    GET_PUBLIC_KEY("/risk/getpublickey", "Get RSA encryption public key"),

    /**
     * Pay to employee.
     */
    PAY_WWS_TRANS_2_POCKET("/mmpaymkttransfers/promotion/paywwsptrans2pocket", "Pay to employee"),

    /**
     * Query pay to employee record.
     */
    QUERY_WWS_TRANS_2_POCKET("/mmpaymkttransfers/promotion/querywwsptrans2pocket", "Query pay to employee record");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new TransferApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    TransferApi(String method, String desc) {
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
