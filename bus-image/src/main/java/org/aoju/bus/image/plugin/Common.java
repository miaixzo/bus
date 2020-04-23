/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.plugin;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.UID;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.Sequence;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.ContentHandlerAdapter;
import org.aoju.bus.image.galaxy.io.DicomInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class Common {

    private static SAXParser saxParser;

    public static void scan(List<String> fnames, Common.Callback scb) {
        scan(fnames, true, scb); //default printout = true
    }

    public static void scan(List<String> fnames, boolean printout, Common.Callback scb) {
        for (String fname : fnames)
            scan(new File(fname), printout, scb);
    }

    private static void scan(File f, boolean printout, Common.Callback scb) {
        if (f.isDirectory()) {
            for (String s : f.list())
                scan(new File(f, s), printout, scb);
            return;
        }
        if (f.getName().endsWith(".xml")) {
            try {
                SAXParser p = saxParser;
                if (p == null)
                    saxParser = p = SAXParserFactory.newInstance().newSAXParser();
                Attributes ds = new Attributes();
                ContentHandlerAdapter ch = new ContentHandlerAdapter(ds);
                p.parse(f, ch);
                Attributes fmi = ch.getFileMetaInformation();
                if (fmi == null)
                    fmi = ds.createFileMetaInformation(UID.ExplicitVRLittleEndian);
                boolean b = scb.dicomFile(f, fmi, -1, ds);
            } catch (Exception e) {
                throw new InstrumentException(e);
            }
        } else {
            DicomInputStream in = null;
            try {
                in = new DicomInputStream(f);
                in.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
                Attributes fmi = in.readFileMetaInformation();
                long dsPos = in.getPosition();
                Attributes ds = in.readDataset(-1, Tag.PixelData);
                if (fmi == null || !fmi.containsValue(Tag.TransferSyntaxUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPClassUID)
                        || !fmi.containsValue(Tag.MediaStorageSOPInstanceUID))
                    fmi = ds.createFileMetaInformation(in.getTransferSyntax());
                boolean b = scb.dicomFile(f, fmi, dsPos, ds);
            } catch (Exception e) {
                throw new InstrumentException(e);
            } finally {
                IoUtils.close(in);
            }
        }
    }

    public static void addAttributes(Attributes attrs, int[] tags, String... ss) {
        Attributes item = attrs;
        for (int i = 0; i < tags.length - 1; i++) {
            int tag = tags[i];
            Sequence sq = item.getSequence(tag);
            if (sq == null)
                sq = item.newSequence(tag, 1);
            if (sq.isEmpty())
                sq.add(new Attributes());
            item = sq.get(0);
        }
        int tag = tags[tags.length - 1];
        VR vr = ElementDictionary.vrOf(tag,
                item.getPrivateCreator(tag));
        if (ss.length == 0)
            if (vr == VR.SQ)
                item.newSequence(tag, 1).add(new Attributes(0));
            else
                item.setNull(tag, vr);
        else
            item.setString(tag, vr, ss);
    }

    public static boolean updateAttributes(Attributes data, Attributes attrs,
                                           String uidSuffix) {
        if (attrs.isEmpty() && uidSuffix == null)
            return false;
        if (uidSuffix != null) {
            data.setString(Tag.StudyInstanceUID, VR.UI,
                    data.getString(Tag.StudyInstanceUID) + uidSuffix);
            data.setString(Tag.SeriesInstanceUID, VR.UI,
                    data.getString(Tag.SeriesInstanceUID) + uidSuffix);
            data.setString(Tag.SOPInstanceUID, VR.UI,
                    data.getString(Tag.SOPInstanceUID) + uidSuffix);
        }
        data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
        return true;
    }

    public static int toTag(String tagOrKeyword) {
        try {
            return Integer.parseInt(tagOrKeyword, 16);
        } catch (IllegalArgumentException e) {
            int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
            if (tag == -1)
                throw new IllegalArgumentException(tagOrKeyword);
            return tag;
        }
    }

    public interface Callback {
        boolean dicomFile(File f, Attributes fmi, long dsPos, Attributes ds)
                throws Exception;
    }

}
