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
 * @version 6.0.3
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"adhocQuery", "sourceRegistry", "destinationRegistry", "ownerAtSource", "ownerAtDestination"})
@XmlRootElement(name = "RelocateObjectsRequest", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0")
public class RelocateObjectsRequest extends RegistryRequestType {

    @XmlElement(name = "AdhocQuery", required = true)
    protected AdhocQueryType adhocQuery;
    @XmlElement(name = "SourceRegistry", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", required = true)
    protected ObjectRefType sourceRegistry;
    @XmlElement(name = "DestinationRegistry", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", required = true)
    protected ObjectRefType destinationRegistry;
    @XmlElement(name = "OwnerAtSource", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", required = true)
    protected ObjectRefType ownerAtSource;
    @XmlElement(name = "OwnerAtDestination", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", required = true)
    protected ObjectRefType ownerAtDestination;

    public AdhocQueryType getAdhocQuery() {
        return this.adhocQuery;
    }

    public void setAdhocQuery(AdhocQueryType value) {
        this.adhocQuery = value;
    }

    public ObjectRefType getSourceRegistry() {
        return this.sourceRegistry;
    }

    public void setSourceRegistry(ObjectRefType value) {
        this.sourceRegistry = value;
    }

    public ObjectRefType getDestinationRegistry() {
        return this.destinationRegistry;
    }

    public void setDestinationRegistry(ObjectRefType value) {
        this.destinationRegistry = value;
    }

    public ObjectRefType getOwnerAtSource() {
        return this.ownerAtSource;
    }

    public void setOwnerAtSource(ObjectRefType value) {
        this.ownerAtSource = value;
    }

    public ObjectRefType getOwnerAtDestination() {
        return this.ownerAtDestination;
    }

    public void setOwnerAtDestination(ObjectRefType value) {
        this.ownerAtDestination = value;
    }

}
