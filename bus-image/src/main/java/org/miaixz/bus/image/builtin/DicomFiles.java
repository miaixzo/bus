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
package org.miaixz.bus.image.builtin;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.logger.Logger;

/**
 * An abstract utility class for scanning DICOM files and XML representations of DICOM datasets. It provides methods to
 * iterate through files and apply a callback function for each DICOM file found.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class DicomFiles {

    /**
     * Constructs a new DicomFiles instance.
     */
    public DicomFiles() {
        // No initialization required.
    }

    /**
     * Static SAXParser instance for parsing XML files. Initialized on first use.
     */
    private static SAXParser saxParser;

    /**
     * Scans a list of file names (which can be files or directories) for DICOM files or their XML representations. For
     * each found file, the {@link Callback#dicomFile(File, Attributes, long, Attributes)} method is invoked. Progress
     * is printed to the console.
     *
     * @param fnames The list of file names (absolute or relative paths) to scan.
     * @param scb    The callback interface to handle each DICOM file.
     */
    public static void scan(List<String> fnames, Callback scb) {
        scan(fnames, true, scb); // default printout = true
    }

    /**
     * Scans a list of file names (which can be files or directories) for DICOM files or their XML representations. For
     * each found file, the {@link Callback#dicomFile(File, Attributes, long, Attributes)} method is invoked.
     *
     * @param fnames   The list of file names (absolute or relative paths) to scan.
     * @param printout A boolean indicating whether to print progress to the console.
     * @param scb      The callback interface to handle each DICOM file.
     */
    public static void scan(List<String> fnames, boolean printout, Callback scb) {
        Logger.info(true, "Image", "DICOM file scan started: rootCount={}, printout={}", fnames.size(), printout);
        for (String fname : fnames) {
            scan(new File(fname), printout, scb);
        }
        Logger.info(false, "Image", "DICOM file scan finished: rootCount={}", fnames.size());
    }

    /**
     * Recursively scans a given file or directory for DICOM files or their XML representations. If the file is a
     * directory, it scans its contents. If it's a file, it attempts to parse it as either a DICOM file or a DICOM XML
     * file and invokes the callback.
     *
     * @param f        The {@link File} object representing the file or directory to scan.
     * @param printout A boolean indicating whether to print progress to the console.
     * @param scb      The callback interface to handle each DICOM file.
     */
    private static void scan(File f, boolean printout, Callback scb) {
        if (f.isDirectory() && f.canRead()) {
            String[] fileList = f.list();
            if (fileList != null) {
                Logger.debug(
                        true,
                        "Image",
                        "DICOM directory scan started: directoryName={}, childCount={}",
                        f.getName(),
                        fileList.length);
                for (String s : fileList) {
                    scan(new File(f, s), printout, scb);
                }
                Logger.debug(
                        false,
                        "Image",
                        "DICOM directory scan finished: directoryName={}, childCount={}",
                        f.getName(),
                        fileList.length);
            } else {
                Logger.warn(
                        false,
                        "Image",
                        "DICOM directory scan skipped: directoryName={}, reason=list-null",
                        f.getName());
            }
            return;
        }
        if (f.getName().endsWith(".xml")) {
            try {
                SAXParser p = saxParser;
                if (p == null) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    saxParser = p = factory.newSAXParser();
                }
                Attributes ds = new Attributes();
                ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
                p.parse(f, ch);
                Attributes fmi = ch.getFileMetaInformation();
                if (fmi == null) {
                    fmi = ds.createFileMetaInformation(UID.ExplicitVRLittleEndian.uid);
                }
                boolean b = scb.dicomFile(f, fmi, -1, ds);
                Logger.debug(false, "Image", "DICOM XML file scanned: fileName={}, accepted={}", f.getName(), b);
                if (printout) {
                    System.out.print(b ? '.' : 'I');
                }
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to parse file " + f + ": " + e.getMessage());
                Logger.warn(
                        false,
                        "Image",
                        e,
                        "DICOM file parse failed: fileName={}, exception={}",
                        f.getName(),
                        e.getClass().getSimpleName());
            }
        } else {
            ImageInputStream in = null;
            try {
                in = new ImageInputStream(f);
                in.setIncludeBulkData(ImageInputStream.IncludeBulkData.NO);
                Attributes fmi = in.readFileMetaInformation();
                long dsPos = in.getPosition();
                Attributes ds = in.readDatasetUntilPixelData();
                if (fmi == null || !fmi.containsValue(Tag.TransferSyntaxUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID)) {
                    fmi = ds.createFileMetaInformation(in.getTransferSyntax());
                }
                boolean b = scb.dicomFile(f, fmi, dsPos, ds);
                Logger.debug(
                        false,
                        "Image",
                        "DICOM file scanned: fileName={}, datasetPosition={}, accepted={}",
                        f.getName(),
                        dsPos,
                        b);
                if (printout) {
                    System.out.print(b ? '.' : 'I');
                }
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to scan file " + f + ": " + e.getMessage());
                Logger.warn(
                        false,
                        "Image",
                        e,
                        "DICOM file scan failed: fileName={}, exception={}",
                        f.getName(),
                        e.getClass().getSimpleName());
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                    Logger.warn(
                            false,
                            "Image",
                            e,
                            "DICOM input stream close failed: fileName={}, exception={}",
                            f.getName(),
                            e.getClass().getSimpleName());
                }
            }
        }
    }

    /**
     * Callback interface for processing each DICOM file found during a scan operation.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public interface Callback {

        /**
         * Called for each DICOM file or DICOM XML file found.
         *
         * @param f     The {@link File} object representing the DICOM file.
         * @param fmi   The File Meta Information {@link Attributes} of the DICOM file.
         * @param dsPos The position of the dataset within the file, or -1 if it's an XML file.
         * @param ds    The main dataset {@link Attributes} of the DICOM file.
         * @return {@code true} if the file was processed successfully, {@code false} otherwise.
         * @throws Exception if an error occurs during processing.
         */
        boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds) throws Exception;

    }

}
