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
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.galaxy.media.MultipartParser;
import org.miaixz.bus.logger.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code WadoRS} class provides a client for retrieving DICOM objects via the DICOMweb WADO-RS (Web Access to DICOM
 * Objects by RESTful Services) standard. It handles making HTTP/HTTPS GET requests and processing the responses, which
 * can be single or multipart.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WadoRS {

    /**
     * A flag indicating whether to use the Accept header or a URL parameter for content negotiation.
     */
    private static boolean header;
    /**
     * A flag to allow connections to any HTTPS host, bypassing hostname verification.
     */
    private static boolean allowAnyHost;
    /**
     * A flag to disable the default SSL trust manager, effectively trusting all server certificates.
     */
    private static boolean disableTM;
    /**
     * The value for the HTTP Accept header.
     */
    private static String accept = "*";
    /**
     * The directory where retrieved files will be stored.
     */
    private static String outDir;
    /**
     * The value for the HTTP Authorization header.
     */
    private static String authorization;
    /**
     * A map of custom HTTP request properties (headers).
     */
    private static Map<String, String> requestProperties;

    /**
     * Creates a map of HTTP request properties (headers) from an array of strings.
     *
     * @param httpHeaders An array of custom headers in "Name:Value" format.
     * @return A map of request properties.
     */
    private static Map<String, String> requestProperties(String[] httpHeaders) {
        Map<String, String> requestProperties = new HashMap<>();
        if (header)
            requestProperties.put("Accept", accept);
        if (authorization != null)
            requestProperties.put("Authorization", authorization);
        if (httpHeaders != null)
            for (String httpHeader : httpHeaders) {
                int delim = httpHeader.indexOf(':');
                requestProperties.put(httpHeader.substring(0, delim), httpHeader.substring(delim + 1).trim());
            }
        return requestProperties;
    }

    /**
     * Creates a Basic Authentication header value.
     *
     * @param user The username and password in "username:password" format.
     * @return The Base64 encoded "Basic" authorization string.
     */
    private static String basicAuth(String user) {
        byte[] userPswdBytes = user.getBytes();
        return "Basic " + Base64.getEncoder().encodeToString(userPswdBytes);
    }

    /**
     * Writes the content of an InputStream to a file.
     *
     * @param in       The input stream to read from.
     * @param fileName The name of the file to write to.
     * @throws IOException if an I/O error occurs.
     */
    private static void write(InputStream in, String fileName) throws IOException {
        Path path = outDir != null ? Files.createDirectories(Paths.get(outDir)).resolve(fileName) : Paths.get(fileName);
        try (OutputStream out = Files.newOutputStream(path)) {
            IoKit.copy(in, out);
        }
    }

    /**
     * Sets the Accept header value from a list of content types.
     *
     * @param accept An array of content type strings.
     */
    private void setAccept(String... accept) {
        StringBuilder sb = new StringBuilder();
        sb.append(!header ? accept[0].replace("+", "%2B") : accept[0]);
        for (int i = 1; i < accept.length; i++)
            sb.append(Symbol.COMMA).append(!header ? accept[i].replace("+", "%2B") : accept[i]);

        WadoRS.accept = sb.toString();
    }

    /**
     * Initiates a WADO-RS request for a given URL.
     *
     * @param url The WADO-RS URL.
     * @throws Exception if an error occurs during the request.
     */
    private void wado(String url) throws Exception {
        final String uid = uidFrom(url);
        if (!header)
            url = appendAcceptToURL(url);
        if (url.startsWith("https"))
            wadoHttps(new URL(url), uid);
        else
            wado(new URL(url), uid);
    }

    /**
     * Performs a WADO-RS request over HTTP.
     *
     * @param url The URL of the WADO-RS service.
     * @param uid A UID extracted from the URL, used for naming the output file.
     * @throws Exception if an error occurs.
     */
    private void wado(URL url, String uid) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        requestProperties.forEach(connection::setRequestProperty);
        logOutgoing(url, connection.getRequestProperties());
        processWadoResp(connection, uid);
        connection.disconnect();
    }

    /**
     * Performs a WADO-RS request over HTTPS.
     *
     * @param url The URL of the WADO-RS service.
     * @param uid A UID extracted from the URL, used for naming the output file.
     * @throws Exception if an error occurs.
     */
    private void wadoHttps(URL url, String uid) throws Exception {
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        requestProperties.forEach(connection::setRequestProperty);
        if (disableTM)
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
        connection.setHostnameVerifier((hostname, session) -> allowAnyHost);
        logOutgoing(url, connection.getRequestProperties());
        processWadoHttpsResp(connection, uid);
        connection.disconnect();
    }

    /**
     * Creates an SSLContext that trusts all certificates.
     *
     * @return The configured SSLContext.
     * @throws GeneralSecurityException if a security error occurs.
     */
    SSLContext sslContext() throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustManagers(), new java.security.SecureRandom());
        return ctx;
    }

    /**
     * Creates an array of TrustManagers that do not validate certificate chains.
     *
     * @return An array containing a permissive X509TrustManager.
     */
    TrustManager[] trustManagers() {
        return new TrustManager[] { new X509TrustManager() {

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
    }

    /**
     * Appends the `accept` parameter to the URL for content negotiation.
     *
     * @param url The original URL.
     * @return The URL with the accept parameter appended.
     */
    private String appendAcceptToURL(String url) {
        return url + (url.contains("?") ? "&" : "?") + "accept=" + accept;
    }

    /**
     * Extracts a UID from the last path segment of a URL.
     *
     * @param url The URL string.
     * @return The extracted UID string.
     */
    private String uidFrom(String url) {
        return url.contains("metadata")
                ? url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.lastIndexOf('/'))
                : url.contains("?")
                        ? url.substring(url.substring(0, url.indexOf('?')).lastIndexOf('/') + 1, url.indexOf('?'))
                        : url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * Logs the outgoing HTTP request headers.
     *
     * @param url          The request URL.
     * @param headerFields The map of request headers.
     */
    private void logOutgoing(URL url, Map<String, List<String>> headerFields) {
        Logger.info("> GET " + url.toString());
        headerFields.forEach((k, v) -> Logger.info("> " + k + " : " + String.join(Symbol.COMMA, v)));
    }

    /**
     * Processes the response from an HTTP WADO-RS request.
     *
     * @param connection The active HttpURLConnection.
     * @param uid        The UID associated with the request.
     * @throws Exception if an error occurs.
     */
    private void processWadoResp(HttpURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    /**
     * Processes the response from an HTTPS WADO-RS request.
     *
     * @param connection The active HttpsURLConnection.
     * @param uid        The UID associated with the request.
     * @throws Exception if an error occurs.
     */
    private void processWadoHttpsResp(HttpsURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    /**
     * Logs the incoming HTTP response headers.
     *
     * @param respCode     The HTTP response code.
     * @param respMsg      The HTTP response message.
     * @param headerFields The map of response headers.
     */
    private void logIncoming(int respCode, String respMsg, Map<String, List<String>> headerFields) {
        Logger.info("< HTTP/1.1 Response: " + respCode + Symbol.SPACE + respMsg);
        for (Map.Entry<String, List<String>> header : headerFields.entrySet())
            if (header.getKey() != null)
                Logger.info("< " + header.getKey() + " : " + String.join(";", header.getValue()));
    }

    /**
     * Unpacks the response body, handling both single-part and multipart responses.
     *
     * @param is          The InputStream of the response body.
     * @param contentType The Content-Type of the response.
     * @param uid         The UID associated with the request.
     */
    private void unpack(InputStream is, String contentType, final String uid) {
        try {
            if (!contentType.contains("multipart/related")) {
                write(uid, partExtension(contentType), is);
                return;
            }

            String boundary = boundary(contentType);
            if (boundary == null) {
                Logger.warn("Invalid response. Unpacking of parts not possible.");
                return;
            }

            new MultipartParser(boundary).parse(
                    new BufferedInputStream(is),
                    (MultipartParser.Handler) (partNumber, multipartInputStream) -> {
                        Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
                        try {
                            String fileName = fileName(
                                    partNumber,
                                    uid,
                                    partExtension(headerParams.get("Content-Type").get(0)));
                            Logger.info("Extract Part #{} {} \n{}", partNumber, fileName, headerParams);
                            write(multipartInputStream, fileName);
                        } catch (Exception e) {
                            Logger.warn("Failed to process Part #" + partNumber + headerParams, e);
                        }
                    });
        } catch (Exception e) {
            Logger.info("Exception caught on unpacking response \n", e);
        }
    }

    /**
     * Writes a single-part response to a file.
     *
     * @param uid The UID for the filename.
     * @param ext The file extension.
     * @param is  The InputStream of the content.
     * @throws IOException if an I/O error occurs.
     */
    private void write(String uid, String ext, InputStream is) throws IOException {
        String fileName = fileName(1, uid, ext);
        Logger.info("Extract {} to {}", ext, fileName);
        write(is, fileName);
    }

    /**
     * Extracts a file extension from a content type string.
     *
     * @param partContentType The content type string.
     * @return The derived file extension.
     */
    private String partExtension(String partContentType) {
        String contentType = partContentType.split(";")[0].replaceAll("[-+/]", "_");
        return contentType.substring(contentType.lastIndexOf("_") + 1);
    }

    /**
     * Extracts the boundary string from a multipart content type header.
     *
     * @param contentType The full Content-Type header value.
     * @return The boundary string, or {@code null} if not found.
     */
    private String boundary(String contentType) {
        String[] respContentTypeParams = contentType.split(";");
        for (String respContentTypeParam : respContentTypeParams)
            if (respContentTypeParam.trim().startsWith("boundary="))
                return respContentTypeParam.substring(respContentTypeParam.indexOf("=") + 1).replaceAll("\"", "");

        return null;
    }

    /**
     * Generates a filename for a response part.
     *
     * @param partNumber The part number.
     * @param uid        The base UID for the filename.
     * @param ext        The file extension.
     * @return The generated filename.
     */
    private String fileName(int partNumber, String uid, String ext) {
        return uid + "-" + String.format("%03d", partNumber) + "." + ext;
    }

}
