package org.aoju.bus.tracer.binding.spring.soap;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.consts.TraceConsts;
import org.aoju.bus.tracer.transport.SoapHeaderTransport;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapHeaderException;
import org.springframework.ws.soap.SoapMessage;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;


abstract class AbstractTraceInterceptor {

    protected static final SoapHeaderTransport soapHeaderTransport = new SoapHeaderTransport();
    protected final Backend backend;
    protected final String profile;

    public AbstractTraceInterceptor(final Backend backend, final String profile) {
        this.backend = backend;
        this.profile = profile;
    }

    protected void parseContextFromSoapHeader(final WebServiceMessage message, final TraceFilterConfiguration.Channel channel) {
        if (message instanceof SoapMessage) {
            final SoapMessage soapMessage = (SoapMessage) message;

            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);

            if (filterConfiguration.shouldProcessContext(channel)) {
                final SoapHeader soapHeader = soapMessage.getSoapHeader();
                if (soapHeader != null) {
                    Iterator<SoapHeaderElement> tpicHeaders;
                    try {
                        tpicHeaders = soapHeader.examineHeaderElements(TraceConsts.SOAP_HEADER_QNAME);
                    } catch (SoapHeaderException ignored) {
                        tpicHeaders = Collections.<SoapHeaderElement>emptyList().iterator();
                    }
                    if (tpicHeaders.hasNext()) {
                        final Map<String, String> parsedTpic = soapHeaderTransport.parseTpicHeader(tpicHeaders.next().getSource());
                        backend.putAll(filterConfiguration.filterDeniedParams(parsedTpic, channel));
                    }
                }
            }
        } else {
            Logger.info("Message is obviously no soap message - Not instance of Spring-WS SoapMessage");
        }
    }

    protected void serializeContextToSoapHeader(final WebServiceMessage message, final TraceFilterConfiguration.Channel channel) {
        if (message instanceof SoapMessage) {
            final SoapMessage soapMessage = (SoapMessage) message;

            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);

            if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(channel)) {
                final SoapHeader soapHeader = soapMessage.getSoapHeader();
                if (soapHeader != null) {

                    final Map<String, String> context = filterConfiguration.filterDeniedParams(backend.copyToMap(), channel);
                    soapHeaderTransport.renderSoapHeader(context, soapHeader.getResult());
                }
            }
        } else {
            Logger.info("Message is obviously no soap message - Not instance of Spring-WS SoapMessage");
        }
    }

}
