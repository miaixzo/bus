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
 * @version 6.2.6
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveRenderedImagingDocumentSetResponseType", namespace = "urn:dicom:wado:ws:2011", propOrder = {"registryResponse", "renderedDocumentResponse"})
public class RetrieveRenderedImagingDocumentSetResponseType {

    @XmlElement(name = "RegistryResponse", required = true)
    protected RegistryResponseType registryResponse;
    @XmlElement(name = "RenderedDocumentResponse")
    protected List<RenderedDocumentResponse> renderedDocumentResponse;

    public RegistryResponseType getRegistryResponse() {
        return this.registryResponse;
    }

    public void setRegistryResponse(RegistryResponseType value) {
        this.registryResponse = value;
    }

    public List<RenderedDocumentResponse> getRenderedDocumentResponse() {
        if (null == this.renderedDocumentResponse) {
            this.renderedDocumentResponse = new ArrayList();
        }
        return this.renderedDocumentResponse;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"homeCommunityId", "repositoryUniqueId", "sourceDocumentUniqueId", "annotation", "rows", "columns", "region", "windowWidth", "windowCenter", "imageQuality", "presentationUID", "presentationSeriesUID", "anonymize", "frameNumber", "mimeType", "document"})
    public static class RenderedDocumentResponse {

        @XmlElement(name = "HomeCommunityId", namespace = "urn:ihe:iti:xds-b:2007")
        protected String homeCommunityId;

        @XmlElement(name = "RepositoryUniqueId", namespace = "urn:ihe:iti:xds-b:2007", required = true)
        protected String repositoryUniqueId;

        @XmlElement(name = "SourceDocumentUniqueId", namespace = "urn:dicom:wado:ws:2011", required = true)
        protected String sourceDocumentUniqueId;

        @XmlElement(name = "Annotation", namespace = "urn:dicom:wado:ws:2011")
        protected String annotation;

        @XmlElement(name = "Rows", namespace = "urn:dicom:wado:ws:2011")
        protected String rows;

        @XmlElement(name = "Columns", namespace = "urn:dicom:wado:ws:2011")
        protected String columns;

        @XmlElement(name = "Region", namespace = "urn:dicom:wado:ws:2011")
        protected String region;

        @XmlElement(name = "WindowWidth", namespace = "urn:dicom:wado:ws:2011")
        protected String windowWidth;

        @XmlElement(name = "WindowCenter", namespace = "urn:dicom:wado:ws:2011")
        protected String windowCenter;

        @XmlElement(name = "ImageQuality", namespace = "urn:dicom:wado:ws:2011")
        protected String imageQuality;

        @XmlElement(name = "PresentationUID", namespace = "urn:dicom:wado:ws:2011")
        protected String presentationUID;

        @XmlElement(name = "PresentationSeriesUID", namespace = "urn:dicom:wado:ws:2011")
        protected String presentationSeriesUID;

        @XmlElement(name = "Anonymize", namespace = "urn:dicom:wado:ws:2011")
        protected String anonymize;

        @XmlElement(name = "FrameNumber", namespace = "urn:dicom:wado:ws:2011")
        protected String frameNumber;

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

        public String getSourceDocumentUniqueId() {
            return this.sourceDocumentUniqueId;
        }

        public void setSourceDocumentUniqueId(String value) {
            this.sourceDocumentUniqueId = value;
        }

        public String getAnnotation() {
            return this.annotation;
        }

        public void setAnnotation(String value) {
            this.annotation = value;
        }

        public String getRows() {
            return this.rows;
        }

        public void setRows(String value) {
            this.rows = value;
        }

        public String getColumns() {
            return this.columns;
        }

        public void setColumns(String value) {
            this.columns = value;
        }

        public String getRegion() {
            return this.region;
        }

        public void setRegion(String value) {
            this.region = value;
        }

        public String getWindowWidth() {
            return this.windowWidth;
        }

        public void setWindowWidth(String value) {
            this.windowWidth = value;
        }

        public String getWindowCenter() {
            return this.windowCenter;
        }

        public void setWindowCenter(String value) {
            this.windowCenter = value;
        }

        public String getImageQuality() {
            return this.imageQuality;
        }

        public void setImageQuality(String value) {
            this.imageQuality = value;
        }

        public String getPresentationUID() {
            return this.presentationUID;
        }

        public void setPresentationUID(String value) {
            this.presentationUID = value;
        }

        public String getPresentationSeriesUID() {
            return this.presentationSeriesUID;
        }

        public void setPresentationSeriesUID(String value) {
            this.presentationSeriesUID = value;
        }

        public String getAnonymize() {
            return this.anonymize;
        }

        public void setAnonymize(String value) {
            this.anonymize = value;
        }

        public String getFrameNumber() {
            return this.frameNumber;
        }

        public void setFrameNumber(String value) {
            this.frameNumber = value;
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
