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
package org.miaixz.bus.image.metric.net;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ItemType {

    public static final int APP_CONTEXT = 0x10;
    public static final int RQ_PRES_CONTEXT = 0x20;
    public static final int AC_PRES_CONTEXT = 0x21;
    public static final int ABSTRACT_SYNTAX = 0x30;
    public static final int TRANSFER_SYNTAX = 0x40;
    public static final int USER_INFO = 0x50;
    public static final int MAX_PDU_LENGTH = 0x51;
    public static final int IMPL_CLASS_UID = 0x52;
    public static final int ASYNC_OPS_WINDOW = 0x53;
    public static final int ROLE_SELECTION = 0x54;
    public static final int IMPL_VERSION_NAME = 0x55;
    public static final int EXT_NEG = 0x56;
    public static final int COMMON_EXT_NEG = 0x57;
    public static final int RQ_USER_IDENTITY = 0x58;
    public static final int AC_USER_IDENTITY = 0x59;

}
