/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecificationLinkType", propOrder = {"usageDescription", "usageParameter"})
public class SpecificationLinkType extends RegistryObjectType {

    @XmlElement(name = "UsageDescription")
    protected InternationalStringType usageDescription;
    @XmlElement(name = "UsageParameter")
    protected List<String> usageParameter;
    @XmlAttribute(name = "serviceBinding", required = true)
    protected String serviceBinding;
    @XmlAttribute(name = "specificationObject", required = true)
    protected String specificationObject;

    public InternationalStringType getUsageDescription() {
        return this.usageDescription;
    }

    public void setUsageDescription(InternationalStringType value) {
        this.usageDescription = value;
    }

    public List<String> getUsageParameter() {
        if (null == this.usageParameter) {
            this.usageParameter = new ArrayList();
        }
        return this.usageParameter;
    }

    public String getServiceBinding() {
        return this.serviceBinding;
    }

    public void setServiceBinding(String value) {
        this.serviceBinding = value;
    }

    public String getSpecificationObject() {
        return this.specificationObject;
    }

    public void setSpecificationObject(String value) {
        this.specificationObject = value;
    }

}

