/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.image.plugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.SeekableByteChannel;
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

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
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

/**
 * The {@code StowRS} class provides a client for the DICOMweb STOW-RS (Store Over the Web by RESTful Services)
 * standard. It can send DICOM objects, or encapsulate other file types (like JPEG, PDF, etc.) into DICOM and send them
 * to a STOW-RS server.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class StowRS {

    /**
     * Constructs a new {@code StowRS} instance.
     */
    public StowRS() {
        // No initialization required.
    }

    /**
     * The boundary string for the multipart request.
     */
    private static final String boundary = "myboundary";

    /**
     * The maximum response body size used for logs.
     */
    private static final int MAX_BODY_LOG = 2048;

    /**
     * The maximum response body size used for error messages.
     */
    private static final int MAX_BODY_MESSAGE = 200;

    /**
     * A counter for the number of files processed.
     */
    private static final AtomicInteger fileCount = new AtomicInteger();

    /**
     * A map to store bulk data objects, keyed by their content location.
     */
    private static final Map<String, StowRSBulkdata> contentLocBulkdata = new HashMap<>();

    /**
     * An array of DICOM tags for Study and Series Instance UIDs.
     */
    private static final int[] IUIDS_TAGS = { Tag.StudyInstanceUID, Tag.SeriesInstanceUID };

    /**
     * An array of Type 2 DICOM tags that should be present in the metadata.
     */
    private static final int[] TYPE2_TAGS = { Tag.ContentDate, Tag.ContentTime };

    /**
     * The standard DICOM element dictionary.
     */
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();

    /**
     * The URL of the STOW-RS service.
     */
    private static String url;

    /**
     * A flag to indicate if the content is a VL Photographic Image.
     */
    private static boolean vlPhotographicImage;

    /**
     * A flag to indicate if the content is a Video Photographic Image.
     */
    private static boolean videoPhotographicImage;

    /**
     * The desired Accept header for the HTTP request.
     */
    private static String requestAccept;

    /**
     * The Content-Type of the request body.
     */
    private static String requestContentType;

    /**
     * The path to a metadata file to be merged.
     */
    private static String metadataFilePathStr;

    /**
     * The metadata file object.
     */
    private static File metadataFile;

    /**
     * A flag to allow connections to any HTTPS host, regardless of certificate validation.
     */
    private static boolean allowAnyHost;

    /**
     * A flag to disable the default SSL trust manager.
     */
    private static boolean disableTM;

    /**
     * A flag to include the Encapsulated Document Length tag.
     */
    private static boolean encapsulatedDocLength;

    /**
     * The value for the HTTP Authorization header.
     */
    private static String authorization;

    /**
     * The maximum number of files to include in a single request.
     */
    private static int limit;

    /**
     * The file content type specified from the command line.
     */
    private static FileContentType fileContentTypeFromCL;

    /**
     * The content type of the first bulk data file processed.
     */
    private static FileContentType firstBulkdataFileContentType;

    /**
     * The content type of the current bulk data file being processed.
     */
    private static FileContentType bulkdataFileContentType;

    /**
     * The DICOM attributes for the current operation.
     */
    private final Attributes attrs = new Attributes();

    /**
     * A list of chunks of data to be sent in separate requests.
     */
    private final List<StowChunk> stowChunks = new ArrayList<>();

    /**
     * A flag to exclude APPn segments from JPEG streams.
     */
    private boolean noApp;

    /**
     * A flag to include pixel data header information.
     */
    private boolean pixelHeader;

    /**
     * A flag related to the Transfer Syntax UID.
     */
    private boolean tsuid;

    /**
     * A suffix for generated UIDs.
     */
    private String uidSuffix;

    /**
     * The prefix for temporary file names.
     */
    private String tmpPrefix;

    /**
     * The suffix for temporary file names.
     */
    private String tmpSuffix;

    /**
     * The directory for temporary files.
     */
    private File tmpDir;

    /**
     * The total number of files scanned.
     */
    private int filesScanned;

    /**
     * The total number of files successfully sent.
     */
    private int filesSent;

    /**
     * The total size in bytes of all successfully sent files.
     */
    private long totalSize;

    /**
     * A map of custom HTTP request properties.
     */
    private Map<String, String> requestProperties;

    /**
     * Logs performance metrics for a single sent chunk.
     *
     * @param stowChunk The chunk that was sent.
     * @param t1        The start time of the operation.
     */
    private static void logSentPerChunk(StowChunk stowChunk, long t1) {
        if (stowChunk.sent == 0)
            return;

        long t2 = System.currentTimeMillis();
        float s = (t2 - t1) / 1000F;
        float mb = stowChunk.getSize() / 1048576F;
        Logger.info(
                false,
                "Image",
                "Sent {} files, total size {:.2f} MB in {:.2f}s ({:.2f} MB/s)",
                stowChunk.sent,
                mb,
                s,
                mb / s);
    }

    /**
     * Logs the overall performance metrics for the entire STOW-RS operation.
     *
     * @param stowRS The {@code StowRS} instance.
     * @param t1     The start time of the operation.
     */
    private static void logSent(StowRS stowRS, long t1) {
        if (stowRS.filesSent == 0 || limit == 0)
            return;

        long t2 = System.currentTimeMillis();
        float s = (t2 - t1) / 1000F;
        float mb = stowRS.totalSize / 1048576F;
        Logger.info(
                false,
                "Image",
                "Sent {} files, total size {:.2f} MB in {:.2f}s ({:.2f} MB/s)",
                stowRS.filesSent,
                mb,
                s,
                mb / s);
    }

    /**
     * Determines the {@link FileContentType} from a string identifier (MIME type or file extension).
     *
     * @param s The string identifier.
     * @return The corresponding {@code FileContentType}.
     * @throws IllegalArgumentException if the type is not supported.
     */
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

    /**
     * Adds attributes from a specified metadata file to the given dataset.
     *
     * @param metadata The dataset to which attributes will be added.
     * @throws Exception if an error occurs while parsing the metadata file.
     */
    private static void addAttributesFromFile(Attributes metadata) throws Exception {
        if (metadataFilePathStr == null)
            return;

        metadata.addAll(SAXReader.parse(metadataFilePathStr, metadata));
    }

    /**
     * Supplements the metadata with UIDs for Study and Series if they are missing.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementMissingUIDs(Attributes metadata) {
        for (int tag : IUIDS_TAGS)
            if (!metadata.containsValue(tag))
                metadata.setString(tag, VR.UI, UID.createUID());
    }

    /**
     * Supplements a missing UID in the metadata.
     *
     * @param metadata The attributes to supplement.
     * @param tag      The tag of the UID attribute.
     */
    private static void supplementMissingUID(Attributes metadata, int tag) {
        if (!metadata.containsValue(tag))
            metadata.setString(tag, VR.UI, UID.createUID());
    }

    /**
     * Supplements the SOP Class UID if it is missing.
     *
     * @param metadata The attributes to supplement.
     * @param value    The SOP Class UID to set.
     */
    private static void supplementSOPClass(Attributes metadata, String value) {
        if (!metadata.containsValue(Tag.SOPClassUID))
            metadata.setString(Tag.SOPClassUID, VR.UI, value);
    }

    /**
     * Ensures that all Type 2 tags are present in the metadata, setting them to null if absent.
     *
     * @param metadata The attributes to supplement.
     */
    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }

    /**
     * Supplements attributes specific to encapsulated documents.
     *
     * @param metadata       The attributes to supplement.
     * @param stowRSBulkdata The bulk data object containing file information.
     */
    private static void supplementEncapsulatedDocAttrs(Attributes metadata, StowRSBulkdata stowRSBulkdata) {
        if (!metadata.contains(Tag.AcquisitionDateTime))
            metadata.setNull(Tag.AcquisitionDateTime, VR.DT);
        if (encapsulatedDocLength)
            metadata.setLong(Tag.EncapsulatedDocumentLength, VR.UL, stowRSBulkdata.getFileLength());
    }

    /**
     * Writes the headers for a new part in a multipart message.
     *
     * @param out             The output stream.
     * @param contentType     The Content-Type of the part.
     * @param contentLocation The Content-Location of the part.
     * @throws IOException if an I/O error occurs.
     */
    private static void writePartHeaders(OutputStream out, String contentType, String contentLocation)
            throws IOException {
        out.write(("\r\n--" + boundary + "\r\n").getBytes());
        out.write(("Content-Type: " + contentType + "\r\n").getBytes());
        if (contentLocation != null)
            out.write(("Content-Location: " + contentLocation + "\r\n").getBytes());
        out.write("\r\n".getBytes());
    }

    /**
     * Reads the HTTP response body without interrupting the main STOW workflow.
     *
     * @param connection   the HTTP connection.
     * @param responseCode the HTTP response code.
     * @return the response body, or {@code null} when no readable body is available.
     */
    private static String readBodyQuietly(HttpURLConnection connection, int responseCode) {
        try (InputStream inputStream = responseBodyStream(connection, responseCode)) {
            if (inputStream == null) {
                return null;
            }
            byte[] bytes = inputStream.readNBytes(MAX_BODY_LOG);
            return bytes.length == 0 ? null : new String(bytes, Charset.UTF_8);
        } catch (IOException e) {
            Logger.debug(false, "Image", "Cannot read STOW-RS response body", e);
            return null;
        }
    }

    /**
     * Selects the HTTP response stream for the response code.
     *
     * @param connection   the HTTP connection.
     * @param responseCode the HTTP response code.
     * @return the response body stream.
     * @throws IOException if the response stream cannot be opened.
     */
    private static InputStream responseBodyStream(HttpURLConnection connection, int responseCode) throws IOException {
        if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            return connection.getErrorStream();
        }
        return connection.getInputStream();
    }

    /**
     * Verifies an HTTP response code and raises an error for failed responses.
     *
     * @param responseCode    the HTTP response code.
     * @param responseMessage the HTTP response message.
     * @param responseBody    the response body.
     * @throws IOException if the response code represents a failure.
     */
    private static void verifySuccessfulResponse(int responseCode, String responseMessage, String responseBody)
            throws IOException {
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            return;
        }

        String header = String.format(
                "STOW-RS server response message: HTTP Status-Code %d: %s",
                responseCode,
                responseMessage == null ? "" : responseMessage);
        if (responseBody != null && !responseBody.isBlank()) {
            Logger.error(false, "Image", "{} - body: {}", header, responseBody);
            throw new IOException(header + " - " + truncate(responseBody, MAX_BODY_MESSAGE));
        }

        Logger.error(false, "Image", header);
        throw new IOException(header);
    }

    /**
     * Truncates a response body to a display-safe length.
     *
     * @param value the response body.
     * @param max   the maximum length.
     * @return the truncated response body.
     */
    private static String truncate(String value, int max) {
        String stripped = value.replaceAll("\\s+", " ").strip();
        return stripped.length() <= max ? stripped : stripped.substring(0, max) + "...";
    }

    /**
     * Sets custom HTTP request properties.
     *
     * @param requestProperties A map of header names to values.
     */
    public final void setRequestProperties(Map<String, String> requestProperties) {
        this.requestProperties = requestProperties;
    }

    /**
     * Sets the prefix for temporary file names.
     *
     * @param prefix The file prefix.
     */
    public final void setTmpFilePrefix(String prefix) {
        this.tmpPrefix = prefix;
    }

    /**
     * Sets the suffix for temporary file names.
     *
     * @param suffix The file suffix.
     */
    public final void setTmpFileSuffix(String suffix) {
        this.tmpSuffix = suffix;
    }

    /**
     * Sets the directory for temporary files.
     *
     * @param tmpDir The temporary directory.
     */
    public final void setTmpFileDirectory(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * Scans a list of files and prepares them for sending.
     *
     * @param files A list of file paths.
     */
    private void scan(List<String> files) {
        long t1 = System.currentTimeMillis();
        scanFiles(files);
        long t2 = System.currentTimeMillis();
        Logger.info(true, "Image", "Scanned {} files in {}s", filesScanned, (t2 - t1) / 1000f);
    }

    /**
     * Creates a new metadata dataset, populating it with static metadata and supplementing missing values.
     *
     * @param staticMetadata The static metadata to include.
     * @return The new metadata dataset.
     */
    private Attributes createMetadata(Attributes staticMetadata) {
        Attributes metadata = new Attributes(staticMetadata);
        supplementMissingUID(metadata, Tag.SOPInstanceUID);
        supplementType2Tags(metadata);
        return metadata;
    }

    /**
     * Supplements a metadata dataset with information derived from a bulk data file.
     *
     * @param bulkdataFilePath The path to the bulk data file.
     * @param metadata         The metadata to supplement.
     * @return The supplemented metadata.
     */
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

    /**
     * Extracts metadata from compressed pixel data (e.g., JPEG, MPEG).
     *
     * @param contentLoc     The content location identifier.
     * @param stowRSBulkdata The bulk data object.
     * @param metadata       The metadata to supplement.
     */
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
                Logger.warn(
                        false,
                        "Image",
                        e,
                        "Pixel metadata parse failed: fileName={}, exception={}",
                        bulkdataFile == null ? null : bulkdataFile.getName(),
                        e.getClass().getSimpleName());
            }
        }
        contentLocBulkdata.put(contentLoc, stowRSBulkdata);
    }

    /**
     * Creates the initial static metadata by loading a template and merging command-line attributes.
     *
     * @return The static metadata dataset.
     * @throws Exception if an error occurs.
     */
    private Attributes createStaticMetadata() throws Exception {
        Logger.info(true, "Image", "Creating static metadata. Set defaults, if essential attributes are not present.");
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

    /**
     * Scans files and groups them into chunks based on the specified limit.
     *
     * @param files A list of file paths.
     */
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
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "Directory file list failed: pathPresent={}, exception={}",
                            f != null,
                            e.getClass().getSimpleName());
                }
            }
            processFilesPerRequest(filePaths.equals(fPR) ? fPR : filePaths);
        });
    }

    /**
     * Scans all files into a single chunk when no limit is specified.
     *
     * @param files A list of file paths.
     */
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
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "STOW scan failed: fileCount={}, requestContentType={}, exception={}",
                    files == null ? 0 : files.size(),
                    requestContentType,
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Processes a list of files intended for a single STOW-RS request.
     *
     * @param fPR A list of file paths for one request.
     */
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
                            Logger.warn(
                                    false,
                                    "Image",
                                    e,
                                    "STOW file scan failed: filePresent={}, requestContentType={}, exception={}",
                                    f != null,
                                    requestContentType,
                                    e.getClass().getSimpleName());
                        }
                    });
                else
                    writeMetadataAndBulkData(out, fPR, createStaticMetadata(), stowChunk);
            }
            filesScanned += stowChunk.getScanned().get();
            stowChunks.add(stowChunk);
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Image",
                    e,
                    "STOW chunk scan failed: fileCount={}, requestContentType={}, exception={}",
                    fPR == null ? 0 : fPR.size(),
                    requestContentType,
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Creates a map of HTTP request properties (headers).
     *
     * @param httpHeaders An array of custom headers in "Name:Value" format.
     * @return A map of request properties.
     */
    private Map<String, String> requestProperties(String[] httpHeaders) {
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put(
                Http.Header.CONTENT_TYPE,
                MediaType.MULTIPART_RELATED + "; type=\"" + requestContentType + "\"; boundary=" + boundary);
        requestProperties.put(Http.Header.ACCEPT, requestAccept);
        requestProperties.put(Http.Header.CONNECTION, "keep-alive");
        if (authorization != null)
            requestProperties.put(Http.Header.AUTHORIZATION, authorization);
        if (httpHeaders != null)
            for (String httpHeader : httpHeaders) {
                int delim = httpHeader.indexOf(':');
                requestProperties.put(httpHeader.substring(0, delim), httpHeader.substring(delim + 1).trim());
            }
        return requestProperties;
    }

    /**
     * Sends a STOW-RS request over a standard HTTP connection.
     *
     * @param connection The HTTP connection.
     * @param stowChunk  The chunk of data to send.
     * @throws Exception if an error occurs.
     */
    private void stow(final HttpURLConnection connection, StowChunk stowChunk) throws Exception {
        File tmpFile = stowChunk.getTmpFile();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty(Http.Header.CONTENT_LENGTH, String.valueOf(tmpFile.length()));
        requestProperties.forEach(connection::setRequestProperty);
        logOutgoing(connection.getURL(), connection.getRequestProperties());
        try (OutputStream out = connection.getOutputStream()) {
            IoKit.copy(new FileInputStream(tmpFile), out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            String responseBody = readBodyQuietly(connection, responseCode);
            logIncoming(responseCode, responseMessage, connection.getHeaderFields(), responseBody);
            verifySuccessfulResponse(responseCode, responseMessage, responseBody);
            filesSent += stowChunk.sent();
            totalSize += stowChunk.getSize();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Opens a standard HTTP connection.
     *
     * @return The opened HttpURLConnection.
     * @throws Exception if an error occurs.
     */
    private HttpURLConnection open() throws Exception {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    /**
     * Opens an HTTPS connection.
     *
     * @return The opened HttpsURLConnection.
     * @throws Exception if an error occurs.
     */
    private HttpsURLConnection openTLS() throws Exception {
        return (HttpsURLConnection) new URL(url).openConnection();
    }

    /**
     * Sends a STOW-RS request over an HTTPS connection.
     *
     * @param connection The HTTPS connection.
     * @param stowChunk  The chunk of data to send.
     * @throws Exception if an error occurs.
     */
    private void stowHttps(final HttpsURLConnection connection, StowChunk stowChunk) throws Exception {
        File tmpFile = stowChunk.getTmpFile();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        if (disableTM)
            connection.setSSLSocketFactory(sslContext().getSocketFactory());
        connection.setRequestProperty(Http.Header.CONTENT_LENGTH, String.valueOf(tmpFile.length()));
        requestProperties.forEach(connection::setRequestProperty);
        connection.setHostnameVerifier((hostname, session) -> allowAnyHost);
        logOutgoing(connection.getURL(), connection.getRequestProperties());
        try (OutputStream out = connection.getOutputStream()) {
            IoKit.copy(new FileInputStream(tmpFile), out);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes());
            out.flush();
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            String responseBody = readBodyQuietly(connection, responseCode);
            logIncoming(responseCode, responseMessage, connection.getHeaderFields(), responseBody);
            verifySuccessfulResponse(responseCode, responseMessage, responseBody);
            filesSent += stowChunk.sent();
            totalSize += stowChunk.getSize();
        } finally {
            connection.disconnect();
        }
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
     * Creates a Basic Authentication header value.
     *
     * @param user The username and password in "username:password" format.
     * @return The Base64 encoded "Basic" authorization string.
     */
    private String basicAuth(String user) {
        byte[] userPswdBytes = user.getBytes();
        return "Basic " + Base64.getEncoder().encodeToString(userPswdBytes);
    }

    /**
     * Logs the outgoing HTTP request headers.
     *
     * @param url          The request URL.
     * @param headerFields The map of request headers.
     */
    private void logOutgoing(URL url, Map<String, List<String>> headerFields) {
        Logger.info(false, "Image", "> POST " + url.toString());
        headerFields.forEach((k, v) -> Logger.info(false, "Image", "> " + k + " : " + String.join(Symbol.COMMA, v)));
    }

    /**
     * Logs the incoming HTTP response.
     *
     * @param respCode     The HTTP response code.
     * @param respMsg      The HTTP response message.
     * @param headerFields The map of response headers.
     * @param responseBody The response body.
     */
    private void logIncoming(
            int respCode,
            String respMsg,
            Map<String, List<String>> headerFields,
            String responseBody) {
        Logger.info(false, "Image", "< HTTP/1.1 Response: " + respCode + Symbol.SPACE + respMsg);
        for (Map.Entry<String, List<String>> header : headerFields.entrySet())
            if (header.getKey() != null)
                Logger.info(false, "Image", "< " + header.getKey() + " : " + String.join(";", header.getValue()));
        Logger.info(false, "Image", "< Response content captured");
        int length = responseBody == null ? 0 : responseBody.length();
        Logger.debug(false, "Image", "Response content captured: chars={}", length);
    }

    /**
     * Writes a DICOM file to the multipart output stream.
     *
     * @param out       The output stream.
     * @param path      The path to the DICOM file.
     * @param stowChunk The current data chunk.
     * @throws IOException if an I/O error occurs.
     */
    private void writeDicomFile(OutputStream out, Path path, StowChunk stowChunk) throws IOException {
        if (Files.probeContentType(path) == null) {
            return;
        }
        writePartHeaders(out, requestContentType, null);
        Files.copy(updateAttrs(path), out);
        stowChunk.setAttributes(path.toFile().length());
    }

    /**
     * Updates the attributes of a DICOM file if necessary.
     *
     * @param path The path to the DICOM file.
     * @return The path to the (potentially modified) DICOM file.
     */
    private Path updateAttrs(Path path) {
        if (attrs.isEmpty() && uidSuffix == null)
            return path;

        try {
            ImageInputStream in = new ImageInputStream(path.toFile());
            File tmpFile = File.createTempFile("stowrs-", null, null);
            tmpFile.deleteOnExit();
            Attributes fmi = in.readFileMetaInformation();
            String tsuid = in.getTransferSyntax();
            try (ImageOutputStream dos = new ImageOutputStream(new BufferedOutputStream(new FileOutputStream(tmpFile)),
                    fmi != null ? UID.ExplicitVRLittleEndian.uid
                            : tsuid != null ? tsuid : UID.ImplicitVRLittleEndian.uid)) {
                dos.writeDataset(fmi, in.readDataset());
                dos.finish();
                dos.flush();
            }
            return tmpFile.toPath();
        } catch (Exception e) {
            Logger.info(false, "Image", "Failed to update attributes for file {}\n", path, e);
        }
        return path;
    }

    /**
     * Writes the metadata and bulk data parts of a multipart request.
     *
     * @param out            The output stream.
     * @param files          A list of bulk data file paths.
     * @param staticMetadata The static metadata to use.
     * @param stowChunk      The current data chunk.
     * @throws Exception if an error occurs.
     */
    private void writeMetadataAndBulkData(
            OutputStream out,
            List<String> files,
            final Attributes staticMetadata,
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
                }
                writeMetadata(out, bOut);

                for (String contentLocation : contentLocBulkdata.keySet())
                    writeFile(contentLocation, out, stowChunk);
            }
        }
        contentLocBulkdata.clear();
    }

    /**
     * Writes the metadata and bulk data for XML content type.
     *
     * @param out            The output stream.
     * @param files          A list of bulk data file paths.
     * @param staticMetadata The static metadata.
     * @param stowChunk      The current data chunk.
     * @throws Exception if an error occurs.
     */
    private void writeXMLMetadataAndBulkdata(
            final OutputStream out,
            List<String> files,
            final Attributes staticMetadata,
            StowChunk stowChunk) throws Exception {
        if (files.isEmpty()) {
            writeXMLMetadata(out, staticMetadata);
            stowChunk.setAttributes(metadataFile.length());
        }

        for (String file : files)
            applyFunctionToFile(file, true, path -> writeXMLMetadataAndBulkdata(out, staticMetadata, path, stowChunk));
    }

    /**
     * Writes the XML metadata and corresponding bulk data for a single file.
     *
     * @param out              The output stream.
     * @param staticMetadata   The static metadata.
     * @param bulkdataFilePath The path to the bulk data file.
     * @param stowChunk        The current data chunk.
     */
    private void writeXMLMetadataAndBulkdata(
            OutputStream out,
            Attributes staticMetadata,
            Path bulkdataFilePath,
            StowChunk stowChunk) {
        try {
            if (ignoreNonMatchingFileContentTypes(bulkdataFilePath))
                return;

            Attributes metadata = supplementMetadataFromFile(bulkdataFilePath, createMetadata(staticMetadata));
            try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
                SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
                writeMetadata(out, bOut);
            }
            writeFile(
                    ((BulkData) metadata.getValue(bulkdataFileContentType.getBulkdataTypeTag())).getURI(),
                    out,
                    stowChunk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a file's content type should be ignored based on the first file's type.
     *
     * @param path The path to the file.
     * @return {@code true} if the file should be ignored.
     * @throws IOException if an I/O error occurs.
     */
    private boolean ignoreNonMatchingFileContentTypes(Path path) throws IOException {
        if (fileCount.incrementAndGet() > 1) {
            if (fileContentTypeFromCL == null) {
                bulkdataFileContentType = FileContentType.valueOf(Files.probeContentType(path), path);
                return !firstBulkdataFileContentType.equals(bulkdataFileContentType);
            } else
                Logger.info(false, "Image", "Ignoring checking of content type of subsequent file {}", path);
        }
        return false;
    }

    /**
     * Writes XML metadata to the output stream.
     *
     * @param out            The output stream.
     * @param staticMetadata The static metadata.
     */
    private void writeXMLMetadata(OutputStream out, Attributes staticMetadata) {
        Attributes metadata = createMetadata(staticMetadata);
        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream()) {
            SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);
            writeMetadata(out, bOut);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the metadata part to the multipart output stream.
     *
     * @param out  The main output stream.
     * @param bOut A stream containing the metadata content.
     * @throws IOException if an I/O error occurs.
     */
    private void writeMetadata(OutputStream out, ByteArrayOutputStream bOut) throws IOException {
        Logger.info(
                true,
                "Image",
                "Metadata part write started: contentType={}, bytes={}",
                requestContentType,
                bOut.size());
        writePartHeaders(out, requestContentType, null);
        Logger.debug(
                false,
                "Image",
                "Metadata part prepared: contentType={}, bytes={}",
                requestContentType,
                bOut.size());
        out.write(bOut.toByteArray());
    }

    /**
     * Writes a bulk data file to the multipart output stream.
     *
     * @param contentLocation The content location identifier.
     * @param out             The output stream.
     * @param stowChunk       The current data chunk.
     * @throws Exception if an error occurs.
     */
    private void writeFile(String contentLocation, OutputStream out, StowChunk stowChunk) throws Exception {
        String bulkdataContentType1 = bulkdataFileContentType.getMediaType();
        StowRSBulkdata stowRSBulkdata = contentLocBulkdata.get(contentLocation);
        XPEGParser parser = stowRSBulkdata.getParser();
        if (bulkdataFileContentType.getBulkdataTypeTag() == Tag.PixelData && tsuid)
            bulkdataContentType1 = bulkdataContentType1 + "; transfer-syntax=" + parser.getTransferSyntaxUID(false);
        Logger.info(false, "Image", "> Bulkdata Content Type: " + bulkdataContentType1);
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

    /**
     * Applies a function to a file or all files in a directory.
     *
     * @param file          The file or directory path.
     * @param continueVisit A flag to continue visiting files in a directory even after an error.
     * @param function      The function to apply to each file.
     * @throws IOException if an I/O error occurs.
     */
    private void applyFunctionToFile(String file, boolean continueVisit, final StowRSFileFunction<Path> function)
            throws IOException {
        Path path = Paths.get(file);
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new StowRSFileVisitor(function::apply, continueVisit));
        } else
            function.apply(path);
    }

    /**
     * An enumeration of supported file content types for encapsulation.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    enum FileContentType {

        /**
         * The pdf value.
         */
        PDF(UID.EncapsulatedPDFStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_PDF,
                "encapsulatedPDFMetadata.xml"),
        /**
         * The cda value.
         */
        CDA(UID.EncapsulatedCDAStorage.uid, Tag.EncapsulatedDocument, MediaType.TEXT_XML,
                "encapsulatedCDAMetadata.xml"),
        /**
         * The sla value.
         */
        SLA(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_SLA,
                "encapsulatedSTLMetadata.xml"),
        /**
         * The stl value.
         */
        STL(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_STL,
                "encapsulatedSTLMetadata.xml"),
        /**
         * The stl binary value.
         */
        STL_BINARY(UID.EncapsulatedSTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_X_STL_BINARY,
                "encapsulatedSTLMetadata.xml"),
        /**
         * The mtl value.
         */
        MTL(UID.EncapsulatedMTLStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_MTL,
                "encapsulatedMTLMetadata.xml"),
        /**
         * The obj value.
         */
        OBJ(UID.EncapsulatedOBJStorage.uid, Tag.EncapsulatedDocument, MediaType.MODEL_OBJ,
                "encapsulatedOBJMetadata.xml"),
        /**
         * The genozip value.
         */
        GENOZIP(UID.PrivateEncapsulatedGenozipStorage.uid, Tag.EncapsulatedDocument, MediaType.APPLICATION_VND_GENOZIP,
                "encapsulatedGenozipMetadata.xml"),
        /**
         * The vcf bzip2 value.
         */
        VCF_BZIP2(UID.PrivateEncapsulatedBzip2VCFStorage.uid, Tag.EncapsulatedDocument,
                MediaType.APPLICATION_PRS_VCFBZIP2, "encapsulatedVCFBzip2Metadata.xml"),
        /**
         * The doc bzip2 value.
         */
        DOC_BZIP2(UID.PrivateEncapsulatedBzip2DocumentStorage.uid, Tag.EncapsulatedDocument,
                MediaType.APPLICATION_X_BZIP2, "encapsulatedDocumentBzip2Metadata.xml"),
        /**
         * The jphc value.
         */
        JPHC(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPHC,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The jpeg value.
         */
        JPEG(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPEG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The jp2 value.
         */
        JP2(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JP2,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The j2 c value.
         */
        J2C(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_J2C,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The jph value.
         */
        JPH(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_JPH,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The png value.
         */
        PNG(vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_PNG,
                vlPhotographicImage ? "vlPhotographicImageMetadata.xml" : "secondaryCaptureImageMetadata.xml"),
        /**
         * The gif value.
         */
        GIF(videoPhotographicImage ? UID.VideoPhotographicImageStorage.uid
                : vlPhotographicImage ? UID.VLPhotographicImageStorage.uid : UID.SecondaryCaptureImageStorage.uid,
                Tag.PixelData, MediaType.IMAGE_GIF,
                vlPhotographicImage || videoPhotographicImage ? "vlPhotographicImageMetadata.xml"
                        : "secondaryCaptureImageMetadata.xml"),
        /**
         * The mpeg value.
         */
        MPEG(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_MPEG,
                "vlPhotographicImageMetadata.xml"),
        /**
         * The mp4 value.
         */
        MP4(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_MP4,
                "vlPhotographicImageMetadata.xml"),
        /**
         * The quicktime value.
         */
        QUICKTIME(UID.VideoPhotographicImageStorage.uid, Tag.PixelData, MediaType.VIDEO_QUICKTIME,
                "vlPhotographicImageMetadata.xml");

        /**
         * The cuid value.
         */
        private final String cuid;

        /**
         * The bulkdata type tag value.
         */
        private final int bulkdataTypeTag;

        /**
         * The media type value.
         */
        private final String mediaType;

        /**
         * The sample metadata file value.
         */
        private final String sampleMetadataFile;

        /**
         * Creates a new instance.
         *
         * @param cuid               the cuid.
         * @param bulkdataTypeTag    the bulkdata type tag.
         * @param mediaType          the media type.
         * @param sampleMetadataFile the sample metadata file.
         */
        FileContentType(String cuid, int bulkdataTypeTag, String mediaType, String sampleMetadataFile) {
            this.cuid = cuid;
            this.bulkdataTypeTag = bulkdataTypeTag;
            this.mediaType = mediaType;
            this.sampleMetadataFile = sampleMetadataFile;
        }

        /**
         * Executes the value of operation.
         *
         * @param contentType the content type.
         * @param path        the path.
         * @return the operation result.
         */
        static FileContentType valueOf(String contentType, Path path) {
            String fileName = path.toFile().getName();
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            return fileContentType(contentType != null ? contentType : ext);
        }

        /**
         * Gets the sop class uid.
         *
         * @return the sop class uid.
         */
        public String getSOPClassUID() {
            return cuid;
        }

        /**
         * Gets the sample metadata resource url.
         *
         * @return the sample metadata resource url.
         */
        public String getSampleMetadataResourceURL() {
            return "resource:" + sampleMetadataFile;
        }

        /**
         * Gets the bulkdata type tag.
         *
         * @return the bulkdata type tag.
         */
        public int getBulkdataTypeTag() {
            return bulkdataTypeTag;
        }

        /**
         * Gets the media type.
         *
         * @return the media type.
         */
        public String getMediaType() {
            return mediaType;
        }

    }

    /**
     * An enumeration to handle parsing of different compressed pixel data formats.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum CompressedPixelData {

        /**
         * The jpeg value.
         */
        JPEG {

            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new JPEGParser(channel));
            }
        },
        /**
         * The mpeg value.
         */
        MPEG {

            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MPEG2Parser(channel));
            }
        },
        /**
         * The mp4 value.
         */
        MP4 {

            @Override
            void parse(SeekableByteChannel channel) throws IOException {
                setParser(new MP4Parser(channel));
            }
        };

        /**
         * The parser value.
         */
        private XPEGParser parser;

        /**
         * Executes the value of operation.
         *
         * @return the operation result.
         */
        static CompressedPixelData valueOf() {
            return bulkdataFileContentType == FileContentType.JP2 || bulkdataFileContentType == FileContentType.J2C
                    || bulkdataFileContentType == FileContentType.JPH || bulkdataFileContentType == FileContentType.JPHC
                            ? JPEG
                            : bulkdataFileContentType == FileContentType.QUICKTIME ? MP4
                                    : valueOf(bulkdataFileContentType.name());
        }

        /**
         * Executes the parse operation.
         *
         * @param channel the channel.
         * @throws IOException if the operation cannot be completed.
         */
        abstract void parse(SeekableByteChannel channel) throws IOException;

        /**
         * Gets the parser.
         *
         * @return the parser.
         */
        public XPEGParser getParser() {
            return parser;
        }

        /**
         * Sets the parser.
         *
         * @param parser the parser.
         */
        void setParser(XPEGParser parser) {
            this.parser = parser;
        }

    }

    /**
     * A functional interface for consuming a file path, allowing for exceptions.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface StowRSFileConsumer<Path> {

        /**
         * Executes the accept operation.
         *
         * @param path the path.
         * @throws IOException if the operation cannot be completed.
         */
        void accept(Path path) throws IOException;

    }

    /**
     * A functional interface for applying a function to a file path, allowing for exceptions.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface StowRSFileFunction<Path> {

        /**
         * Executes the apply operation.
         *
         * @param path the path.
         * @throws IOException if the operation cannot be completed.
         */
        void apply(Path path) throws IOException;

    }

    /**
     * A container class representing a chunk of data to be sent in a single STOW-RS request.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class StowChunk {

        /**
         * The tmp file value.
         */
        private final File tmpFile;

        /**
         * The scanned value.
         */
        private final AtomicInteger scanned = new AtomicInteger();

        /**
         * The sent value.
         */
        private int sent;

        /**
         * The size value.
         */
        private long size;

        /**
         * Creates a new instance.
         *
         * @param tmpFile the tmp file.
         */
        StowChunk(File tmpFile) {
            this.tmpFile = tmpFile;
        }

        /**
         * Sets the attributes.
         *
         * @param length the length.
         */
        void setAttributes(long length) {
            scanned.getAndIncrement();
            this.size += length;
        }

        /**
         * Gets the scanned.
         *
         * @return the scanned.
         */
        AtomicInteger getScanned() {
            return scanned;
        }

        /**
         * Gets the tmp file.
         *
         * @return the tmp file.
         */
        File getTmpFile() {
            return tmpFile;
        }

        /**
         * Gets the size.
         *
         * @return the size.
         */
        long getSize() {
            return size;
        }

        /**
         * Executes the sent operation.
         *
         * @return the operation result.
         */
        int sent() {
            this.sent = scanned.get();
            return sent;
        }

    }

    /**
     * A container class for information about a bulk data file.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class StowRSBulkdata {

        /**
         * The bulkdata file path value.
         */
        Path bulkdataFilePath;

        /**
         * The bulkdata file value.
         */
        File bulkdataFile;

        /**
         * The parser value.
         */
        XPEGParser parser;

        /**
         * The file length value.
         */
        long fileLength;

        /**
         * Creates a new instance.
         *
         * @param bulkdataFilePath the bulkdata file path.
         */
        StowRSBulkdata(Path bulkdataFilePath) {
            this.bulkdataFilePath = bulkdataFilePath;
            this.bulkdataFile = bulkdataFilePath.toFile();
            this.fileLength = bulkdataFile.length();
        }

        /**
         * Gets the bulkdata file path.
         *
         * @return the bulkdata file path.
         */
        Path getBulkdataFilePath() {
            return bulkdataFilePath;
        }

        /**
         * Gets the bulkdata file.
         *
         * @return the bulkdata file.
         */
        File getBulkdataFile() {
            return bulkdataFile;
        }

        /**
         * Gets the file length.
         *
         * @return the file length.
         */
        long getFileLength() {
            return fileLength;
        }

        /**
         * Gets the parser.
         *
         * @return the parser.
         */
        XPEGParser getParser() {
            return parser;
        }

        /**
         * Sets the parser.
         *
         * @param parser the parser.
         */
        void setParser(XPEGParser parser) {
            this.parser = parser;
        }

    }

    /**
     * A file visitor for traversing directories and applying a function to each file.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    static class StowRSFileVisitor extends SimpleFileVisitor<Path> {

        /**
         * The consumer value.
         */
        private final StowRSFileConsumer<Path> consumer;

        /**
         * The continue visit value.
         */
        private final boolean continueVisit;

        /**
         * Creates a new instance.
         *
         * @param consumer      the consumer.
         * @param continueVisit the continue visit.
         */
        StowRSFileVisitor(StowRSFileConsumer<Path> consumer, boolean continueVisit) {
            this.consumer = consumer;
            this.continueVisit = continueVisit;
        }

        /**
         * Executes the visit file operation.
         *
         * @param path  the path.
         * @param attrs the attrs.
         * @return the operation result.
         * @throws IOException if the operation cannot be completed.
         */
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            consumer.accept(path);
            return continueVisit ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
        }

    }

}
