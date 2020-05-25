/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.image.metric.internal.xdsi;

import javax.xml.bind.annotation.*;

/**
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtrinsicObjectType", propOrder = {"contentVersionInfo"})
public class ExtrinsicObjectType extends RegistryObjectType {

    @XmlElement(name = "ContentVersionInfo")
    protected VersionInfoType contentVersionInfo;
    @XmlAttribute(name = "mimeType")
    protected String mimeType;
    @XmlAttribute(name = "isOpaque")
    protected Boolean isOpaque;

    public VersionInfoType getContentVersionInfo() {
        return this.contentVersionInfo;
    }

    public void setContentVersionInfo(VersionInfoType value) {
        this.contentVersionInfo = value;
    }

    public String getMimeType() {
        if (this.mimeType == null) {
            return "application/octet-stream";
        }
        return this.mimeType;
    }

    public void setMimeType(String value) {
        this.mimeType = value;
    }

    public boolean isIsOpaque() {
        if (this.isOpaque == null) {
            return false;
        }
        return this.isOpaque.booleanValue();
    }

    public void setIsOpaque(Boolean value) {
        this.isOpaque = value;
    }

}
