package org.aoju.bus.trace4j.binding.jaxws;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.trace4j.Trace;
import org.aoju.bus.trace4j.TraceBackend;
import org.aoju.bus.trace4j.config.TraceFilterConfiguration;
import org.aoju.bus.trace4j.transport.SoapHeaderTransport;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;


public class TraceClientHandler extends AbstractTraceHandler {


    private final SoapHeaderTransport transportSerialization = new SoapHeaderTransport();

    public TraceClientHandler() {
        this(Trace.getBackend());
    }

    public TraceClientHandler(TraceBackend TraceBackend) {
        super(TraceBackend);
    }

    @Override
    public final boolean handleFault(final SOAPMessageContext context) {
        return true;
    }

    protected final void handleIncoming(final SOAPMessageContext context) {

        final SOAPMessage msg = context.getMessage();
        if (msg != null && TraceBackend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingRequest)) {

            try {
                final SOAPHeader header = msg.getSOAPHeader();

                if (header != null) {
                    final Map<String, String> parsedContext = transportSerialization.parseSoapHeader(header);
                    TraceBackend.putAll(TraceBackend.getConfiguration().filterDeniedParams(parsedContext, TraceFilterConfiguration.Channel.OutgoingRequest));
                }
            } catch (final SOAPException e) {
                Logger.warn("Error during precessing of inbound soap header: " + e.getMessage());
                Logger.debug("Detailed: Error during precessing of inbound soap header: {}", e.getMessage(), e);
            }
        }
    }

    protected final void handleOutgoing(final SOAPMessageContext context) {

        final SOAPMessage msg = context.getMessage();
        if (msg != null && !TraceBackend.isEmpty() && TraceBackend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.IncomingResponse)) {

            try {
                final SOAPHeader header = getOrCreateHeader(msg);

                final Map<String, String> filteredContext = TraceBackend.getConfiguration().filterDeniedParams(TraceBackend.copyToMap(), TraceFilterConfiguration.Channel.IncomingResponse);
                transportSerialization.renderSoapHeader(filteredContext, header);

                msg.saveChanges();
            } catch (final SOAPException e) {
                Logger.warn("TraceClientHandler : Exception occurred during processing of outbound message.", e);
            }
        }
    }

}
