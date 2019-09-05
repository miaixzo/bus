/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/
package org.aoju.bus.tracer.binding.jaxws;


import org.aoju.bus.tracer.Backend;
import org.aoju.bus.tracer.Builder;
import org.aoju.bus.tracer.config.TraceFilterConfiguration;
import org.aoju.bus.tracer.transport.SoapHeaderTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 3.1.9
 * @since JDK 1.8
 */
public class TraceServerHandler extends AbstractTraceHandler {

    private static final Logger logger = LoggerFactory.getLogger(TraceServerHandler.class);

    private final SoapHeaderTransport transportSerialization;

    public TraceServerHandler() {
        this(Builder.getBackend(), new SoapHeaderTransport());
    }

    public TraceServerHandler(Backend Backend, SoapHeaderTransport soapHeaderTransport) {
        super(Backend);
        this.transportSerialization = soapHeaderTransport;
    }

    protected final void handleIncoming(SOAPMessageContext context) {
        final SOAPMessage soapMessage = context.getMessage();
        try {
            final SOAPHeader header = soapMessage.getSOAPHeader();

            if (header != null && Backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.IncomingRequest)) {
                final Map<String, String> parsedContext = transportSerialization.parseSoapHeader(header);
                final Map<String, String> filteredContext = Backend.getConfiguration().filterDeniedParams(parsedContext, TraceFilterConfiguration.Channel.IncomingRequest);
                Backend.putAll(filteredContext);
            }
        } catch (final SOAPException e) {
            logger.warn("Error during precessing of inbound soap header: {}", e.getMessage());
            logger.debug("Detailed: Error during precessing of inbound soap header: {}", e.getMessage(), e);
        }

        Builder.generateInvocationIdIfNecessary(Backend);
    }

    protected final void handleOutgoing(SOAPMessageContext context) {
        final SOAPMessage msg = context.getMessage();
        try {
            if (msg != null && !Backend.isEmpty() && Backend.getConfiguration().shouldProcessContext(TraceFilterConfiguration.Channel.OutgoingResponse)) {

                final SOAPHeader header = getOrCreateHeader(msg);

                final Map<String, String> filteredContext = Backend.getConfiguration().filterDeniedParams(Backend.copyToMap(), TraceFilterConfiguration.Channel.OutgoingResponse);
                transportSerialization.renderSoapHeader(filteredContext, header);

                msg.saveChanges();
                context.setMessage(msg);
            }

        } catch (final SOAPException e) {
            logger.error("TraceServerHandler : Exception occurred during processing of outbound message.");
            logger.debug("Detailed: TraceServerHandler : Exception occurred during processing of outbound message: {}", e.getMessage(), e);
        } finally {
            Backend.clear();
        }
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        this.handleOutgoing(context);
        return true;
    }

}
