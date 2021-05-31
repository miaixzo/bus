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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.2.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceBindingQueryType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", propOrder = {"serviceQuery", "specificationLinkQuery", "targetBindingQuery"})
public class ServiceBindingQueryType extends RegistryObjectQueryType {

    @XmlElement(name = "ServiceQuery")
    protected ServiceQueryType serviceQuery;
    @XmlElement(name = "SpecificationLinkQuery")
    protected List<SpecificationLinkQueryType> specificationLinkQuery;
    @XmlElement(name = "TargetBindingQuery")
    protected ServiceBindingQueryType targetBindingQuery;

    public ServiceQueryType getServiceQuery() {
        return this.serviceQuery;
    }

    public void setServiceQuery(ServiceQueryType value) {
        this.serviceQuery = value;
    }

    public List<SpecificationLinkQueryType> getSpecificationLinkQuery() {
        if (null == this.specificationLinkQuery) {
            this.specificationLinkQuery = new ArrayList();
        }
        return this.specificationLinkQuery;
    }

    public ServiceBindingQueryType getTargetBindingQuery() {
        return this.targetBindingQuery;
    }

    public void setTargetBindingQuery(ServiceBindingQueryType value) {
        this.targetBindingQuery = value;
    }

}

