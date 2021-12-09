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

import org.aoju.bus.core.lang.MediaType;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProvideAndRegisterDocumentSetRequestType", namespace = "urn:ihe:iti:xds-b:2007", propOrder = {"submitObjectsRequest", "document"})
public class ProvideAndRegisterDocumentSetRequestType {

    @XmlElement(name = "SubmitObjectsRequest", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0", required = true)
    protected SubmitObjectsRequest submitObjectsRequest;
    @XmlElement(name = "Document")
    protected List<Document> document;

    public SubmitObjectsRequest getSubmitObjectsRequest() {
        return this.submitObjectsRequest;
    }


    public void setSubmitObjectsRequest(SubmitObjectsRequest value) {
        this.submitObjectsRequest = value;
    }

    public List<Document> getDocument() {
        if (null == this.document) {
            this.document = new ArrayList();
        }
        return this.document;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"value"})
    public static class Document {

        @XmlValue
        @XmlMimeType(MediaType.APPLICATION_OCTET_STREAM)
        protected DataHandler value;

        @XmlAttribute(name = "id", required = true)
        @XmlSchemaType(name = "anyURI")
        protected String id;


        public DataHandler getValue() {
            return this.value;
        }

        public void setValue(DataHandler value) {
            this.value = value;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String value) {
            this.id = value;
        }

    }

}
