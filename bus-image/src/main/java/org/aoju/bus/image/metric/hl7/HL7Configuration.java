/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.metric.hl7;

import org.aoju.bus.core.lang.exception.InstrumentException;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public interface HL7Configuration {

    boolean registerHL7Application(String name) throws InstrumentException;

    void unregisterHL7Application(String name) throws InstrumentException;

    HL7Application findHL7Application(String name) throws InstrumentException;

    String[] listRegisteredHL7ApplicationNames() throws InstrumentException;

    /**
     * Query for HL7 Applications with specified attributes.
     *
     * @param keys HL7 Application attributes which shall match or null to
     *             get information for all configured HL7 Applications
     * @return array of  HL7ApplicationInfo objects for configured HL7 Application
     * with matching attributes
     * @throws InstrumentException exception
     */
    HL7ApplicationInfo[] listHL7AppInfos(HL7ApplicationInfo keys) throws InstrumentException;

}
