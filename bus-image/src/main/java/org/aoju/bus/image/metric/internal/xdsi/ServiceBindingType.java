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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceBindingType", propOrder = {"specificationLink"})
public class ServiceBindingType extends RegistryObjectType {

    @XmlElement(name = "SpecificationLink")
    protected List<SpecificationLinkType> specificationLink;
    @XmlAttribute(name = "service", required = true)
    protected String service;
    @XmlAttribute(name = "accessURI")
    @XmlSchemaType(name = "anyURI")
    protected String accessURI;
    @XmlAttribute(name = "targetBinding")
    protected String targetBinding;

    public List<SpecificationLinkType> getSpecificationLink() {
        if (this.specificationLink == null) {
            this.specificationLink = new ArrayList();
        }
        return this.specificationLink;
    }

    public String getService() {
        return this.service;
    }

    public void setService(String value) {
        this.service = value;
    }

    public String getAccessURI() {
        return this.accessURI;
    }

    public void setAccessURI(String value) {
        this.accessURI = value;
    }

    public String getTargetBinding() {
        return this.targetBinding;
    }

    public void setTargetBinding(String value) {
        this.targetBinding = value;
    }

}

