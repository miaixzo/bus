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
package org.miaixz.bus.image.metric.pdu;

/**
 * Represents the PDUType type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
class PDUType {

    /**
     * The a associate rq value.
     */
    public static final int A_ASSOCIATE_RQ = 0x01;

    /**
     * The a associate ac value.
     */
    public static final int A_ASSOCIATE_AC = 0x02;

    /**
     * The a associate rj value.
     */
    public static final int A_ASSOCIATE_RJ = 0x03;

    /**
     * The p data tf value.
     */
    public static final int P_DATA_TF = 0x04;

    /**
     * The a release rq value.
     */
    public static final int A_RELEASE_RQ = 0x05;

    /**
     * The a release rp value.
     */
    public static final int A_RELEASE_RP = 0x06;

    /**
     * The a abort value.
     */
    public static final int A_ABORT = 0x07;

}
