/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.image.galaxy.media;

import org.aoju.bus.core.exception.InternalException;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.BulkData;
import org.aoju.bus.image.galaxy.data.DatePrecision;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.SAXReader;
import org.aoju.bus.logger.Logger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AbstractStowrs implements AutoCloseable {

    protected static final String MULTIPART_BOUNDARY = "mimeTypeBoundary";

    private final List<HttpURLConnection> connections;
    private final String contentType;
    private final String requestURL;
    private final String agentName;
    private final Map<String, String> headers;

    /**
     * @param requestURL  STOW服务的URL
     * @param contentType Content-Type HTTP属性中类型的值
     * @param agentName   User-Agent HTTP属性的值
     * @param headers     一些其他标题属性
     */
    public AbstractStowrs(String requestURL, String contentType, String agentName,
                          Map<String, String> headers) {
        this.contentType = Objects.requireNonNull(contentType);
        this.requestURL = Objects.requireNonNull(requestURL, "requestURL cannot be null");
        this.headers = headers;
        this.agentName = agentName;
        this.connections = new ArrayList<>();
    }

    protected static void ensureUID(Attributes attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.setString(tag, VR.UI, UID.createUID());
        }
    }

    protected static void setEncapsulatedDocumentAttributes(Path bulkDataFile, Attributes metadata, String mdiaType) {
        metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
        metadata.setString(Tag.ContentDate, VR.DA,
                formatDA(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.ContentTime, VR.TM,
                formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.AcquisitionDateTime, VR.DT,
                formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
        metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
        metadata.setNull(Tag.DocumentTitle, VR.ST);
        metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
        metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO, mdiaType);
    }

    public static String formatDA(TimeZone tz, Date date) {
        return formatDA(tz, date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(TimeZone tz, Date date,
                                         StringBuilder toAppendTo) {
        return formatDT(cal(tz, date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    private static StringBuilder formatDT(Calendar cal, StringBuilder toAppendTo,
                                          int lastField) {
        appendXXXX(cal.get(Calendar.YEAR), toAppendTo);
        if (lastField > Calendar.YEAR) {
            appendXX(cal.get(Calendar.MONTH) + 1, toAppendTo);
            if (lastField > Calendar.MONTH) {
                appendXX(cal.get(Calendar.DAY_OF_MONTH), toAppendTo);
                if (lastField > Calendar.DAY_OF_MONTH) {
                    formatTM(cal, toAppendTo, lastField);
                }
            }
        }
        return toAppendTo;
    }

    public static String formatTM(TimeZone tz, Date date) {
        return formatTM(tz, date, new DatePrecision());
    }

    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return formatTM(cal(tz, date), new StringBuilder(10),
                precision.lastField).toString();
    }

    private static StringBuilder formatTM(Calendar cal,
                                          StringBuilder toAppendTo, int lastField) {
        appendXX(cal.get(Calendar.HOUR_OF_DAY), toAppendTo);
        if (lastField > Calendar.HOUR_OF_DAY) {
            appendXX(cal.get(Calendar.MINUTE), toAppendTo);
            if (lastField > Calendar.MINUTE) {
                appendXX(cal.get(Calendar.SECOND), toAppendTo);
                if (lastField > Calendar.SECOND) {
                    toAppendTo.append(Symbol.C_DOT);
                    appendXXX(cal.get(Calendar.MILLISECOND), toAppendTo);
                }
            }
        }
        return toAppendTo;
    }

    private static Calendar cal(TimeZone tz, Date date) {
        Calendar cal = (null != tz)
                ? new GregorianCalendar(tz)
                : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000)
            toAppendTo.append('0');
        appendXXX(i, toAppendTo);
    }

    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100)
            toAppendTo.append('0');
        appendXX(i, toAppendTo);
    }

    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10)
            toAppendTo.append('0');
        toAppendTo.append(i);
    }

    protected HttpURLConnection buildConnection() throws IOException {
        try {

            URL url = new URL(requestURL);
            HttpURLConnection httpPost = (HttpURLConnection) url.openConnection();

            httpPost.setUseCaches(false);
            httpPost.setDoOutput(true);// indicates POST method
            httpPost.setDoInput(true);
            httpPost.setRequestMethod("POST");
            httpPost.setConnectTimeout(10000);
            httpPost.setReadTimeout(60000);
            httpPost.setRequestProperty(Header.CONTENT_TYPE,
                    MediaType.MULTIPART_RELATED + "; type=\"" + contentType + "\"; boundary=" + MULTIPART_BOUNDARY);
            httpPost.setRequestProperty(Header.USER_AGENT, null == agentName ? "STOWRS" : agentName);
            httpPost.setRequestProperty(Header.ACCEPT,
                    contentType == MediaType.APPLICATION_DICOM_JSON ? MediaType.APPLICATION_DICOM_JSON : MediaType.APPLICATION_DICOM_XML);

            if (null != headers && !headers.isEmpty()) {
                for (Entry<String, String> element : headers.entrySet()) {
                    httpPost.setRequestProperty(element.getKey(), element.getValue());
                }
            }
            connections.add(httpPost);
            return httpPost;

        } catch (IOException e) {
            try {
                close();
            } catch (Exception e1) {
                // Do nothing
            }
            throw e;
        }
    }

    private void endMarkers(DataOutputStream out) throws IOException {
        out.write(MultipartParser.Separator.BOUNDARY.getType());
        out.writeBytes(MULTIPART_BOUNDARY);
        out.write(MultipartParser.Separator.STREAM.getType());
        out.flush();
        out.close();
    }

    protected void writeContentMarkers(DataOutputStream out) throws IOException {
        out.write(MultipartParser.Separator.BOUNDARY.getType());
        out.writeBytes(MULTIPART_BOUNDARY);
        out.write(MultipartParser.Separator.FIELD.getType());
        out.writeBytes(Header.CONTENT_TYPE + ": ");
        out.writeBytes(contentType);
        out.write(MultipartParser.Separator.HEADER.getType());
    }

    protected void writeEndMarkers(HttpURLConnection httpPost, DataOutputStream out, String iuid) throws IOException {
        endMarkers(out);

        int code = httpPost.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            Logger.info("STOWRS server response message: HTTP Status-Code 200: OK for {}", iuid);
        } else {
            throw new InternalException(
                    String.format("STOWRS server response message: %s", httpPost.getResponseMessage()));
        }
    }

    protected Attributes writeEndMarkers(HttpURLConnection httpPost, DataOutputStream out)
            throws IOException, ParserConfigurationException, SAXException {
        endMarkers(out);

        int code = httpPost.getResponseCode();
        if (code == HttpURLConnection.HTTP_OK) {
            Logger.info("STOWRS server response message: HTTP Status-Code 200: OK for all the image set");
        } else if (code == HttpURLConnection.HTTP_ACCEPTED || code == HttpURLConnection.HTTP_CONFLICT) {
            Logger.warn("STOWRS server response message: HTTP Status-Code {}: {}", code, httpPost.getResponseMessage());
            return SAXReader.parse(httpPost.getInputStream());
        } else {
            throw new InternalException(String.format("STOWRS server response message: HTTP Status-Code %d: %s",
                    code, httpPost.getResponseMessage()));
        }
        return null;
    }

    protected String getContentLocation(Attributes metadata) {
        BulkData data = ((BulkData) metadata.getValue(Tag.EncapsulatedDocument));
        if (null != data) {
            return data.getURI();
        }

        data = ((BulkData) metadata.getValue(Tag.PixelData));
        if (null != data) {
            return data.getURI();
        }
        return null;
    }

    protected void removeConnection(HttpURLConnection httpPost) {
        connections.remove(httpPost);
    }

    @Override
    public void close() {
        connections.forEach(HttpURLConnection::disconnect);
        connections.clear();
    }

    public String getContentType() {
        return contentType;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
