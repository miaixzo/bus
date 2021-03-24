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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.Duration;

/**
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistryType")
public class RegistryType extends RegistryObjectType {
    @XmlAttribute(name = "operator", required = true)
    protected String operator;
    @XmlAttribute(name = "specificationVersion", required = true)
    protected String specificationVersion;
    @XmlAttribute(name = "replicationSyncLatency")
    protected Duration replicationSyncLatency;
    @XmlAttribute(name = "catalogingLatency")
    protected Duration catalogingLatency;
    @XmlAttribute(name = "conformanceProfile")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String conformanceProfile;

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String value) {
        this.operator = value;
    }

    public String getSpecificationVersion() {
        return this.specificationVersion;
    }

    public void setSpecificationVersion(String value) {
        this.specificationVersion = value;
    }

    public Duration getReplicationSyncLatency() {
        return this.replicationSyncLatency;
    }

    public void setReplicationSyncLatency(Duration value) {
        this.replicationSyncLatency = value;
    }

    public Duration getCatalogingLatency() {
        return this.catalogingLatency;
    }

    public void setCatalogingLatency(Duration value) {
        this.catalogingLatency = value;
    }

    public String getConformanceProfile() {
        if (this.null == conformanceProfile) {
            return "registryLite";
        }
        return this.conformanceProfile;
    }

    public void setConformanceProfile(String value) {
        this.conformanceProfile = value;
    }

}
