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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.stream.StreamResult;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXReader;
import org.miaixz.bus.image.galaxy.io.SAXTransformer;
import org.miaixz.bus.image.metric.json.JSONWriter;
import org.miaixz.bus.image.nimble.codec.XPEGParser;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEG;
import org.miaixz.bus.image.nimble.codec.jpeg.JPEGParser;
import org.miaixz.bus.image.nimble.codec.mp4.MP4Parser;
import org.miaixz.bus.image.nimble.codec.mpeg.MPEG2Parser;
import org.miaixz.bus.logger.Logger;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StowRS {

    private static final String boundary = "myboundary";
    private static final AtomicInteger fileCount = new AtomicInteger();
    private static final Map<String, StowRSBulkdata> contentLocBulkdata = new HashMap<>();
    private static final int[] IUIDS_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID };
    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime };
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static String url;
    private static boolean vlPhotographicImage;
    private static boolean videoPhotographicImage;
    private static String requestAccept;
    private static String requestContentType;
    private static String metadataFilePathStr;
    private static File metadataFile;
    private static boolean allowAnyHost;
    private static boolean disableTM;
    private static boolean encapsulatedDocLength;
    private static String authorization;
    private static int limit;
    private static FileContentType fileContentTypeFromCL;
    private static FileContentType firstBulkdataFileContentType;
    private static FileContentType bulkdataFileContentType;
    private final Attributes attrs = new Attributes();
    private final List<StowChunk> stowChunks = new ArrayList<>();
    private boolean noApp;
    private boolean pixelHeader;
    private boolean tsuid;
    private String uidSuffix;
    private String tmpPrefix;
    private String tmpSuffix;
    private File tmpDir;
    private int filesScanned;
    private int filesSent;
    private long totalSize;
    private Map<String, String> requestProperties;

    private static void logSentPerChunk(StowChunk stowChunk, long t1) {
        if (stowChunk.sent == 0)
            return;

        long t2 = System.currentTimeMillis();
        float s = (t2 - t1) / 1000F;
        float mb = stowChunk.getSize() / 1048576F;
    }

    private static void logSent(StowRS stowRS, long t1) {
        if (stowRS.filesSent == 0 || limit == 0)
            return;

        long t2 = System.currentTimeMillis();
        float s = (t2 - t1) / 1000F;
        float mb = stowRS.totalSize / 1048576F;
    }

    private static FileContentType fileContentType(String s) {
        switch (s.toLowerCase(Locale.ENGLISH)) {
        case "stl":
        case "model/stl":
            return FileContentType.STL;
        case "model/x.stl-binary":
            return FileContentType.STL_BINARY;
        case "application/sla":
            return FileContentType.SLA;
        case "pdf":
        case "application/pdf":
            return FileContentType.PDF;
        case "xml":
        case "application/xml":
            return FileContentType.CDA;
        case "mtl":
        case "model/mtl":
            return FileContentType.MTL;
        case "obj":
        case "model/obj":
            return FileContentType.OBJ;
        case "genozip":
        case "application/vnd.genozip":
            return FileContentType.GENOZIP;
        case "vcf.bz2":
        case "vcfbzip2":
        case "vcfbz2":
        case "application/prs.vcfbzip2":
            return FileContentType.VCF_BZIP2;
        case "boz":
        case "bz2":
        case "application/x-bzip2":
            return FileContentType.DOC_BZIP2;
        case "jhc":
        case "image/jphc":
            return FileContentType.JPHC;
        case "jph":
        case "image/jph":
            return FileContentType.JPH;
        case "jpg":
        case "jpeg":
        case "image/jpeg":
            return FileContentType.JPEG;
        case "j2c":
        case "j2k":
        case "image/j2c":
            return FileContentType.J2C;
        case "jp2":
        case "image/jp2":
            return FileContentType.JP2;
        case "png":
        case "image/png":
            return FileContentType.PNG;
        case "gif":
        case "image/gif":
            return FileContentType.GIF;
        case "mpeg":
        case "video/mpeg":
            return FileContentType.MPEG;
        case "mp4":
        case "video/mp4":
            return FileContentType.MP4;
        case "mov":
        case "video/quicktime":
            return FileContentType.QUICKTIME;
        default:
            throw new IllegalArgumentException(s);
        }
    }

    private static void addAttributesFromFile(Attributes metadata) throws Exception {
        if (metadataFilePathStr == null)
            return;

        metadata.addAll(SAXReader.parse(metadataFilePathStr, metadata));
    }

    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUIDS_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    private static void supplementMissingUID(Attributes metadata, int tag) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, VR.UI, UID.createUID());
    }

    private static void supplementSOPClass(Attributes metadata, String value) {
        if (!metadata.containsValue(Tag.SOPClassUID))
            metadata.setString(Tag.SOPClassUID, VR.UI, value);
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    private static void supplementEncapsulatedDocAttrs(Attributes metadata, StowRSBulkdata stowRSBulkdata) {
        if (!metadata.contains(Tag.AcquisitionDateTime))
            metadata.setNull(Tag.AcquisitionDateTime, VR.DT);
        if (encapsulatedDocLength)
            metadata.setLong(Tag.EncapsulatedDocumentLength, VR.UL, stowRSBulkdata.getFileLength());
    }

    private static String readFullyAsString(InputStream inputStream) throws IOException {
        return readFully(inputStream).toString(StandardCharsets.UTF_8);
    }

    private static ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[16384];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos;
        }
    }

    private static void writePartHeaders(OutputStream out, String contentType, String contentLocation)
            throws IOException {
        out.write(("\r\n--" + boundary + "\r\n").getBytes());
        out.write(("Content-Type: " + contentType + "\r\n").getBytes());
        if (contentLocation != null)
            out.write(("Content-Location: " + contentLocation + "\r\n").getBytes());
        out.write("\r\n".getBytes());
    }

    public final void setRequestProperties(Map<String, String> requestProperties) {
        this.requestProperties = requestProperties;
    }

    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix;
    }

    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    public final void setTmpFileDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    private void scan(List<String> files) {
        long t1, t2;
        t1 = System.currentTimeMillis();
        scanFiles(files);
        t2 = System.currentTimeMillis();
        System.out.println("..");
        if (filesScanned == 0) {
        }
    }

    private Attributes createMetadata(Attributes staticMetadata) {
        Attributes metadata = new Attributes(staticMetadata);
        supplementMissingUID(metadata, Tag.SOPInstanceUID);
        supplementType2Tags(metadata);
        return metadata;
    }

    private Attributes supplementMetadataFromFile(Path bulkdataFilePath, Attributes metadata) {
        String contentLoc = "bulk" + UID.createUID();
        metadata.setValue(bulkdataFileContentType.getBulkdataTypeTag(), VR.OB, new BulkData(null, contentLoc, false));
        StowRSBulkdata stowRSBulkdata = new StowRSBulkdata(bulkdataFilePath);
        switch (bulkdataFileContentType) {
        case SLA:
        case STL:
        case STL_BINARY:
        case OBJ:
            supplementMissingUID(metadata, Tag.FrameOfReferenceUID);
        case PDF:
        case CDA:
        case MTL:
        case GENOZIP:
        case VCF_BZIP2:
        case DOC_BZIP2:
            supplementEncapsulatedDocAttrs(metadata, stowRSBulkdata);
            contentLocBulkdata.put(contentLoc, stowRSBulkdata);
            break;
        case JPH:
        case JPHC:
        case JPEG:
        case JP2:
        case J2C:
        case PNG:
        case GIF:
        case MPEG:
        case MP4:
        case QUICKTIME:
            pixelMetadata(contentLoc, stowRSBulkdata, metadata);
            break;
        }
        return metadata;
    }

    private void pixelMetadata(String contentLoc, StowRSBulkdata stowRSBulkdata, Attributes metadata) {
        File bulkdataFile = stowRSBulkdata.getBulkdataFile();
        if (pixelHeader || tsuid || noApp) {
            CompressedPixelData compressedPixelData = CompressedPixelData.valueOf();
            try (FileInputStream fis = new FileInputStream(bulkdataFile)) {
                compressedPixelData.parse(fis.getChannel());
                XPEGParser parser = compressedPixelData.getParser();
                if (pixelHeader)
                    parser.getAttributes(metadata);
                stowRSBulkdata.setParser(parser);
            } catch (IOException e) {
                Logger.info("Exception caught getting pixel data from file {}: {}", bulkdataFile, e.getMessage());
            }
        }
        contentLocBulkdata.put(contentLoc, stowRSBulkdata);
    }

    private Attributes createStaticMetadata() throws Exception {
        Logger.info("Creating static metadata. Set defaults, if essential attributes are not present.");
        Attributes metadata;
        metadata = SAXReader.parse(IoKit.openFileOrURL(firstBulkdataFileContentType.getSampleMetadataResourceURL()));
        addAttributesFromFile(metadata);
        supplementSOPClass(metadata, firstBulkdataFileContentType.getSOPClassUID());
        metadata.addAll(attrs);
        if (!url.endsWith("studies"))
            metadata.setString(Tag.StudyInstanceUID, VR.UI, url.substring(url.lastIndexOf("/") + 1));
        supplementMissingUIDs(metadata);
        return metadata;
    }

    private void scanFiles(List<String> files) {
        if (limit == 0) {
            scanFilesNoLimit(files);
            return;
        }

        final AtomicInteger counter = new AtomicInteger();
        files.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / limit)).values().forEach(fPR -> {
            List<String> filePaths = new ArrayList<>();
            for (String f : fPR) {
                try {
                    Path path = Paths.get(f);
                    if (Files.isDirectory(path)) {
                        List<String> dirPaths = Files.list(path).map(Path::toString).collect(Collectors.toList());
                        scanFiles(dirPaths);
                    } else
                        filePaths.add(f);
                } catch (Exception e) {
                    Logger.info("Failed to list files of directory : {}\n", f, e);
                }
            }
            processFilesPerRequest(filePaths.equals(fPR) ? fPR : filePaths);
        });
    }

    private void scanFilesNoLimit(List<String> files) {
        try {
            File tmpFile = File.createTempFile("stowrs-", null, null);
            tmpFile.deleteOnExit();
            StowChunk stowChunk = new StowChunk(tmpFile);
            try (FileOutputStream out = new FileOutputStream(tmpFile)) {
                if (requestContentType.equals(MediaType.APPLICATION_DICOM))
                    for (String file : files)
                        applyFunctionToFile(file, true, path -> writeDicomFile(out, path, stowChunk));
                else
                    writeMetadataAndBulkData(out, files, createStaticMetadata(), stowChunk);
            }
            stowChunks.add(stowChunk);
        } catch (Exception e) {
            Logger.info("Failed to scan files in tmp file\n", e);
        }
    }

    private void processFilesPerRequest(List<String> fPR) {
        if (fPR.isEmpty())
            return;

        try {
            File tmpFile = File.createTempFile(tmpPrefix, tmpSuffix, tmpDir);
            tmpFile.deleteOnExit();
            StowChunk stowChunk = new StowChunk(tmpFile);
            try (FileOutputStream out = new FileOutputStream(tmpFile)) {
                if (requestContentType.equals(MediaType.APPLICATION_DICOM))
                    fPR.forEach(f -> {
                        try {
                            applyFunctionToFile(f, true, path -> writeDicomFile(out, path, stowChunk));
                        } catch (Exception e) {
                            Logger.info("Failed to scan : {}\n", f, e);
                        }
                    });
                else
                    writeMetadataAndBulkData(out, fPR, createStaticMetadata(), stowChunk);
            }
            filesScanned += stowChunk.getScanned().get();
            stowChunks.add(stowChunk);
        } catch (Exception e) {
            Logger.info("Failed to scan {} in tmp file\n", fPR, e);
        }
    }

    private Map<String, String> requestProperties(String[] httpHeaders) {
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Content-Type",
                MediaType.MULTIPART_RELATED + "; type=\"" + requestContentType + "\"; boundary=" + boundary);
        requestProperties.put("Accept", requestAccept);
        requestProperties.put("Connection", "keep-alive");
        if (authorization != null)
            requestProperties.put("Authorization", authorization);
        if (httpHeaders != null)
            for (String httpHeader : httpHeaders) {
                int delim = httpHeader.indexOf(':');
                requestProperties.put(httpHeader.substring(0, delim), httpHeader.substring(delim + 1));
            }
        return requestProperties;
    }

    private void stow(final HttpURLConnection connection, StowChunk stowChunk) throws Exception {
        File tmpFile = stowChunk.getTmpFile();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Length", String.valueOf(tmpFile.length()));
        requestProperties.forEach(connection::setRequestProperty);
        logOutgoing(connection.getURL(), connection.getRequestProperties());
        OutputStream out = connection.getOutputStream();
        try {
            IoKit.copy(new FileInputStream(tmpFile), out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            logIncoming(connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields(),
                    connection.getInputStream());
            connection.disconnect();
            filesSent += stowChunk.sent();
            totalSize += stowChunk.getSize();
        } finally {
            out.close();
        }
    }

    private HttpURLConnection open() throws Exception {
        long t1, t2;
        t1 = System.currentTimeMillis();
        URLConnection urlConnection = new URL(url).openConnection();
        final HttpURLConnection connection = (HttpURLConnection) urlConnection;
        t2 = System.currentTimeMillis();
        return connection;
    }

    private HttpsURLConnection openTLS() throws Exception {
        long t1, t2;
        t1 = System.currentTimeMillis();
        final HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        t2 = System.currentTimeMillis();
        return connection;
    }

    private void stowHttps(final HttpsURLConnection connection, StowChunk stowChunk) throws Exception {
        File tmpFile = stowChunk.getTmpFile();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        if (disableTM)
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
        connection.setRequestProperty("Content-Length", String.valueOf(tmpFile.length()));
        requestProperties.forEach(connection::setRequestProperty);
        connection.setHostnameVerifier((hostname, session) -> allowAnyHost);
        logOutgoing(connection.getURL(), connection.getRequestProperties());
        try (OutputStream out = connection.getOutputStream()) {
            IoKit.copy(new FileInputStream(tmpFile), out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            logIncoming(connection.getResponseCode(), connection.getResponseMessage(), connection.getHeaderFields(),
                    connection.getInputStream());
            connection.disconnect();
            filesSent += stowChunk.sent();
            totalSize += stowChunk.getSize();
        }
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

    private String basicAuth(String user) {
        byte[] userPswdBytes = user.getBytes();
        int len = (userPswdBytes.length * 4 / 3 + 3) & ~3;
        char[] ch = new char[len];
        Builder.encode(userPswdBytes, 0, userPswdBytes.length, ch, 0);
        return "Basic " + new String(ch);
    }

    private void logOutgoing(URL url, Map<String, List<String>> headerFields) {
        Logger.info("> POST " + url.toString());
        headerFields.forEach((k, v) -> Logger.info("> " + k + " : " + String.join(Symbol.COMMA, v)));
    }

    private void logIncoming(int respCode, String respMsg, Map<String, List<String>> headerFields, InputStream is) {
        Logger.info("< HTTP/1.1 Response: " + respCode + Symbol.SPACE + respMsg);
        for (Map.Entry<String, List<String>> header : headerFields.entrySet())
            if (header.getKey() != null)
                Logger.info("< " + header.getKey() + " : " + String.join(";", header.getValue()));
        Logger.info("< Response Content: ");
        try {
            Logger.debug(readFullyAsString(is));
            is.close();
        } catch (Exception e) {
            Logger.info("Exception caught on reading response body \n", e);
        }
    }

    private void writeDicomFile(OutputStream out, Path path, StowChunk stowChunk) throws IOException {
        if (Files.probeContentType(path) == null) {
            return;
        }
        writePartHeaders(out, requestContentType, null);
        Files.copy(updateAttrs(path), out);
        stowChunk.setAttributes(path.toFile().length());
    }

    private Path updateAttrs(Path path) {
        if (attrs.isEmpty() && uidSuffix == null)
            return path;

        try {
            ImageInputStream in = new ImageInputStream(path.toFile());
            File tmpFile = File.createTempFile("stowrs-", null, null);
            tmpFile.deleteOnExit();
            Attributes fmi = in.readFileMetaInformation();
            Attributes data = in.readDataset();
            String tsuid = in.getTransferSyntax();
            try (ImageOutputStream dos = new ImageOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)),
                    fmi != null ? UID.ExplicitVRLittleEndian.uid
                            : tsuid != null ? tsuid : UID.ImplicitVRLittleEndian.uid)) {
                dos.writeDataset(fmi, data);
                dos.finish();
                dos.flush();
            }
            return tmpFile.toPath();
        } catch (Exception e) {
            Logger.info("Failed to update attributes for file {}\n", path, e);
        }
        return path;
    }

    private void writeMetadataAndBulkData(OutputStream out, List<String> files, final Attributes staticMetadata,
            StowChunk stowChunk) throws Exception {
        if (requestContentType.equals(MediaType.APPLICATION_DICOM_XML))
            writeXMLMetadataAndBulkdata(out, files, staticMetadata, stowChunk);

        else {
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                try (JsonGenerator gen = Json.createGenerator(bOut)) {
                    gen.writeStartArray();
                    if (files.isEmpty()) {
                        new JSONWriter(gen).write(createMetadata(staticMetadata));
                        stowChunk.setAttributes(metadataFile.length());
                    }

                    for (String file : files)
                        applyFunctionToFile(file, true, path -> {
                            if (!ignoreNonMatchingFileContentTypes(path))
                                new JSONWriter(gen)
                                        .write(supplementMetadataFromFile(path, createMetadata(staticMetadata)));
                        });

                    gen.writeEnd();
                    gen.flush();
                }
                writeMetadata(out, bOut);

                for (String contentLocation : contentLocBulkdata.keySet())
                    writeFile(contentLocation, out, stowChunk);
            }
        }
        contentLocBulkdata.clear();
    }

    private void writeXMLMetadataAndBulkdata(final OutputStream out, List<String> files,
            final Attributes staticMetadata, StowChunk stowChunk) throws Exception {
        if (files.isEmpty()) {
            writeXMLMetadata(out, staticMetadata);
            stowChunk.setAttributes(metadataFile.length());
        }

        for (String file : files)
            applyFunctionToFile(file, true, path -> writeXMLMetadataAndBulkdata(out, staticMetadata, path, stowChunk));
    }

    private void writeXMLMetadataAndBulkdata(OutputStream out, Attributes staticMetadata, Path bulkdataFilePath,
            StowChunk stowChunk) {
        try {
            if (ignoreNonMatchingFileContentTypes(bulkdataFilePath))
                return;

            Attributes metadata = supplementMetadataFromFile(bulkdataFilePath, createMetadata(staticMetadata));
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
                writeMetadata(out, bOut);
            }
            writeFile(((BulkData) metadata.getValue(bulkdataFileContentType.getBulkdataTypeTag())).getURI(), out,
                    stowChunk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ignoreNonMatchingFileContentTypes(Path path) throws IOException {
        if (fileCount.incrementAndGet() > 1) {
            if (fileContentTypeFromCL == null) {
                bulkdataFileContentType = FileContentType.valueOf(Files.probeContentType(path), path);
                return !firstBulkdataFileContentType.equals(bulkdataFileContentType);
            } else
                Logger.info("Ignoring checking of content type of subsequent file {}", path);
        }
        return false;
    }

    private void writeXMLMetadata(OutputStream out, Attributes staticMetadata) {
        Attributes metadata = createMetadata(staticMetadata);
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
            writeMetadata(out, bOut);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMetadata(OutputStream out, ByteArrayOutputStream bOut) throws IOException {
        Logger.info("> Metadata Content Type: " + requestContentType);
        writePartHeaders(out, requestContentType, null);
        Logger.debug("Metadata being sent is : " + bOut.toString());
        out.write(bOut.toByteArray());
    }

    private void writeFile(String contentLocation, OutputStream out, StowChunk stowChunk) throws Exception {
        String bulkdataContentType1 = bulkdataFileContentType.getMediaType();
        StowRSBulkdata stowRSBulkdata = contentLocBulkdata.get(contentLocation);
        XPEGParser parser = stowRSBulkdata.getParser();
        if (bulkdataFileContentType.getBulkdataTypeTag() == Tag.PixelData && tsuid)
            bulkdataContentType1 = bulkdataContentType1 + "; transfer-syntax=" + parser.getTransferSyntaxUID(false);
        Logger.info("> Bulkdata Content Type: " + bulkdataContentType1);
        writePartHeaders(out, bulkdataContentType1, contentLocation);

        int offset = 0;
        int length = (int) stowRSBulkdata.getFileLength();
        long positionAfterAPPSegments = parser != null ? parser.getPositionAfterAPPSegments() : -1L;
        if (noApp && positionAfterAPPSegments != -1L) {
            offset = (int) positionAfterAPPSegments;
            out.write(-1);
            out.write((byte) JPEG.SOI);
        }
        length -= offset;
        out.write(Files.readAllBytes(stowRSBulkdata.getBulkdataFilePath()), offset, length);
        stowChunk.setAttributes(stowRSBulkdata.bulkdataFile.length());
    }

    private void applyFunctionToFile(String file, boolean continueVisit, final StowRSFileFunction<Path> function)
            throws IOException {
        Path path = Paths.get(file);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new StowRSFileVisitor(function::apply, continueVisit));
        } else
            function.apply(path);
    }

    enum FileContentType {
        PDF(UID.EncapsulatedPDFStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_PDF,
                "encapsulatedPDFMetadata.xml"),
        CDA(UID.EncapsulatedCDAStorage.uid, Tag.EncapsulatedDocument, MediaType.TEXT_XML,
                "encapsulatedCDAMetadata.xml"),
        SLA(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_SLA,
                "encapsulatedSTLMetadata.xml"),
        STL(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_STL,
                "encapsulatedSTLMetadata.xml"),
        STL_BINARY(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_X_STL_BINARY,
                "encapsulatedSTLMetadata.xml"),
        MTL(UID.EncapsulatedMTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_MTL,
                "encapsulatedMTLMetadata.xml"),
        OBJ(UID.EncapsulatedOBJStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_OBJ,
                "encapsulatedOBJMetadata.xml"),
        GENOZIP(UID.PrivateEncapsulatedGenozipStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_VND_GENOZIP,
                "encapsulatedGenozipMetadata.xml"),
        VCF_BZIP2(UID.PrivateEncapsulatedBzip2VCFStorage.uid, Tag.EncapsulatedDocument,
                MediaType.APPLICATION_PRS_VCFBZIP2, "encapsulatedVCFBzip2Metadata.xml"),
        DOC_BZIP2(UID.PrivateEncapsulatedBzip2DocumentStorage.uid, Tag.EncapsulatedDocument,
                MediaType.APPLICATION_X_BZIP2, "encapsulatedDocumentBzip2Metadata.xml"),
        JPHC(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPHC,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        JPEG(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPEG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        JP2(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JP2,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        J2C(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_J2C,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        JPH(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPH,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        PNG(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_PNG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        GIF(videoPhotographicImage ? UID.VideoPhotographicImageStorage.uid
                : vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_GIF,
                vlPhotographicImage || videoPhotographicImage ? "vlPhotographicImageMetadata.xml"
                        : "secondaryCaptureImageMetadata.xml"),
        MPEG(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_MPEG,
                "vlPhotographicImageMetadata.xml"),
        MP4(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_MP4,
                "vlPhotographicImageMetadata.xml"),
        QUICKTIME(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_QUICKTIME,
                "vlPhotographicImageMetadata.xml");

        private final String cuid;
        private final int bulkdataTypeTag;
        private final String mediaType;
        private final String sampleMetadataFile;

        FileContentType(String cuid, int bulkdataTypeTag, String mediaType, String sampleMetadataFile) {
            this.cuid = cuid;
            this.bulkdataTypeTag = bulkdataTypeTag;
            this.sampleMetadataFile = sampleMetadataFile;
            this.mediaType = mediaType;
        }

        static FileContentType valueOf(String contentType, Path path) {
            String fileName = path.toFile().getName();
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            return fileContentType(contentType != null ? contentType : ext);
        }

        public String getSOPClassUID() {
            return cuid;
        }

        public String getSampleMetadataResourceURL() {
            return "resource:" + sampleMetadataFile;
        }

        public int getBulkdataTypeTag() {
            return bulkdataTypeTag;
        }

        public String getMediaType() {
            return mediaType;
        }
    }

    private enum CompressedPixelData {
        JPEG {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new JPEGParser(channel));
            }
        },
        MPEG {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MPEG2Parser(channel));
            }
        },
        MP4 {
            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MP4Parser(channel));
            }
        };

        private XPEGParser parser;

        static CompressedPixelData valueOf() {
            return bulkdataFileContentType == FileContentType.JP2 || bulkdataFileContentType == FileContentType.J2C
                    || bulkdataFileContentType == FileContentType.JPH || bulkdataFileContentType == FileContentType.JPHC
                            ? JPEG
                            : bulkdataFileContentType == FileContentType.QUICKTIME ? MP4
                                    : valueOf(bulkdataFileContentType.name());
        }

        abstract void parse(SeekableByteChannel channel) throws IOException;

        public XPEGParser getParser() {
            return parser;
        }

        void setParser(XPEGParser parser) {
            this.parser = parser;
        }
    }

    interface StowRSFileConsumer<Path> {
        void accept(Path path) throws IOException;
    }

    interface StowRSFileFunction<Path> {
        void apply(Path path) throws IOException;
    }

    static class StowChunk {
        private final File tmpFile;
        private final AtomicInteger scanned = new AtomicInteger();
        private int sent;
        private long size;

        StowChunk(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        void setAttributes(long length) {
            scanned.getAndIncrement();
            this.size += length;
        }

        AtomicInteger getScanned() {
            return scanned;
        }

        File getTmpFile() {
            return tmpFile;
        }

        long getSize() {
            return size;
        }

        int sent() {
            this.sent = scanned.get();
            return sent;
        }
    }

    static class StowRSBulkdata {
        Path bulkdataFilePath;
        File bulkdataFile;
        XPEGParser parser;
        long fileLength;

        StowRSBulkdata(Path bulkdataFilePath) {
            this.bulkdataFilePath = bulkdataFilePath;
            this.bulkdataFile = bulkdataFilePath.toFile();
            this.fileLength = bulkdataFile.length();
        }

        Path getBulkdataFilePath() {
            return bulkdataFilePath;
        }

        File getBulkdataFile() {
            return bulkdataFile;
        }

        long getFileLength() {
            return fileLength;
        }

        XPEGParser getParser() {
            return parser;
        }

        void setParser(XPEGParser parser) {
            this.parser = parser;
        }
    }

    static class StowRSFileVisitor extends SimpleFileVisitor<Path> {
        private final StowRSFileConsumer<Path> consumer;
        private final boolean continueVisit;

        StowRSFileVisitor(StowRSFileConsumer<Path> consumer, boolean continueVisit) {
            this.consumer = consumer;
            this.continueVisit = continueVisit;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            consumer.accept(path);
            return continueVisit ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }
    }
}
