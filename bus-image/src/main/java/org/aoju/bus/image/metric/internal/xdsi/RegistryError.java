/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.metric.internal.xdsi;

import javax.xml.bind.annotation.*;

/**
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"value"})
@XmlRootElement(name = "RegistryError", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0")
public class RegistryError {

    @XmlValue
    protected String value;
    @XmlAttribute(name = "codeContext", required = true)
    protected String codeContext;
    @XmlAttribute(name = "errorCode", required = true)
    protected String errorCode;
    @XmlAttribute(name = "severity")
    protected String severity;
    @XmlAttribute(name = "location")
    protected String location;

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCodeContext() {
        return this.codeContext;
    }

    public void setCodeContext(String value) {
        this.codeContext = value;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String value) {
        this.errorCode = value;
    }

    public String getSeverity() {
        if (this.severity == null) {
            return "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error";
        }
        return this.severity;
    }

    public void setSeverity(String value) {
        this.severity = value;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String value) {
        this.location = value;
    }

}
