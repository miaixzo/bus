/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.galaxy.io.SAXTransformer;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.hl7.*;
import org.miaixz.bus.image.metric.hl7.net.HL7Application;
import org.miaixz.bus.image.metric.hl7.net.HL7DeviceExtension;
import org.miaixz.bus.image.metric.hl7.net.HL7MessageListener;
import org.miaixz.bus.image.metric.hl7.net.UnparsedHL7Message;
import org.miaixz.bus.logger.Logger;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * The {@code HL7Rcv} class implements an HL7 receiver that listens for incoming HL7 messages over an MLLP connection.
 * It can store received messages to the file system and send back a standard or custom acknowledgment. The response can
 * be generated using an XSLT transformation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Rcv {

    /**
     * The SAX transformer factory for creating XSLT handlers.
     */
    private static final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();

    /**
     * The main device for this receiver.
     */
    private final Device device = new Device("hl7rcv");
    /**
     * The HL7 device extension.
     */
    private final HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
    /**
     * The HL7 application that handles all message types.
     */
    private final HL7Application hl7App = new HL7Application(Symbol.STAR);
    /**
     * The network connection configuration.
     */
    private final Connection conn = new Connection();
    /**
     * The directory to store received messages.
     */
    private String storageDir;
    /**
     * The default character set to use if not specified in the message.
     */
    private String charset;
    /**
     * The compiled XSLT templates for response generation.
     */
    private Templates tpls;
    /**
     * Parameters to be passed to the XSLT transformation.
     */
    private String[] xsltParams;
    /**
     * A flag to use UUIDs for filenames when storing messages.
     */
    private boolean useUUIDForFilename;
    /**
     * A delay in milliseconds before sending a response.
     */
    private int responseDelay;

    /**
     * The message listener that processes incoming HL7 messages.
     */
    private final HL7MessageListener handler = (hl7App, conn, s, msg) -> {
        try {
            return HL7Rcv.this.onMessage(msg);
        } catch (Exception e) {
            throw new HL7Exception(new ERRSegment(msg.msh()).setUserMessage(e.getMessage()), e);
        }
    };

    /**
     * Constructs a new {@code HL7Rcv} instance and initializes its components.
     */
    public HL7Rcv() {
        conn.setProtocol(Connection.Protocol.HL7);
        device.addDeviceExtension(hl7Ext);
        device.addConnection(conn);
        hl7Ext.addHL7Application(hl7App);
        hl7App.setAcceptedMessageTypes(Symbol.STAR);
        hl7App.addConnection(conn);
        hl7App.setHL7MessageListener(handler);
    }

    /**
     * Sets the directory where received HL7 messages will be stored.
     *
     * @param storageDir The path to the storage directory.
     */
    public void setStorageDirectory(String storageDir) {
        this.storageDir = storageDir;
    }

    /**
     * Sets the XSLT stylesheet to be used for transforming the response message.
     *
     * @param xslt The URL of the XSLT file.
     * @throws Exception if the templates cannot be created.
     */
    public void setXSLT(URL xslt) throws Exception {
        tpls = SAXTransformer.newTemplates(new StreamSource(xslt.openStream(), xslt.toExternalForm()));
    }

    /**
     * Sets the parameters to be passed to the XSLT transformation.
     *
     * @param xsltParams An array of key-value pairs for the XSLT parameters.
     */
    public void setXSLTParameters(String[] xsltParams) {
        this.xsltParams = xsltParams;
    }

    /**
     * Sets the default character set to use if not specified in the MSH-18 field.
     *
     * @param charset The name of the character set.
     */
    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    /**
     * Sets whether to use a UUID for the filename when storing messages. If false, the Message Control ID (MSH-10) is
     * used.
     *
     * @param useUUIDForFilename {@code true} to use UUIDs, {@code false} otherwise.
     */
    public void setUseUUIDForFilename(boolean useUUIDForFilename) {
        this.useUUIDForFilename = useUUIDForFilename;
    }

    /**
     * Handles an incoming HL7 message. It stores the message to a file if a storage directory is configured, applies a
     * delay if specified, and generates a response.
     *
     * @param msg The received unparsed HL7 message.
     * @return The response message.
     * @throws Exception if an error occurs during processing.
     */
    private UnparsedHL7Message onMessage(UnparsedHL7Message msg) throws Exception {
        if (storageDir != null)
            storeToFile(
                    msg.data(),
                    new File(new File(storageDir, msg.msh().getMessageType()),
                            useUUIDForFilename ? UUID.randomUUID().toString() : msg.msh().getField(9, "_NULL_")));
        if (responseDelay > 0)
            try {
                Thread.sleep(responseDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        return new UnparsedHL7Message(
                tpls == null ? HL7Message.makeACK(msg.msh(), HL7Exception.AA, null).getBytes(null) : xslt(msg));
    }

    /**
     * Stores the raw byte data of a message to a file.
     *
     * @param data The byte array of the message.
     * @param f    The file to write to.
     * @throws IOException if an I/O error occurs.
     */
    private void storeToFile(byte[] data, File f) throws IOException {
        Logger.info("M-WRITE {}", f);
        f.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(data);
        }
    }

    /**
     * Transforms an HL7 message using the configured XSLT stylesheet to generate a response.
     *
     * @param msg The incoming message.
     * @return The transformed message as a byte array.
     * @throws Exception if an error occurs during transformation.
     */
    private byte[] xslt(UnparsedHL7Message msg) throws Exception {
        String charsetName = HL7Charset.toCharsetName(msg.msh().getField(17, charset));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerHandler th = factory.newTransformerHandler(tpls);
        Transformer t = th.getTransformer();
        t.setParameter("MessageControlID", HL7Segment.nextMessageControlID());
        t.setParameter("DateTimeOfMessage", HL7Segment.timeStamp(new Date()));
        if (xsltParams != null)
            for (int i = 1; i < xsltParams.length; i++, i++)
                t.setParameter(xsltParams[i - 1], xsltParams[i]);
        th.setResult(new SAXResult(new HL7ContentHandler(new OutputStreamWriter(out, charsetName))));
        new HL7Parser(th).parse(new InputStreamReader(new ByteArrayInputStream(msg.data()), charsetName));
        return out.toByteArray();
    }

    /**
     * Gets the main device object for this receiver.
     *
     * @return The device.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Gets the network connection configuration.
     *
     * @return The connection.
     */
    public Connection getConn() {
        return conn;
    }

}
