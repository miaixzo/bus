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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;

/**
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
@WebService(targetNamespace = "urn:ihe:rad:xdsi-b:2009", name = "ImagingDocumentSource_PortType")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface ImagingDocumentSourcePortType {

    @WebMethod(operationName = "ImagingDocumentSource_RetrieveImagingDocumentSet", action = "urn:ihe:rad:2009:RetrieveImagingDocumentSet")
    @Action(input = "urn:ihe:rad:2009:RetrieveImagingDocumentSet", output = "urn:ihe:iti:2007:RetrieveDocumentSetResponse")
    @WebResult(name = "RetrieveDocumentSetResponse", targetNamespace = "urn:ihe:iti:xds-b:2007", partName = "body")
    RetrieveDocumentSetResponseType imagingDocumentSourceRetrieveImagingDocumentSet(@WebParam(partName = "body", name = "RetrieveImagingDocumentSetRequest", targetNamespace = "urn:ihe:rad:xdsi-b:2009") RetrieveImagingDocumentSetRequestType paramRetrieveImagingDocumentSetRequestType);

    @WebMethod(operationName = "ImagingDocumentSource_RetrieveRenderedImagingDocumentSet", action = "urn:dicom:wado:ws:2011:RetrieveRenderedImagingDocumentSet")
    @Action(input = "urn:dicom:wado:ws:2011:RetrieveRenderedImagingDocumentSet", output = "urn:dicom:wado:ws:2011:RetrieveRenderedImagingDocumentSetResponse")
    @WebResult(name = "RetrieveRenderedImagingDocumentSetResponse", targetNamespace = "urn:dicom:wado:ws:2011", partName = "body")
    RetrieveRenderedImagingDocumentSetResponseType imagingDocumentSourceRetrieveRenderedImagingDocumentSet(@WebParam(partName = "body", name = "RetrieveRenderedImagingDocumentSetRequest", targetNamespace = "urn:dicom:wado:ws:2011") RetrieveRenderedImagingDocumentSetRequestType paramRetrieveRenderedImagingDocumentSetRequestType);

}
