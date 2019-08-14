package org.aoju.bus.trace4j.binding.apache.cxf.interceptor;

import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.consts.TraceConsts;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.HttpHeaderTransport;
import org.aoju.bus.trace4j.transport.jaxb.TpicMap;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractTraceOutInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTraceOutInterceptor.class);

    protected final TraceBackend backend;

    private final HttpHeaderTransport httpSerializer;
    private final TraceFilterConfiguration.Channel channel;

    private String profile;

    public AbstractTraceOutInterceptor(String phase, TraceFilterConfiguration.Channel channel, TraceBackend backend, String profile) {
        super(phase);
        this.channel = channel;
        this.backend = backend;
        this.profile = profile;
        this.httpSerializer = new HttpHeaderTransport();
    }

    @Override
    public void handleMessage(final Message message) {
        if (shouldHandleMessage(message)) {
            final TraceFilterConfiguration filterConfiguration = backend.getConfiguration(profile);
            if (!backend.isEmpty() && filterConfiguration.shouldProcessContext(channel)) {
                final Map<String, String> filteredParams = filterConfiguration.filterDeniedParams(backend.copyToMap(), channel);

                LOGGER.debug("Interceptor handles message!");
                if (Boolean.TRUE.equals(message.getExchange().get(Message.REST_MESSAGE))) {
                    Map<String, List<String>> responseHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
                    if (responseHeaders == null) {
                        responseHeaders = new HashMap<>();
                        message.put(Message.PROTOCOL_HEADERS, responseHeaders);
                    }

                    final String contextAsHeader = httpSerializer.render(filteredParams);
                    responseHeaders.put(TraceConsts.TPIC_HEADER, Collections.singletonList(contextAsHeader));
                } else {
                    try {
                        final SoapMessage soapMessage = (SoapMessage) message;
                        addSoapHeader(filteredParams, soapMessage);
                    } catch (NoClassDefFoundError e) {
                        LOGGER.error("Should handle SOAP-message but it seems that cxf soap dependency is not on the classpath. Unable to add Trace-Headers: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void addSoapHeader(Map<String, String> filteredParams, SoapMessage soapMessage) {
        try {
            final Header tpicHeader = new Header(TraceConsts.SOAP_HEADER_QNAME, TpicMap.wrap(filteredParams),
                    new JAXBDataBinding(TpicMap.class));
            soapMessage.getHeaders().add(tpicHeader);
        } catch (JAXBException e) {
            LOGGER.warn("Error occured during Trace soap header creation: {}", e.getMessage());
            LOGGER.debug("Detailed exception", e);
        }
    }

    protected abstract boolean shouldHandleMessage(Message message);

}
