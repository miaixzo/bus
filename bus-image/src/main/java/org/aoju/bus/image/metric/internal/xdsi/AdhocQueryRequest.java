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
import java.math.BigInteger;

/**
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"responseOption", "adhocQuery"})
@XmlRootElement(name = "AdhocQueryRequest", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0")
public class AdhocQueryRequest extends RegistryRequestType {

    @XmlElement(name = "ResponseOption", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0", required = true)
    protected ResponseOptionType responseOption;
    @XmlElement(name = "AdhocQuery", required = true)
    protected AdhocQueryType adhocQuery;
    @XmlAttribute(name = "federated")
    protected Boolean federated;
    @XmlAttribute(name = "federation")
    @XmlSchemaType(name = "anyURI")
    protected String federation;
    @XmlAttribute(name = "startIndex")
    protected BigInteger startIndex;
    @XmlAttribute(name = "maxResults")
    protected BigInteger maxResults;

    public ResponseOptionType getResponseOption() {
        return this.responseOption;
    }

    public void setResponseOption(ResponseOptionType value) {
        this.responseOption = value;
    }

    public AdhocQueryType getAdhocQuery() {
        return this.adhocQuery;
    }

    public void setAdhocQuery(AdhocQueryType value) {
        this.adhocQuery = value;
    }

    public boolean isFederated() {

        if (this.federated == null) {
            return false;
        }
        return this.federated.booleanValue();
    }

    public void setFederated(Boolean value) {
        this.federated = value;
    }

    public String getFederation() {
        return this.federation;
    }

    public void setFederation(String value) {
        this.federation = value;
    }

    public BigInteger getStartIndex() {
        if (this.startIndex == null) {
            return new BigInteger("0");
        }
        return this.startIndex;
    }

    public void setStartIndex(BigInteger value) {
        this.startIndex = value;
    }

    public BigInteger getMaxResults() {
        if (this.maxResults == null) {
            return new BigInteger("-1");
        }
        return this.maxResults;
    }

    public void setMaxResults(BigInteger value) {
        this.maxResults = value;
    }

}
