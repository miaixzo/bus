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
 * @version 6.0.2
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveRenderedImagingDocumentSetRequestType", namespace = "urn:dicom:wado:ws:2011", propOrder = {"studyRequest"})
public class RetrieveRenderedImagingDocumentSetRequestType {

    @XmlElement(name = "StudyRequest", required = true)
    protected List<StudyRequest> studyRequest;

    public List<StudyRequest> getStudyRequest() {
        if (this.studyRequest == null) {
            this.studyRequest = new ArrayList();
        }
        return this.studyRequest;
    }

    @XmlAccessorType(XmlAccessType.FIELD)

    @XmlType(propOrder = {"seriesRequest"})
    public static class StudyRequest {

        @XmlElement(name = "SeriesRequest", namespace = "urn:dicom:wado:ws:2011", required = true)
        protected List<SeriesRequest> seriesRequest;

        @XmlAttribute(name = "studyInstanceUID", required = true)
        protected String studyInstanceUID;

        public List<SeriesRequest> getSeriesRequest() {
            if (this.seriesRequest == null) {
                this.seriesRequest = new ArrayList();

            }
            return this.seriesRequest;

        }

        public String getStudyInstanceUID() {
            return this.studyInstanceUID;
        }

        public void setStudyInstanceUID(String value) {
            this.studyInstanceUID = value;

        }

        @XmlAccessorType(XmlAccessType.FIELD)

        @XmlType(propOrder = {"renderedDocumentRequest"})
        public static class SeriesRequest {

            @XmlElement(name = "RenderedDocumentRequest", namespace = "urn:dicom:wado:ws:2011", required = true)
            protected List<RenderedDocumentRequest> renderedDocumentRequest;

            @XmlAttribute(name = "seriesInstanceUID", required = true)
            protected String seriesInstanceUID;


            public List<RenderedDocumentRequest> getRenderedDocumentRequest() {
                if (this.renderedDocumentRequest == null) {
                    this.renderedDocumentRequest = new ArrayList();
                }
                return this.renderedDocumentRequest;
            }

            public String getSeriesInstanceUID() {
                return this.seriesInstanceUID;
            }

            public void setSeriesInstanceUID(String value) {
                this.seriesInstanceUID = value;
            }

            @XmlAccessorType(XmlAccessType.FIELD)

            @XmlType(propOrder = {"homeCommunityId", "repositoryUniqueId", "documentUniqueId", "annotation", "rows", "columns", "region", "windowWidth", "windowCenter", "imageQuality", "presentationUID", "presentationSeriesUID", "anonymize", "frameNumber", "contentTypeList", "charsetList", "any"})
            public static class RenderedDocumentRequest {

                @XmlElement(name = "HomeCommunityId", namespace = "urn:ihe:iti:xds-b:2007")
                protected String homeCommunityId;

                @XmlElement(name = "RepositoryUniqueId", namespace = "urn:ihe:iti:xds-b:2007", required = true)
                protected String repositoryUniqueId;

                @XmlElement(name = "DocumentUniqueId", namespace = "urn:ihe:iti:xds-b:2007", required = true)
                protected String documentUniqueId;

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

                @XmlElement(name = "ContentTypeList", namespace = "urn:dicom:wado:ws:2011", required = true)
                protected ContentTypeList contentTypeList;

                @XmlElement(name = "CharsetList", namespace = "urn:dicom:wado:ws:2011")
                protected CharsetList charsetList;

                @XmlAnyElement(lax = true)
                protected List<Object> any;

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

                public ContentTypeList getContentTypeList() {
                    return this.contentTypeList;
                }

                public void setContentTypeList(ContentTypeList value) {
                    this.contentTypeList = value;
                }

                public CharsetList getCharsetList() {
                    return this.charsetList;
                }

                public void setCharsetList(CharsetList value) {
                    this.charsetList = value;
                }

                public List<Object> getAny() {
                    if (this.any == null) {
                        this.any = new ArrayList();
                    }
                    return this.any;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(propOrder = {"charset"})
                public static class CharsetList {

                    @XmlElement(name = "Charset", namespace = "urn:dicom:wado:ws:2011", required = true)
                    protected List<String> charset;

                    public List<String> getCharset() {
                        if (this.charset == null) {
                            this.charset = new ArrayList();
                        }
                        return this.charset;
                    }
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(propOrder = {"contentType"})
                public static class ContentTypeList {

                    @XmlElement(name = "ContentType", namespace = "urn:dicom:wado:ws:2011", required = true)
                    protected List<String> contentType;

                    public List<String> getContentType() {
                        if (this.contentType == null) {
                            this.contentType = new ArrayList();
                        }
                        return this.contentType;
                    }
                }
            }
        }
    }

}
