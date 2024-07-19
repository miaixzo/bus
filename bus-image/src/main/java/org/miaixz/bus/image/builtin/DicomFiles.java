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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ContentHandlerAdapter;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class DicomFiles {

    private static SAXParser saxParser;

    public static void scan(List<String> fnames, Callback scb) {
        scan(fnames, true, scb); // default printout = true
    }

    public static void scan(List<String> fnames, boolean printout, Callback scb) {
        for (String fname : fnames) {
            scan(new File(fname), printout, scb);
        }
    }

    private static void scan(File f, boolean printout, Callback scb) {
        if (f.isDirectory()) {
            for (String s : f.list()) {
                scan(new File(f, s), printout, scb);
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
                Attributes ds = in.readDataset(-1, Tag.PixelData);
                if (fmi == null
                        || !fmi.containsValue(Tag.TransferSyntaxUID)
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

    public interface Callback {
        boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds) throws Exception;
    }

}
