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
package org.miaixz.bus.image.metric.net;

/**
 * Represents the ItemType type.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ItemType {

    /**
     * The app context value.
     */
    public static final int APP_CONTEXT = 0x10;

    /**
     * The rq pres context value.
     */
    public static final int RQ_PRES_CONTEXT = 0x20;

    /**
     * The ac pres context value.
     */
    public static final int AC_PRES_CONTEXT = 0x21;

    /**
     * The abstract syntax value.
     */
    public static final int ABSTRACT_SYNTAX = 0x30;

    /**
     * The transfer syntax value.
     */
    public static final int TRANSFER_SYNTAX = 0x40;

    /**
     * The user info value.
     */
    public static final int USER_INFO = 0x50;

    /**
     * The max pdu length value.
     */
    public static final int MAX_PDU_LENGTH = 0x51;

    /**
     * The impl class uid value.
     */
    public static final int IMPL_CLASS_UID = 0x52;

    /**
     * The async ops window value.
     */
    public static final int ASYNC_OPS_WINDOW = 0x53;

    /**
     * The role selection value.
     */
    public static final int ROLE_SELECTION = 0x54;

    /**
     * The impl version name value.
     */
    public static final int IMPL_VERSION_NAME = 0x55;

    /**
     * The ext neg value.
     */
    public static final int EXT_NEG = 0x56;

    /**
     * The common ext neg value.
     */
    public static final int COMMON_EXT_NEG = 0x57;

    /**
     * The rq user identity value.
     */
    public static final int RQ_USER_IDENTITY = 0x58;

    /**
     * The ac user identity value.
     */
    public static final int AC_USER_IDENTITY = 0x59;

}
