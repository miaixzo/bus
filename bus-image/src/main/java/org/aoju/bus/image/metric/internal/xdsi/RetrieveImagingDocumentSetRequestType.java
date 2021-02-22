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

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.2.0
 * @since JDK 1.8+
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RetrieveImagingDocumentSetRequestType", namespace = "urn:ihe:rad:xdsi-b:2009", propOrder = {"studyRequest", "transferSyntaxUIDList"})
public class RetrieveImagingDocumentSetRequestType {

    @XmlElement(name = "StudyRequest", required = true)
    protected List<StudyRequest> studyRequest;
    @XmlElement(name = "TransferSyntaxUIDList", required = true)
    protected TransferSyntaxUIDList transferSyntaxUIDList;

    public List<StudyRequest> getStudyRequest() {
        if (this.studyRequest == null) {
            this.studyRequest = new ArrayList();
        }
        return this.studyRequest;
    }

    public TransferSyntaxUIDList getTransferSyntaxUIDList() {
        return this.transferSyntaxUIDList;
    }

    public void setTransferSyntaxUIDList(TransferSyntaxUIDList value) {
        this.transferSyntaxUIDList = value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"seriesRequest"})
    public static class StudyRequest {

        @XmlElement(name = "SeriesRequest", namespace = "urn:ihe:rad:xdsi-b:2009", required = true)
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
        @XmlType
        public static class SeriesRequest extends RetrieveDocumentSetRequestType {
            @XmlAttribute(name = "seriesInstanceUID", required = true)
            protected String seriesInstanceUID;

            public String getSeriesInstanceUID() {
                return this.seriesInstanceUID;
            }

            public void setSeriesInstanceUID(String value) {
                this.seriesInstanceUID = value;
            }
        }
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"transferSyntaxUID"})
    public static class TransferSyntaxUIDList {
        @XmlElement(name = "TransferSyntaxUID", namespace = "urn:ihe:rad:xdsi-b:2009", required = true)
        protected List<String> transferSyntaxUID;

        public List<String> getTransferSyntaxUID() {
            if (this.transferSyntaxUID == null) {
                this.transferSyntaxUID = new ArrayList();
            }
            return this.transferSyntaxUID;
        }
    }

}

