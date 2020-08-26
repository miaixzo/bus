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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
@WebServiceClient(name = "ImagingDocumentSource", wsdlLocation = "/wsdl/XDS-I.b_ImagingDocumentSource.wsdl", targetNamespace = "urn:ihe:rad:xdsi-b:2009")
public class ImagingDocumentSource extends Service {

    public static final URL WSDL_LOCATION;
    public static final QName SERVICE = new QName("urn:ihe:rad:xdsi-b:2009", "ImagingDocumentSource");
    public static final QName ImagingDocumentSourcePortSoap12 = new QName("urn:ihe:rad:xdsi-b:2009", "ImagingDocumentSource_Port_Soap12");

    static {
        URL url = ImagingDocumentSource.class.getResource("/wsdl/XDS-I.b_ImagingDocumentSource.wsdl");
        if (url == null) {
            url = ImagingDocumentSource.class.getClassLoader().getResource("/wsdl/XDS-I.b_ImagingDocumentSource.wsdl");
        }
        if (url == null) {
            Logger.getLogger(ImagingDocumentSource.class.getName()).log(Level.INFO, "Can not initialize the default wsdl from {0}", "/wsdl/XDS-I.b_ImagingDocumentSource.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ImagingDocumentSource(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ImagingDocumentSource(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ImagingDocumentSource() {
        super(WSDL_LOCATION, SERVICE);
    }

    public ImagingDocumentSource(WebServiceFeature... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    public ImagingDocumentSource(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, SERVICE, features);
    }

    public ImagingDocumentSource(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    @WebEndpoint(name = "ImagingDocumentSource_Port_Soap12")
    public ImagingDocumentSourcePortType getImagingDocumentSourcePortSoap12() {
        return super.getPort(ImagingDocumentSourcePortSoap12, ImagingDocumentSourcePortType.class);
    }

    @WebEndpoint(name = "ImagingDocumentSource_Port_Soap12")
    public ImagingDocumentSourcePortType getImagingDocumentSourcePortSoap12(WebServiceFeature... features) {
        return super.getPort(ImagingDocumentSourcePortSoap12, ImagingDocumentSourcePortType.class, features);
    }

}
