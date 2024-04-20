/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.tracer.transport;

import jakarta.xml.bind.*;
import jakarta.xml.soap.SOAPHeader;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.tracer.Builder;
import org.miaixz.bus.tracer.transport.jaxb.TpicMap;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SoapHeaderTransport {

    private static final SOAPHeaderMarshaller SOAP_HEADER_MARSHALLER = new SOAPHeaderMarshaller();
    private static final ResultMarshaller RESULT_MARSHALLER = new ResultMarshaller();
    private static final ElementUnmarshaller ELEMENT_UNMARSHALLER = new ElementUnmarshaller();
    private static final SourceUnmarshaller SOURCE_UNMARSHALLER = new SourceUnmarshaller();
    private final JAXBContext jaxbContext;

    public SoapHeaderTransport() {
        try {
            jaxbContext = JAXBContext.newInstance(TpicMap.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> parseSoapHeader(final Element soapHeader) {
        final NodeList tpicHeaders = soapHeader.getElementsByTagNameNS(Builder.SOAP_HEADER_NAMESPACE, Builder.TPIC_HEADER);
        final HashMap<String, String> contextMap = new HashMap<>();
        if (null != tpicHeaders && tpicHeaders.getLength() > 0) {
            final int items = tpicHeaders.getLength();
            for (int i = 0; i < items; i++) {
                contextMap.putAll(parseTpicHeader((Element) tpicHeaders.item(i)));
            }
        }
        return contextMap;
    }

    public Map<String, String> parseTpicHeader(final Element element) {
        return parseTpicHeader(ELEMENT_UNMARSHALLER, element);
    }

    public Map<String, String> parseTpicHeader(final Source source) {
        return parseTpicHeader(SOURCE_UNMARSHALLER, source);
    }

    private <T> Map<String, String> parseTpicHeader(final Unmarshallable<T> unmarshallable, final T xmlContext) {
        try {
            if (null != xmlContext) {
                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                final JAXBElement<TpicMap> unmarshal = unmarshallable.unmarshal(unmarshaller, xmlContext);
                if (null != unmarshal) {
                    return unmarshal.getValue().unwrapValues();
                }
            }
        } catch (JAXBException e) {
            Logger.warn("Unable to parse TPIC header: {}", e.getMessage());
            Logger.debug("WithStack: Unable to parse TPIC header: {}", e.getMessage(), e);
        }
        return new HashMap<>();
    }

    public void renderSoapHeader(final Map<String, String> context, final SOAPHeader soapHeader) {
        renderSoapHeader(SOAP_HEADER_MARSHALLER, context, soapHeader);
    }

    public void renderSoapHeader(final Map<String, String> context, final Result result) {
        renderSoapHeader(RESULT_MARSHALLER, context, result);
    }

    private <T> void renderSoapHeader(final Marshallable<T> marshallable, final Map<String, String> context, T xmlContext) {
        try {
            final Marshaller marshaller = jaxbContext.createMarshaller();
            marshallable.marshal(marshaller, TpicMap.wrap(context), xmlContext);
        } catch (JAXBException e) {
            Logger.warn("Unable to render TPIC header: {}", e.getMessage());
            Logger.debug("WithStack: Unable to render TPIC header: {}", e.getMessage(), e);
        }
    }

    private interface Unmarshallable<T> {
        JAXBElement<TpicMap> unmarshal(Unmarshaller unmarshaller, T xmlContext) throws JAXBException;
    }

    private interface Marshallable<T> {
        void marshal(Marshaller marshaller, TpicMap tpic, T xmlContext) throws JAXBException;
    }

    private static class SourceUnmarshaller implements Unmarshallable<Source> {
        @Override
        public JAXBElement<TpicMap> unmarshal(final Unmarshaller unmarshaller, final Source source) throws JAXBException {
            return unmarshaller.unmarshal(source, TpicMap.class);
        }
    }

    private static class ElementUnmarshaller implements Unmarshallable<Element> {
        @Override
        public JAXBElement<TpicMap> unmarshal(final Unmarshaller unmarshaller, final Element element) throws JAXBException {
            return unmarshaller.unmarshal(element, TpicMap.class);
        }
    }

    private static class ResultMarshaller implements Marshallable<Result> {
        @Override
        public void marshal(final Marshaller marshaller, final TpicMap tpic, final Result result) throws JAXBException {
            marshaller.marshal(tpic, result);
        }
    }

    private static class SOAPHeaderMarshaller implements Marshallable<SOAPHeader> {
        @Override
        public void marshal(final Marshaller marshaller, final TpicMap tpic, final SOAPHeader soapHeader) throws JAXBException {
            marshaller.marshal(tpic, soapHeader);
        }
    }

}
