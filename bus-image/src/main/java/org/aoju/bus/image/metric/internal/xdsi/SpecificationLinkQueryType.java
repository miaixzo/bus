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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpecificationLinkQueryType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {"usageDescriptionBranch", "serviceBindingQuery", "specificationObjectQuery"})
public class SpecificationLinkQueryType extends RegistryObjectQueryType {

    @XmlElement(name = "UsageDescriptionBranch")
    protected InternationalStringBranchType usageDescriptionBranch;
    @XmlElement(name = "ServiceBindingQuery")
    protected ServiceBindingQueryType serviceBindingQuery;
    @XmlElement(name = "SpecificationObjectQuery")
    protected RegistryObjectQueryType specificationObjectQuery;

    public InternationalStringBranchType getUsageDescriptionBranch() {
        return this.usageDescriptionBranch;
    }

    public void setUsageDescriptionBranch(InternationalStringBranchType value) {
        this.usageDescriptionBranch = value;
    }

    public ServiceBindingQueryType getServiceBindingQuery() {
        return this.serviceBindingQuery;
    }

    public void setServiceBindingQuery(ServiceBindingQueryType value) {
        this.serviceBindingQuery = value;
    }

    public RegistryObjectQueryType getSpecificationObjectQuery() {
        return this.specificationObjectQuery;
    }

    public void setSpecificationObjectQuery(RegistryObjectQueryType value) {
        this.specificationObjectQuery = value;
    }

}
