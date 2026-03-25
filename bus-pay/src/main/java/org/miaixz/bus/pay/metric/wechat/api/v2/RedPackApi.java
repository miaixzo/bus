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
 * WeChat Pay V2 API interfaces related to red packets.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum RedPackApi implements Matcher {

    /**
     * Send red packet.
     */
    SEND_RED_PACK("/mmpaymkttransfers/sendredpack", "Send red packet"),

    /**
     * Send group red packet.
     */
    SEND_GROUP_RED_PACK("/mmpaymkttransfers/sendgroupredpack", "Send group red packet"),

    /**
     * Query red packet record.
     */
    GET_HB_INFO("/mmpaymkttransfers/gethbinfo", "Query red packet record"),

    /**
     * Mini Program send red packet.
     */
    SEND_MINI_PROGRAM_HB("/mmpaymkttransfers/sendminiprogramhb", "Mini Program send red packet"),

    /**
     * Send corporate red packet.
     */
    SEND_WORK_WX_RED_PACK("/mmpaymkttransfers/sendworkwxredpack", "Send corporate red packet"),

    /**
     * Query corporate red packet record.
     */
    QUERY_WORK_WX_RED_PACK("/mmpaymkttransfers/queryworkwxredpack", "Query corporate red packet record");

    /**
     * The API method.
     */
    private final String method;

    /**
     * The API description.
     */
    private final String desc;

    /**
     * Constructs a new RedPackApi enum.
     *
     * @param method The API method.
     * @param desc   The API description.
     */
    RedPackApi(String method, String desc) {
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
