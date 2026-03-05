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
 * WeChat Pay V2 API interfaces related to deduction services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum EntrustPayApi implements Matcher {

    /**
     * Official Account pure contract signing.
     */
    ENTRUST_WEB("/papay/entrustweb", "Official Account pure contract signing"),

    /**
     * Official Account pure contract signing (service provider mode).
     */
    PARTNER_ENTRUST_WEB("/papay/partner/entrustweb", "Official Account pure contract signing (service provider mode)"),

    /**
     * APP pure contract signing.
     */
    PRE_ENTRUST_WEB("/papay/preentrustweb", "APP pure contract signing"),

    /**
     * APP pure contract signing (service provider mode).
     */
    PARTNER_PRE_ENTRUST_WEB("/papay/partner/preentrustweb", "APP pure contract signing (service provider mode)"),

    /**
     * H5 pure contract signing.
     */
    H5_ENTRUST_WEB("/papay/h5entrustweb", "H5 pure contract signing"),
    /**
     * H5 pure contract signing (service provider mode).
     */
    PARTNER_H5_ENTRUST_WEB("/papay/partner/h5entrustweb", "H5 pure contract signing (service provider mode)"),

    /**
     * Contract signing during payment.
     */
    PAY_CONTRACT_ORDER("/pay/contractorder", "Contract signing during payment"),

    /**
     * Query contract relationship.
     */
    QUERY_ENTRUST_CONTRACT("/papay/querycontract", "Query contract relationship"),

    /**
     * Query contract relationship (service provider mode).
     */
    PARTNER_QUERY_ENTRUST_CONTRACT("/papay/partner/querycontract",
            "Query contract relationship (service provider mode)"),

    /**
     * Apply for deduction.
     */
    PAP_PAY_APPLY("/pay/pappayapply", "Apply for deduction"),

    /**
     * Apply for deduction (service provider mode).
     */
    PARTNER_PAP_PAY_APPLY("/pay/partner/pappayapply", "Apply for deduction (service provider mode)"),

    /**
     * Apply to terminate a contract.
     */
    DELETE_ENTRUST_CONTRACT("/papay/deletecontract", "Apply to terminate a contract"),
    /**
     * Apply to terminate a contract (service provider mode).
     */
    PARTNER_DELETE_ENTRUST_CONTRACT("/papay/partner/deletecontract",
            "Apply to terminate a contract (service provider mode)");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new EntrustPayApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    EntrustPayApi(String method, String desc) {
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
