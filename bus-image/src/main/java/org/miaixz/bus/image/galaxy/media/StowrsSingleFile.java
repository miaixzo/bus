/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.media;

import org.miaixz.bus.core.net.Header;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.data.BulkData;
import org.miaixz.bus.image.galaxy.data.VR;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.image.galaxy.io.SAXTransformer;

import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.Map;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class StowrsSingleFile extends AbstractStowrs implements UploadSingleFile {

    public StowrsSingleFile(String requestURL, String contentType) {
        this(requestURL, contentType, null, null);
    }

    public StowrsSingleFile(String requestURL, String contentType, String agentName, Map<String, String> headers) {
        super(requestURL, contentType, agentName, headers);
    }

    @Override
    public void uploadDicom(InputStream in, Attributes fmi, String tsuid, String iuid) throws IOException {
        HttpURLConnection httpPost = buildConnection();
        try (DataOutputStream out = new DataOutputStream(httpPost.getOutputStream());
             ImageOutputStream dos = new ImageOutputStream(out, tsuid)) {
            writeContentMarkers(out);
            dos.writeFileMetaInformation(fmi);

            byte[] buf = new byte[4096];
            int offset;
            while ((offset = in.read(buf)) > 0) {
                dos.write(buf, 0, offset);
            }
            writeEndMarkers(httpPost, out, iuid);
        } finally {
            removeConnection(httpPost);
        }
    }

    @Override
    public void uploadDicom(Attributes metadata, String tsuid) throws IOException {
        HttpURLConnection httpPost = buildConnection();
        try (DataOutputStream out = new DataOutputStream(httpPost.getOutputStream());
             ImageOutputStream dos = new ImageOutputStream(out, tsuid)) {
            writeContentMarkers(out);
            Attributes fmi = metadata.createFileMetaInformation(tsuid);
            dos.writeDataset(fmi, metadata);
            writeEndMarkers(httpPost, out, metadata.getString(Tag.SOPInstanceUID));
        } finally {
            removeConnection(httpPost);
        }
    }

    @Override
    public void uploadEncapsulatedDocument(Attributes metadata, File bulkDataFile, String mdiaType, String sopClassUID)
            throws Exception {
        HttpURLConnection httpPost = buildConnection();

        setEncapsulatedDocumentAttributes(bulkDataFile.toPath(), metadata, mdiaType);
        if (null == metadata.getValue(Tag.EncapsulatedDocument)) {
            metadata.setValue(Tag.EncapsulatedDocument, VR.OB, new BulkData(null, "bulk", false));
        }
        metadata.setValue(Tag.SOPClassUID, VR.UI, sopClassUID);
        ensureUID(metadata, Tag.StudyInstanceUID);
        ensureUID(metadata, Tag.SeriesInstanceUID);
        ensureUID(metadata, Tag.SOPInstanceUID);

        try (ByteArrayOutputStream bOut = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(httpPost.getOutputStream())) {

            SAXTransformer.getSAXWriter(new StreamResult(bOut)).write(metadata);

            writeContentMarkers(out);

            out.write(bOut.toByteArray());

            out.write(MultipartParser.Separator.BOUNDARY.getType());
            out.writeBytes(MULTIPART_BOUNDARY);
            byte[] fsep = MultipartParser.Separator.FIELD.getType();
            out.write(fsep);
            out.writeBytes(Header.CONTENT_TYPE + ": ");
            out.writeBytes(mdiaType);
            out.write(fsep);
            out.writeBytes(Header.CONTENT_LOCATION + ": ");
            out.writeBytes(getContentLocation(metadata));
            out.write(MultipartParser.Separator.HEADER.getType());

            Files.copy(bulkDataFile.toPath(), out);

            writeEndMarkers(httpPost, out, metadata.getString(Tag.SOPInstanceUID));
        } finally {
            removeConnection(httpPost);
        }
    }

}
