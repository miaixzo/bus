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
import org.aoju.bus.image.Tag;
import org.aoju.bus.image.builtin.DeIdentifier;
import org.aoju.bus.image.galaxy.data.Attributes;
import org.aoju.bus.image.galaxy.data.ElementDictionary;
import org.aoju.bus.image.galaxy.data.VR;
import org.aoju.bus.image.galaxy.io.DicomEncodingOptions;
import org.aoju.bus.image.galaxy.io.DicomInputStream;
import org.aoju.bus.image.galaxy.io.DicomOutputStream;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class Deidentify {

    private final DeIdentifier deidentifier;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    public Deidentify(DeIdentifier.Option... options) {
        deidentifier = new DeIdentifier(options);
    }

    public void setEncodingOptions(DicomEncodingOptions encOpts) {
        this.encOpts = encOpts;
    }

    private void setDummyValues(String[] optVals) {
        if (optVals != null)
            for (int i = 1; i < optVals.length; i++, i++) {
                int tag = Common.toTag(optVals[i - 1]);
                VR vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                deidentifier.setDummyValue(tag, vr, optVals[i]);
            }
    }

    private void mtranscode(File src, File dest) {
        if (src.isDirectory()) {
            dest.mkdir();
            for (File file : src.listFiles())
                mtranscode(file, new File(dest, file.getName()));
            return;
        }
        if (dest.isDirectory())
            dest = new File(dest, src.getName());
        try {
            transcode(src, dest);
            Logger.error(
                    MessageFormat.format("deidentified",
                            src, dest));
        } catch (Exception e) {
            Logger.error(
                    MessageFormat.format("failed",
                            src, e.getMessage()));
            throw new InstrumentException(e);
        }
    }

    public void transcode(File src, File dest) throws IOException {
        Attributes fmi;
        Attributes dataset;
        try (DicomInputStream dis = new DicomInputStream(src)) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            fmi = dis.readFileMetaInformation();
            dataset = dis.readDataset(-1, -1);
        }
        deidentifier.deidentify(dataset);
        if (fmi != null)
            fmi = dataset.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID));
        try (DicomOutputStream dos = new DicomOutputStream(dest)) {
            dos.setEncodingOptions(encOpts);
            dos.writeDataset(fmi, dataset);
        }
    }

}
