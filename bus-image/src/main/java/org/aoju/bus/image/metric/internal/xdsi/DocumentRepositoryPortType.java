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
 * @version 6.1.1
 * @since JDK 1.8+
 */
@WebService(targetNamespace = "urn:ihe:iti:xds-b:2007", name = "DocumentRepository_PortType")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface DocumentRepositoryPortType {

    @WebMethod(operationName = "DocumentRepository_ProvideAndRegisterDocumentSet-b", action = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b")
    @Action(input = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b", output = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse")
    @WebResult(name = "RegistryResponse", targetNamespace = "urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0", partName = "body")
    RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(@WebParam(partName = "body", name = "ProvideAndRegisterDocumentSetRequest", targetNamespace = "urn:ihe:iti:xds-b:2007") ProvideAndRegisterDocumentSetRequestType paramProvideAndRegisterDocumentSetRequestType);

    @WebMethod(operationName = "DocumentRepository_RetrieveDocumentSet", action = "urn:ihe:iti:2007:RetrieveDocumentSet")
    @Action(input = "urn:ihe:iti:2007:RetrieveDocumentSet", output = "urn:ihe:iti:2007:RetrieveDocumentSetResponse")
    @WebResult(name = "RetrieveDocumentSetResponse", targetNamespace = "urn:ihe:iti:xds-b:2007", partName = "body")
    RetrieveDocumentSetResponseType documentRepositoryRetrieveDocumentSet(@WebParam(partName = "body", name = "RetrieveDocumentSetRequest", targetNamespace = "urn:ihe:iti:xds-b:2007") RetrieveDocumentSetRequestType paramRetrieveDocumentSetRequestType);

}
