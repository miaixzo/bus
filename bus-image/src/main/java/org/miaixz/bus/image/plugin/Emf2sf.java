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

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.image.Tag;
import org.miaixz.bus.image.builtin.MultiframeExtractor;
import org.miaixz.bus.image.galaxy.data.Attributes;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * The {@code Emf2sf} class provides functionality to extract individual frames from an Enhanced Multi-frame (EMF) DICOM
 * object and save them as single-frame (SF) DICOM objects.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Emf2sf {

    /**
     * The core component for extracting frames.
     */
    private final MultiframeExtractor extractor = new MultiframeExtractor();
    /**
     * An array of specific frame numbers to extract (1-based). If null, all frames are extracted.
     */
    private int[] frames;
    /**
     * A formatter for the output filenames.
     */
    private DecimalFormat outFileFormat;
    /**
     * The directory where the single-frame files will be saved.
     */
    private File outDir;

    /**
     * Sets the output directory for the extracted single-frame files. If the directory does not exist, it will be
     * created.
     *
     * @param outDir The destination directory.
     */
    public final void setOutputDirectory(File outDir) {
        outDir.mkdirs();
        this.outDir = outDir;
    }

    /**
     * Sets the format for the output filenames using a {@link DecimalFormat} pattern.
     *
     * @param outFileFormat The pattern for formatting the frame number in the filename.
     */
    public final void setOutputFileFormat(String outFileFormat) {
        this.outFileFormat = new DecimalFormat(outFileFormat);
    }

    /**
     * Sets the specific frames to be extracted. The frame numbers are 1-based.
     *
     * @param frames An array of frame numbers to extract.
     */
    public final void setFrames(int[] frames) {
        this.frames = frames;
    }

    /**
     * Sets whether to preserve the original Series Instance UID in the extracted frames.
     *
     * @param PreserveSeriesInstanceUID {@code true} to preserve the Series Instance UID, {@code false} to generate a
     *                                  new one.
     */
    public void setPreserveSeriesInstanceUID(boolean PreserveSeriesInstanceUID) {
        extractor.setPreserveSeriesInstanceUID(PreserveSeriesInstanceUID);
    }

    /**
     * Sets the format for the Instance Number in the extracted single-frame objects.
     *
     * @param instanceNumberFormat A {@link java.util.Formatter} pattern string.
     */
    public void setInstanceNumberFormat(String instanceNumberFormat) {
        extractor.setInstanceNumberFormat(instanceNumberFormat);
    }

    /**
     * Generates a filename for an extracted frame.
     *
     * @param srcFile The source multi-frame file.
     * @param frame   The 1-based frame number.
     * @return The generated filename string.
     */
    private String fname(File srcFile, int frame) {
        if (outFileFormat != null)
            synchronized (outFileFormat) {
                return outFileFormat.format(frame);
            }
        return String.format(srcFile.getName() + "-%04d", frame);
    }

    /**
     * Extracts frames from a given multi-frame DICOM file. If specific frames are set via {@link #setFrames(int[])},
     * only those are extracted. Otherwise, all frames are extracted.
     *
     * @param file The source multi-frame DICOM file.
     * @return The number of frames extracted.
     * @throws IOException if an I/O error occurs.
     */
    public int extract(File file) throws IOException {
        Attributes src;
        ImageInputStream dis = new ImageInputStream(file);
        try {
            dis.setIncludeBulkData(ImageInputStream.IncludeBulkData.URI);
            src = dis.readDataset();
        } finally {
            IoKit.close(dis);
        }
        Attributes fmi = dis.getFileMetaInformation();
        if (frames == null) {
            int n = src.getInt(Tag.NumberOfFrames, 1);
            for (int frame = 0; frame < n; ++frame)
                extract(file, fmi, src, frame);
            return n;
        } else {
            for (int frame : frames)
                extract(file, fmi, src, frame - 1); // Adjust to 0-based index for extractor
            return frames.length;
        }
    }

    /**
     * Extracts a single frame from the source dataset and writes it to a new file.
     *
     * @param file  The original source file, used for naming the output.
     * @param fmi   The File Meta Information of the source file.
     * @param src   The source dataset containing the multi-frame data.
     * @param frame The 0-based index of the frame to extract.
     * @throws IOException if an I/O error occurs during writing.
     */
    private void extract(File file, Attributes fmi, Attributes src, int frame) throws IOException {
        Attributes sf = extractor.extract(src, frame);
        File outFile = new File(outDir, fname(file, frame + 1));
        try (ImageOutputStream out = new ImageOutputStream(outFile)) {
            out.writeDataset(
                    fmi != null ? sf.createFileMetaInformation(fmi.getString(Tag.TransferSyntaxUID)) : null,
                    sf);
        }
    }

}
