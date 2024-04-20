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
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.galaxy.io.SAXTransformer;
import org.miaixz.bus.image.metric.Connection;
import org.miaixz.bus.image.metric.acquire.HL7DeviceExtension;
import org.miaixz.bus.image.metric.internal.hl7.*;
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

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HL7Rcv {

    private static final SAXTransformerFactory factory =
            (SAXTransformerFactory) TransformerFactory.newInstance();

    private final Device device = new Device("hl7rcv");
    private final HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
    private final HL7Application hl7App = new HL7Application(Symbol.STAR);
    private final Connection conn = new Connection();
    private String storageDir;
    private String charset;
    private Templates tpls;
    private String[] xsltParams;
    private final HL7MessageListener handler = (hl7App, conn, s, msg) -> {
        try {
            return HL7Rcv.this.onMessage(msg);
        } catch (Exception e) {
            throw new HL7Exception(
                    new ERRSegment(msg.msh()).setUserMessage(e.getMessage()),
                    e);
        }
    };

    public HL7Rcv() {
        conn.setProtocol(Connection.Protocol.HL7);
        device.addDeviceExtension(hl7Ext);
        device.addConnection(conn);
        hl7Ext.addHL7Application(hl7App);
        hl7App.setAcceptedMessageTypes(Symbol.STAR);
        hl7App.addConnection(conn);
        hl7App.setHL7MessageListener(handler);
    }

    public void setStorageDirectory(String storageDir) {
        this.storageDir = storageDir;
    }

    public void setXSLT(URL xslt) throws Exception {
        tpls = SAXTransformer.newTemplates(
                new StreamSource(xslt.openStream(), xslt.toExternalForm()));
    }

    public void setXSLTParameters(String[] xsltParams) {
        this.xsltParams = xsltParams;
    }

    public void setCharacterSet(String charset) {
        this.charset = charset;
    }

    private UnparsedHL7Message onMessage(UnparsedHL7Message msg)
            throws Exception {
        if (null != storageDir)
            storeToFile(msg.data(), new File(
                    new File(storageDir, msg.msh().getMessageType()),
                    msg.msh().getField(9, "_NULL_")));
        return new UnparsedHL7Message(null == tpls
                ? HL7Message.makeACK(msg.msh(), HL7Exception.AA, null).getBytes(null)
                : xslt(msg));
    }

    private void storeToFile(byte[] data, File f) throws IOException {
        Logger.info("M-WRITE {}", f);
        f.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(data);
        } finally {
            out.close();
        }
    }

    private byte[] xslt(UnparsedHL7Message msg)
            throws Exception {
        String charsetName = HL7Charset.toCharsetName(msg.msh().getField(17, charset));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerHandler th = factory.newTransformerHandler(tpls);
        Transformer t = th.getTransformer();
        t.setParameter("MessageControlID", HL7Segment.nextMessageControlID());
        t.setParameter("DateTimeOfMessage", HL7Segment.timeStamp(new Date()));
        if (null != xsltParams)
            for (int i = 1; i < xsltParams.length; i++, i++)
                t.setParameter(xsltParams[i - 1], xsltParams[i]);
        th.setResult(new SAXResult(new HL7ContentHandler(
                new OutputStreamWriter(out, charsetName))));
        new HL7Parser(th).parse(new InputStreamReader(
                new ByteArrayInputStream(msg.data()),
                charsetName));
        return out.toByteArray();
    }

    public Device getDevice() {
        return device;
    }

    public Connection getConn() {
        return conn;
    }

}
