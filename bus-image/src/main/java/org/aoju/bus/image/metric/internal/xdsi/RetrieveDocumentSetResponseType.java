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

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveDocumentSetResponseType", namespace = "urn:ihe:iti:xds-b:2007", propOrder = {"registryResponse", "documentResponse"})
public class RetrieveDocumentSetResponseType {

    @XmlElement(name = "RegistryResponse", namespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", required = true)
    protected RegistryResponseType registryResponse;
    @XmlElement(name = "DocumentResponse")
    protected List<DocumentResponse> documentResponse;

    public RegistryResponseType getRegistryResponse() {
        return this.registryResponse;
    }

    public void setRegistryResponse(RegistryResponseType value) {
        this.registryResponse = value;
    }

    public List<DocumentResponse> getDocumentResponse() {
        if (null == this.documentResponse) {
            this.documentResponse = new ArrayList();
        }
        return this.documentResponse;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"homeCommunityId", "repositoryUniqueId", "documentUniqueId", "newRepositoryUniqueId", "newDocumentUniqueId", "mimeType", "document"})
    public static class DocumentResponse {

        @XmlElement(name = "HomeCommunityId", namespace = "urn:ihe:iti:xds-b:2007")
        protected String homeCommunityId;

        @XmlElement(name = "RepositoryUniqueId", namespace = "urn:ihe:iti:xds-b:2007", required = true)
        protected String repositoryUniqueId;

        @XmlElement(name = "DocumentUniqueId", namespace = "urn:ihe:iti:xds-b:2007", required = true)
        protected String documentUniqueId;

        @XmlElement(name = "NewRepositoryUniqueId", namespace = "urn:ihe:iti:xds-b:2007")
        protected String newRepositoryUniqueId;

        @XmlElement(name = "NewDocumentUniqueId", namespace = "urn:ihe:iti:xds-b:2007")
        protected String newDocumentUniqueId;

        @XmlElement(namespace = "urn:ihe:iti:xds-b:2007", required = true)
        protected String mimeType;

        @XmlElement(name = "Document", namespace = "urn:ihe:iti:xds-b:2007", required = true)
        @XmlMimeType("*/*")
        protected DataHandler document;

        public String getHomeCommunityId() {
            return this.homeCommunityId;
        }

        public void setHomeCommunityId(String value) {
            this.homeCommunityId = value;
        }

        public String getRepositoryUniqueId() {
            return this.repositoryUniqueId;
        }

        public void setRepositoryUniqueId(String value) {
            this.repositoryUniqueId = value;
        }

        public String getDocumentUniqueId() {
            return this.documentUniqueId;
        }

        public void setDocumentUniqueId(String value) {
            this.documentUniqueId = value;
        }

        public String getNewRepositoryUniqueId() {
            return this.newRepositoryUniqueId;
        }

        public void setNewRepositoryUniqueId(String value) {
            this.newRepositoryUniqueId = value;
        }

        public String getNewDocumentUniqueId() {
            return this.newDocumentUniqueId;
        }

        public void setNewDocumentUniqueId(String value) {
            this.newDocumentUniqueId = value;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public void setMimeType(String value) {
            this.mimeType = value;
        }

        public DataHandler getDocument() {
            return this.document;
        }

        public void setDocument(DataHandler value) {
            this.document = value;
        }
    }

}

