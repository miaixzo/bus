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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Kimi Liu
 * @version 5.9.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleFilterType", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0")
@XmlSeeAlso({BooleanFilterType.class, IntegerFilterType.class, FloatFilterType.class, DateTimeFilterType.class, StringFilterType.class})
public abstract class SimpleFilterType extends FilterType {

    @XmlAttribute(name = "domainAttribute", required = true)
    protected String domainAttribute;
    @XmlAttribute(name = "comparator", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String comparator;

    public String getDomainAttribute() {
        return this.domainAttribute;
    }

    public void setDomainAttribute(String value) {
        this.domainAttribute = value;
    }

    public String getComparator() {
        return this.comparator;
    }

    public void setComparator(String value) {
        this.comparator = value;
    }

}

