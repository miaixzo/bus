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
package org.miaixz.bus.image.metric.hl7.api;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.hl7.net.HL7ApplicationInfo;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public interface HL7Configuration {

    boolean registerHL7Application(String name) throws InternalException;

    void unregisterHL7Application(String name) throws InternalException;

    HL7Application findHL7Application(String name) throws InternalException;

    String[] listRegisteredHL7ApplicationNames() throws InternalException;

    /**
     * 查询具有指定属性的HL7应用程序
     *
     * @param keys HL7应用程序属性，该属性应匹配或为*以获取所有已配置的HL7应用程序的信息
     * @return 具有匹配属性的已配置HL7 Application *的HL7ApplicationInfo对象的数组
     * @throws InternalException 异常
     */
    HL7ApplicationInfo[] listHL7AppInfos(HL7ApplicationInfo keys) throws InternalException;

}
