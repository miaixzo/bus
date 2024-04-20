/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.plugin;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.builtin.Multiframe;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Emf2sf {

    private final Multiframe extractor = new Multiframe();
    private int[] frames;
    private DecimalFormat outFileFormat;
    private File outDir;

    private static int[] toFrames(String[] ss) throws InternalException {
        if (null == ss)
            return null;

        int[] is = new int[ss.length];
        for (int i = 0; i < is.length; i++)
            try {
                is[i] = Integer.parseInt(ss[i]) - 1;
            } catch (NumberFormatException e) {
                throw new InternalException(
                        "Invalid argument of option --frame: " + ss[i]);
            }

        return is;
    }

    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    public final void setFrames(int[] frames) {
        this.frames = frames;
    }

    public void setPreserveSeriesInstanceUID(boolean PreserveSeriesInstanceUID) {
        extractor.setPreserveSeriesInstanceUID(PreserveSeriesInstanceUID);
    }

    public void setInstanceNumberFormat(String instanceNumberFormat) {
        extractor.setInstanceNumberFormat(instanceNumberFormat);
    }

    private String fname(File srcFile, int frame) {
        if (null != outFileFormat)
            synchronized (outFileFormat) {
                return outFileFormat.format(frame);
            }
        return String.format(srcFile.getName() + "-%04d", frame);
    }

    public int extract(File file) throws IOException {
        Attributes src;
        ImageInputStream dis = new ImageInputStream(file);
        try {
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            src = dis.readDataset(-1, -1);
        } finally {
            IoKit.close(dis);
        }
        Attributes fmi = dis.getFileMetaInformation();
        if (null == frames) {
            int n = src.getInt(Tag.NumberOfFrames, 1);
            for (int frame = 0; frame < n; ++frame)
                extract(file, fmi, src, frame);
            return n;
        } else {
            for (int frame : frames)
                extract(file, fmi, src, frame);
            return frames.length;
        }
    }

    private void extract(File file, Attributes fmi, Attributes src, int frame)
            throws IOException {
        Attributes sf = extractor.extract(src, frame);
        ImageOutputStream out = new ImageOutputStream(
                new File(outDir, fname(file, frame + 1)));
        try {
            out.writeDataset(null != fmi
                    ? sf.createFileMetaInformation(
                    fmi.getString(Tag.TransferSyntaxUID))
                    : null, sf);
        } finally {
            IoKit.close(out);
        }
    }

}
