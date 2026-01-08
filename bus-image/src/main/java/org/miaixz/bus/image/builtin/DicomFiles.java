/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.builtin;

import java.io.File;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

/**
 * An abstract utility class for scanning DICOM files and XML representations of DICOM datasets. It provides methods to
 * iterate through files and apply a callback function for each DICOM file found.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class DicomFiles {

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
        for (String fname : fnames) {
            scan(new File(fname), printout, scb);
        }
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
                for (String s : fileList) {
                    scan(new File(f, s), printout, scb);
                }
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
                if (printout) {
                    System.out.print(b ? '.' : 'I');
                }
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to parse file " + f + ": " + e.getMessage());
                e.printStackTrace(System.out);
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
                if (printout) {
                    System.out.print(b ? '.' : 'I');
                }
            } catch (Exception e) {
                System.out.println();
                System.out.println("Failed to scan file " + f + ": " + e.getMessage());
                e.printStackTrace(System.out);
            } finally {
                IoKit.close(in);
            }
        }
    }

    /**
     * Callback interface for processing each DICOM file found during a scan operation.
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
