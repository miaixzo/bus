/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.metric.internal.hl7;

import org.aoju.bus.core.exception.InternalException;

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
