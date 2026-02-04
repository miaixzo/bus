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
package org.miaixz.bus.image.galaxy.data;

import org.miaixz.bus.core.lang.Normal;

/**
 * 实现类，提供DICOM实现相关的UID和版本名称。 该类用于标识DICOM实现的唯一标识符和版本信息，符合DICOM标准中的要求。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Implementation {

    /**
     * 实现类UID，用于唯一标识此DICOM实现
     */
    private static final String IMPL_CLASS_UID = "1.3.51.0.42.1.1";

    /**
     * 实现版本名称，用于标识此DICOM实现的版本
     */
    private static final String IMPL_VERS_NAME = versionName();

    /**
     * 生成版本名称
     *
     * @return 版本名称字符串，最大长度为16个字符
     */
    private static String versionName() {
        StringBuilder sb = new StringBuilder(Normal._16);
        sb.append("miaixz-");
        sb.append(Implementation.class.getPackage().getImplementationVersion());
        return sb.substring(0, Math.min(16, sb.length()));
    }

    /**
     * 获取实现类UID
     *
     * @return 实现类UID字符串
     */
    public static String getClassUID() {
        return IMPL_CLASS_UID;
    }

    /**
     * 获取实现版本名称
     *
     * @return 实现版本名称字符串
     */
    public static String getVersionName() {
        return IMPL_VERS_NAME;
    }

}
