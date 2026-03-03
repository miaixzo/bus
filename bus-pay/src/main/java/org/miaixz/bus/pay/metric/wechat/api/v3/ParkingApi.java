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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * WeChat Pay V3 API interfaces related to WeChat Pay Score parking services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ParkingApi implements Matcher {

    /**
     * Query vehicle service activation information.
     */
    VEHICLE_PARKING_SERVICES_FIND("/v3/vehicle/parking/services/find", "Query vehicle service activation information"),

    /**
     * Create parking entry.
     */
    VEHICLE_PARKING("/v3/vehicle/parking/parkings", "Create parking entry"),

    /**
     * Deduction acceptance.
     */
    VEHICLE_TRANSACTION_PARKING("/v3/vehicle/transactions/parking", "Deduction acceptance"),

    /**
     * Query order by out trade number.
     */
    VEHICLE_TRANSACTION_QUERY_BY_OUT_TRADE_NO("/v3/vehicle/transactions/out-trade-no/%s",
            "Query order by out trade number");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new ParkingApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    ParkingApi(String method, String desc) {
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
