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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.galaxy.media.MultipartInputStream;
import org.miaixz.bus.image.galaxy.media.MultipartParser;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class WadoRS {

    private static boolean header;
    private static boolean allowAnyHost;
    private static boolean disableTM;
    private static String accept = "*";
    private static String outDir;
    private static String authorization;
    private static Map<String, String> requestProperties;

    private static Map<String, String> requestProperties(String[] httpHeaders) {
        Map<String, String> requestProperties = new HashMap<>();
        if (header)
            requestProperties.put("Accept", accept);
        if (authorization != null)
            requestProperties.put("Authorization", authorization);
        if (httpHeaders != null)
            for (String httpHeader : httpHeaders) {
                int delim = httpHeader.indexOf(':');
                requestProperties.put(httpHeader.substring(0, delim), httpHeader.substring(delim + 1));
            }
        return requestProperties;
    }

    private static String basicAuth(String user) {
        byte[] userPswdBytes = user.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Builder.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private static void write(InputStream in, String fileName) throws IOException {
        Path path = outDir != null ? Files.createDirectories(Paths.get(outDir)).resolve(fileName) : Paths.get(fileName);
        try (OutputStream out = Files.newOutputStream(path)) {
            IoKit.copy(in, out);
        }
    }

    private void setAccept(String... accept) {
        StringBuilder sb = new StringBuilder();
        sb.append(!header ? accept[0].replace("+", "%2B") : accept[0]);
        for (int i = 1; i < accept.length; i++)
            sb.append(",").append(!header ? accept[i].replace("+", "%2B") : accept[i]);

        WadoRS.accept = sb.toString();
    }

    private void wado(String url) throws Exception {
        final String uid = uidFrom(url);
        if (!header)
            url = appendAcceptToURL(url);
        if (url.startsWith("https"))
            wadoHttps(new URL(url), uid);
        else
            wado(new URL(url), uid);
    }

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

    SSLContext sslContext() throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustManagers(), new java.security.SecureRandom());
        return ctx;
    }

    TrustManager[] trustManagers() {
        return new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        } };
    }

    private String appendAcceptToURL(String url) {
        return url + (url.indexOf('?') != -1 ? "&" : "?") + "accept=" + accept;
    }

    private String uidFrom(String url) {
        return url.contains("metadata")
                ? url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.lastIndexOf('/'))
                : url.contains("?")
                        ? url.substring(url.substring(0, url.indexOf('?')).lastIndexOf('/') + 1, url.indexOf('?'))
                        : url.substring(url.lastIndexOf('/') + 1);
    }

    private void logOutgoing(URL url, Map<String, List<String>> headerFields) {
        Logger.info("> GET " + url.toString());
        headerFields.forEach((k, v) -> Logger.info("> " + k + " : " + String.join(",", v)));
    }

    private void processWadoResp(HttpURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    private void processWadoHttpsResp(HttpsURLConnection connection, String uid) throws Exception {
        int respCode = connection.getResponseCode();
        logIncoming(respCode, connection.getResponseMessage(), connection.getHeaderFields());
        if (respCode != 200 && respCode != 206)
            return;

        unpack(connection.getInputStream(), connection.getContentType(), uid);
    }

    private void logIncoming(int respCode, String respMsg, Map<String, List<String>> headerFields) {
        Logger.info("< HTTP/1.1 Response: " + respCode + " " + respMsg);
        for (Map.Entry<String, List<String>> header : headerFields.entrySet())
            if (header.getKey() != null)
                Logger.info("< " + header.getKey() + " : " + String.join(";", header.getValue()));
    }

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

            new MultipartParser(boundary).parse(new BufferedInputStream(is), new MultipartParser.Handler() {
                @Override
                public void bodyPart(int partNumber, MultipartInputStream multipartInputStream) throws IOException {
                    Map<String, List<String>> headerParams = multipartInputStream.readHeaderParams();
                    try {
                        String fileName = fileName(partNumber, uid,
                                partExtension(headerParams.get("content-type").get(0)));
                        Logger.info("Extract Part #{} {} \n{}", partNumber, fileName, headerParams);
                        write(multipartInputStream, fileName);
                    } catch (Exception e) {
                        Logger.warn("Failed to process Part #" + partNumber + headerParams, e);
                    }
                }
            });
        } catch (Exception e) {
            Logger.info("Exception caught on unpacking response \n", e);
        }
    }

    private void write(String uid, String ext, InputStream is) throws IOException {
        String fileName = fileName(1, uid, ext);
        Logger.info("Extract {} to {}", ext, fileName);
        write(is, fileName);
    }

    private String partExtension(String partContentType) {
        String contentType = partContentType.split(";")[0].replaceAll("[-+/]", "_");
        return contentType.substring(contentType.lastIndexOf("_") + 1);
    }

    private String boundary(String contentType) {
        String[] respContentTypeParams = contentType.split(";");
        for (String respContentTypeParam : respContentTypeParams)
            if (respContentTypeParam.replace(" ", "").startsWith("boundary="))
                return respContentTypeParam.substring(respContentTypeParam.indexOf("=") + 1).replaceAll("\"", "");

        return null;
    }

    private String fileName(int partNumber, String uid, String ext) {
        return uid + "-" + String.format("%03d", partNumber) + "." + ext;
    }

}
